(ns funadaiku.cells.steel-block-fabrication.state-machine
  "1:1 port of cells/steel_block_fabrication/state_machine.py (ADR-2606013400). steel_block_fabrication state machine — ADR-2606013400 (L1 panel line + curved/flat block + sub-assembly).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-material-verified [state]
  {"cell_state" (assoc (cs state) "phase" "material_verified" "completionPct" 20) "next_node" "panel_cut_welded"})
(defn transition-to-panel-cut-welded [state]
  {"cell_state" (assoc (cs state) "phase" "panel_cut_welded" "completionPct" 40) "next_node" "block_formed"})
(defn transition-to-block-formed [state]
  {"cell_state" (assoc (cs state) "phase" "block_formed" "completionPct" 60) "next_node" "block_qa_passed"})
(defn transition-to-block-qa-passed [state]
  {"cell_state" (assoc (cs state) "phase" "block_qa_passed" "completionPct" 80) "next_node" "attestation_emitted"})
(defn transition-to-attestation-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "attestation_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate steel_block_fabrication via Council ADR (post-2606013415 ratification)" {:scaffold true})))
