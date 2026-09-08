(ns funadaiku.cells.decarbonization-audit.state-machine
  "1:1 port of cells/decarbonization_audit/state_machine.py (ADR-2606013400). decarbonization_audit state machine — ADR-2606013400 (cross-cutting well-to-wake zero-emission verification).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-telemetry-ingested [state]
  {"cell_state" (assoc (cs state) "phase" "telemetry_ingested" "completionPct" 20) "next_node" "well_to_wake_computed"})
(defn transition-to-well-to-wake-computed [state]
  {"cell_state" (assoc (cs state) "phase" "well_to_wake_computed" "completionPct" 40) "next_node" "green_h2_coc_verified"})
(defn transition-to-green-h2-coc-verified [state]
  {"cell_state" (assoc (cs state) "phase" "green_h2_coc_verified" "completionPct" 60) "next_node" "eexi_cii_scored"})
(defn transition-to-eexi-cii-scored [state]
  {"cell_state" (assoc (cs state) "phase" "eexi_cii_scored" "completionPct" 80) "next_node" "audit_emitted"})
(defn transition-to-audit-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "audit_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate decarbonization_audit via Council ADR (post-2606013415 ratification)" {:scaffold true})))
