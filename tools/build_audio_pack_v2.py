#!/usr/bin/env python3
"""Build and QC a licensed human-recorded Audio Pack v2 candidate for D027.

The source series is Wikimedia Commons `Буква <LETTER>.wav` by Kalaider25,
CC BY-SA 4.0. Processing is intentionally conservative: trim boundary silence
and transcode to mono Ogg Vorbis; do not change pitch or tempo. The script
reports D027's 400–700 ms target instead of forcing unsuitable recordings into it.
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
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

COMMONS_API = "https://commons.wikimedia.org/w/api.php"
USER_AGENT = "App_ABC-AudioPackV2/1.1 (https://github.com/dronrome1245/App_ABC)"
EXPECTED_AUTHOR = "Kalaider25"
EXPECTED_LICENSE_TOKEN = "CC BY-SA 4.0"
TARGET_MIN_SECONDS = 0.400
TARGET_MAX_SECONDS = 0.700
DOWNLOAD_DELAY_SECONDS = 1.25

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
ARTICULATION_EXCEPTION_LETTERS = {"Й", "Ъ", "Ь"}
TAG_RE = re.compile(r"<[^>]+>")


def plain(value: str | None) -> str:
    return html.unescape(TAG_RE.sub("", value or "")).strip()


def request_json(url: str) -> dict:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def commons_manifest() -> dict[str, dict[str, str]]:
    titles = "|".join(f"File:Буква {letter}.wav" for letter, _, _ in LETTERS)
    params = urllib.parse.urlencode({
        "action": "query",
        "format": "json",
        "formatversion": "2",
        "prop": "imageinfo",
        "iiprop": "url|extmetadata",
        "titles": titles,
    })
    payload = request_json(f"{COMMONS_API}?{params}")
    result: dict[str, dict[str, str]] = {}
    for page in payload.get("query", {}).get("pages", []):
        title = page.get("title", "")
        if page.get("missing") is True or not title.startswith("File:"):
            continue
        image_info = page.get("imageinfo", [])
        if len(image_info) != 1:
            continue
        info = image_info[0]
        metadata = info.get("extmetadata", {})
        file_name = title.removeprefix("File:")

        def meta(key: str) -> str:
            return plain(metadata.get(key, {}).get("value"))

        result[file_name] = {
            "download_url": info["url"],
            "description_url": info.get("descriptionurl", ""),
            "author": meta("Artist"),
            "license": meta("LicenseShortName"),
            "license_url": meta("LicenseUrl"),
        }
    return result


def download_with_backoff(url: str, destination: Path) -> None:
    waits = (0, 8, 20, 45)
    last_error: Exception | None = None
    for wait in waits:
        if wait:
            time.sleep(wait)
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        try:
            with urllib.request.urlopen(request, timeout=90) as response, destination.open("wb") as output:
                shutil.copyfileobj(response, output)
            return
        except urllib.error.HTTPError as error:
            last_error = error
            if error.code not in {429, 503}:
                raise
    raise RuntimeError(f"Unable to download after retries: {url}") from last_error


def run(command: list[str]) -> None:
    subprocess.run(command, check=True)


def duration(path: Path) -> float:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration",
         "-of", "default=noprint_wrappers=1:nokey=1", str(path)],
        check=True, capture_output=True, text=True,
    )
    return float(result.stdout.strip())


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def qc(letter: str, seconds: float) -> tuple[str, str]:
    if TARGET_MIN_SECONDS <= seconds <= TARGET_MAX_SECONDS:
        return "TARGET", "within D027 400–700 ms target"
    if letter in ARTICULATION_EXCEPTION_LETTERS and seconds > TARGET_MAX_SECONDS:
        return "ARTICULATION_EXCEPTION", "multi-word letter name; candidate for justified D027 exception"
    if seconds < TARGET_MIN_SECONDS:
        return "REVIEW_SHORT", "shorter than D027 target"
    return "REVIEW_LONG", "longer than D027 target"


def build(output_dir: Path) -> None:
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        raise RuntimeError("ffmpeg and ffprobe are required")

    source_manifest = commons_manifest()
    expected_sources = {f"Буква {letter}.wav" for letter, _, _ in LETTERS}
    missing = expected_sources - source_manifest.keys()
    if missing:
        raise RuntimeError(f"Missing Commons metadata: {sorted(missing)}")

    for source_name, info in source_manifest.items():
        if EXPECTED_AUTHOR.casefold() not in info["author"].casefold():
            raise RuntimeError(f"Unexpected author for {source_name}: {info['author']!r}")
        if EXPECTED_LICENSE_TOKEN.casefold() not in info["license"].casefold():
            raise RuntimeError(f"Unexpected license for {source_name}: {info['license']!r}")

    output_dir.mkdir(parents=True, exist_ok=True)
    records: list[dict[str, object]] = []

    # Trim only the two boundaries. Reverse-pass end trimming avoids cutting on
    # short internal pauses inside consonant/multi-word names.
    boundary_trim = (
        "silenceremove=start_periods=1:start_duration=0.02:start_threshold=-45dB:start_silence=0.03,"
        "areverse,"
        "silenceremove=start_periods=1:start_duration=0.02:start_threshold=-45dB:start_silence=0.05,"
        "areverse"
    )

    with tempfile.TemporaryDirectory(prefix="appabc-audio-v2-") as temp_name:
        temp_dir = Path(temp_name)
        for index, (letter, spoken_name, token) in enumerate(LETTERS, start=1):
            source_name = f"Буква {letter}.wav"
            info = source_manifest[source_name]
            source_path = temp_dir / f"{index:02d}_{token}.wav"
            output_path = output_dir / f"sound_letter_{token}_v2.ogg"

            download_with_backoff(info["download_url"], source_path)
            original = duration(source_path)
            run([
                "ffmpeg", "-y", "-hide_banner", "-loglevel", "error",
                "-i", str(source_path), "-vn", "-af", boundary_trim,
                "-ac", "1", "-ar", "48000", "-c:a", "libvorbis", "-q:a", "5",
                str(output_path),
            ])
            processed = duration(output_path)
            status, rationale = qc(letter, processed)
            records.append({
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
                "original_duration_ms": round(original * 1000),
                "processed_duration_ms": round(processed * 1000),
                "qc_status": status,
                "qc_rationale": rationale,
                "sha256": sha256(output_path),
                "transformation": "boundary-silence trim; mono 48 kHz Ogg Vorbis; no pitch/tempo change",
            })
            print(f"{index:02d} {letter}: {original:.3f}s -> {processed:.3f}s [{status}]", flush=True)
            time.sleep(DOWNLOAD_DELAY_SECONDS)

    report = {
        "decision": "D027",
        "task": "AUDIO-02",
        "source_series": "Wikimedia Commons: Буква <LETTER>.wav",
        "expected_author": EXPECTED_AUTHOR,
        "expected_license": EXPECTED_LICENSE_TOKEN,
        "target_duration_ms": [400, 700],
        "processing_policy": "boundary silence trim only; no pitch or tempo manipulation",
        "assets": records,
        "summary": {
            "count": len(records),
            "target": sum(r["qc_status"] == "TARGET" for r in records),
            "articulation_exception": sum(r["qc_status"] == "ARTICULATION_EXCEPTION" for r in records),
            "review_short": sum(r["qc_status"] == "REVIEW_SHORT" for r in records),
            "review_long": sum(r["qc_status"] == "REVIEW_LONG" for r in records),
        },
    }
    (output_dir / "audio_pack_v2_qc.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    lines = [
        "# Audio Pack v2 candidate — attribution and QC", "",
        "Human-recorded source candidate for D027 / AUDIO-02.", "",
        f"- Source author: `{EXPECTED_AUTHOR}` (validated via Commons metadata for all 33 files).",
        f"- Source license: `{EXPECTED_LICENSE_TOKEN}` (validated for all 33 files).",
        "- Changes: boundary-silence trim and Ogg Vorbis transcode; no pitch/tempo change.",
        "- D027 target: 400–700 ms; multi-word names may require justified articulation exceptions.",
        "- Derivative audio remains CC BY-SA 4.0 and requires attribution + indication of changes.", "",
        "| # | Letter | Expected name | Output | Original ms | Candidate ms | QC | Source |",
        "|---:|:---:|---|---|---:|---:|---|---|",
    ]
    for r in records:
        lines.append(
            "| {index} | {letter} | {expected_spoken_name} | `{output_file}` | {original_duration_ms} | "
            "{processed_duration_ms} | {qc_status} | [{source_file}]({source_page}) |".format(**r)
        )
    s = report["summary"]
    lines += ["", "## Summary", "", f"- TARGET: {s['target']}",
              f"- ARTICULATION_EXCEPTION: {s['articulation_exception']}",
              f"- REVIEW_SHORT: {s['review_short']}", f"- REVIEW_LONG: {s['review_long']}", "",
              "Duration QC is mechanical only; human listening/device smoke is still required."]
    (output_dir / "AUDIO_PACK_V2_ATTRIBUTION.md").write_text("\n".join(lines) + "\n", encoding="utf-8")

    if len(records) != 33:
        raise RuntimeError(f"Expected 33 assets, generated {len(records)}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    build(args.output)


if __name__ == "__main__":
    main()
