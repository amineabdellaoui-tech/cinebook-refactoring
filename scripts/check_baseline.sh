#!/bin/sh
set -e
SRC="${1:-legacy/CinemaApp.java}"
BASE="legacy/baseline_output.txt"
TMP="$(mktemp)"
java "$SRC" > "$TMP" 2>&1
rm -f cinema_report.txt
if diff -u "$BASE" "$TMP"; then
  echo "OK: output identical to baseline"
  rm -f "$TMP"
else
  echo "FAIL: behaviour changed (see diff above)"
  rm -f "$TMP"
  exit 1
fi
