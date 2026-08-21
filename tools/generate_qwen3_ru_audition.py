#!/usr/bin/env python3
"""Generate one batched Russian letter-name audition with Qwen3-TTS VoiceDesign.

This intentionally uses a single model request. The goal is phonetic screening first:
verify that the candidate voice actually pronounces Russian letter names correctly before
we attempt segmentation, duration shaping, or Android integration.
"""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
from pathlib import Path

from gradio_client import Client

SPACE = "Qwen/Qwen3-TTS"

DIAGNOSTIC_NAMES = [
    "бэ", "вэ", "гэ", "дэ", "жэ", "зэ", "и краткое", "эль", "эм", "эн",
    "эр", "эс", "эф", "ща", "твёрдый знак", "ы", "мягкий знак", "э", "ю", "я",
]

VOICE_DESCRIPTION = (
    "A warm, gentle native Russian female primary-school teacher speaking to young children. "
    "Standard modern Russian pronunciation with absolutely no foreign accent. Soft, calm, "
    "slightly melodic and encouraging delivery. Clear consonants and natural Russian vowels. "
    "Pronounce each Russian letter name distinctly and naturally. Speak, do not sing or whisper."
)

# Keep the actual synthesis text entirely Cyrillic. Periods create natural boundaries that can
# later be used for segmentation if the pronunciation is accepted by a human listener.
DIAGNOSTIC_TEXT = ". ".join(DIAGNOSTIC_NAMES) + "."


def as_path(value) -> Path:
    if isinstance(value, str):
        return Path(value)
    if isinstance(value, dict):
        path = value.get("path") or value.get("name")
        if path:
            return Path(path)
    path = getattr(value, "path", None) or getattr(value, "name", None)
    if path:
        return Path(path)
    raise TypeError(f"Unsupported Gradio audio result: {value!r}")


def duration_ms(path: Path) -> int:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", str(path)],
        check=True,
        capture_output=True,
        text=True,
    )
    return round(float(result.stdout.strip()) * 1000)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/qwen3-ru-audition")
    args = parser.parse_args()

    root = Path(args.output)
    root.mkdir(parents=True, exist_ok=True)
    client = Client(SPACE)

    generated_audio, status = client.predict(
        text=DIAGNOSTIC_TEXT,
        language="Russian",
        voice_description=VOICE_DESCRIPTION,
        api_name="/generate_voice_design",
    )
    print("VOICE_DESIGN_STATUS", status)

    source = as_path(generated_audio)
    wav = root / "qwen3_russian_diagnostic.wav"
    mp3 = root / "qwen3_russian_diagnostic.mp3"
    ogg = root / "qwen3_russian_diagnostic.ogg"
    shutil.copy2(source, wav)

    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libmp3lame", "-q:a", "2", str(mp3)],
        check=True,
    )
    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libvorbis", "-q:a", "5", str(ogg)],
        check=True,
    )

    metadata = {
        "engine": "Qwen3-TTS VoiceDesign",
        "language": "Russian",
        "purpose": "phonetic screening only",
        "names": DIAGNOSTIC_NAMES,
        "text": DIAGNOSTIC_TEXT,
        "duration_ms": duration_ms(wav),
        "post_time_stretch": False,
        "post_pitch_shift": False,
        "status": status,
    }
    (root / "diagnostic.json").write_text(json.dumps(metadata, ensure_ascii=False, indent=2), encoding="utf-8")
    (root / "voice_description.txt").write_text(VOICE_DESCRIPTION + "\n", encoding="utf-8")
    (root / "spoken_sequence.txt").write_text(DIAGNOSTIC_TEXT + "\n", encoding="utf-8")

    print("SEQUENCE", DIAGNOSTIC_TEXT)
    print("DURATION_MS", metadata["duration_ms"])
    print("SUMMARY generated=1 batched_russian_diagnostic")


if __name__ == "__main__":
    main()
