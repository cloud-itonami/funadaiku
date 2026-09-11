(ns funadaiku.cells.outfitting.state-machine
  "1:1 port of cells/outfitting/state_machine.py (ADR-2606013400). outfitting state machine — ADR-2606013400 (L5a cargo systems + coatings + accommodation + autonomy sensors).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-cargo-systems-fitted [state]
  {"cell_state" (assoc (cs state) "phase" "cargo_systems_fitted" "completionPct" 20) "next_node" "coatings_applied"})
(defn transition-to-coatings-applied [state]
  {"cell_state" (assoc (cs state) "phase" "coatings_applied" "completionPct" 40) "next_node" "accommodation_fitted"})
(defn transition-to-accommodation-fitted [state]
  {"cell_state" (assoc (cs state) "phase" "accommodation_fitted" "completionPct" 60) "next_node" "sensor_suite_installed"})
(defn transition-to-sensor-suite-installed [state]
  {"cell_state" (assoc (cs state) "phase" "sensor_suite_installed" "completionPct" 80) "next_node" "attestation_emitted"})
(defn transition-to-attestation-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "attestation_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate outfitting via Council ADR (post-2606013415 ratification)" {:scaffold true})))
