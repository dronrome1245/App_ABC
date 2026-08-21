#!/usr/bin/env python3
"""Build a reproducible Audio Pack v2 candidate from licensed Wikimedia recordings.

This script intentionally does not alter pitch or speaking rate. It only removes leading/trailing
silence and transcodes the human-recorded source WAV files to Android-friendly Ogg Vorbis. D027's
400–700 ms target is reported as QC evidence; multi-word letter names may be accepted as explicit
articulation exceptions after review.
"""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import re
import shutil
import subprocess
import tempfile
import urllib.parse
import urllib.request
from pathlib import Path

COMMONS_API = "https://commons.wikimedia.org/w/api.php"
USER_AGENT = "App_ABC-AudioPackV2/1.0 (https://github.com/dronrome1245/App_ABC)"
EXPECTED_AUTHOR = "Kalaider25"
EXPECTED_LICENSE_TOKEN = "CC BY-SA 4.0"
TARGET_MIN_SECONDS = 0.400
TARGET_MAX_SECONDS = 0.700

# Letter / expected spoken name / Android resource token.
LETTERS = [
    ("А", "а", "a"), ("Б", "бэ", "b"), ("В", "вэ", "v"),
    ("Г", "гэ", "g"), ("Д", "дэ", "d"), ("Е", "е", "e"),
    ("Ё", "ё", "yo"), ("Ж", "жэ", "zh"), ("З", "зэ", "z"),
    ("И", "и", "i"), ("Й", "и краткое", "short_i"), ("К", "ка", "k"),
    ("Л", "эль", "l"), ("М", "эм", "m"), ("Н", "эн", "n"),
    ("О", "о", "o"), ("П", "пэ", "p"), ("Р", "эр", "r"),
    ("С", "эс", "s"), ("Т", "тэ", "t"), ("У", "у", "u"),
    ("Ф", "эф", "f"), ("Х", "ха", "h"), ("Ц", "цэ", "ts"),
    ("Ч", "чэ", "ch"), ("Ш", "ша", "sh"), ("Щ", "ща", "shch"),
    ("Ъ", "твёрдый знак", "hard"), ("Ы", "ы", "y"), ("Ь", "мягкий знак", "soft"),
    ("Э", "э", "eh"), ("Ю", "ю", "yu"), ("Я", "я", "ya"),
]

# These names are inherently multi-word and are the strongest D027 articulation-exception candidates.
ARTICULATION_EXCEPTION_LETTERS = {"Й", "Ъ", "Ь"}

TAG_RE = re.compile(r"<[^>]+>")


def plain_metadata(value: str | None) -> str:
    if not value:
        return ""
    return html.unescape(TAG_RE.sub("", value)).strip()


def commons_file_info(file_name: str) -> dict[str, str]:
    params = urllib.parse.urlencode(
        {
            "action": "query",
            "format": "json",
            "formatversion": "2",
            "prop": "imageinfo",
            "iiprop": "url|extmetadata",
            "titles": f"File:{file_name}",
        }
    )
    request = urllib.request.Request(f"{COMMONS_API}?{params}", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=45) as response:
        payload = json.load(response)

    pages = payload.get("query", {}).get("pages", [])
    if len(pages) != 1 or pages[0].get("missing") is True:
        raise RuntimeError(f"Commons file not found: {file_name}")

    image_info = pages[0].get("imageinfo", [])
    if len(image_info) != 1:
        raise RuntimeError(f"No imageinfo for Commons file: {file_name}")

    info = image_info[0]
    metadata = info.get("extmetadata", {})

    def meta(key: str) -> str:
        return plain_metadata(metadata.get(key, {}).get("value"))

    return {
        "file_name": file_name,
        "download_url": info["url"],
        "description_url": info.get("descriptionurl", ""),
        "author": meta("Artist"),
        "license": meta("LicenseShortName"),
        "license_url": meta("LicenseUrl"),
        "date_time_original": meta("DateTimeOriginal"),
    }


def download(url: str, destination: Path) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=90) as response, destination.open("wb") as output:
        shutil.copyfileobj(response, output)


def run(command: list[str]) -> None:
    subprocess.run(command, check=True)


def probe_duration(path: Path) -> float:
    completed = subprocess.run(
        [
            "ffprobe",
            "-v", "error",
            "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1:nokey=1",
            str(path),
        ],
        check=True,
        capture_output=True,
        text=True,
    )
    return float(completed.stdout.strip())


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def qc_status(letter: str, duration: float) -> tuple[str, str]:
    if TARGET_MIN_SECONDS <= duration <= TARGET_MAX_SECONDS:
        return "TARGET", "within D027 400–700 ms target"
    if letter in ARTICULATION_EXCEPTION_LETTERS and duration > TARGET_MAX_SECONDS:
        return "ARTICULATION_EXCEPTION", "multi-word Russian letter name; review as D027 articulation exception"
    if duration < TARGET_MIN_SECONDS:
        return "REVIEW_SHORT", "shorter than D027 target; do not stretch automatically"
    return "REVIEW_LONG", "longer than D027 target; manual listening/QC required"


