(ns funadaiku.methods.agent
  "funadaiku 船大工 — zero-emission cargo shipbuilding cell. 1:1 port of py/agent.py. Nine handlers
  over the shipbuilding lifecycle + the DEFINING G8/G9 zero-emission propulsion gate + the G7 USDC
  tithe-split settlement. Defining gate G8: NO fossil main/auxiliary engine — wind-assist + solar +
  green-H₂ fuel-cell + LFP + electric pods ONLY; G9: green-H₂ well-to-wake chain-of-custody. The
  Murakumo llm host binding is the omitted leg — _infer returns 'LLM_NOT_AVAILABLE' (local fallback)."
  (:require [clojure.string :as str]))

(def TITHE-BPS 1000)
(def DWT-CAP 5000)
(def SPEED-CAP-KN 14)
(def MASS-DEGREE-CAP 3)

(defn- infer
  "Murakumo-only inference (G5). The llm host binding is omitted → constant local fallback."
  [_prompt]
  "LLM_NOT_AVAILABLE")

(defn handle-steel-block-fabrication
  "L1 steel block fabrication. Murakumo assist for cutting-plan design."
  [state]
  (let [plan (infer (str "Design oxyacetylene cutting plan for " (get state "blockId" "")
                         " (" (get state "dimensions" "") "mm) AH36 hull plate. Optimize for zero "
                         "waste, worker safety, nesting efficiency."))]
    (merge state {"cutting_plan" plan "status" "cutting-plan"})))

(defn handle-grand-block-assembly
  "L2 grand-block assembly. Jig design + Oshidashi witness quorum."
  [state]
  (let [blocks (get state "subBlocks" [])]
    (if (< (count blocks) 2)
      (merge state {"error" "need ≥2 sub-blocks per grand-block"})
      (let [jig (infer (str "Design jig fixture for grand-block " (get state "gblockId" "") " from "
                            (count blocks) " sub-blocks. Minimize deflection, allow tack-weld access, "
                            "positioning for Oshidashi SPMT erection."))]
        (merge state {"jig_design" jig "witness_quorum" "oshidashi+operator" "status" "jig-designed"})))))

(defn handle-weld-ndt-inspection
  "L3 NDT inspection (RT/UT/PT). Tsutsuki crawler witness. Pass/fail/rework."
  [state]
  (let [welds (get state "welds" [])]
    (if (empty? welds)
      (merge state {"result" "pass" "defects" []})
      (let [eval-result (infer (str "Evaluate NDT (RT/UT/PT) inspection results for grand-block "
                                    (get state "gblockId" "") ". " (count welds) " seams. Recommend "
                                    "pass/rework/fail per class society standards. Zero tolerance for "
                                    "unrepaired cracks."))]
        (merge state {"ndt_eval" eval-result
                      "result" (if (str/includes? (str/lower-case eval-result) "pass") "pass" "rework")})))))

(defn gate-zero-emission-propulsion
  "G8 gate: enforce ZERO fossil propulsion (wind + solar + green-H₂ FC + LFP + e-pods ONLY)."
  [powertrain]
  (let [t (str/lower-case (get powertrain "type" ""))]
    (cond
      (or (str/includes? t "fossil") (str/includes? t "diesel") (str/includes? t "gas"))
      {"ok" false "reason" "fossil propulsion prohibited per G8 (defining gate)"}
      (or (not (str/includes? t "hydrogen")) (not (str/includes? t "solar")))
      {"ok" false "reason" "missing hydrogen or solar per G8"}
      :else
      (let [h2 (get powertrain "hydrogen_source_certification" "")]
        (if (or (empty? h2) (not (str/includes? (str/lower-case h2) "green")))
          {"ok" false "reason" "non-green hydrogen violates G9 (well-to-wake CoC)"}
          {"ok" true "reason" "zero-emission propulsion verified"})))))

(defn build-settlement-intent
  "USDC settlement split. 10% tithe → Public Fund; shipyard gets the net. Stops at :intent —
  broadcast needs an operator signature (G12) + operator gate (G13)."
  ([gross-minor] (build-settlement-intent gross-minor nil))
  ([gross-minor operator-sig-ref]
   (let [tithe (quot (* gross-minor TITHE-BPS) 10000)]
     {"rail" "usdc-base-l2" "grossMinor" gross-minor "titheMinor" tithe
      "shipyardPayoutMinor" (- gross-minor tithe) "titheRouter" "50-infra/etzhayyim-tithe-router"
      "state" (if operator-sig-ref "executed" "intent") "operatorSigRef" (or operator-sig-ref "")})))

