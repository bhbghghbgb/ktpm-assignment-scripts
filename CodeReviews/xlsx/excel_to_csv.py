import pandas as pd
import os
import glob

# --- Configuration ---
OUTPUT_DIR = "out"
# --- End Configuration ---


def excel_to_csv():
    """
    Finds the first .xlsx or .xls file in the current directory and exports
    each sheet to a separate, indexed CSV file in the 'out/' directory.
    """
    # 1. Prepare environment
    print("--- Starting Excel to CSV Export ---")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Search for the first Excel file
    excel_files = glob.glob("*.xlsx") + glob.glob("*.xls")

    if not excel_files:
        print("❌ Error: No .xlsx or .xls file found in the current directory.")
        return

    input_file = excel_files[0]
    print(f"✅ Found Excel file: {input_file}")

    try:
        # Read all sheets from the Excel file
        # sheet_name=None reads all sheets into a dictionary of DataFrames
        xls = pd.ExcelFile(input_file)
        sheet_names = xls.sheet_names

        # Determine the number of digits needed for zero-padding (e.g., 01, 02, ..., 10)
        padding = len(str(len(sheet_names)))

        print(f"Found {len(sheet_names)} sheets. Exporting...")

        for i, sheet_name in enumerate(sheet_names, 1):
            # Read the sheet
            df = xls.parse(sheet_name)

            # Create the indexed filename (e.g., 01.Sheet Name.csv)
            # Replace characters that might be invalid in file paths
            safe_sheet_name = sheet_name.replace("/", "_").replace("\\", "_")
            index_prefix = str(i).zfill(padding)
            output_filename = f"{index_prefix}.{safe_sheet_name}.csv"
            output_filepath = os.path.join(OUTPUT_DIR, output_filename)

            # Save DataFrame to CSV. Setting index=False prevents writing the pandas row index.
            df.to_csv(output_filepath, index=False, encoding="utf-8")

            print(f"   -> Sheet '{sheet_name}' saved as: {output_filepath}")

        print(f"\n✨ Export Complete! All files saved in the '{OUTPUT_DIR}/' folder.")

    except Exception as e:
        print(f"❌ An error occurred during processing: {e}")


if __name__ == "__main__":
    # Ensure pandas and openpyxl are installed: pip install pandas openpyxl
    excel_to_csv()
