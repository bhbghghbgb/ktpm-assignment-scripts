import os
import sys


def annotate_java_file(input_path):
    if not os.path.isfile(input_path):
        print(f"Error: File '{input_path}' does not exist.")
        return

    dir_name, base_name = os.path.split(input_path)
    name, ext = os.path.splitext(base_name)
    output_path = os.path.join(dir_name, f"{name}_annotated{ext}")

    with (
        open(input_path, "r", encoding="utf-8") as infile,
        open(output_path, "w", encoding="utf-8") as outfile,
    ):
        for i, line in enumerate(infile, start=1):
            stripped_line = line.rstrip("\n")
            # Add inline comment at the end of the line
            annotated_line = f"{stripped_line} /*{i}*/\n"
            outfile.write(annotated_line)

    print(f"Annotated file saved to: {output_path}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python annotate_java.py <path_to_java_file>")
    else:
        annotate_java_file(sys.argv[1])
