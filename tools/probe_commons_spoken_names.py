#!/usr/bin/env python3
"""Probe Wikimedia Commons for short Russian letter-name recordings matching ApprovedCurriculum.

No audio is modified. The script downloads only files that exist, measures their duration,
and reports author/license consistency so a candidate source can be accepted or rejected
before binary assets are committed.
"""

from __future__ import annotations

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
USER_AGENT = "App_ABC-AudioPackV2-Probe/1.0 (https://github.com/dronrome1245/App_ABC)"
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
TAG_RE = re.compile(r"<[^>]+>")


def plain(value: str | None) -> str:
    return html.unescape(TAG_RE.sub("", value or "")).strip()


def request_json(url: str) -> dict:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def manifest() -> dict[str, dict[str, str]]:
    titles = "|".join(f"File:Ru-{spoken}.ogg" for _, spoken, _ in LETTERS)
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
        file_name = title.removeprefix("File:")
        if page.get("missing") is True:
            result[file_name] = {"missing": "true"}
            continue
        infos = page.get("imageinfo", [])
        if len(infos) != 1:
            result[file_name] = {"missing": "true"}
            continue
        info = infos[0]
        metadata = info.get("extmetadata", {})
        def meta(key: str) -> str:
            return plain(metadata.get(key, {}).get("value"))
        result[file_name] = {
            "missing": "false",
            "download_url": info.get("url", ""),
            "description_url": info.get("descriptionurl", ""),
            "author": meta("Artist"),
            "license": meta("LicenseShortName"),
            "license_url": meta("LicenseUrl"),
            "source": meta("Credit") or meta("Source"),
        }
    return result


def download(url: str, destination: Path) -> None:
    last_error: Exception | None = None
    for wait in (0, 5, 15):
        if wait:
            time.sleep(wait)
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        try:
            with urllib.request.urlopen(request, timeout=60) as response, destination.open("wb") as output:
                shutil.copyfileobj(response, output)
            return
        except urllib.error.HTTPError as error:
            last_error = error
            if error.code not in {429, 503}:
                raise
    raise RuntimeError(f"download failed: {url}") from last_error


def duration_ms(path: Path) -> int:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration",
         "-of", "default=noprint_wrappers=1:nokey=1", str(path)],
        check=True, capture_output=True, text=True,
    )
    return round(float(result.stdout.strip()) * 1000)


def qc(letter: str, ms: int) -> str:
    if TARGET_MIN_MS <= ms <= TARGET_MAX_MS:
        return "TARGET"
    if letter in MULTIWORD and ms > TARGET_MAX_MS:
        return "ARTICULATION_EXCEPTION_CANDIDATE"
    return "REVIEW_SHORT" if ms < TARGET_MIN_MS else "REVIEW_LONG"


def main() -> None:
    if shutil.which("ffprobe") is None:
        raise RuntimeError("ffprobe is required")
    source_manifest = manifest()
    records = []
    with tempfile.TemporaryDirectory(prefix="appabc-spoken-name-probe-") as tmp:
        temp = Path(tmp)
        for index, (letter, spoken, token) in enumerate(LETTERS, start=1):
            source_name = f"Ru-{spoken}.ogg"
            info = source_manifest.get(source_name, {"missing": "true"})
            record = {
                "index": index,
                "letter": letter,
                "spoken_name": spoken,
                "token": token,
                "source_file": source_name,
                "missing": info.get("missing") == "true",
                "author": info.get("author", ""),
                "license": info.get("license", ""),
                "license_url": info.get("license_url", ""),
                "source_page": info.get("description_url", ""),
            }
            if not record["missing"]:
                local = temp / f"{index:02d}.ogg"
                download(info["download_url"], local)
                ms = duration_ms(local)
                record["duration_ms"] = ms
                record["qc_status"] = qc(letter, ms)
            else:
                record["duration_ms"] = None
                record["qc_status"] = "MISSING"
            records.append(record)
            print(
                f"{index:02d} {letter} {spoken!r}: {record['qc_status']} "
                f"duration={record['duration_ms']} author={record['author']!r} license={record['license']!r}",
                flush=True,
            )
            time.sleep(0.35)

    summary = {
        "count": len(records),
        "found": sum(not r["missing"] for r in records),
        "missing": sum(r["missing"] for r in records),
        "target": sum(r["qc_status"] == "TARGET" for r in records),
        "articulation_exception_candidate": sum(r["qc_status"] == "ARTICULATION_EXCEPTION_CANDIDATE" for r in records),
        "review_short": sum(r["qc_status"] == "REVIEW_SHORT" for r in records),
        "review_long": sum(r["qc_status"] == "REVIEW_LONG" for r in records),
        "authors": sorted({r["author"] for r in records if r["author"]}),
        "licenses": sorted({r["license"] for r in records if r["license"]}),
    }
    report = {"decision": "D027", "task": "AUDIO-02", "source_pattern": "Ru-<ApprovedCurriculum.spokenName>.ogg", "summary": summary, "assets": records}
    Path("build").mkdir(exist_ok=True)
    Path("build/commons-spoken-name-probe.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("SUMMARY " + json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
