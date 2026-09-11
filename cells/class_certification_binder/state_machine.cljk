(ns funadaiku.cells.class-certification-binder.state-machine
  "1:1 port of cells/class_certification_binder/state_machine.py (ADR-2606013400). class_certification_binder state machine — ADR-2606013400 (terminal class + IMO MASS audit binder).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-records-gathered [state]
  {"cell_state" (assoc (cs state) "phase" "records_gathered" "completionPct" 25) "next_node" "class_survey_bound"})
(defn transition-to-class-survey-bound [state]
  {"cell_state" (assoc (cs state) "phase" "class_survey_bound" "completionPct" 50) "next_node" "mass_code_bound"})
(defn transition-to-mass-code-bound [state]
  {"cell_state" (assoc (cs state) "phase" "mass_code_bound" "completionPct" 75) "next_node" "binder_anchored"})
(defn transition-to-binder-anchored [state]
  {"cell_state" (assoc (cs state) "phase" "binder_anchored" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate class_certification_binder via Council ADR (post-2606013415 ratification)" {:scaffold true})))
