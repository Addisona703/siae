#!/usr/bin/env python3
"""
Simple multipart upload helper for SIAE media service.

Usage:
    python scripts/multipart_upload.py \
        --file ./big.pdf \
        --token <JWT> \
        --tenant tenant-001 \
        --base-url http://localhost/api/v1/media \
        --part-size 10

Environment variables:
    MEDIA_TOKEN   (fallback for --token)
    MEDIA_TENANT  (fallback for --tenant)
"""

from __future__ import annotations

import argparse
import json
import math
import mimetypes
import os
from pathlib import Path
from typing import List, Dict, Any

import requests


def _read_env_or_arg(value: str | None, env_key: str, display_name: str) -> str:
    resolved = value or os.getenv(env_key)
    if not resolved:
        raise SystemExit(f"{display_name} is required. Pass --{env_key.lower()} or set {env_key}.")
    return resolved


def init_upload(args, file_path: Path, size: int, mime: str) -> Dict[str, Any]:
    payload = {
        "filename": file_path.name,
        "size": size,
        "mime": mime,
        "tenantId": args.tenant,
        "bizTags": args.tags,
        "multipart": {
            "enabled": True,
            "partSize": args.part_size * 1024 * 1024,
        },
        "ext": {
            "filename": file_path.name,
        },
    }
    headers = {
        "Authorization": f"Bearer {args.token}",
        "X-Tenant-Id": args.tenant,
        "Content-Type": "application/json",
    }
    resp = requests.post(f"{args.base_url}/uploads/init", headers=headers, data=json.dumps(payload), timeout=30)
    resp.raise_for_status()
    data = resp.json().get("data")
    if not data:
        raise SystemExit(f"Unexpected init response: {resp.text}")
    return data


def upload_parts(file_path: Path, mime: str, parts: List[Dict[str, Any]], part_size_bytes: int) -> List[Dict[str, Any]]:
    etags: List[Dict[str, Any]] = []
    with file_path.open("rb") as fh:
        for part in parts:
            chunk = fh.read(part_size_bytes)
            if not chunk:
                break
            url = part["url"]
            put_resp = requests.put(url, data=chunk, headers={"Content-Type": mime})
            put_resp.raise_for_status()
            etag = put_resp.headers.get("ETag") or put_resp.headers.get("etag")
            if not etag:
                raise SystemExit("Storage response missing ETag header; cannot complete multipart upload.")
            etags.append({"partNumber": part["partNumber"], "etag": etag.strip('"')})
    if len(etags) != len(parts):
        raise SystemExit(f"Uploaded {len(etags)} parts, expected {len(parts)}.")
    return etags


def complete_upload(args, upload_id: str, etags: List[Dict[str, Any]], size: int) -> Dict[str, Any]:
    payload = {
        "parts": etags,
        "checksum": {
            "size": str(size),
        },
    }
    headers = {
        "Authorization": f"Bearer {args.token}",
        "X-Tenant-Id": args.tenant,
        "Content-Type": "application/json",
    }
    resp = requests.post(f"{args.base_url}/uploads/{upload_id}/complete", headers=headers, data=json.dumps(payload), timeout=30)
    resp.raise_for_status()
    return resp.json().get("data")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Multipart upload helper for SIAE media service.")
    parser.add_argument("--file", required=True, help="Path to the file to upload.")
    parser.add_argument("--base-url", default="http://localhost/api/v1/media", help="Media service base URL.")
    parser.add_argument("--token", help="JWT token (or set MEDIA_TOKEN).")
    parser.add_argument("--tenant", help="Tenant ID (or set MEDIA_TENANT).")
    parser.add_argument("--part-size", type=int, default=10, help="Part size in MB (default: 10).")
    parser.add_argument("--tags", nargs="*", default=["multipart"], help="Optional biz tags.")
    parser.add_argument("--mime", help="Override MIME type.")
    return parser.parse_args()


def main():
    args = parse_args()
    args.token = _read_env_or_arg(args.token, "MEDIA_TOKEN", "JWT token")
    args.tenant = _read_env_or_arg(args.tenant, "MEDIA_TENANT", "Tenant ID")

    file_path = Path(args.file).expanduser().resolve()
    if not file_path.is_file():
        raise SystemExit(f"File not found: {file_path}")

    size = file_path.stat().st_size
    mime = args.mime or mimetypes.guess_type(file_path.name)[0] or "application/octet-stream"
    part_size_bytes = args.part_size * 1024 * 1024
    expected_parts = math.ceil(size / part_size_bytes)
    if expected_parts > 10000:
        raise SystemExit("Too many parts (>10,000). Increase part size.")

    print(f"Initializing upload for {file_path.name} ({size} bytes, {expected_parts} parts)...")
    init_data = init_upload(args, file_path, size, mime)
    parts = init_data["parts"]
    if len(parts) != expected_parts:
        print(f"Warning: server returned {len(parts)} parts, expected {expected_parts}. Using server list.")

    print("Uploading parts...")
    etags = upload_parts(file_path, mime, parts, part_size_bytes)

    print("Completing upload...")
    result = complete_upload(args, init_data["uploadId"], etags, size)
    print("Upload complete.")
    print(json.dumps(result or {}, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