def build(output_dir: Path) -> None:
    if shutil.which("ffmpeg") is None or shutil.which("ffprobe") is None:
        raise RuntimeError("ffmpeg and ffprobe are required")

    output_dir.mkdir(parents=True, exist_ok=True)
    records: list[dict[str, object]] = []

    # Keep a small amount of natural boundary silence so fricatives/plosives are not clipped.
    silence_filter = (
        "silenceremove="
        "start_periods=1:start_duration=0.02:start_threshold=-50dB:start_silence=0.03:"
        "stop_periods=1:stop_duration=0.05:stop_threshold=-50dB:stop_silence=0.05"
    )

    with tempfile.TemporaryDirectory(prefix="appabc-audio-v2-") as temp_dir_name:
        temp_dir = Path(temp_dir_name)
        for index, (letter, spoken_name, token) in enumerate(LETTERS, start=1):
            source_name = f"Буква {letter}.wav"
            info = commons_file_info(source_name)

            if EXPECTED_AUTHOR.casefold() not in info["author"].casefold():
                raise RuntimeError(
                    f"Unexpected author for {source_name}: {info['author']!r}; expected {EXPECTED_AUTHOR}"
                )
            if EXPECTED_LICENSE_TOKEN.casefold() not in info["license"].casefold():
                raise RuntimeError(
                    f"Unexpected license for {source_name}: {info['license']!r}; expected {EXPECTED_LICENSE_TOKEN}"
                )

            source_path = temp_dir / f"{index:02d}_{token}.wav"
            output_path = output_dir / f"sound_letter_{token}_v2.ogg"
            download(info["download_url"], source_path)
            original_duration = probe_duration(source_path)

            run(
                [
                    "ffmpeg", "-y", "-hide_banner", "-loglevel", "error",
                    "-i", str(source_path),
                    "-vn",
                    "-af", silence_filter,
                    "-ac", "1",
                    "-ar", "48000",
                    "-c:a", "libvorbis",
                    "-q:a", "5",
                    str(output_path),
                ]
            )
            processed_duration = probe_duration(output_path)
            status, rationale = qc_status(letter, processed_duration)

            records.append(
                {
                    "index": index,
                    "letter": letter,
                    "expected_spoken_name": spoken_name,
                    "resource_name": output_path.stem,
                    "output_file": output_path.name,
                    "source_file": source_name,
                    "source_page": info["description_url"],
                    "source_author": info["author"],
                    "source_license": info["license"],
                    "source_license_url": info["license_url"],
                    "original_duration_ms": round(original_duration * 1000),
                    "processed_duration_ms": round(processed_duration * 1000),
                    "qc_status": status,
                    "qc_rationale": rationale,
                    "sha256": sha256(output_path),
                    "transformation": "leading/trailing silence trim; mono 48 kHz Ogg Vorbis; no pitch or tempo change",
                }
            )
            print(
                f"{index:02d} {letter}: {source_name} -> {output_path.name} "
                f"{original_duration:.3f}s -> {processed_duration:.3f}s [{status}]"
            )

    if len(records) != 33:
        raise RuntimeError(f"Expected 33 assets, generated {len(records)}")

    report = {
        "decision": "D027",
        "task": "AUDIO-02",
        "source_series": "Wikimedia Commons: Буква <LETTER>.wav",
        "expected_author": EXPECTED_AUTHOR,
        "expected_license": EXPECTED_LICENSE_TOKEN,
        "target_duration_ms": [400, 700],
        "processing_policy": "silence trim only; no pitch or tempo manipulation",
        "assets": records,
        "summary": {
            "count": len(records),
            "target": sum(item["qc_status"] == "TARGET" for item in records),
            "articulation_exception": sum(item["qc_status"] == "ARTICULATION_EXCEPTION" for item in records),
            "review_short": sum(item["qc_status"] == "REVIEW_SHORT" for item in records),
            "review_long": sum(item["qc_status"] == "REVIEW_LONG" for item in records),
        },
    }
    (output_dir / "audio_pack_v2_qc.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    table_lines = [
        "# Audio Pack v2 candidate — source attribution and QC",
        "",
        "Generated for D027 / AUDIO-02 from the Wikimedia Commons `Буква <LETTER>.wav` series.",
        "",
        f"- Author: `{EXPECTED_AUTHOR}` (validated from Commons metadata for every source file).",
        f"- License: `{EXPECTED_LICENSE_TOKEN}` (validated for every source file).",
        "- Transformation: leading/trailing silence trim, mono 48 kHz Ogg Vorbis; pitch and tempo are unchanged.",
        "- D027 duration target: 400–700 ms; multi-word names may remain longer as articulation exceptions.",
        "- This derivative audio remains subject to CC BY-SA 4.0; attribution and indication of changes are required.",
        "",
        "| # | Letter | Expected name | Output | Original ms | v2 ms | QC | Source |",
        "|---:|:---:|---|---|---:|---:|---|---|",
    ]
    for item in records:
        table_lines.append(
            "| {index} | {letter} | {expected_spoken_name} | `{output_file}` | {original_duration_ms} | "
            "{processed_duration_ms} | {qc_status} | [{source_file}]({source_page}) |".format(**item)
        )

    table_lines.extend(
        [
            "",
            "## Summary",
            "",
            f"- TARGET: {report['summary']['target']}",
            f"- ARTICULATION_EXCEPTION: {report['summary']['articulation_exception']}",
            f"- REVIEW_SHORT: {report['summary']['review_short']}",
            f"- REVIEW_LONG: {report['summary']['review_long']}",
            "",
            "The QC labels are mechanical duration checks only. Human listening/device smoke is still required before AUDIO-02 can be marked DONE.",
        ]
    )
    (output_dir / "AUDIO_PACK_V2_ATTRIBUTION.md").write_text("\n".join(table_lines) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    build(args.output)


if __name__ == "__main__":
    main()
