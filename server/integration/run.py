#! /usr/bin/env python3

import time
from pathlib import Path
import subprocess
import socket
import sys

HOST = '127.0.0.1'
PORT = 42069
TIMEOUT = 10

def ensure_connection():
    deadline = time.monotonic() + TIMEOUT
    while time.monotonic() < deadline:
        try:
            with socket.create_connection((HOST, PORT)):
                return
        except ConnectionRefusedError:
            time.sleep(0.1)
    raise TimeoutError(
            f"Server did not start on {HOST}:{PORT}"
            )

def main():
    project_dir = Path(__file__).resolve().parent.parent
    jar = project_dir / "target" / "server-1.0-SNAPSHOT.jar"

    server = subprocess.Popen([
        "java",
        "-jar",
        str(jar)
        ])

    try:
        ensure_connection()

        process_result = subprocess.run([
            sys.executable,
            "-m",
            "pytest",
            "integration/python",
            ])

        return process_result.returncode
    finally:
        server.terminate()
        server.wait()

if __name__ == "__main__":
    sys.exit(main())
