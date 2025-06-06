from json import loads
import sys
from pathlib import Path
from typing import Optional, Tuple
import yaml

sys.path.append(str(Path(__file__).resolve().parent / "submodules" / "navie-editor"))

from navie.editor import Editor
from navie.with_cache import with_cache


def execute_command(command: str, options: str = ""):
    """
    Execute a command using Python subprocess module.
    """
    import subprocess

    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(f"Command failed: {command}\n{result.stderr}")
    return result.stdout


def compute_diff() -> tuple[str, Path]:
    work_dir = "diff"
    editor = Editor(Path("review") / "navie" / work_dir)
    diff = editor.diff(base="feat/design-review", format="json")
    diff_output_file = Path("review") / "navie" / "diff" / "diff" / "diff.output.txt"
    return (diff, diff_output_file)


def list_webservices(diff_output_file: Path) -> list:
    # work_dir = "list_webservices"
    # editor = Editor(Path("navie") / work_dir)
    # webservices = editor.search(
    #     "list webservices",
    #     options="/diff /base=feat/design-review",
    #     format="Emit a list of affected web services, with one per line. Do not emit anything else.",
    #     extension="txt",
    # )

    # webservices_output_file = Path("navie") / work_dir / "search" / "search.output.txt"
    # with open(webservices_output_file, "r") as f:
    #     webservices_content = f.read()
    #     webservices_data = webservices_content.splitlines()
    #     return (webservices_data, webservices_output_file)

    command = [
        "llm",
        "--schema-multi",
        '"name, description"',
        "-f",
        str(diff_output_file),
        '"List each webservice that is affected by the code change, and explain how it\'s affected. Emit an exhaustive list."',
    ]
    command_str = " ".join(command)

    with open(diff_output_file, "r") as f:
        diff = f.read()

    def _list_web_services():
        return execute_command(command_str)

    work_dir = Path("review") / "llm" / "list_webservices"

    output = with_cache(
        work_dir=str(work_dir),
        implementation_func=_list_web_services,
        command=command,
        diff=diff,
    )

    # Parse output as JSON
    webservices_data = loads(output)
    if not isinstance(webservices_data, dict) or "items" not in webservices_data:
        raise ValueError(
            "Invalid output format. Expected a JSON object with 'items' key."
        )
    return webservices_data["items"]


def ensure_tracking_id_facility() -> Optional[str]:
    """
    Ensure that a tracking ID facility is available in the project.
    """
    command_str = [
        "llm",
        "-f",
        "review/combined_code.txt",
        "\"Does the codebase have a facility used to assign (or re-use) a Tracking ID to each HTTP server request? If so, describe it. Otherwise, emit 'None' and nothing else.\"",
    ]

    def _detect_tracking_id_facility():
        return execute_command(" ".join(command_str))

    work_dir = Path("review") / "llm" / "tracking_id_facility"
    # TODO: add a hash of the combined code file to the cache key
    output = with_cache(
        work_dir=str(work_dir),
        implementation_func=_detect_tracking_id_facility,
        command=command_str,
    )
    if "None" in output.strip():
        return None

    return output


def recommend_tracking_id_facility():
    """
    Recommend a tracking ID facility if not found.
    """

    command = [
        "llm",
        "-f",
        "review/combined_code.txt",
        '"Recommend a facility to assign (or re-use) a Tracking ID to each HTTP server request"',
    ]

    def _get_recommendation():
        return execute_command(" ".join(command))

    work_dir = Path("review") / "llm" / "recommend_tracking_id_facility"
    output = with_cache(
        work_dir=str(work_dir),
        implementation_func=_get_recommendation,
        command=command,
    )
    print(f"Recommendation for tracking ID facility: {output.strip()}")


def main():
    """
    Main function to run the script.
    """
    execute_command(
        """find . -not -path "./build/*" -type f \\( -name "*.java" \\) -exec sh -c 'echo "// filepath: $1"; cat "$1"; echo' _ {} \\; > review/combined_code.txt"""
    )

    _, diff_output_file = compute_diff()
    webservices = list_webservices(diff_output_file)
    print(f"Webservices affected by the code change:")
    for service in webservices:
        print(f"- {service['name']}: {service['description']}")

    tracking_facility = ensure_tracking_id_facility()
    if tracking_facility:
        print(f"Tracking ID facility: {tracking_facility}")
    else:
        print("No tracking ID facility found in the codebase.")
        print("Recommending a tracking ID facility...")
        recommend_tracking_id_facility()


if __name__ == "__main__":
    main()
