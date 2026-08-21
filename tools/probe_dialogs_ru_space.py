#!/usr/bin/env python3
"""Discover the public Gradio API for the Dialogs-RU expressive Russian TTS Space.

This is a lightweight discovery step for AUDIO-02. It does not generate or commit audio.
The next step will call only the documented public endpoint and upload generated candidates
as CI artifacts for QC before any production resource is accepted.
"""

from __future__ import annotations

import contextlib
import inspect
import io
from pathlib import Path

from gradio_client import Client

SPACE = "frappuccino/dialogs-ru-tts"


def main() -> None:
    Path("build").mkdir(exist_ok=True)
    buffer = io.StringIO()
    client = Client(SPACE, verbose=False)

    with contextlib.redirect_stdout(buffer):
        print(f"SPACE={SPACE}")
        print(f"Client.view_api signature: {inspect.signature(client.view_api)}")
        try:
            result = client.view_api(all_endpoints=True, print_info=True)
        except TypeError:
            # Compatibility with gradio_client releases whose view_api signature differs.
            result = client.view_api(all_endpoints=True)
        print("\nRETURN_VALUE:")
        print(repr(result))

    text = buffer.getvalue()
    Path("build/dialogs-ru-space-api.txt").write_text(text, encoding="utf-8")
    print(text, flush=True)


if __name__ == "__main__":
    main()
