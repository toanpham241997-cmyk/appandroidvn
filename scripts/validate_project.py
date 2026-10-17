#!/usr/bin/env python3
"""Offline sanity checks run before downloading Gradle dependencies."""
from pathlib import Path
from xml.etree import ElementTree as ET
import hashlib
import re
import sys

root = Path(__file__).resolve().parents[1]
checks = 0

def check(ok: bool, label: str) -> None:
    global checks
    checks += 1
    if not ok:
        sys.exit(f"[FAIL] {label}")
    print(f"[PASS] {label}")

workflow = (root / '.github/workflows/build-apk.yml').read_text()
root_gradle = (root / 'build.gradle.kts').read_text()
app_gradle = (root / 'app/build.gradle.kts').read_text()
settings_gradle = (root / 'settings.gradle.kts').read_text()
key_gate = (root / 'app/src/main/java/com/aujunpeak/monitor/auth/KeyGate.kt').read_text()

check('android-actions/setup-android@v3' not in re.sub(r'^\s*#.*$', '', workflow, flags=re.M),
      'Old Android SDK setup action not executed')
check('runs-on: ubuntu-24.04' in workflow, 'Fixed Linux runner image')
check('workflow_dispatch:' in workflow and 'actions/upload-artifact@v4' in workflow,
      'Manual build and APK upload enabled')
check('gradle-version: \'8.11.1\'' in workflow, 'Gradle pinned to 8.11.1')
check('version "8.9.2"' in root_gradle, 'Android Gradle Plugin pinned to 8.9.2')
check(root_gradle.count('version "2.2.21"') == 2,
      'Kotlin Android and Compose compiler use the same compatible version')
check('compileSdk = 35' in app_gradle and 'targetSdk = 35' in app_gradle,
      'compile/target SDK match image SDK 35')
check('include(":app")' in settings_gradle, 'App module is included')
check('app-debug.apk' in workflow and 'test -s "$APK"' in workflow,
      'APK is checked before upload')
expected = hashlib.sha256(b'AUJUN-2026').hexdigest()
check(f'"{expected}"' in key_gate, 'Demo key SHA-256 is correct')
for path in sorted((root / 'app/src/main/res').rglob('*.xml')) + [root / 'app/src/main/AndroidManifest.xml']:
    ET.parse(path)
    print(f"[PASS] Valid Android XML: {path.relative_to(root)}")
    checks += 1
kotlin_files = list((root / 'app/src/main/java').rglob('*.kt'))
check(len(kotlin_files) >= 4 and all(x.read_text().count('{') >= x.read_text().count('}') for x in kotlin_files),
      'Main Kotlin source files are present')
print(f"Project checks passed: {checks}")
