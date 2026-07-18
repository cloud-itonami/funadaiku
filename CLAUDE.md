# 20-actors/funadaiku — CLAUDE.md

## Identity

- **Name**: funadaiku (船大工 / ふなだいく — "shipwright")
- **DID**: `did:web:etzhayyim.com:funadaiku`
- **ADR**: ADR-2606013400 (R0 scaffold, 2026-06-01)
- **Status**: R0 scaffold — all cells import-clean, raise `RuntimeError` on `.solve()`
- **Parent actor**: etzhayyim religious-corp (zero-emission autonomous cargo shipbuilding Tier-B)
- **Surface counterpart of**: watatsumi (綿津見 submersible, ADR-2605252200)
- **Reuses GNC**: kami-autodrive `VehicleClass::Ship` → `dynamics::ShipHydro` (ADR-2606010600)

## What this actor is

The **shipwright** — it designs and builds **zero-emission autonomous cargo ships**. It is the
surface sibling of `watatsumi` (which builds submersibles): it inherits watatsumi's modular
grand-block shipyard line and the `kami-autodrive` ShipHydro autonomy brain, and adds the
**wind-assist + solar + hydrogen fuel-cell + LFP-battery + electric-pod powertrain** as its
defining, constitutional subsystem.

**There is no fossil main or auxiliary engine. That is the whole point (G13 / N5).**

## Reference vessel — Nagi 凪 class (`data/vessel.edn`)

Coastal / short-sea zero-emission autonomous cargo carrier (honest, achievable scale):

| Param | Value |
|---|---|
| Type | general cargo, coastal / short-sea |
| DWT | ~3,000 |
| LOA × beam | ~90 m × ~15 m |
| Service speed | ~10 kn |
| Wind-assist | 2 × 24 m rotor (Flettner) or rigid wing sail |
| Solar deck | ~800 m² PV ≈ 160 kWp (hikari `solar-pv-400w`) |
| Hydrogen | PEM FC ≈ 2 × 1.2 MW, green-H₂ mandatory (G14) |
| Battery | LFP ≈ 2 MWh (hikari `storage`) |
| Drive | 2 × 1.5 MW electric azimuth pods |
| Autonomy | IMO MASS Degree 3 (remote, no crew); Degree 4 Council-gated |

The propulsion-energy budget is computed empirically by `methods/voyage_energy.py`
(`out/voyage-energy-report.md`) — wind-assist + solar + hydrogen shares of a representative voyage.

## Shipyard — funadaiku yard (`data/shipyard.edn` + `data/building.edn`)

The plant that builds the Nagi class, modelled in kami-engine + kotoba EAVT exactly like the
`giemon factory` (ADR-2606010030): building dock, panel line, block shops, grand-block erection
area, outfitting quay, paint shed, **H₂-safe (ATEX-zoned) powertrain integration bay**, routed
MEP (power / compressed air / cooling water / **H₂ + N₂ purge** / fire). `building.edn` is the
vessel SBOM (rotor sail, PV, FC stack, LFP, e-pod, hull steel → CycloneDX → kotoba `kg/claim/part/*`).

## Architecture — 9 Pregel cells (L1 → L5 + cross + terminal)

```
steel_block_fabrication → grand_block_assembly → weld_ndt_inspection → powertrain_integration
     (L1, naphtali)            (L2, zebulun)         (L3, joseph)            (L4, simeon)
                                                                                  ↓
                       ↓ decarbonization_audit (cross, levi)                      ↓
                                                                                  ↓
       sea_trial ← launch_commissioning ← outfitting ←───────────────────────────┘
        (L5c, levi)      (L5b, dan)         (L5a, dan)
              ↓
       class_certification_binder (terminal, judah)
```

`powertrain_integration` (L4) is the zero-emission heart: wind-assist rig → solar array → H₂
fuel cell → LFP battery + e-pod → GNC flash.

## Robotics Fleet (R0 reservation only)

| Robot | Glyph | Role | Status |
|---|---|---|---|
| Ita-ori | 板織 | Panel-line / curved-block automated welder | R1+ |
| Oshidashi | 押出 | Grand-block transport + erection crane / SPMT | R1+ |
| Tsutsuki | 突き | Hull-seam NDT crawler (RT/UT/PT), weld witness (inherits watatsumi Tako) | R1+ |
| Nuri-de | 塗手 | Hull coating / blasting robot, VOC-bounded | R2+ |
| Otete-yard | — | kuni-umi Otete arm for outfitting + powertrain bay | R1+ |

**G1**: all firmware open-source (Apache 2.0 + Charter Rider).

## Constitutional Gates (G1–G14)

