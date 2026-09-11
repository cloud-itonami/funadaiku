(ns funadaiku.cells.grand-block-assembly.state-machine
  "1:1 port of cells/grand_block_assembly/state_machine.py (ADR-2606013400). grand_block_assembly state machine — ADR-2606013400 (L2 grand-block erection + joining on building dock).
  R0 scaffold: phase transitions are structural placeholders; .solve() raises until Council Lv6+ ratifies ADR-2606013415."
  (:require [kotoba.lang.text]))

(def cell-state-defaults
  {"phase" "init" "vesselId" "NAGI-COASTAL-0001" "vesselClass" "Nagi 凪"
   "completionPct" 0 "robotSignatures" [] "payload" {}})

(defn- cs [state] (merge cell-state-defaults (get state "cell_state" {})))

(defn transition-to-blocks-staged [state]
  {"cell_state" (assoc (cs state) "phase" "blocks_staged" "completionPct" 20) "next_node" "aligned_on_dock"})
(defn transition-to-aligned-on-dock [state]
  {"cell_state" (assoc (cs state) "phase" "aligned_on_dock" "completionPct" 40) "next_node" "block_joins_welded"})
(defn transition-to-block-joins-welded [state]
  {"cell_state" (assoc (cs state) "phase" "block_joins_welded" "completionPct" 60) "next_node" "hull_girder_qa"})
(defn transition-to-hull-girder-qa [state]
  {"cell_state" (assoc (cs state) "phase" "hull_girder_qa" "completionPct" 80) "next_node" "attestation_emitted"})
(defn transition-to-attestation-emitted [state]
  {"cell_state" (assoc (cs state) "phase" "attestation_emitted" "completionPct" 100) "next_node" "end"})

(defn solve [_input-state]
  (throw (ex-info "funadaiku R0 scaffold: activate grand_block_assembly via Council ADR (post-2606013415 ratification)" {:scaffold true})))
