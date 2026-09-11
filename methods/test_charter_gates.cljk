(ns funadaiku.methods.test-charter-gates
  "funadaiku — constitutional-gate conformance tests (manifest + central lexicons).
  Substrate-native Clojure (ADR-2606160842). 1:1 port of the pruned methods/test_charter_gates.py."
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.set :as set]
            [cheshire.core :as json]))

(def ^:private here (.getParentFile (java.io.File. ^String *file*)))      ;; methods/
(def ^:private actor-dir (.getParentFile here))                          ;; funadaiku/
(def ^:private actor-name (.getName actor-dir))
(def ^:private root (.. actor-dir getParentFile getParentFile))          ;; 20-actors → ROOT
(def ^:private lexdir (java.io.File. root (str "00-contracts/lexicons/com/etzhayyim/" actor-name)))
(defn- manifest [] (json/parse-string (slurp (java.io.File. actor-dir "manifest.jsonld"))))
(defn- lex [name] (json/parse-string (slurp (java.io.File. lexdir (str name ".json")))))

(def ^:private all-records
  ["blockFabricationAttestation" "grandBlockAssemblyAttestation"
   "outfittingAttestation" "powertrainIntegrationAttestation"
   "launchCommissioningRecord" "seaTrialRecord" "weldInspectionRecord"
   "decarbonizationAudit"])

(defn- collect [doc attr]
  (let [acc (atom {})]
    (letfn [(walk [x parent]
              (cond (map? x) (do (when (and (string? parent) (contains? x attr))
                                   (swap! acc assoc parent (get x attr)))
                                 (doseq [[k v] x] (walk v k)))
                    (sequential? x) (doseq [v x] (walk v parent))))]
      (walk doc nil)) @acc))
(defn- known [doc field] (some-> (get (collect doc "knownValues") field) set))
(defn- required-union [doc]
  (let [acc (atom #{})]
    (letfn [(walk [x] (cond (map? x) (do (when (sequential? (get x "required")) (swap! acc into (get x "required")))
                                         (doseq [v (vals x)] (walk v)))
                            (sequential? x) (doseq [v x] (walk v))))]
      (walk doc)) @acc))

;; ── full gate set ──
(deftest test-all-14-gates-declared
  (is (= (set (keys (get-in (manifest) ["constitutionalGates" "gates"])))
         (set (map #(str "G" %) (range 1 15))))))

;; ── G13 — every powertrain + decarbonization record attests fossil-engine status ──
(deftest test-g13-fossil-engine-attested
  (is (contains? (required-union (lex "powertrainIntegrationAttestation")) "fossilEngine"))
  (is (contains? (required-union (lex "decarbonizationAudit")) "fossilEngine")))

;; ── G14 — green-fuel chain-of-custody, well-to-wake ──
(deftest test-g14-green-fuel-coc-well-to-wake
  (let [doc (lex "decarbonizationAudit")
        req (required-union doc)]
    (doseq [field ["greenFuelCocVerified" "scope"]]
      (is (contains? req field)))
    (is (contains? (known doc "scope") "well-to-wake"))))

;; ── G7/G12 — autonomy degree + fuel-cell power recorded on the powertrain ──
(deftest test-g7-g12-mass-degree-and-fuelcell
  (let [req (required-union (lex "powertrainIntegrationAttestation"))]
    (doseq [field ["massDegree" "fuelCellKw"]]
      (is (contains? req field)))))

;; ── COLREG-compliant sea trial ──
(deftest test-colreg-compliant-sea-trial
  (let [req (required-union (lex "seaTrialRecord"))]
    (doseq [field ["colregCompliant" "speedTrialKn"]]
      (is (contains? req field)))))

;; ── witness-signed attestation on every build record ──
(deftest test-witness-signed-on-all-records
  (doseq [name all-records]
    (let [req (required-union (lex name))]
      (is (and (contains? req "robotDid") (contains? req "signature"))))))

;; ── weld inspection uses a standard NDT method ──
(deftest test-weld-inspection-ndt-methods
  (is (= #{"RT" "UT" "PT" "MT" "VT"} (known (lex "weldInspectionRecord") "method"))))
