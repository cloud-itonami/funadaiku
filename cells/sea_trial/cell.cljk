(ns funadaiku.cells.sea-trial.cell
  "SeaTrialCell — funadaiku 船大工 R0 Pregel cell (L5c speed / endurance /
  autonomy (MASS) / COLREG trial). Per ADR-2606013400.

  1:1 port of `cells/sea_trial/cell.py` + its sibling `state_machine.py` (the
  Python `cell.py` imports the five `transition_to_*` fns from `state_machine.py`,
  so both are folded into this one namespace — there is no separate
  state_machine.cljc for funadaiku, mirroring how the LangGraph wrapper imports
  the pure transitions).

  R0 scaffold: phase transitions are structural placeholders; `solve` raises
  until Council Lv6+ ratifies the R1 activation ADR-2606013415. Lexicon:
  com.etzhayyim.funadaiku.seaTrialRecord.

  Conventions (mimamori/methods/bond.cljc + shionome regime_observer house style):
    - SeaTrialPhase enum → keyword-string map preserving Python value identities
    - @dataclass CellState → a plain map with kebab keyword keys; Python field
      defaults preserved (\"NAGI-COASTAL-0001\", \"Nagi 凪\", etc.)
    - Python str field identities (vessel ids/class) stay strings
    - transitions are pure fns; the closed CellState surface + R0 solve gate
      → ex-info"
  (:require [kotoba.lang.text :as str]))

;; ── SeaTrialPhase (enum — Python value identities preserved) ───────

(def sea-trial-phases
  "The closed SeaTrialPhase vocabulary. Keyed by the idiomatic Clojure enum
  keyword; the value is the Python `SeaTrialPhase.<X>.value` string identity."
  {:init                "init"
   :speed-trial         "speed_trial"
   :endurance-trial     "endurance_trial"
   :mass-autonomy-trial "mass_autonomy_trial"
   :colreg-trial        "colreg_trial"
   :record-emitted      "record_emitted"})

(def phase-init                (:init sea-trial-phases))                ;; "init"
(def phase-speed-trial         (:speed-trial sea-trial-phases))         ;; "speed_trial"
(def phase-endurance-trial     (:endurance-trial sea-trial-phases))     ;; "endurance_trial"
(def phase-mass-autonomy-trial (:mass-autonomy-trial sea-trial-phases)) ;; "mass_autonomy_trial"
(def phase-colreg-trial        (:colreg-trial sea-trial-phases))        ;; "colreg_trial"
(def phase-record-emitted      (:record-emitted sea-trial-phases))      ;; "record_emitted"

;; ── CellState (dataclass → plain map, kebab keys, field defaults) ──

(def cell-state
  "CellState default value — the @dataclass field defaults as a plain map.
  Field names stay camelCase-keyword to match the Python `cs.__dict__` surface
  the LangGraph step returns (vesselId/vesselClass/completionPct), while the
  structural keys follow house kebab style."
  {:phase           phase-init          ;; SeaTrialPhase.INIT.value
   :vesselId        "NAGI-COASTAL-0001"
   :vesselClass     "Nagi 凪"
   :completionPct   0
   :robotSignatures []
   :payload         {}})

(defn make-cell-state
  "Construct a CellState map from a partial cell-state map, filling the
  dataclass defaults (CellState(**state.get(\"cell_state\", {}))). Unknown keys
  → ex-info (closed CellState surface — CellState(**...) would TypeError on an
  unexpected kwarg)."
  [cs]
  (let [cs (or cs {})
        allowed (set (keys cell-state))
        extra (remove allowed (keys cs))]
    (when (seq extra)
      (throw (ex-info (str "unknown CellState field(s): " (vec extra))
                      {:funadaiku/closed-vocab true :extra (vec extra)})))
    (merge cell-state cs)))

;; ── transitions (pure; 1:1 port of state_machine.py) ──────────────

(defn transition-to-speed-trial
  "INIT -> SPEED_TRIAL. Port of `transition_to_speed_trial(state)`."
  [state]
  (let [cs (make-cell-state (:cell-state state))
        cs (assoc cs :phase phase-speed-trial :completionPct 20)]
    {:cell-state cs :next-node "endurance_trial"}))

(defn transition-to-endurance-trial
  "SPEED_TRIAL -> ENDURANCE_TRIAL. Port of `transition_to_endurance_trial(state)`."
  [state]
  (let [cs (make-cell-state (:cell-state state))
        cs (assoc cs :phase phase-endurance-trial :completionPct 40)]
    {:cell-state cs :next-node "mass_autonomy_trial"}))

(defn transition-to-mass-autonomy-trial
  "ENDURANCE_TRIAL -> MASS_AUTONOMY_TRIAL. Port of
  `transition_to_mass_autonomy_trial(state)`."
  [state]
  (let [cs (make-cell-state (:cell-state state))
        cs (assoc cs :phase phase-mass-autonomy-trial :completionPct 60)]
    {:cell-state cs :next-node "colreg_trial"}))

(defn transition-to-colreg-trial
  "MASS_AUTONOMY_TRIAL -> COLREG_TRIAL. Port of `transition_to_colreg_trial(state)`."
  [state]
  (let [cs (make-cell-state (:cell-state state))
        cs (assoc cs :phase phase-colreg-trial :completionPct 80)]
    {:cell-state cs :next-node "record_emitted"}))

(defn transition-to-record-emitted
  "COLREG_TRIAL -> RECORD_EMITTED. Port of `transition_to_record_emitted(state)`."
  [state]
  (let [cs (make-cell-state (:cell-state state))
        cs (assoc cs :phase phase-record-emitted :completionPct 100)]
    {:cell-state cs :next-node "end"}))

;; ── SeaTrialCell (LangGraph wrapper → data-described graph + R0 solve) ──

(def sea-trial-graph
  "The compiled cell graph as plain data (`SeaTrialCell._build_graph`). The
  LangGraph wiring START→speed_trial→…→record_emitted→END is described as an
  edge list + node→transition map; pure data, no langgraph dependency at R0."
  {:nodes ["speed_trial" "endurance_trial" "mass_autonomy_trial"
           "colreg_trial" "record_emitted"]
   :steps {"speed_trial"         transition-to-speed-trial          ;; _step_0
           "endurance_trial"     transition-to-endurance-trial      ;; _step_1
           "mass_autonomy_trial" transition-to-mass-autonomy-trial  ;; _step_2
           "colreg_trial"        transition-to-colreg-trial         ;; _step_3
           "record_emitted"      transition-to-record-emitted}      ;; _step_4
   :edges [["START" "speed_trial"]
           ["speed_trial" "endurance_trial"]
           ["endurance_trial" "mass_autonomy_trial"]
           ["mass_autonomy_trial" "colreg_trial"]
           ["colreg_trial" "record_emitted"]
           ["record_emitted" "END"]]})

(defn make-sea-trial-cell
  "Construct a SeaTrialCell — L5c speed / endurance / autonomy (MASS) / COLREG
  trial (R0 scaffold). Port of `SeaTrialCell.__init__` (self.graph = build)."
  [& _]
  {:graph sea-trial-graph})

(defn solve
  "Execute the cell — R0 scaffold raises until R1 activation. Port of
  `SeaTrialCell.solve` (raise RuntimeError). The cell map is accepted as the
  first arg to mirror the Python instance-method shape; both args are ignored."
  [& _]
  (throw (ex-info (str "funadaiku R0 scaffold: activate via Council "
                       "ADR-2606013415 post-ratification")
                  {:funadaiku/r0-gate true})))