**IMMUTABLE R0–R3.** Stored in `manifest.jsonld` under `constitutionalGates`. Changes require
Council Lv6+ supermajority + new ADR. Key enforcement:

- **G7**: Autonomy ≤ IMO MASS Degree 3 baseline (SAE-equiv L4 ceiling, mirrors wadachi); Degree 4 = N10.
- **G8**: Navigation/obstacle sonar ≤180 dB re 1µPa @1m (cetacean protection).
- **G9**: Vendor-free CAD only (FreeCAD / OpenSCAD / Open CASCADE).
- **G10**: Murakumo-only inference (ADR-2605215000).
- **G12**: KPI caps — ≤5,000 DWT / ≤14 kn / MASS ≤ Degree 3.
- **G13 (defining)**: zero-emission propulsion ONLY — wind-assist + solar + H₂/NH₃/methanol
  fuel-cell + LFP battery + electric drive. **No fossil main or auxiliary engine.**
- **G14**: MARPOL Annex VI + EEXI + CII + IMO GHG **well-to-wake**; H₂/NH₃/methanol must carry a
  **green production chain-of-custody** — hydrogen made from fossil power is not zero-emission.

## Non-Goals (N1–N12)

N1 naval/armed · N2 nuclear · N3 stealth/grey-hull · N4 warship hull forms · **N5 fossil
propulsion** · N6 flag-of-convenience · N7 IUU/dumping · N8 beaching ship-breaking · N9 ballast
invasive transfer · N10 MASS Degree 4 without Council · N11 dark-fleet/sanctions-evasion · N12
speed-record vanity. See `manifest.jsonld` + ADR-2606013400 §Non-Goals.

## Lexicon Namespace

**Root**: `com.etzhayyim.funadaiku` — 9 record stubs (R0):
`blockFabricationAttestation` · `grandBlockAssemblyAttestation` · `weldInspectionRecord` ·
`powertrainIntegrationAttestation` · `outfittingAttestation` · `launchCommissioningRecord` ·
`seaTrialRecord` · `decarbonizationAudit` · `silenCargoShipReview` (Council 5-of-7).

## Build & Deploy (R0 → R1)

**R0 status**: scaffold only, no physical fabrication. All cells raise
`RuntimeError("funadaiku R0 scaffold: activate via Council ADR-2606013415 post-ratification")`.

**Smoke test** (all 9 cells import-clean; requires a working langgraph env):

```bash
cd 20-actors/funadaiku
for c in steel_block_fabrication grand_block_assembly weld_ndt_inspection \
         powertrain_integration outfitting launch_commissioning sea_trial \
         decarbonization_audit class_certification_binder; do
  python -c "import importlib; importlib.import_module('cells.$c')" && echo "$c OK"
done
```

**Voyage energy budget** (no heavy deps; stdlib only):

```bash
python methods/voyage_energy.py    # writes out/voyage-energy-report.md
```

**Tests** (stdlib-only, langgraph-free; the R0 `solve()` RuntimeError gate is
**preserved** — these never call `solve()`, so they do not bypass the Council
ADR-2606013415 activation gate):

```bash
cd 20-actors/funadaiku
python3 methods/test_voyage_energy.py     # 7 tests — zero-emission invariant (fossil=0, wind+solar+H2≈100%, green-H2>0, Admiralty cube law)
python3 cells/test_state_machines.py      # 2 tests — all 9 cell state machines INIT→…→100% (state_machine.py 100% coverage each)
```

Coverage: all 9 `state_machine.py` at 100%; `methods/voyage_energy.py` ~99%.
The `cell.py` Pregel wrappers stay uncovered here (this env's langgraph is broken
by a pydantic/pydantic-core version mismatch — same caveat as other actors); they
are import-smoke-only until the env is fixed and `solve()` is R1-activated.

## Related Files

- `/20-actors/funadaiku/manifest.jsonld` — DID + cells + gates + non-goals + reference vessel
- `/20-actors/funadaiku/data/vessel.edn` — Nagi-class reference design (kotoba EAVT)
- `/20-actors/funadaiku/data/shipyard.edn` — yard plant + MEP
- `/20-actors/funadaiku/data/building.edn` — vessel SBOM
- `/20-actors/funadaiku/methods/voyage_energy.py` — wind/solar/hydrogen energy-budget sim
- `/90-docs/adr/2606013400-funadaiku-zero-emission-cargo-shipbuilding-r0.md` — Master ADR
- `/orgs/etzhayyim/com-etzhayyim-watatsumi/CLAUDE.md` — submersible sibling (block-shipyard methodology, G13)
- `/90-docs/adr/2606010600-kami-autodrive-gnc-autonomy-layer.md` — ShipHydro GNC
