(ns funadaiku.cells.weld-ndt-inspection.state-machine
  "1:1 port of cells/weld_ndt_inspection/state_machine.py (ADR-2606013400). weld_ndt_inspection state machine — ADR-2606013400 (L3 100% RT/UT/PT hull-seam NDT).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-seams-registered [state]
  {"cell_state" (assoc (cs state) "phase" "seams_registered" "completionPct" 25) "next_node" "rt_ut_pt_run"})
(defn transition-to-rt-ut-pt-run [state]
  {"cell_state" (assoc (cs state) "phase" "rt_ut_pt_run" "completionPct" 50) "next_node" "defects_dispositioned"})
(defn transition-to-defects-dispositioned [state]
  {"cell_state" (assoc (cs state) "phase" "defects_dispositioned" "completionPct" 75) "next_node" "record_emitted"})
(defn transition-to-record-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "record_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate weld_ndt_inspection via Council ADR (post-2606013415 ratification)" {:scaffold true})))
