#!/usr/bin/env python3
"""Generate a native-Russian TTS audition pack with Vosk TTS.

This is an evaluation tool only. Generated files are written under build/ and are
not bundled into the Android application automatically.
"""

from __future__ import annotations

import argparse
import csv
import json
import subprocess
import wave
from pathlib import Path

from vosk_tts import Model, Synth

LETTERS = [
    ("А", "а", "a"),
    ("Б", "бэ", "b"),
    ("В", "вэ", "v"),
    ("Г", "гэ", "g"),
    ("Д", "дэ", "d"),
    ("Е", "е", "e"),
    ("Ё", "ё", "yo"),
    ("Ж", "жэ", "zh"),
    ("З", "зэ", "z"),
    ("И", "и", "i"),
    ("Й", "и краткое", "short_i"),
    ("К", "ка", "k"),
    ("Л", "эль", "l"),
    ("М", "эм", "m"),
    ("Н", "эн", "n"),
    ("О", "о", "o"),
    ("П", "пэ", "p"),
    ("Р", "эр", "r"),
    ("С", "эс", "s"),
    ("Т", "тэ", "t"),
    ("У", "у", "u"),
    ("Ф", "эф", "f"),
    ("Х", "ха", "h"),
    ("Ц", "цэ", "ts"),
    ("Ч", "чэ", "ch"),
    ("Ш", "ша", "sh"),
    ("Щ", "ща", "shch"),
    ("Ъ", "твёрдый знак", "hard_sign"),
    ("Ы", "ы", "y"),
    ("Ь", "мягкий знак", "soft_sign"),
    ("Э", "э", "eh"),
    ("Ю", "ю", "yu"),
    ("Я", "я", "ya"),
]

# Vosk 0.9 exposes 5 speakers. The first 3 are the female voices F01/F02/F03.
SPEAKERS = {0: "F01", 1: "F02", 2: "F03"}
SAMPLE_RATE = 22050


def duration_ms(path: Path) -> int:
    with wave.open(str(path), "rb") as wav:
        return round(wav.getnframes() * 1000 / wav.getframerate())


def concat_preview(wavs: list[Path], output: Path, silence_ms: int = 350) -> None:
    silence_frames = int(SAMPLE_RATE * silence_ms / 1000)
    silence = (b"\x00\x00") * silence_frames
    with wave.open(str(output), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(SAMPLE_RATE)
        for idx, path in enumerate(wavs):
            with wave.open(str(path), "rb") as src:
                if (src.getnchannels(), src.getsampwidth(), src.getframerate()) != (1, 2, SAMPLE_RATE):
                    raise RuntimeError(f"Unexpected WAV format: {path}")
                out.writeframes(src.readframes(src.getnframes()))
            if idx != len(wavs) - 1:
                out.writeframes(silence)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", default="vosk-model-tts-ru-0.9-multi")
    parser.add_argument("--output", default="build/vosk-ru-audition")
    parser.add_argument("--speech-rate", type=float, default=1.0)
    args = parser.parse_args()

    root = Path(args.output)
    root.mkdir(parents=True, exist_ok=True)

    model = Model(model_name=args.model)
    synth = Synth(model)

    summary = {"model": args.model, "speech_rate": args.speech_rate, "speakers": {}}

    for speaker_id, speaker_name in SPEAKERS.items():
        speaker_dir = root / speaker_name
        speaker_dir.mkdir(parents=True, exist_ok=True)
        wavs: list[Path] = []
        rows = []

        for letter, spoken, token in LETTERS:
            wav_path = speaker_dir / f"sound_letter_{token}_v2.wav"
            ogg_path = speaker_dir / f"sound_letter_{token}_v2.ogg"
            # Final punctuation helps the Russian front-end finish short isolated tokens cleanly.
            synth.synth(f"{spoken}.", str(wav_path), speaker_id=speaker_id, speech_rate=args.speech_rate)
            subprocess.run(
                [
                    "ffmpeg", "-y", "-loglevel", "error", "-i", str(wav_path),
                    "-c:a", "libvorbis", "-q:a", "5", str(ogg_path),
                ],
                check=True,
            )
            ms = duration_ms(wav_path)
            wavs.append(wav_path)
            rows.append({"letter": letter, "spoken": spoken, "token": token, "duration_ms": ms})
            print(f"{speaker_name} {letter} {spoken!r}: {ms} ms")

        preview_wav = speaker_dir / f"preview_{speaker_name}.wav"
        preview_mp3 = speaker_dir / f"preview_{speaker_name}.mp3"
        concat_preview(wavs, preview_wav)
        subprocess.run(
            [
                "ffmpeg", "-y", "-loglevel", "error", "-i", str(preview_wav),
                "-c:a", "libmp3lame", "-q:a", "2", str(preview_mp3),
            ],
            check=True,
        )

        with (speaker_dir / "manifest.csv").open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=["letter", "spoken", "token", "duration_ms"])
            writer.writeheader()
            writer.writerows(rows)

        summary["speakers"][speaker_name] = {
            "count": len(rows),
            "min_ms": min(row["duration_ms"] for row in rows),
            "max_ms": max(row["duration_ms"] for row in rows),
        }

    (root / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print("SUMMARY", json.dumps(summary, ensure_ascii=False))


if __name__ == "__main__":
    main()
