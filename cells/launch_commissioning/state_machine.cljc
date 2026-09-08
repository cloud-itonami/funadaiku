(ns funadaiku.cells.launch-commissioning.state-machine
  "1:1 port of cells/launch_commissioning/state_machine.py (ADR-2606013400). launch_commissioning state machine — ADR-2606013400 (L5b float-out + inclining test + dock trial).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-floated-out [state]
  {"cell_state" (assoc (cs state) "phase" "floated_out" "completionPct" 25) "next_node" "inclining_test_done"})
(defn transition-to-inclining-test-done [state]
  {"cell_state" (assoc (cs state) "phase" "inclining_test_done" "completionPct" 50) "next_node" "dock_trial_done"})
(defn transition-to-dock-trial-done [state]
  {"cell_state" (assoc (cs state) "phase" "dock_trial_done" "completionPct" 75) "next_node" "record_emitted"})
(defn transition-to-record-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "record_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate launch_commissioning via Council ADR (post-2606013415 ratification)" {:scaffold true})))
