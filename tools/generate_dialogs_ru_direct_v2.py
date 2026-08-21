#!/usr/bin/env python3
"""Generate duration-calibrated D027 Audio Pack v2 candidates directly from Dialogs-RU VITS2.

Unlike the public Gradio demo (whose Speed slider is clamped to >= 0.5), the model's public
Python API exposes `length_scale` directly. This generator uses that model-level control so
short Russian letter names can be prolonged toward D027's 400–700 ms target without changing
pitch or applying post-hoc time stretching.

Generated audio is written only to a build directory/CI artifact. It is NOT copied into Android
resources automatically; human listening and final source/license acceptance are still required.
"""

from __future__ import annotations

import argparse
import importlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

import numpy as np
from huggingface_hub import snapshot_download
from scipy.io.wavfile import write as write_wav

MODEL_REPO = "frappuccino/dialogs-ru-vits2"
MODEL_LICENSE = "OpenRAIL"
DATASET_REPO = "langswap/dialogs-ru-emotional-conversations"
TARGET_MIN_MS = 400
TARGET_MAX_MS = 700
TARGET_CENTER_MS = 520
MAX_CALIBRATION_PASSES = 2
MIN_LENGTH_SCALE = 0.5
MAX_LENGTH_SCALE = 7.0

# Explicit stress text avoids an additional ruaccent model and makes generation reproducible.
# '+' marks the stressed vowel in the Dialogs-RU vocabulary.
LETTERS = [
    ("А", "а", "+а", "a"),
    ("Б", "бэ", "б+э", "b"),
    ("В", "вэ", "в+э", "v"),
    ("Г", "гэ", "г+э", "g"),
    ("Д", "дэ", "д+э", "d"),
    ("Е", "е", "+е", "e"),
    ("Ё", "ё", "+ё", "yo"),
    ("Ж", "жэ", "ж+э", "zh"),
    ("З", "зэ", "з+э", "z"),
    ("И", "и", "+и", "i"),
    ("Й", "и краткое", "+и кр+аткое", "short_i"),
    ("К", "ка", "к+а", "k"),
    ("Л", "эль", "+эль", "l"),
    ("М", "эм", "+эм", "m"),
    ("Н", "эн", "+эн", "n"),
    ("О", "о", "+о", "o"),
    ("П", "пэ", "п+э", "p"),
    ("Р", "эр", "+эр", "r"),
    ("С", "эс", "+эс", "s"),
    ("Т", "тэ", "т+э", "t"),
    ("У", "у", "+у", "u"),
    ("Ф", "эф", "+эф", "f"),
    ("Х", "ха", "х+а", "h"),
    ("Ц", "цэ", "ц+э", "ts"),
    ("Ч", "чэ", "ч+э", "ch"),
    ("Ш", "ша", "ш+а", "sh"),
    ("Щ", "ща", "щ+а", "shch"),
    ("Ъ", "твёрдый знак", "тв+ёрдый знак", "hard"),
    ("Ы", "ы", "+ы", "y"),
    ("Ь", "мягкий знак", "м+ягкий знак", "soft"),
    ("Э", "э", "+э", "eh"),
    ("Ю", "ю", "+ю", "yu"),
    ("Я", "я", "+я", "ya"),
]
MULTIWORD = {"Й", "Ъ", "Ь"}
SPEAKERS = {"masha": 0, "sveta": 1}
EMOTIONS = {"neutral": 0, "happy": 1}


def duration_ms_from_samples(audio: np.ndarray, sample_rate: int) -> int:
    return round(len(audio) / sample_rate * 1000)


def probe_duration_ms(path: Path) -> int:
    completed = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration",
         "-of", "default=noprint_wrappers=1:nokey=1", str(path)],
        check=True, capture_output=True, text=True,
    )
    return round(float(completed.stdout.strip()) * 1000)


def transcode(source: Path, destination: Path) -> None:
    subprocess.run(
        [
            "ffmpeg", "-y", "-hide_banner", "-loglevel", "error", "-i", str(source),
            "-vn", "-ac", "1", "-ar", "48000", "-c:a", "libvorbis", "-q:a", "5",
            str(destination),
        ],
        check=True,
    )


def qc(letter: str, duration_ms: int) -> str:
    if TARGET_MIN_MS <= duration_ms <= TARGET_MAX_MS:
        return "TARGET"
    if letter in MULTIWORD and duration_ms > TARGET_MAX_MS:
        return "ARTICULATION_EXCEPTION"
    return "REVIEW_SHORT" if duration_ms < TARGET_MIN_MS else "REVIEW_LONG"


def select_letters(letters_arg: str | None) -> list[tuple[str, str, str, str]]:
    if not letters_arg:
        return LETTERS
    requested = {x.strip().upper() for x in letters_arg.split(",") if x.strip()}
    known = {x[0] for x in LETTERS}
    unknown = requested - known
    if unknown:
        raise ValueError(f"Unknown letters: {sorted(unknown)}")
    return [x for x in LETTERS if x[0] in requested]


def load_model():
    model_dir = Path(snapshot_download(MODEL_REPO))
    os.environ["DIALOGS_LOCAL_CKPT"] = str(model_dir)
    sys.path.insert(0, str(model_dir))
    module = importlib.import_module("tts")
    return module.DialogsTTS(), model_dir


