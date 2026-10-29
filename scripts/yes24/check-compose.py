"""Validate deployment wiring without reading real env files or starting containers."""
import json
import os
from pathlib import Path
import subprocess
import shutil
import tempfile

compose = Path(__file__).resolve().parents[2] / "compose.yml"
with tempfile.TemporaryDirectory(prefix="checkmo-compose-") as folder:
    (Path(folder) / ".env").touch()  # Compose checks existence even with resolution disabled.
    fixture = Path(folder) / "test-values.txt"
    fixture.write_text("GRAFANA_ADMIN_USER=test\nGRAFANA_ADMIN_PASSWORD=test-only\n")
    # Never inherit actual credentials. --no-env-resolution also skips service env_file.
    environment = {"PATH": os.environ["PATH"], "DOCKER_CONFIG": folder}
    executable = ["docker-compose"] if shutil.which("docker-compose") else ["docker", "compose"]
    command = executable + ["--project-directory", folder, "--env-file", str(fixture),
               "-f", str(compose), "config", "--no-env-resolution", "--format", "json"]
    missing = subprocess.run(command, env=environment, capture_output=True, text=True)
    assert missing.returncode != 0 and "YES24_API_KEY is required" in missing.stderr, "Missing key must stop config validation"
    configured = subprocess.run(command, env={**environment, "YES24_API_KEY": "test-only-key"},
                                capture_output=True, text=True)
    assert configured.returncode == 0, "Configured deployment must validate"
    app = json.loads(configured.stdout)["services"]["app"]["environment"]
    assert app["YES24_API_KEY"] == "test-only-key"
    profiles = app["SPRING_PROFILES_ACTIVE"].split(",")
    assert "yes24" in profiles and "aladin" not in profiles
    print("PASS: missing key rejected; YES24 profile and test key wired (2 cases)")
