(ns funadaiku.cells.sea-trial.state-machine
  "1:1 port of cells/sea_trial/state_machine.py (ADR-2606013400). sea_trial state machine — ADR-2606013400 (L5c speed / endurance / autonomy (MASS) / COLREG trial).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-speed-trial [state]
  {"cell_state" (assoc (cs state) "phase" "speed_trial" "completionPct" 20) "next_node" "endurance_trial"})
(defn transition-to-endurance-trial [state]
  {"cell_state" (assoc (cs state) "phase" "endurance_trial" "completionPct" 40) "next_node" "mass_autonomy_trial"})
(defn transition-to-mass-autonomy-trial [state]
  {"cell_state" (assoc (cs state) "phase" "mass_autonomy_trial" "completionPct" 60) "next_node" "colreg_trial"})
(defn transition-to-colreg-trial [state]
  {"cell_state" (assoc (cs state) "phase" "colreg_trial" "completionPct" 80) "next_node" "record_emitted"})
(defn transition-to-record-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "record_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate sea_trial via Council ADR (post-2606013415 ratification)" {:scaffold true})))
