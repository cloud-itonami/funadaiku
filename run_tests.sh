#!/usr/bin/env bash
# funadaiku 船大工 — bb/clj test suite (ADR-2606160842 py->clj port wave; cell+method Python pruned).
set -euo pipefail
cd "$(dirname "$0")/../.."
exec kbb -e '(require (quote clojure.test) (quote funadaiku.cells.test-state-machine) (quote funadaiku.methods.test-charter-gates) (quote funadaiku.methods.test-voyage-energy) (quote funadaiku.cells.sea-trial.test-cell) (quote funadaiku.py.test-agent) )(let [r (clojure.test/run-tests (quote funadaiku.cells.test-state-machine) (quote funadaiku.methods.test-charter-gates) (quote funadaiku.methods.test-voyage-energy) (quote funadaiku.cells.sea-trial.test-cell) (quote funadaiku.py.test-agent) )](System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
