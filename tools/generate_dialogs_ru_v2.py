#!/usr/bin/env python3
"""Generate D027 Audio Pack v2 candidates through the public Dialogs-RU Gradio Space.

No generated audio is committed automatically. CI uploads OGG candidates + QC manifests as
artifacts for owner listening/selection. A full production pack is accepted only after one
speaker/style is selected consistently for all 33 letters.
"""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import time
from pathlib import Path

from gradio_client import Client

SPACE = "frappuccino/dialogs-ru-tts"
API_NAME = "/generate"
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
PREVIEW_LETTERS = {"А", "Б", "Ё", "Л", "Ф", "Й", "Ъ", "Ь", "Я"}

PROFILES = {
    "masha-neutral": ("👩 Masha / Маша", "😐 neutral / нейтральный"),
    "masha-happy": ("👩 Masha / Маша", "😊 happy / радостный"),
    "sveta-neutral": ("👩 Sveta / Света", "😐 neutral / нейтральный"),
    "sveta-happy": ("👩 Sveta / Света", "😊 happy / радостный"),
}


def probe_duration_ms(path: Path) -> int:
    completed = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration",
         "-of", "default=noprint_wrappers=1:nokey=1", str(path)],
        check=True, capture_output=True, text=True,
    )
    return round(float(completed.stdout.strip()) * 1000)


def qc(letter: str, duration_ms: int) -> str:
    if TARGET_MIN_MS <= duration_ms <= TARGET_MAX_MS:
        return "TARGET"
    if letter in MULTIWORD and duration_ms > TARGET_MAX_MS:
        return "ARTICULATION_EXCEPTION_CANDIDATE"
    return "REVIEW_SHORT" if duration_ms < TARGET_MIN_MS else "REVIEW_LONG"


def generated_path(value: object) -> Path:
    if isinstance(value, str):
        return Path(value)
    if isinstance(value, dict) and isinstance(value.get("path"), str):
        return Path(value["path"])
    path_attr = getattr(value, "path", None)
    if isinstance(path_attr, str):
        return Path(path_attr)
    raise TypeError(f"Unsupported Gradio audio return value: {value!r}")


def predict_with_retry(client: Client, spoken_name: str, speaker: str, emotion: str,
                       speed: float, expressiveness: float) -> tuple[Path, str]:
    last_error: Exception | None = None
    for delay in (0, 3, 8, 20):
        if delay:
            time.sleep(delay)
        try:
            result = client.predict(
                text=spoken_name,
                speaker_label=speaker,
                emotion_label=emotion,
                normalize=False,
                auto_stress=True,
                speed=speed,
                expressiveness=expressiveness,
                api_name=API_NAME,
            )
            if not isinstance(result, (tuple, list)) or len(result) != 2:
                raise RuntimeError(f"Unexpected /generate result: {result!r}")
            return generated_path(result[0]), str(result[1])
        except Exception as error:
            last_error = error
    raise RuntimeError(f"Dialogs-RU generation failed for {spoken_name!r}") from last_error


def transcode(source: Path, destination: Path) -> None:
    subprocess.run(
        [
            "ffmpeg", "-y", "-hide_banner", "-loglevel", "error", "-i", str(source),
            "-vn", "-ac", "1", "-ar", "48000", "-c:a", "libvorbis", "-q:a", "5",
            str(destination),
        ],
        check=True,
    )


def select_letters(mode: str, letters_arg: str | None) -> list[tuple[str, str, str]]:
    if letters_arg:
        requested = [item.strip().upper() for item in letters_arg.split(",") if item.strip()]
        known = {letter for letter, _, _ in LETTERS}
        unknown = set(requested) - known
        if unknown:
            raise ValueError(f"Unknown letters: {sorted(unknown)}")
        requested_set = set(requested)
        return [item for item in LETTERS if item[0] in requested_set]
    if mode == "full":
        return LETTERS
    return [item for item in LETTERS if item[0] in PREVIEW_LETTERS]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=("preview", "full"), default="preview")
    parser.add_argument("--letters", help="Optional comma-separated Cyrillic letters, e.g. А,Б,Л")
    parser.add_argument("--profiles", default=",".join(PROFILES.keys()))
    parser.add_argument("--output", type=Path, default=Path("build/dialogs-ru-v2"))
    parser.add_argument("--speed", type=float, default=1.0)
    parser.add_argument("--expressiveness", type=float, default=0.667)
    args = parser.parse_args()

    if shutil.which("ffprobe") is None or shutil.which("ffmpeg") is None:
        raise RuntimeError("ffmpeg/ffprobe are required")

    selected_profiles = [p.strip() for p in args.profiles.split(",") if p.strip()]
    unknown = set(selected_profiles) - set(PROFILES)
    if unknown:
        raise ValueError(f"Unknown profiles: {sorted(unknown)}")

    selected_letters = select_letters(args.mode, args.letters)
    client = Client(SPACE, verbose=False)
    all_reports = []

    for profile in selected_profiles:
        speaker, emotion = PROFILES[profile]
        profile_dir = args.output / profile
        profile_dir.mkdir(parents=True, exist_ok=True)
        records = []
        for index, (letter, spoken_name, token) in enumerate(selected_letters, start=1):
            source_wav, normalized = predict_with_retry(
                client, spoken_name, speaker, emotion, args.speed, args.expressiveness
            )
            output = profile_dir / f"sound_letter_{token}_v2.ogg"
            transcode(source_wav, output)
            duration_ms = probe_duration_ms(output)
            status = qc(letter, duration_ms)
            record = {
                "index": index,
                "letter": letter,
                "spoken_name": spoken_name,
                "token": token,
                "output_file": output.name,
                "duration_ms": duration_ms,
                "qc_status": status,
                "model_text": normalized,
            }
            records.append(record)
            print(
                f"{profile} speed={args.speed:g} {letter} {spoken_name!r}: "
                f"{duration_ms}ms [{status}] model={normalized!r}",
                flush=True,
            )

        summary = {
            "count": len(records),
            "target": sum(r["qc_status"] == "TARGET" for r in records),
            "articulation_exception_candidate": sum(
                r["qc_status"] == "ARTICULATION_EXCEPTION_CANDIDATE" for r in records
            ),
            "review_short": sum(r["qc_status"] == "REVIEW_SHORT" for r in records),
            "review_long": sum(r["qc_status"] == "REVIEW_LONG" for r in records),
            "min_duration_ms": min(r["duration_ms"] for r in records),
            "max_duration_ms": max(r["duration_ms"] for r in records),
        }
        report = {
            "decision": "D027",
            "task": "AUDIO-02",
            "mode": args.mode,
            "explicit_letters": args.letters,
            "source": SPACE,
            "model": "frappuccino/dialogs-ru-vits2",
            "model_license": "OpenRAIL (verify/retain license terms for production release)",
            "speaker": speaker,
            "emotion": emotion,
            "speed": args.speed,
            "expressiveness": args.expressiveness,
            "summary": summary,
            "assets": records,
            "acceptance_note": (
                "Mechanical QC only. One profile must be selected consistently and human-listened "
                "before generated files can be promoted to app/src/main/res/raw."
            ),
        }
        (profile_dir / "qc.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
        all_reports.append({"profile": profile, **summary})

    (args.output / "summary.json").write_text(
        json.dumps({"mode": args.mode, "profiles": all_reports}, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print("SUMMARY " + json.dumps(all_reports, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
