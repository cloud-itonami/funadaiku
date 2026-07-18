(ns funadaiku.methods.test-agent
  "funadaiku 船大工 — agent cell tests. 1:1 port of py/test_agent.py (custom harness → clojure.test).
  Offline (no llm): zero-emission propulsion enforcement (G8), green-H₂ chain-of-custody (G9), USDC
  tithe-split (G7), and the 9 lifecycle handlers."
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [funadaiku.methods.agent :as agent]))

(deftest test-steel-block-fabrication-plan
  (let [out (agent/handle-steel-block-fabrication {"blockId" "block-001" "dimensions" "12000x8000x25"})]
    (is (= "cutting-plan" (get out "status")))
    (is (contains? out "cutting_plan"))))

(deftest test-grand-block-assembly-needs-minimum-blocks
  (let [out (agent/handle-grand-block-assembly {"gblockId" "gb-001" "subBlocks" []})]
    (is (some? (get out "error")))))

(deftest test-grand-block-assembly-success
  (let [out (agent/handle-grand-block-assembly {"gblockId" "gb-001" "subBlocks" ["block-a" "block-b" "block-c"]})]
    (is (= "jig-designed" (get out "status")))
    (is (contains? out "witness_quorum"))))

(deftest test-weld-ndt-zero-welds-pass
  (let [out (agent/handle-weld-ndt-inspection {"gblockId" "gb-001" "welds" []})]
    (is (= "pass" (get out "result")))
    (is (= [] (get out "defects")))))

(deftest test-weld-ndt-with-welds
  (let [out (agent/handle-weld-ndt-inspection {"gblockId" "gb-001" "welds" [{"id" "w1"} {"id" "w2"}]})]
    (is (some? (get out "ndt_eval")))))

(deftest test-gate-g8-rejects-fossil
  (let [out (agent/gate-zero-emission-propulsion {"type" "diesel-main-engine"})]
    (is (= false (get out "ok")))
    (is (str/includes? (str/lower-case (get out "reason" "")) "fossil"))))

(deftest test-gate-g8-rejects-missing-hydrogen
  (let [out (agent/gate-zero-emission-propulsion {"type" "solar-wind"})]
    (is (= false (get out "ok")))
    (is (str/includes? (str/lower-case (get out "reason" "")) "hydrogen"))))

(deftest test-gate-g8-rejects-missing-solar
  (let [out (agent/gate-zero-emission-propulsion {"type" "wind-hydrogen"})]
    (is (= false (get out "ok")))
    (is (str/includes? (str/lower-case (get out "reason" "")) "solar"))))

(deftest test-gate-g9-rejects-non-green-hydrogen
  (let [out (agent/gate-zero-emission-propulsion {"type" "wind-solar-hydrogen"
                                                  "hydrogen_source_certification" "fossil-steam-reforming"})]
    (is (= false (get out "ok")))
    (is (str/includes? (str/lower-case (get out "reason" "")) "green"))))

(deftest test-gate-g8-g9-pass-green-zero-emission
  (let [out (agent/gate-zero-emission-propulsion {"type" "wind-solar-hydrogen"
                                                  "hydrogen_source_certification" "green-h2-electrolyzer-renewable"})]
    (is (= true (get out "ok")))))

(deftest test-powertrain-integration-blocked-by-non-zero-emission
  (let [out (agent/handle-powertrain-integration {"powertrain" {"type" "coal-auxiliary-boiler"}})]
    (is (= true (get out "blocked")))
    (is (some? (get out "reason")))))

(deftest test-powertrain-integration-success
  (let [out (agent/handle-powertrain-integration
             {"vesselId" "nagi-0001"
              "powertrain" {"type" "wind-solar-hydrogen" "wind_rig_type" "Flettner rotor"
                            "solar_wattage" 160000 "fc_power_mw" 2.4 "battery_capacity_mwh" 2.0
                            "hydrogen_source_certification" "green-h2-electrolyzer-norway"}
              "gross_minor" 500000000})]
    (is (= true (get out "powertrain_verified")))
    (is (contains? out "settlement_intent"))))

(deftest test-outfitting-mep-verify
  (let [out (agent/handle-outfitting {"vesselId" "nagi-0001"})]
    (is (= "outfitting-complete" (get out "status")))
    (is (contains? out "mep_verification"))))

(deftest test-launch-commissioning-gnc-smoke
  (let [out (agent/handle-launch-commissioning {"vesselId" "nagi-0001"})]
    (is (= true (get out "gnc_ready")))
    (is (= "launched" (get out "status")))))

(deftest test-sea-trial-efficiency-measure
  (let [out (agent/handle-sea-trial {"vesselId" "nagi-0001"})]
    (is (= true (get out "sea_trial_pass")))
    (is (contains? out "sea_trial_plan"))))

(deftest test-decarbonization-audit-imo-ghg
  (let [out (agent/handle-decarbonization-audit {"vesselId" "nagi-0001"})]
    (is (some? (get out "eexi_rating")))
    (is (some? (get out "cii_rating")))))

(deftest test-class-certification-terminal
  (let [out (agent/handle-class-certification-binder {"vesselId" "nagi-0001"})]
    (is (= true (get out "imo_registered")))
    (is (= "class-certified" (get out "status")))))

(deftest test-settlement-tithe-split
  (let [s (agent/build-settlement-intent 500000000)]
    (is (= 50000000 (get s "titheMinor")))
    (is (= 450000000 (get s "shipyardPayoutMinor")))
    (is (= "intent" (get s "state")))
    (is (= "usdc-base-l2" (get s "rail")))))

(deftest test-settlement-executed-only-with-operator-sig
  (is (= "executed" (get (agent/build-settlement-intent 1000000 "0xoperatorsig") "state"))))
