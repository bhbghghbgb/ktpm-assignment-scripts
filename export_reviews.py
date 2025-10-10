#!/usr/bin/env python3
"""
export_reviews.py

Reads CodeReviews.xlsx (multiple sheets). For each sheet whose A1 contains
"Code Review Report", generates a section .tex file under output_tex/sections/.

Changes:
 - Reviewer column is omitted from the table; rows are grouped by reviewer and
   each reviewer becomes a \\subsubsection*{Reviewer Name}.
 - The table no longer includes the "Check code description" column.
 - Columns are left-aligned only using ragged-right p{<fraction>\textwidth}.
 - Generates a codes_table.tex (single lookup table of Check Code -> Description)
   using the mapping provided by the user.
"""
import csv
import re
import shutil
import sys
from pathlib import Path

import pandas as pd

# === Configuration ===
export_dir = Path("output_tex")
sections_dir = export_dir / "sections"
review_list_file = export_dir / "reviews_list.tex"
codes_table_file = export_dir / "codes_table.tex"
input_file = Path("CodeReviews.xlsx")

export_dir.mkdir(exist_ok=True)
sections_dir.mkdir(exist_ok=True)


def sanitize_latex(text):
    """
    Escape LaTeX special characters and make certain punctuation breakable.
    This function processes the input string character-by-character to avoid
    re-escaping characters introduced by earlier replacements.
    """

    # Handle None or NaN input gracefully
    if text is None or (isinstance(text, float) and pd.isna(text)):
        return ""

    # Convert input to string and normalize newlines to single spaces
    # This helps avoid unexpected line breaks in LaTeX output
    s = str(text).replace("\r\n", " ").replace("\r", " ").replace("\n", " ").strip()

    # Map of LaTeX special characters to their escaped versions
    # These characters have special meaning in LaTeX and must be escaped to display correctly
    escape_map = {
        "\\": r"\textbackslash{}",  # Backslash
        "&": r"\&",  # Ampersand
        "%": r"\%",  # Percent
        "$": r"\$",  # Dollar sign
        "#": r"\#",  # Hash
        "{": r"\{",  # Left brace
        "}": r"\}",  # Right brace
        "~": r"\textasciitilde{}",  # Tilde
        "^": r"\textasciicircum{}",  # Caret
        "|": r"\textbar{}",  # pipe
        # "_": r"\_",  # Underscore
    }

    # Punctuation characters that should allow line breaks if followed by a non-space
    # This helps LaTeX break long strings like "obj.method()" or "1,2,3" more gracefully
    breakable_punct = ".,/:-_"

    out_parts = []  # List to collect processed characters

    # Iterate through each character in the string
    for i, ch in enumerate(s):
        # Look ahead to the next character (if any)
        next_ch = s[i + 1] if i + 1 < len(s) else ""

        # If character is a LaTeX special character, escape it using the map
        if ch in escape_map:
            out_parts.append(escape_map[ch])
            continue

        # If character is breakable punctuation and followed by a non-space,
        # keep the punctuation and add \allowbreak{} to hint LaTeX to break line here
        # Make punctuation and underscores breakable
        if ch in breakable_punct and next_ch and next_ch != " ":
            if ch == "_":
                out_parts.append(r"\_\allowbreak{}")
            else:
                out_parts.append(f"{ch}\\allowbreak{{}}")
            continue

        # Default case: keep character as-is
        out_parts.append(ch)

    # Join all parts and collapse multiple spaces into one
    result = re.sub(r"\s+", " ", "".join(out_parts)).strip()
    return result


def find_start_row(df):
    """Find the row index (0-based) containing 'Check code' in the first column."""
    for i, val in enumerate(df.iloc[:, 0]):
        if isinstance(val, str) and val.strip() == "Check code":
            return i
    return None


