#!/usr/bin/env python3
"""Inspect the public Qwen3-TTS Hugging Face Space API for Russian audition use."""

from gradio_client import Client

SPACE = "Qwen/Qwen3-TTS"

client = Client(SPACE)
print(client.view_api(all_endpoints=True))
