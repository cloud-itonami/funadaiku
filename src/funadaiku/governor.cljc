(ns funadaiku.governor
  "funadaiku 船大工 — constitutional Governor.

  `manifest.jsonld` declares 14 immutable constitutional gates (G1-G14) and 12
  non-goals (N1-N12). Until this namespace existed they were declared and
  nothing evaluated them: a design proposal with a diesel main engine would have
  been carried by every cell in the repo without a single line of code objecting.
  This namespace is the part that says no.

  It is a pure `.cljc` decision function — no IO, no ambient authority. Callers
  load the design record (`data/vessel.edn` shape: a vector of EAVT-ish entity
  maps) and hand it in; the Governor returns a verdict. Reading the manifest and
  appending to an audit ledger belong to the host, not here.

  ## Three outcomes, and why there is no second way to say yes

  `:approved` / `:refused` / `:indeterminate`. The third is the point. A rule
  whose input is absent has *not* passed — a proposal that simply omits its
  propulsion cannot be confirmed zero-emission, and G13 is the gate that defines
  this actor. Folding that case into `:approved` would make a missing field the
  cheapest way through the constitution. Use `approved?`, which is true for
  `:approved` alone.

  ## Enforced vs screened, both reported

  `:enforced` rules are always evaluated; missing input makes them
  `:indeterminate`. `:screen` rules can only fire on a positive declaration —
  a design record that declares armament is refused under N1, but a record that
  is silent about armament has not thereby proven civilian intent. Screens that
  did not fire are reported as `:not-declared`, never as satisfied.

  Gates about process and artifacts (G1-G6, G9-G11, and the conduct non-goals)
  are not decidable from a design record at all. They are listed in
  `:deferred` on every verdict, so an `:approved` states in its own output how
  much of the constitution it did not check. An approval that quietly covered
  all 26 provisions would be the more dangerous artifact."
  (:require [clojure.string :as str]))

;; ── constitutional constants ────────────────────────────────────────────────
;; These mirror G7/G12 in manifest.jsonld. `governor-test` cross-checks each
;; number against the declared gate text, so editing one here without amending
;; the constitution fails the suite rather than silently widening a cap.

(def max-dwt              5000)  ; G12
(def max-service-speed-kn 14)    ; G12
(def max-mass-degree      3)     ; G7 / G12 (Degree 4 is N10)
(def max-sonar-db         180)   ; G8 — NMFS cetacean Level A

(def allowed-propulsion-kinds
  "G13 is an allowlist, not a blocklist. An unrecognised kind is refused rather
  than passed, so the gate does not depend on this repo having enumerated every
  fossil variant somebody might write."
  #{:wind-assist :solar-pv
    :hydrogen-fuelcell :ammonia-fuelcell :methanol-fuelcell
    :lfp-battery :battery :electric-azimuth-pod :electric-drive})

(def fossil-propulsion-kinds
  "Named only to tell a fossil engine apart from an unknown word in the
  refusal reason. Removing a member does not admit it — the allowlist decides."
  #{:hfo :mgo :diesel :marine-diesel :lng :lng-dual-fuel
    :fossil-genset :auxiliary-diesel :steam-turbine-hfo})

