import pandas as pd
import os
import glob
import re

# --- Configuration ---
OUTPUT_DIR = "out"
OUTPUT_FILENAME = "Combined_Workbook.xlsx"
# --- End Configuration ---


def natural_sort_key(s):
    """
    Key for natural sorting (correctly handles 1, 2, ..., 10, 11 instead of 1, 10, 11, 2).
    Extracts numerical parts from the string for comparison.
    """
    return [
        int(text) if text.isdigit() else text.lower()
        for text in re.split("([0-9]+)", s)
    ]


def csv_to_excel():
    """
    Finds all CSV files in the current directory, sorts them numerically by the
    leading index, and combines them into a single Excel file in the 'out/' directory.
    """
    print("--- Starting CSV to Excel Import ---")

    # 1. Find and sort CSV files
    csv_files = glob.glob("*.csv")
    if not csv_files:
        print("❌ Error: No .csv files found in the current directory.")
        return

    # Sort files using the natural_sort_key to respect the index order (01, 02, 10...)
    csv_files.sort(key=natural_sort_key)
    print(f"✅ Found and ordered {len(csv_files)} CSV files.")

    # 2. Prepare environment and Excel writer
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    output_filepath = os.path.join(OUTPUT_DIR, OUTPUT_FILENAME)

    # Initialize the Excel writer engine
    writer = pd.ExcelWriter(output_filepath, engine="openpyxl")

    print(f"Combining files into: {output_filepath}")

    try:
        # 3. Read and write each CSV as a sheet
        for i, filepath in enumerate(csv_files):
            # Infer the sheet name from the filename
            # E.g., from '01.My Sheet.csv' -> 'My Sheet'
            filename = os.path.basename(filepath)

            # Use regex to strip the leading index and the .csv extension
            match = re.match(r"^\d+\.(.*)\.csv$", filename)
            if match:
                sheet_name = match.group(1).strip()
            else:
                # Fallback for files without a leading index
                sheet_name = filename.replace(".csv", "").strip()

            # Pandas limits sheet names to 31 characters
            sheet_name = sheet_name[:31]

            # Read the CSV file
            df = pd.read_csv(filepath, encoding="utf-8")

            # Write to the Excel writer object
            df.to_excel(writer, sheet_name=sheet_name, index=False)

            print(f"   -> Writing '{filename}' as sheet '{sheet_name}'")

        # 4. Save the combined workbook
        writer.close()

        print(f"\n✨ Import Complete! Combined workbook saved to '{output_filepath}'.")

    except Exception as e:
        print(f"❌ An error occurred during processing: {e}")
        writer.close()  # Ensure writer is closed even on error
        # Attempt to delete partial file if error occurred
        if os.path.exists(output_filepath):
            os.remove(output_filepath)


if __name__ == "__main__":
    # Ensure pandas and openpyxl are installed: pip install pandas openpyxl
    csv_to_excel()