def extract_metadata(df):
    """
    Extract metadata as ordered pairs for a LaTeX tabular layout.
    Handles:
      - Multi-row metadata entries (continuation rows with blank key)
      - Tabs correctly (each tab -> new cell)
      - Appends ':' if missing in key
      - Replaces empty values with '---'
      - Joins multiple values on the same row with ' - '
    Returns: list of tuples (key, [values])
    """
    metadata = []
    current_key = None
    current_values = []

    for _, row in df.iterrows():
        # Normalize each cell (strip, replace NaN with "")
        cells = [str(x).strip() if pd.notna(x) else "" for x in row]
        if not any(cells):
            continue

        key = cells[0].strip()
        # join multiple filled cells (same row) with " - "
        joined_value = " - ".join(v for v in cells[1:] if v).strip()
        if not joined_value:
            joined_value = "---"

        if key:  # new key starts
            if current_key:
                metadata.append((current_key, current_values))
            if not key.endswith(":"):
                key += ":"
            current_key = key
            current_values = [joined_value]
        elif current_key:  # continuation line (same metadata)
            current_values.append(joined_value)

    if current_key:
        metadata.append((current_key, current_values))

    return metadata


def rows_by_reviewer(table_df):
    """
    Given the review table rows (pandas DataFrame starting after header),
    produce a mapping reviewer -> list of rows (each row is list of 6 padded cells).
    Assumes columns are:
      0 Check code
      1 Check code description (omitted)
      2 Line
      3 Comment
      4 Suggestion / Fix ?
      5 Reviewer
    Will pad/truncate each row to at least 6 columns so indexing is safe.
    """
    group = {}
    for _, r in table_df.iterrows():
        if r.isna().all():
            continue
        cells = [sanitize_latex(x) for x in list(r)]
        # pad to 6 cells
        if len(cells) < 6:
            cells.extend([""] * (6 - len(cells)))
        else:
            cells = cells[:6]
        # extract reviewer (index 5). If empty, use "Unknown"
        reviewer = cells[5] if cells[5].strip() else "Unknown"
        group.setdefault(reviewer, []).append(cells)
    return group


def make_reviewer_tables(grouped_rows):
    """
    Given grouped_rows: reviewer -> rows, produce LaTeX lines.
    Per reviewer:
      - Write \\subsubsection*{Reviewer Name}
      - Short table with columns: Code | Line | Comment | Suggestion
    Column widths use fractions of \textwidth and left-aligned raggedright.
    """
    lines = []
    # Column widths (fractions of \textwidth)
    # Code: 6%, Line: 10%, Comment: 40%, Suggestion: 44% (sum=1.0)
    col_spec = (
        ">{\\raggedright\\arraybackslash}p{0.06\\textwidth} "
        ">{\\raggedright\\arraybackslash}p{0.10\\textwidth} "
        ">{\\raggedright\\arraybackslash}p{0.40\\textwidth} "
        ">{\\raggedright\\arraybackslash}p{0.44\\textwidth}"
    )
    for reviewer, rows in grouped_rows.items():
        reviewer_title = f"Reviewer: {sanitize_latex(reviewer)}"
        lines.append(f"\\subsubsection{{{reviewer_title}}}")
        lines.append("\\vspace{0.3em}")
        lines.append(f"\\begin{{longtable}}{{{col_spec}}}")
        lines.append("\\toprule")
        lines.append(
            "\\textbf{Code} & \\textbf{Line} & \\textbf{Comment} & \\textbf{Suggestion / Fix} \\\\"
        )
        lines.append("\\midrule")
        lines.append("\\endfirsthead")
        lines.append("\\toprule")
        lines.append(
            "\\textbf{Code} & \\textbf{Line} & \\textbf{Comment} & \\textbf{Suggestion / Fix} \\\\"
        )
        lines.append("\\midrule")
        lines.append("\\endhead")
        lines.append("\\midrule")
        lines.append("\\multicolumn{4}{r}{\\textit{Continued on next page}} \\\\")
        lines.append("\\endfoot")
        lines.append("\\bottomrule")
        lines.append("\\endlastfoot")

        # Add this reviewer's rows
        for cells in rows:
            # cells layout: [code, descr, line, comment, suggestion, reviewer]
            code = cells[0]
            line_no = cells[2]
            comment = cells[3]
            suggestion = cells[4]
            row_tex = f"{code} & {line_no} & {comment} & {suggestion} \\\\"
            lines.append(row_tex)
        lines.append("\\end{longtable}")
        lines.append("")  # blank line between reviewers
    return lines


