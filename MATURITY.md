# funadaiku 船大工 — Maturity

**Stage: R0** (scaffold) — ADR-2606013400. Zero-emission autonomous cargo-ship building
(grand-block shipyard + kami-autodrive ShipHydro GNC). Defining gate G13: zero-emission
propulsion ONLY — no fossil main or auxiliary engine. The operator-side counterpart of
niyaku (port cargo handling).

| Dimension | State |
|---|---|
| Lexicons | ✅ 9 under `com.etzhayyim.funadaiku.*` (block/grandBlock/outfitting/powertrain/launch/seaTrial/weld + decarbonizationAudit + silenCargoShipReview) |
| Cells | 🟡 path-reserved (grand-block → outfitting → powertrain → launch → trial, R0) |
| Manifest | ✅ `manifest.jsonld` — `constitutionalGates` (G1–G14) machine-readable |
| Tests | ✅ **4 suites green** — `methods/test_charter_gates.cljk` (**7**, added 2026-06-16) + `methods/test_voyage_energy.py` + `cells/test_state_machines.py` + `py/test_agent.py`; `./run_tests.sh` aggregates all (all standalone-runnable) |
| Methods | 🟡 `voyage_energy.py`/`.cljc` present; offline build engine = R1 |

## Charter gates pinned by the new charter-gate test

- **Full gate set** — manifest declares exactly G1–G14.
- **G13 zero-emission** — `powertrainIntegrationAttestation` + `decarbonizationAudit` both
  require `fossilEngine` (the per-vessel attestation hook; **value (`fossilEngine=false`) is
  enforced in the R1 cell logic**, not schema-const).
- **G14 green-fuel CoC** — `decarbonizationAudit` requires `greenFuelCocVerified` + `scope`
  (scope includes `well-to-wake`).
- **G7/G12 autonomy + powertrain** — `powertrainIntegrationAttestation` requires `massDegree`
  + `fuelCellKw` (MASS ≤ Degree 3 + size/speed caps cell-enforced).
- **COLREG sea trial** — `seaTrialRecord` requires `colregCompliant` + `speedTrialKn`.
- **witness-signed** — every build/audit record requires `robotDid` + `signature`.
- **weld NDT** — `weldInspectionRecord.method` is exactly the standard NDT set {RT,UT,PT,MT,VT}.

## Value caps enforced in cell logic (NOT schema-const — noted for honesty)

`fossilEngine=false` (G13), `massDegree ≤ 3` (G7), `≤5,000 DWT` + `≤14 kn` (G12), sonar
`≤180 dB re 1µPa` (G8 cetacean). These are R1-cell invariants, not schema enums.

## R0 → R1 gate

silenCargoShipReview `r1-scale-model-hil` + Council; cell `.solve()` stays R0-gated.

> **2026-06-17 substrate-native migration (ADR-2606160842):** the charter-gate test above was ported Python→Clojure (`methods/test_charter_gates.py` → `methods/test_charter_gates.cljk`, ns `funadaiku.methods.test-charter-gates`, reads the lexicons via cheshire/edn) and the Python was pruned. Run via `./run_tests.sh` (now `exec bb`) or `kbb -M:test:charter` (all 34 charter suites; 244 tests / 924 assertions green). Assertions unchanged (1:1 port).