def synthesize(tts, text: str, speaker_id: int, emotion_id: int, length_scale: float):
    return tts.synthesize(
        text,
        speaker_id=speaker_id,
        emotion_id=emotion_id,
        normalize=False,
        auto_stress=False,
        length_scale=float(length_scale),
    )


def calibrated_scale(tts, letter: str, model_text: str, speaker_id: int, emotion_id: int):
    sr, baseline_audio, used = synthesize(tts, model_text, speaker_id, emotion_id, 1.0)
    baseline_ms = duration_ms_from_samples(baseline_audio, sr)

    # Multi-word names are explicit D027 articulation-exception candidates. Preserve natural
    # timing if already above the ordinary-letter target rather than forcing them unnaturally short.
    if letter in MULTIWORD and baseline_ms > TARGET_MAX_MS:
        return sr, baseline_audio, used, baseline_ms, 1.0, baseline_ms, 0

    target_ms = TARGET_CENTER_MS
    scale = min(MAX_LENGTH_SCALE, max(MIN_LENGTH_SCALE, target_ms / max(baseline_ms, 1)))
    final_audio = baseline_audio
    final_ms = baseline_ms
    passes = 0

    for passes in range(1, MAX_CALIBRATION_PASSES + 1):
        sr, final_audio, used = synthesize(tts, model_text, speaker_id, emotion_id, scale)
        final_ms = duration_ms_from_samples(final_audio, sr)
        if TARGET_MIN_MS <= final_ms <= TARGET_MAX_MS:
            break
        correction = target_ms / max(final_ms, 1)
        scale = min(MAX_LENGTH_SCALE, max(MIN_LENGTH_SCALE, scale * correction))

    return sr, final_audio, used, baseline_ms, scale, final_ms, passes


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--speaker", choices=sorted(SPEAKERS), default="masha")
    parser.add_argument("--emotion", choices=sorted(EMOTIONS), default="neutral")
    parser.add_argument("--letters", help="Optional comma-separated subset, e.g. А,Б,Л,Ф")
    parser.add_argument("--output", type=Path, default=Path("build/dialogs-ru-direct-v2"))
    args = parser.parse_args()

    if shutil.which("ffmpeg") is None or shutil.which("ffprobe") is None:
        raise RuntimeError("ffmpeg/ffprobe are required")

    tts, model_dir = load_model()
    selected = select_letters(args.letters)
    profile = f"{args.speaker}-{args.emotion}"
    profile_dir = args.output / profile
    profile_dir.mkdir(parents=True, exist_ok=True)
    records = []

    for index, (letter, spoken_name, model_text, token) in enumerate(selected, start=1):
        sr, audio, used, baseline_ms, scale, generated_ms, passes = calibrated_scale(
            tts,
            letter,
            model_text,
            SPEAKERS[args.speaker],
            EMOTIONS[args.emotion],
        )
        wav_path = profile_dir / f"sound_letter_{token}_v2.wav"
        ogg_path = profile_dir / f"sound_letter_{token}_v2.ogg"
        write_wav(wav_path, sr, audio)
        transcode(wav_path, ogg_path)
        ogg_ms = probe_duration_ms(ogg_path)
        wav_path.unlink()
        status = qc(letter, ogg_ms)
        record = {
            "index": index,
            "letter": letter,
            "spoken_name": spoken_name,
            "model_text": model_text,
            "model_used_text": used,
            "resource_file": ogg_path.name,
            "baseline_duration_ms": baseline_ms,
            "length_scale": round(scale, 4),
            "model_duration_ms": generated_ms,
            "ogg_duration_ms": ogg_ms,
            "calibration_passes": passes,
            "qc_status": status,
        }
        records.append(record)
        print(
            f"{profile} {letter} {spoken_name!r}: baseline={baseline_ms}ms "
            f"scale={scale:.3f} -> ogg={ogg_ms}ms [{status}]",
            flush=True,
        )

    summary = {
        "count": len(records),
        "target": sum(r["qc_status"] == "TARGET" for r in records),
        "articulation_exception": sum(r["qc_status"] == "ARTICULATION_EXCEPTION" for r in records),
        "review_short": sum(r["qc_status"] == "REVIEW_SHORT" for r in records),
        "review_long": sum(r["qc_status"] == "REVIEW_LONG" for r in records),
    }
    report = {
        "decision": "D027",
        "task": "AUDIO-02",
        "generator": "direct Dialogs-RU VITS2 with adaptive model-level length_scale",
        "model_repo": MODEL_REPO,
        "model_license": MODEL_LICENSE,
        "training_dataset": DATASET_REPO,
        "speaker": args.speaker,
        "emotion": args.emotion,
        "target_duration_ms": [TARGET_MIN_MS, TARGET_MAX_MS],
        "ordinary_target_center_ms": TARGET_CENTER_MS,
        "processing": "model-level duration control; Ogg transcode only; no pitch shift/time-stretch",
        "model_cache": str(model_dir),
        "summary": summary,
        "assets": records,
        "acceptance_note": (
            "Mechanical duration/mapping QC only. Human listening is required before promoting "
            "one consistent profile into app/src/main/res/raw."
        ),
    }
    (profile_dir / "qc.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print("SUMMARY " + json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