(def nuclear-propulsion-kinds #{:nuclear :nuclear-reactor :smr})

(def green-fuel-kinds
  "Fuel-cell kinds whose well-to-wake claim depends on a green production
  chain-of-custody (G14)."
  #{:hydrogen-fuelcell :ammonia-fuelcell :methanol-fuelcell})

(def naval-vessel-types #{:naval :warship :naval-auxiliary :military-sealift :grey-hull})

;; ── proposal shape ──────────────────────────────────────────────────────────

(defn- entities
  "Normalise a proposal to a seq of entity maps. Accepts the `data/vessel.edn`
  vector directly, or a map wrapping it under :entities."
  [proposal]
  (cond
    (sequential? proposal) (filter map? proposal)
    (map? proposal)        (let [es (:entities proposal)]
                             (if (sequential? es) (filter map? es) [proposal]))
    :else                  []))

(defn- entity-with [es k] (first (filter #(contains? % k) es)))

(defn- context [proposal]
  (let [es (entities proposal)]
    {:entities    es
     :vessel      (entity-with es :vessel/id)
     :autonomy    (entity-with es :autonomy/id)
     :decarb      (entity-with es :decarb/id)
     :propulsions (filterv #(contains? % :propulsion/kind) es)}))

;; ── finding constructors ────────────────────────────────────────────────────

(defn- violated [reason detail subject]
  {:outcome :violated :reason reason :detail detail :subject subject})

(defn- indeterminate [reason detail]
  {:outcome :indeterminate :reason reason :detail detail})

(defn- not-declared [reason detail]
  {:outcome :not-declared :reason reason :detail detail})

(defn- over? [v limit] (and (number? v) (> v limit)))

;; ── the rules ───────────────────────────────────────────────────────────────
;; Each :check returns a seq of findings; empty means satisfied. :reason keywords
;; are the stable literals the tests pin — renaming one is a contract change.

(def rules
  [{:id "G7" :kind :gate :mode :enforced
    :title "Autonomy <= IMO MASS Degree 3"
    :check (fn [{:keys [autonomy]}]
             (let [d (:autonomy/mass-degree autonomy)]
               (cond
                 (nil? d)             [(indeterminate :mass-degree-not-declared
                                        "no :autonomy/mass-degree in the proposal")]
                 (over? d max-mass-degree)
                 [(violated :autonomy-degree-over-cap
                    (str "MASS Degree " d " exceeds the Degree " max-mass-degree " ceiling")
                    (:autonomy/id autonomy))]
                 :else [])))}

   {:id "G8" :kind :gate :mode :enforced
    :title "Navigation/obstacle sonar <= 180 dB re 1uPa @1m"
    :check (fn [{:keys [autonomy]}]
             (let [db (:autonomy/sonar-db-cap autonomy)]
               (cond
                 (nil? db) [(indeterminate :sonar-cap-not-declared
                              "no :autonomy/sonar-db-cap in the proposal")]
                 (over? db max-sonar-db)
                 [(violated :sonar-over-cetacean-cap
                    (str db " dB exceeds the " max-sonar-db " dB NMFS cetacean Level A cap")
                    (:autonomy/id autonomy))]
                 :else [])))}

   {:id "G12" :kind :gate :mode :enforced
    :title "KPI caps: <=5000 DWT, <=14 kn service speed, MASS <= Degree 3"
    :check (fn [{:keys [vessel autonomy]}]
             (let [dwt   (:vessel/dwt vessel)
                   speed (:vessel/service-speed-kn vessel)
                   deg   (:autonomy/mass-degree autonomy)]
               (cond-> []
                 (nil? dwt)   (conj (indeterminate :dwt-not-declared
                                      "no :vessel/dwt in the proposal"))
                 (nil? speed) (conj (indeterminate :service-speed-not-declared
                                      "no :vessel/service-speed-kn in the proposal"))
                 (over? dwt max-dwt)
                 (conj (violated :dwt-over-cap
                         (str dwt " DWT exceeds the " max-dwt " DWT cap")
                         (:vessel/id vessel)))
                 (over? speed max-service-speed-kn)
                 (conj (violated :service-speed-over-cap
                         (str speed " kn exceeds the " max-service-speed-kn " kn service-speed cap")
                         (:vessel/id vessel)))
                 (over? deg max-mass-degree)
                 (conj (violated :autonomy-degree-over-cap
                         (str "MASS Degree " deg " exceeds the Degree " max-mass-degree " cap")
                         (:vessel/id vessel))))))}

   {:id "G13" :kind :gate :mode :enforced
    :title "DEFINING — zero-emission propulsion ONLY; no fossil main or auxiliary engine"
    :check (fn [{:keys [vessel decarb propulsions]}]
             (if (empty? propulsions)
               ;; The defining gate cannot be confirmed against an absent
               ;; powertrain. Silence is not a zero-emission claim.
               [(indeterminate :no-propulsion-declared
                  "no :propulsion/kind entity in the proposal; zero-emission cannot be confirmed")]
               (let [kind-findings
                     (for [p propulsions
                           :let [k (:propulsion/kind p)]
                           :when (not (contains? allowed-propulsion-kinds k))
                           ;; Nuclear is refused here too, but N2 is the provision
                           ;; that names it; G13 reports it as off-allowlist.
                           :when (not (contains? nuclear-propulsion-kinds k))]
                       (if (contains? fossil-propulsion-kinds k)
                         (violated :fossil-propulsion
                           (str "propulsion kind " k " is a fossil prime mover")
                           (:propulsion/id p))
                         (violated :unrecognised-propulsion
                           (str "propulsion kind " k " is not on the zero-emission allowlist")
                           (:propulsion/id p))))]
                 (cond-> (vec kind-findings)
                   (false? (:vessel/zero-emission vessel))
                   (conj (violated :zero-emission-disclaimed
                           "the proposal declares :vessel/zero-emission false"
                           (:vessel/id vessel)))
                   (true? (:decarb/fossil-engine decarb))
                   (conj (violated :fossil-engine-declared
                           "the proposal declares :decarb/fossil-engine true"
                           (:decarb/id decarb)))))))}

   {:id "G14" :kind :gate :mode :enforced
    :title "Well-to-wake accounting; green production chain-of-custody for H2/NH3/methanol"
    :check (fn [{:keys [decarb propulsions]}]
             (let [fuel-cells (filterv #(contains? green-fuel-kinds (:propulsion/kind %)) propulsions)
                   scope      (:decarb/scope decarb)
                   ;; `contains?`, not `or`: an explicit `false` is a waiver
                   ;; the constitution must refuse, and `(or false nil)` would
                   ;; hand it back as nil and downgrade it to "not declared".
                   coc-of     (fn [p] (cond
                                        (contains? p :hydrogen/green-coc-required)
                                        (:hydrogen/green-coc-required p)
                                        (contains? p :fuel/green-coc-required)
                                        (:fuel/green-coc-required p)
                                        :else nil))]
               (cond-> []
                 (and (some? scope) (not= :well-to-wake scope))
                 (conj (violated :scope-not-well-to-wake
                         (str "decarbonization scope " scope
                              " understates emissions; G14 requires :well-to-wake")
                         (:decarb/id decarb)))

                 (and (seq fuel-cells) (nil? scope))
                 (conj (indeterminate :decarb-scope-not-declared
                         "a fuel cell is proposed but no :decarb/scope is declared"))

                 :always
                 (into (for [p fuel-cells
                             :let [coc (coc-of p)]
                             :when (not (true? coc))]
                         (if (nil? coc)
                           (indeterminate :green-coc-not-declared
                             (str "fuel cell " (:propulsion/id p)
                                  " declares no green chain-of-custody requirement"))
                           (violated :green-coc-waived
                             (str "fuel cell " (:propulsion/id p)
                                  " waives the green chain-of-custody requirement")
                             (:propulsion/id p))))))))}

   {:id "N2" :kind :non-goal :mode :enforced
    :title "Nuclear propulsion excluded"
    :check (fn [{:keys [propulsions]}]
             (for [p propulsions
                   :when (contains? nuclear-propulsion-kinds (:propulsion/kind p))]
               (violated :nuclear-propulsion
                 (str "propulsion kind " (:propulsion/kind p) " is nuclear")
                 (:propulsion/id p))))}

   {:id "N5" :kind :non-goal :mode :enforced
    :title "Fossil-fuel main or auxiliary propulsion excluded (reinforces G13)"
    :check (fn [{:keys [decarb propulsions]}]
             (cond-> (vec (for [p propulsions
                                :when (contains? fossil-propulsion-kinds (:propulsion/kind p))]
                            (violated :fossil-propulsion
                              (str "propulsion kind " (:propulsion/kind p)
                                   " is a fossil main or auxiliary engine")
                              (:propulsion/id p))))
               (true? (:decarb/fossil-engine decarb))
               (conj (violated :fossil-engine-declared
                       "the proposal declares :decarb/fossil-engine true"
                       (:decarb/id decarb)))))}

   {:id "N10" :kind :non-goal :mode :enforced
    :title "MASS Degree 4 without independent Council review excluded"
    :check (fn [{:keys [autonomy]}]
             (let [d (:autonomy/mass-degree autonomy)]
               (if (and (number? d) (>= d 4)
                        (not (true? (:autonomy/degree-4-council-review autonomy))))
                 [(violated :degree-4-without-council-review
                    (str "MASS Degree " d " is proposed without an independent Council review")
                    (:autonomy/id autonomy))]
                 [])))}

   ;; ── screens: fire on a positive declaration only ──
   {:id "N1" :kind :non-goal :mode :screen
    :title "Naval / armed vessels excluded"
    :check (fn [{:keys [vessel]}]
             (let [t (:vessel/type vessel)
                   arm (:vessel/armament vessel)]
               (cond-> []
                 (contains? naval-vessel-types t)
                 (conj (violated :naval-vessel-type
                         (str "vessel type " t " is a naval type") (:vessel/id vessel)))
                 (and (some? arm) (not= [] arm) (not (false? arm)))
                 (conj (violated :armament-declared
                         "the proposal declares :vessel/armament" (:vessel/id vessel)))
                 (and (nil? t) (nil? arm))
                 (conj (not-declared :no-naval-declaration
                         "no vessel type or armament declared; civilian intent is not thereby proven")))))}

   {:id "N3" :kind :non-goal :mode :screen
    :title "Military stealth / low-observable vessels excluded"
    :check (fn [{:keys [vessel]}]
             (let [lo (:vessel/low-observable vessel)]
               (cond
                 (true? lo) [(violated :low-observable-declared
                               "the proposal declares :vessel/low-observable true"
                               (:vessel/id vessel))]
                 (nil? lo)  [(not-declared :no-signature-declaration
                               "no low-observable declaration in the proposal")]
                 :else [])))}

   {:id "N8" :kind :non-goal :mode :screen
    :title "Beaching-yard ship-breaking excluded"
    :check (fn [{:keys [vessel]}]
             (let [eol (:vessel/eol-route vessel)]
               (cond
                 (nil? eol) [(not-declared :no-eol-route-declared
                               "no :vessel/eol-route in the proposal")]
                 (re-find #"(?i)beach|alang|chittagong|gadani" (str (name eol)))
                 [(violated :beaching-yard-eol
                    (str "end-of-life route " eol " is a beaching yard")
                    (:vessel/id vessel))]
                 :else [])))}

   {:id "N12" :kind :non-goal :mode :screen
    :title "Speed-record / vanity priority over voyage-energy efficiency excluded"
    :check (fn [{:keys [vessel]}]
             (let [p (:vessel/design-priority vessel)]
               (cond
                 (nil? p) [(not-declared :no-design-priority-declared
                             "no :vessel/design-priority in the proposal")]
                 (contains? #{:speed-record :vanity :max-speed} p)
                 [(violated :speed-record-priority
                    (str "design priority " p " ranks speed over voyage-energy efficiency")
                    (:vessel/id vessel))]
                 :else [])))}])

(def deferred-provisions
  "Provisions that a design record cannot decide. Reported on every verdict so
  an :approved never reads as constitutional clearance. Each needs evidence
  from outside the proposal — a signed artifact, a personnel roster, a build log."
  [{:id "G1"  :why "open-source licensing is a property of released CAD/FEA/firmware artifacts"}
   {:id "G2"  :why "class-certification audit log lives on kotoba, not in the design record"}
   {:id "G3"  :why "per-weld IPFS-pinned photo/video is build evidence"}
   {:id "G4"  :why "witness quorum is an Ed25519 signature set on build records"}
   {:id "G5"  :why "JP+EN bilingual minimum is a property of issued reports"}
   {:id "G6"  :why "Charter Rider scan runs over CAD and firmware artifacts"}
   {:id "G9"  :why "vendor-free CAD toolchain is a property of the authoring environment"}
   {:id "G10" :why "Murakumo-only inference is a runtime routing property"}
   {:id "G11" :why "SBT-gated personnel is an operations roster property"}
   {:id "N4"  :why "warship hull-form intent is not decidable from declared scantlings alone"}
   {:id "N6"  :why "flag-of-convenience engineering is a registration decision"}
   {:id "N7"  :why "IUU-fishing / dumping support is an operating-pattern property"}
   {:id "N9"  :why "ballast-water compliance is verified against the installed BWMS"}
   {:id "N11" :why "dark-fleet operation is an AIS/behaviour property at sea"}])

;; ── review ──────────────────────────────────────────────────────────────────

(defn review
  "Review a vessel design proposal against funadaiku's constitution.

  `proposal` is the `data/vessel.edn` shape (a vector of entity maps) or a map
  with :entities. Returns a verdict map:

    :status     :approved | :refused | :indeterminate
    :violations findings that fired, each naming :provision and :reason
    :unresolved rules whose input was absent (NOT passes)
    :screened   screens that found no positive declaration
    :checked    provision ids actually evaluated to a satisfied result
    :deferred   provisions this layer cannot decide

  Refusal wins over indeterminacy, and indeterminacy wins over approval."
  [proposal]
  (let [ctx     (context proposal)
        results (for [{:keys [id kind mode check title]} rules
                      f (check ctx)]
                  (assoc f :provision id :kind kind :mode mode :title title))
        by      (group-by :outcome results)
        viol    (vec (:violated by))
        unres   (vec (:indeterminate by))
        screen  (vec (:not-declared by))
        fired   (into #{} (map :provision) results)
        checked (vec (sort (remove fired (map :id rules))))]
    {:status     (cond (seq viol)  :refused
                       (seq unres) :indeterminate
                       :else       :approved)
     :violations viol
     :unresolved unres
     :screened   screen
     :checked    checked
     :deferred   deferred-provisions}))

(defn approved?
  "True only for a clean :approved. An :indeterminate verdict is not a pass —
  this is the single predicate callers should branch on."
  [verdict]
  (= :approved (:status verdict)))

(defn explain
  "One line per finding, for an operator or an audit ledger entry."
  [verdict]
  (str/join "\n"
    (concat
      [(str "verdict: " (name (:status verdict)))]
      (for [{:keys [provision reason detail]} (:violations verdict)]
        (str "  REFUSED " provision " " reason " — " detail))
      (for [{:keys [provision reason detail]} (:unresolved verdict)]
        (str "  UNRESOLVED " provision " " reason " — " detail))
      [(str "  deferred: " (str/join " " (map :id (:deferred verdict)))
            " (not decidable from a design record)")])))