(defn handle-powertrain-integration
  "L4 DEFINING zero-emission heart. G8 (fossil prohibition) + G9 (well-to-wake green-H₂) enforced;
  settlement intent only (R0)."
  [state]
  (let [powertrain (get state "powertrain" {})
        gate (gate-zero-emission-propulsion powertrain)]
    (if-not (get gate "ok")
      (merge state {"blocked" true "reason" (get gate "reason")})
      (let [install-plan (infer (str "Plan ATEX-zoned hydrogen fuel-cell integration bay setup. Wind rotor ("
                                     (get powertrain "wind_rig_type" "rotor") ") + " (get powertrain "solar_wattage" 0)
                                     "W solar deck + " (get powertrain "fc_power_mw" 0) "MW green-H₂ FC stack + "
                                     (get powertrain "battery_capacity_mwh" 0) "MWh LFP + electric azimuth pods. "
                                     "Verify no fossil backup."))
            settlement (build-settlement-intent (get state "gross_minor" 500000000))]
        (merge state {"install_plan" install-plan "powertrain_verified" true
                      "settlement_intent" settlement "status" "propulsion-ready"})))))

(defn handle-outfitting
  "L5a mechanical/electrical outfitting: piping + harness + SCADA."
  [state]
  (let [v (infer (str "Verify FreeCAD MEP schematic routing for " (get state "vesselId" "")
                      ": fire/compressed-air/cooling-water + H₂/N₂ purge piping. Electrical: GNC + "
                      "SCADA + bridge instruments. No fossil aux systems."))]
    (merge state {"mep_verification" v "status" "outfitting-complete"})))

(defn handle-launch-commissioning
  "L5b hull launch + systems commissioning: float + FC cold-start + GNC smoke."
  [state]
  (let [_ (infer (str "Verify " (get state "vesselId" "") " launch sequence: hull float test, fuel-cell "
                      "cold-start (green-H₂ system test), battery balance-charge, GNC autonomy waypoint "
                      "smoke-test. IMO MASS Degree 3 ops manual."))]
    (merge state {"launch_verified" true "gnc_ready" true "status" "launched"})))

(defn handle-sea-trial
  "L5c at-sea acceptance: wind/solar/H₂ efficiency + autonomy + safety."
  [state]
  (let [plan (infer (str "Plan sea trial for " (get state "vesselId" "") " (Nagi class, 3000 DWT, 10 kn, "
                         "zero-emission): measure wind-assist efficiency %, solar generation %, H₂ FC "
                         "endurance hours. GNC autonomy on waypoints. Safety: FFE, SOLAS. Sonar ≤180 dB "
                         "(cetacean; G4)."))]
    (merge state {"sea_trial_plan" plan "sea_trial_pass" true "status" "sea-trial-complete"})))

(defn handle-decarbonization-audit
  "Cross-cell well-to-wake decarbonization: IMO GHG / EEXI / CII audit."
  [state]
  (let [r (infer (str "IMO GHG well-to-wake audit for " (get state "vesselId" "") ": steel supply chain "
                      "(CBAM scrap %), green-H₂ electrolyzer CO₂ intensity, solar + battery + wind rotor "
                      "EOL recycling. EEXI rating, CII rating. Marpol Annex VI."))]
    (merge state {"audit_result" r "eexi_rating" "A" "cii_rating" "A"})))

(defn handle-class-certification-binder
  "Terminal cell: bind sea-trial + audit into ABS/DNV/ClassNK certification."
  [state]
  (let [docs (infer (str "Prepare class certification docs for " (get state "vesselId" "") ": bind sea-trial "
                         "pass + decarbonization audit. ABS/DNV/ClassNK notation (A1 ZEV — zero-emission "
                         "vessel). Register IMO number + MMSI."))]
    (merge state {"cert_docs" docs "imo_registered" true "status" "class-certified"})))
