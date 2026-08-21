#!/usr/bin/env python3
"""Probe the complete Russian GCompris alphabet voice pack for D027/AUDIO-02.

The probe intentionally keeps GCompris audio OUT of the Android app. It downloads the
33 GPL-licensed reference clips into a CI artifact, measures duration/format, and emits
an explicit license warning so the files cannot be accidentally treated as production
Audio Pack v2 assets.
"""

from __future__ import annotations

import json
import shutil
import subprocess
import time
import urllib.request
from pathlib import Path

BASE_RAW = "https://raw.githubusercontent.com/gcompris/GCompris-data/master/voices/ru/alphabet"
SOURCE_PAGE = "https://github.com/gcompris/GCompris-data/tree/master/voices/ru/alphabet"
SOURCE_AUTHOR = "Alexey V. Lubimov"
SOURCE_LICENSE = "GPL (per voices/ru/alphabet/README)"
TARGET_MIN_MS = 400
TARGET_MAX_MS = 700

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
MULTIWORD = {"Й", "Ъ", "Ь"}
USER_AGENT = "App_ABC-AUDIO-02-GCompris-Probe/1.0"


def resource_filename(letter: str) -> str:
    lower = letter.lower()
    if len(lower) != 1:
        raise ValueError(f"Expected one code point: {letter!r}")
    return f"U{ord(lower):04X}.ogg"


def download(url: str, destination: Path) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response, destination.open("wb") as out:
        shutil.copyfileobj(response, out)


def probe(path: Path) -> dict[str, object]:
    completed = subprocess.run(
        [
            "ffprobe", "-v", "error", "-select_streams", "a:0",
            "-show_entries", "format=duration:stream=codec_name,sample_rate,channels",
            "-of", "json", str(path),
        ],
        check=True, capture_output=True, text=True,
    )
    payload = json.loads(completed.stdout)
    stream = payload["streams"][0]
    duration_ms = round(float(payload["format"]["duration"]) * 1000)
    return {
        "duration_ms": duration_ms,
        "codec": stream.get("codec_name"),
        "sample_rate_hz": int(stream.get("sample_rate", 0)),
        "channels": int(stream.get("channels", 0)),
    }


def qc(letter: str, duration_ms: int) -> str:
    if TARGET_MIN_MS <= duration_ms <= TARGET_MAX_MS:
        return "TARGET"
    if letter in MULTIWORD and duration_ms > TARGET_MAX_MS:
        return "ARTICULATION_EXCEPTION_CANDIDATE"
    return "REVIEW_SHORT" if duration_ms < TARGET_MIN_MS else "REVIEW_LONG"


def main() -> None:
    if shutil.which("ffprobe") is None:
        raise RuntimeError("ffprobe is required")

    output_dir = Path("build/gcompris-audio-probe")
    output_dir.mkdir(parents=True, exist_ok=True)
    records: list[dict[str, object]] = []

    for index, (letter, spoken_name, token) in enumerate(LETTERS, start=1):
        source_file = resource_filename(letter)
        local = output_dir / f"{index:02d}_{token}_{source_file}"
        download(f"{BASE_RAW}/{source_file}", local)
        technical = probe(local)
        status = qc(letter, int(technical["duration_ms"]))
        record = {
            "index": index,
            "letter": letter,
            "expected_spoken_name": spoken_name,
            "token": token,
            "source_file": source_file,
            "source_author": SOURCE_AUTHOR,
            "source_license": SOURCE_LICENSE,
            "source_page": SOURCE_PAGE,
            **technical,
            "qc_status": status,
        }
        records.append(record)
        print(
            f"{index:02d} {letter} {spoken_name!r}: {source_file} "
            f"{technical['duration_ms']}ms {technical['codec']} "
            f"{technical['sample_rate_hz']}Hz [{status}]",
            flush=True,
        )
        time.sleep(0.1)

    if len(records) != 33:
        raise RuntimeError(f"Expected 33 files, got {len(records)}")

    summary = {
        "count": len(records),
        "target": sum(r["qc_status"] == "TARGET" for r in records),
        "articulation_exception_candidate": sum(
            r["qc_status"] == "ARTICULATION_EXCEPTION_CANDIDATE" for r in records
        ),
        "review_short": sum(r["qc_status"] == "REVIEW_SHORT" for r in records),
        "review_long": sum(r["qc_status"] == "REVIEW_LONG" for r in records),
        "min_duration_ms": min(int(r["duration_ms"]) for r in records),
        "max_duration_ms": max(int(r["duration_ms"]) for r in records),
    }
    report = {
        "decision": "D027",
        "task": "AUDIO-02",
        "purpose": "reference/QC only; NOT approved for bundling",
        "license_warning": (
            "Russian alphabet README labels these recordings GPL. Do not copy them into "
            "App_ABC production resources without an explicit repository/license decision."
        ),
        "semantic_warning": (
            "Filename/code-point coverage proves letter mapping, not that every clip exactly "
            "matches App_ABC ApprovedCurriculum spokenName. Human listening is required."
        ),
        "summary": summary,
        "assets": records,
    }
    (output_dir / "gcompris_audio_probe.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print("SUMMARY " + json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