def process_sheet(sheet_name, df):
    """
    Produce the .tex contents for a sheet (string), or None if sheet is not a report.
    """
    if df.shape[0] == 0:
        return None
    if not isinstance(df.iat[0, 0], str) or "Code Review Report" not in df.iat[0, 0]:
        return None

    start_row = find_start_row(df)
    if start_row is None:
        print(
            f"⚠️  Skipping '{sheet_name}' — no 'Check code' header found.",
            file=sys.stderr,
        )
        return None

    meta_df = df.iloc[:start_row]
    table_df = df.iloc[start_row + 1 :].reset_index(drop=True)
    table_df = table_df.dropna(how="all")

    metadata = extract_metadata(meta_df)

    lines = []
    lines.append(
        "% --- Begin section for sheet: " + sanitize_latex(sheet_name) + " ---"
    )
    lines.append("\\clearpage")
    module_title = sanitize_latex(sheet_name)
    lines.append(f"\\subsection{{{module_title}}}")
    lines.append("\\noindent\\rule{\\textwidth}{0.4pt}\n")

    # --- Metadata table ---
    lines.append("\\begin{flushleft}")
    lines.append(
        "\\begin{tabular}{@{}>{\\raggedright\\arraybackslash}p{0.34\\textwidth} >{\\raggedright\\arraybackslash}p{0.62\\textwidth}@{}}"
    )  # Adjust widths for A4
    for key, values in metadata:
        key_tex = f"\\textbf{{{sanitize_latex(key)}}}"
        for i, val in enumerate(values):
            val_tex = sanitize_latex(val)
            if i == 0:
                lines.append(f"{key_tex} & {val_tex} \\\\")
            else:
                lines.append(f" & {val_tex} \\\\")
    lines.append("\\end{tabular}")
    lines.append("\\end{flushleft}")
    lines.append("\\vspace{1em}")

    lines.append("\n\\vspace{1em}")
    lines.append("% Reviewer-specific tables (no reviewer column)")

    grouped = rows_by_reviewer(table_df)
    if not grouped:
        lines.append("% (no review rows found)")
    else:
        lines.extend(make_reviewer_tables(grouped))

    lines.append(
        "% --- End section for sheet: " + sanitize_latex(sheet_name) + " ---\n"
    )
    return "\n".join(lines)


# === Generate codes_table.tex from user-provided mapping ===
CODES_MAPPING_RAW = Path("codes_mapping.txt").read_text(encoding="utf-8")


def generate_codes_table(raw_text, out_path):
    """
    Parse the raw mapping and write codes_table.tex with sections and subsections.
    Lines starting with Roman numerals (I, II, III, etc.) define new sections.
    Lines starting with '#' define subsections.
    Lines starting with digits are table rows.
    """
    lines = raw_text.strip().splitlines()
    current_section = None
    current_subsection = None
    tex = []
    table_open = False  # Track whether a longtable is currently open

    tex.append("% --- Check Code lookup table (auto-generated) ---")
    tex.append(
        "\\noindent This table lists the check codes used in the per-file reviews."
    )
    tex.append("\\vspace{0.5em}")

    for raw in lines:
        s = raw.strip()
        if not s:
            continue

        # --- Section (Roman numeral) ---
        if re.match(r"^[IVXLCDM]+\s*[-–]", s):
            if table_open:
                tex.append("\\bottomrule")
                tex.append("\\end{longtable}")
                table_open = False
            current_section = s
            current_subsection = None
            tex.append(
                f"\\subsection*{{{sanitize_latex(current_section)}}}"
            )  # not in TOC
            continue

        # --- Subsection (starts with #) ---
        if s.startswith("#"):
            if table_open:
                tex.append("\\bottomrule")
                tex.append("\\end{longtable}")
                table_open = False
            current_subsection = s.lstrip("#").strip()
            tex.append(f"\\subsubsection*{{{sanitize_latex(current_subsection)}}}")
            continue

        # --- Table rows (start with a number) ---
        m = re.match(r"^(\d+)\s+(.*)$", s)
        if m:
            if not table_open:
                tex.append(
                    "\\begin{longtable}{>{\\raggedright\\arraybackslash}p{0.12\\textwidth} >{\\raggedright\\arraybackslash}p{0.84\\textwidth}}"
                )
                tex.append("\\toprule")
                tex.append(
                    "\\textbf{Check Code} & \\textbf{Check code description} \\\\"
                )
                tex.append("\\midrule")
                table_open = True

            code = sanitize_latex(m.group(1))
            desc = sanitize_latex(m.group(2))
            tex.append(f"{code} & {desc} \\\\")
            continue

        # Ignore unrecognized lines silently
        continue

    # --- Close last table if still open ---
    if table_open:
        tex.append("\\bottomrule")
        tex.append("\\end{longtable}")

    out_path.write_text(apply_redactions("\n".join(tex) + "\n"), encoding="utf-8")
    print(f"📄 Created codes lookup: {out_path}")


