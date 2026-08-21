#!/usr/bin/env python3
"""Discover short licensed Wikimedia Commons recordings for ApprovedCurriculum letter names.

The probe first checks exact `Ru-<spokenName>.ogg` files, then searches Commons for
semantically exact filename variants such as `Ru-ё (jo).ogg`. It does not modify audio.
Candidates are measured and grouped by author/license to determine whether a coherent
33-letter D027 pack actually exists before binaries are committed.
"""

from __future__ import annotations

import html
import json
import re
import shutil
import subprocess
import tempfile
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

COMMONS_API = "https://commons.wikimedia.org/w/api.php"
USER_AGENT = "App_ABC-AudioPackV2-Probe/1.1 (https://github.com/dronrome1245/App_ABC)"
TARGET_MIN_MS = 400
TARGET_MAX_MS = 700
SEARCH_LIMIT = 12

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
PAREN_SUFFIX_RE = re.compile(r"\s*\([^)]*\)\s*$")


def plain(value: str | None) -> str:
    return html.unescape(TAG_RE.sub("", value or "")).strip()


def normalized_name(value: str) -> str:
    value = unicodedata.normalize("NFD", value).replace("\u0301", "")
    return unicodedata.normalize("NFC", value).strip().casefold()


def spoken_from_file_title(title: str) -> str | None:
    name = title.removeprefix("File:")
    if not name.casefold().endswith((".ogg", ".oga", ".wav", ".flac")):
        return None
    stem = name.rsplit(".", 1)[0]
    if not stem.startswith("Ru-"):
        return None
    spoken = PAREN_SUFFIX_RE.sub("", stem.removeprefix("Ru-")).strip()
    return normalized_name(spoken)


def request_json(params: dict[str, str | int]) -> dict:
    request = urllib.request.Request(
        f"{COMMONS_API}?{urllib.parse.urlencode(params)}", headers={"User-Agent": USER_AGENT}
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def search_titles(spoken: str) -> list[str]:
    payload = request_json({
        "action": "query", "format": "json", "formatversion": "2",
        "list": "search", "srnamespace": 6, "srlimit": SEARCH_LIMIT,
        "srsearch": f'intitle:"Ru-{spoken}"',
    })
    expected = normalized_name(spoken)
    titles = []
    for hit in payload.get("query", {}).get("search", []):
        title = hit.get("title", "")
        if spoken_from_file_title(title) == expected:
            titles.append(title)
    return titles


def image_info_for_titles(titles: list[str]) -> dict[str, dict[str, str]]:
    result: dict[str, dict[str, str]] = {}
    for offset in range(0, len(titles), 40):
        chunk = titles[offset:offset + 40]
        payload = request_json({
            "action": "query", "format": "json", "formatversion": "2",
            "prop": "imageinfo", "iiprop": "url|extmetadata", "titles": "|".join(chunk),
        })
        for page in payload.get("query", {}).get("pages", []):
            title = page.get("title", "")
            if page.get("missing") is True:
                continue
            infos = page.get("imageinfo", [])
            if len(infos) != 1:
                continue
            info = infos[0]
            metadata = info.get("extmetadata", {})
            def meta(key: str) -> str:
                return plain(metadata.get(key, {}).get("value"))
            result[title] = {
                "download_url": info.get("url", ""),
                "description_url": info.get("descriptionurl", ""),
                "author": meta("Artist"),
                "license": meta("LicenseShortName"),
                "license_url": meta("LicenseUrl"),
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

    discovered: dict[str, list[str]] = {}
    all_titles: list[str] = []
    for _, spoken, _ in LETTERS:
        exact = f"File:Ru-{spoken}.ogg"
        titles = [exact] + [t for t in search_titles(spoken) if t != exact]
        titles = list(dict.fromkeys(titles))
        discovered[spoken] = titles
        all_titles.extend(titles)
        time.sleep(0.2)

    infos = image_info_for_titles(list(dict.fromkeys(all_titles)))
    records = []
    with tempfile.TemporaryDirectory(prefix="appabc-commons-name-discovery-") as tmp:
        temp = Path(tmp)
        serial = 0
        for index, (letter, spoken, token) in enumerate(LETTERS, start=1):
            candidates = []
            for title in discovered[spoken]:
                info = infos.get(title)
                if not info or not info.get("download_url"):
                    continue
                serial += 1
                suffix = title.rsplit(".", 1)[-1].lower()
                local = temp / f"{serial:03d}.{suffix}"
                download(info["download_url"], local)
                ms = duration_ms(local)
                candidate = {
                    "title": title,
                    "duration_ms": ms,
                    "qc_status": qc(letter, ms),
                    "author": info.get("author", ""),
                    "license": info.get("license", ""),
                    "license_url": info.get("license_url", ""),
                    "source_page": info.get("description_url", ""),
                }
                candidates.append(candidate)
                time.sleep(0.25)

            candidates.sort(key=lambda c: (
                0 if c["qc_status"] == "TARGET" else 1,
                0 if "pogrebnoj" in c["author"].casefold() else 1,
                abs(c["duration_ms"] - 550),
            ))
            best = candidates[0] if candidates else None
            records.append({
                "index": index, "letter": letter, "spoken_name": spoken, "token": token,
                "candidate_count": len(candidates), "best": best, "candidates": candidates,
            })
            if best:
                print(
                    f"{index:02d} {letter} {spoken!r}: {best['qc_status']} {best['duration_ms']}ms "
                    f"{best['title']} author={best['author']!r} license={best['license']!r} "
                    f"({len(candidates)} candidates)", flush=True,
                )
            else:
                print(f"{index:02d} {letter} {spoken!r}: NO EXACT-SEMANTIC FILE CANDIDATE", flush=True)

    bests = [r["best"] for r in records if r["best"]]
    targets = [b for b in bests if b["qc_status"] == "TARGET"]
    pogrebnoj_targets = [b for b in targets if "pogrebnoj" in b["author"].casefold()]
    summary = {
        "letters": 33,
        "letters_with_candidates": len(bests),
        "letters_without_candidates": 33 - len(bests),
        "best_in_target": len(targets),
        "best_target_by_pogrebnoj": len(pogrebnoj_targets),
        "best_authors": sorted({b["author"] for b in bests if b["author"]}),
        "best_licenses": sorted({b["license"] for b in bests if b["license"]}),
    }
    report = {
        "decision": "D027", "task": "AUDIO-02",
        "search_rule": "Commons namespace 6; exact spoken name with optional parenthetical suffix",
        "summary": summary, "assets": records,
    }
    Path("build").mkdir(exist_ok=True)
    Path("build/commons-spoken-name-probe.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print("SUMMARY " + json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
