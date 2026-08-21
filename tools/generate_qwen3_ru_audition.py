#!/usr/bin/env python3
"""Generate a Russian letter-name diagnostic audition with Qwen3-TTS.

Evaluation workflow:
1. VoiceDesign creates one synthetic reference voice (not an imitation of a person).
2. VoiceClone reuses that exact synthetic reference for every isolated letter name.
3. We audition pronunciation before doing any duration shaping or Android integration.

Generated files are evaluation artifacts only and are never bundled automatically.
"""

from __future__ import annotations

import argparse
import csv
import shutil
import subprocess
import wave
from pathlib import Path

from gradio_client import Client

SPACE = "Qwen/Qwen3-TTS"

DIAGNOSTIC = [
    ("Б", "бэ", "b"),
    ("В", "вэ", "v"),
    ("Г", "гэ", "g"),
    ("Д", "дэ", "d"),
    ("Ж", "жэ", "zh"),
    ("З", "зэ", "z"),
    ("Й", "и краткое", "short_i"),
    ("Л", "эль", "l"),
    ("М", "эм", "m"),
    ("Н", "эн", "n"),
    ("Р", "эр", "r"),
    ("С", "эс", "s"),
    ("Ф", "эф", "f"),
    ("Щ", "ща", "shch"),
    ("Ъ", "твёрдый знак", "hard_sign"),
    ("Ы", "ы", "y"),
    ("Ь", "мягкий знак", "soft_sign"),
    ("Э", "э", "eh"),
    ("Ю", "ю", "yu"),
    ("Я", "я", "ya"),
]

VOICE_DESCRIPTION = (
    "A warm, gentle native Russian female primary-school teacher speaking to young children. "
    "Standard Russian pronunciation with absolutely no foreign accent. Soft, calm, melodic, "
    "encouraging delivery. Clear consonants and natural Russian vowels. Speak, do not sing or "
    "whisper. The voice should sound pedagogical, friendly, and easy for a child to imitate."
)

REFERENCE_TEXT = (
    "Здравствуйте. Слушай внимательно и спокойно. "
    "Я произношу звуки чётко, по-русски, мягким учительским голосом."
)


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
    # ffprobe handles WAV/FLAC/etc returned by the Space consistently.
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", str(path)],
        check=True,
        capture_output=True,
        text=True,
    )
    return round(float(result.stdout.strip()) * 1000)


def concat_preview(inputs: list[Path], output: Path, silence_s: float = 0.45) -> None:
    # Normalize container/codec only; no time stretch or pitch manipulation.
    concat_list = output.with_suffix(".txt")
    silence = output.parent / "silence.wav"
    subprocess.run(
        [
            "ffmpeg", "-y", "-loglevel", "error", "-f", "lavfi",
            "-i", f"anullsrc=r=24000:cl=mono", "-t", str(silence_s), str(silence),
        ],
        check=True,
    )
    normalized = []
    for index, src in enumerate(inputs):
        wav = output.parent / f"preview_part_{index:02d}.wav"
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", str(src), "-ar", "24000", "-ac", "1", str(wav)],
            check=True,
        )
        normalized.append(wav)
    with concat_list.open("w", encoding="utf-8") as handle:
        for idx, wav in enumerate(normalized):
            handle.write(f"file '{wav.resolve()}'\n")
            if idx != len(normalized) - 1:
                handle.write(f"file '{silence.resolve()}'\n")
    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-f", "concat", "-safe", "0", "-i", str(concat_list), "-c:a", "libmp3lame", "-q:a", "2", str(output)],
        check=True,
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/qwen3-ru-audition")
    args = parser.parse_args()

    root = Path(args.output)
    root.mkdir(parents=True, exist_ok=True)
    client = Client(SPACE)

    designed_audio, design_status = client.predict(
        text=REFERENCE_TEXT,
        language="Russian",
        voice_description=VOICE_DESCRIPTION,
        api_name="/generate_voice_design",
    )
    print("VOICE_DESIGN_STATUS", design_status)
    reference_source = as_path(designed_audio)
    reference = root / "synthetic_reference_voice.wav"
    shutil.copy2(reference_source, reference)

    outputs: list[Path] = []
    rows = []
    for letter, spoken, token in DIAGNOSTIC:
        # Punctuation helps the model resolve an isolated Russian name as a complete utterance.
        generated_audio, status = client.predict(
            ref_audio=str(reference),
            ref_text=REFERENCE_TEXT,
            target_text=f"{spoken}.",
            language="Russian",
            use_xvector_only=False,
            model_size="1.7B",
            api_name="/generate_voice_clone",
        )
        print(f"{letter} {spoken!r}: {status}")
        source = as_path(generated_audio)
        wav = root / f"sound_letter_{token}_qwen.wav"
        ogg = root / f"sound_letter_{token}_qwen.ogg"
        shutil.copy2(source, wav)
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libvorbis", "-q:a", "5", str(ogg)],
            check=True,
        )
        ms = duration_ms(wav)
        outputs.append(wav)
        rows.append({"letter": letter, "spoken": spoken, "token": token, "duration_ms": ms, "status": status})
        print(f"DURATION {letter}: {ms} ms")

    with (root / "manifest.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=["letter", "spoken", "token", "duration_ms", "status"])
        writer.writeheader()
        writer.writerows(rows)

    (root / "voice_description.txt").write_text(VOICE_DESCRIPTION + "\n", encoding="utf-8")
    (root / "reference_text.txt").write_text(REFERENCE_TEXT + "\n", encoding="utf-8")
    concat_preview(outputs, root / "preview_qwen3_russian_diagnostic.mp3")
    print(f"SUMMARY generated={len(rows)} diagnostic_letter_names")


if __name__ == "__main__":
    main()
