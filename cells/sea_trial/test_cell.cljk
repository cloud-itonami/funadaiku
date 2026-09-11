(ns funadaiku.cells.sea-trial.test-cell
  "clojure.test port of the sea_trial assertions from
  `cells/test_state_machines.py` (ADR-2606013400).

  The Python `test_state_machines.py` is GENERIC over all 9 funadaiku cells
  (it discovers every `cells/*/state_machine.py` and asserts each transition
  chain reaches 100%). This file ports ONLY the sea_trial slice of that generic
  contract — the INIT→…→RECORD_EMITTED chain + CellState defaults + the R0
  `solve()` raise — exercised against the ported `cell.cljc`.

  Deferred (still Python-only `state_machine.py` / `cell.py`, not yet ported to
  .cljc): the OTHER 8 cells the generic Python test also covers —
  steel_block_fabrication, grand_block_assembly, weld_ndt_inspection,
  powertrain_integration, outfitting, launch_commissioning,
  decarbonization_audit, class_certification_binder — plus
  `test_nine_cells_present` (the cross-cell directory-set drift guard) and the
  generic `test_all_state_machines_transition_to_completion` over all 9."
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [funadaiku.cells.sea-trial.cell :as cell]))

;; ── CellState defaults (fresh 0% INIT record) ─────────────────────

(deftest test-cell-state-defaults-to-zero-pct-init
  (let [cs cell/cell-state]
    (is (= 0 (:completionPct cs)) "CellState should default to 0%")
    (is (= "init" (:phase cs)) "default phase is SeaTrialPhase.INIT.value")
    (is (= "NAGI-COASTAL-0001" (:vesselId cs)))
    (is (= "Nagi 凪" (:vesselClass cs)))))

;; ── the transition chain (each step well-formed; phase ∈ enum) ────

(def ^:private phase-values (set (vals cell/sea-trial-phases)))

(deftest test-each-transition-well-formed
  (testing "every transition_to_* returns {cell-state {phase∈enum, 0<pct≤100}, next-node}"
    (doseq [fn [cell/transition-to-speed-trial
                cell/transition-to-endurance-trial
                cell/transition-to-mass-autonomy-trial
                cell/transition-to-colreg-trial
                cell/transition-to-record-emitted]]
      (let [out (fn {:cell-state {}})
            new (:cell-state out)
            pct (:completionPct new)]
        (is (map? out))
        (is (contains? phase-values (:phase new)))
        (is (and (integer? pct) (< 0 pct) (<= pct 100)))
        (is (contains? out :next-node))))))

(deftest test-transitions-monotone-and-reach-completion
  (let [pcts (map (fn [f] (-> (f {:cell-state {}}) :cell-state :completionPct))
                  [cell/transition-to-speed-trial
                   cell/transition-to-endurance-trial
                   cell/transition-to-mass-autonomy-trial
                   cell/transition-to-colreg-trial
                   cell/transition-to-record-emitted])]
    (is (= [20 40 60 80 100] (vec pcts)))
    (is (= (count pcts) (count (set pcts))) "no duplicate completionPct")
    (is (= 100 (apply max pcts)) "transitions reach 100%")))

(deftest test-full-chain-init-to-record-emitted
  ;; thread the chain so the next-node hand-offs line up with the graph edges
  (let [s0 (cell/transition-to-speed-trial {:cell-state {}})
        s1 (cell/transition-to-endurance-trial s0)
        s2 (cell/transition-to-mass-autonomy-trial s1)
        s3 (cell/transition-to-colreg-trial s2)
        s4 (cell/transition-to-record-emitted s3)]
    (is (= "endurance_trial" (:next-node s0)))
    (is (= "mass_autonomy_trial" (:next-node s1)))
    (is (= "colreg_trial" (:next-node s2)))
    (is (= "record_emitted" (:next-node s3)))
    (is (= "end" (:next-node s4)))
    (is (= "record_emitted" (:phase (:cell-state s4))))
    (is (= 100 (:completionPct (:cell-state s4))))))

;; ── closed CellState surface (CellState(**...) parity) ────────────

(deftest test-closed-cell-state-surface
  (testing "an unexpected cell-state field raises"
    (is (thrown? clojure.lang.ExceptionInfo
                 (cell/transition-to-speed-trial {:cell-state {:bogus 1}})))))

;; ── R0 solve gate (cell .solve() raises — ADR-2606013415) ─────────

(deftest test-solve-raises-at-r0
  (testing "solve raises until Council R1 activation (the R0 scaffold gate)"
    (is (thrown? clojure.lang.ExceptionInfo (cell/solve {} {})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"ADR-2606013415"
                          (cell/solve (cell/make-sea-trial-cell) {})))))

#?(:clj
   (defn -main [& _]
     (run-tests 'funadaiku.cells.sea-trial.test-cell)))