def apply_redactions(text):
    if pattern:
        return pattern.sub(redactor_func, text)
    else:
        return text


# === Main execution ===
redaction_file = Path("redactions.csv")
redaction_map = {}

if redaction_file.exists():
    with redaction_file.open(encoding="utf-8") as f:
        reader = csv.reader(f)
        for row in reader:
            if len(row) < 2:
                continue
            orig, repl = row[0], row[1]
            redaction_map[re.escape(orig)] = repl


if redaction_map:
    # Build regex of all escaped original strings, joined by |
    pattern = re.compile("|".join(sorted(redaction_map, key=len, reverse=True)))

    def redactor_func(match):
        return redaction_map[re.escape(match.group(0))]

else:
    pattern = None

    def redactor_func(match):
        return match.group(0)


try:
    excel = pd.ExcelFile(input_file)
except FileNotFoundError:
    print(f"ERROR: Excel file not found: {input_file}", file=sys.stderr)
    sys.exit(2)
except Exception as e:
    print(f"ERROR: Failed to open Excel file: {e}", file=sys.stderr)
    sys.exit(3)

inputs = []

for sheet in excel.sheet_names:
    df = excel.parse(sheet_name=sheet, header=None, dtype=object)
    tex_content = process_sheet(sheet, df)
    if tex_content:
        safe_name = re.sub(r"[^A-Za-z0-9_-]+", "_", sheet)
        output_path = sections_dir / f"{safe_name}.tex"
        output_path.write_text(apply_redactions(tex_content), encoding="utf-8")
        inputs.append(f"\\input{{sections/{safe_name}.tex}}")
        print(f"✅ Exported {output_path}")

# Write reviews_list.tex
if inputs:
    review_list_file.write_text(
        apply_redactions("\n".join(inputs) + "\n"), encoding="utf-8"
    )
    print(f"📄 Created {review_list_file}")

# Generate codes_table
generate_codes_table(CODES_MAPPING_RAW, codes_table_file)

# Copy master template if it exists (safe)
files_to_copy = ["main_report.tex", "main.tex"]

for filename in files_to_copy:
    src = Path(filename)
    dst = export_dir / src.name
    if not src.exists():
        print(f"ℹ️  {src} not found; skipping copy.")
        continue

    # Simple heuristic for text files:
    if src.suffix.lower() in {".tex", ".txt", ".md"}:
        try:
            content = src.read_text(encoding="utf-8")
            redacted = apply_redactions(content)
            dst.write_text(redacted, encoding="utf-8")
            print(f"📄 Copied and redacted {src} to {dst}")
        except Exception as e:
            print(f"⚠️ Failed to redact {src}, copying raw: {e}")
            shutil.copy(src, dst)
    else:
        # Binary or unknown file: copy as-is
        shutil.copy(src, dst)
        print(f"📄 Copied {src} to {dst} (binary/no redaction)")


print("Done.")
