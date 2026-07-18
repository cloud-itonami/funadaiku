#!/usr/bin/env bash
# funadaiku 船大工 — bb/clj test suite (ADR-2606160842 py->clj port wave; cell+method Python pruned).
# The former test_agent_parity.clj (python3-subprocess-vs-clj LIVE parity) is gone: it always
# compared against a py/agent.py that no longer exists anywhere in this repo, so every run
# silently no-op'd via its own graceful-skip fallback.
set -euo pipefail
repo_dir="$(cd "$(dirname "$0")" && pwd)"
classpath_dir="$(mktemp -d "${TMPDIR:-/tmp}/funadaiku-classpath.XXXXXX")"
trap 'rm -rf "$classpath_dir"' EXIT
ln -s "$repo_dir" "$classpath_dir/funadaiku"
cd "$repo_dir"
exec bb -cp "$classpath_dir" -e '(require (quote clojure.test) (quote funadaiku.cells.test-state-machine) (quote funadaiku.methods.test-charter-gates) (quote funadaiku.methods.test-voyage-energy) (quote funadaiku.cells.sea-trial.test-cell) (quote funadaiku.methods.test-agent) )(let [r (clojure.test/run-tests (quote funadaiku.cells.test-state-machine) (quote funadaiku.methods.test-charter-gates) (quote funadaiku.methods.test-voyage-energy) (quote funadaiku.cells.sea-trial.test-cell) (quote funadaiku.methods.test-agent) )](System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
