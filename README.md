# funadaiku 船大工 — zero-emission autonomous cargo shipbuilding

> Tier-B actor · `did:web:etzhayyim.com:funadaiku` · ADR-2606013400 · **R0 scaffold**
> Surface counterpart of **watatsumi** (綿津見 submersible) · reuses **kami-autodrive** ShipHydro GNC

**Organism axis**: Axis 2 — Metabolism (代謝 / 産霊 musuhi) — generative production cycle: builds the zero-emission cargo ships the commons moves goods on (see [`90-docs/2606022500-organism-axis-affiliation-convention.md`](../../90-docs/2606022500-organism-axis-affiliation-convention.md))

**船大工** (shipwright) designs and builds **zero-emission autonomous cargo ships**. It knits two
things the repo already had but had never wired together: the **autonomy brain**
(`kami-autodrive` `VehicleClass::Ship` → `dynamics::ShipHydro`, ADR-2606010600) and the
**energy components** (`hikari` solar PV + LFP storage), and adds a **wind-assist + solar +
hydrogen fuel-cell + LFP-battery + electric-pod powertrain** — with **no fossil engine** (G13/N5).

The shipyard line and modular grand-block method are inherited from **watatsumi**; the whole-plant
layout + MEP + 4D-BIM + SBOM follow the **giemon factory** pattern (ADR-2606010030); both the
vessel and the yard are first-class **kotoba EAVT** data.

## Why hybrid (the design brief)

No single zero-carbon source is a complete prime mover at cargo scale, so the Nagi class combines
them by their strengths:

| Source | Role in Nagi class | Honest limit |
|---|---|---|
| **Wind-assist** (rotor/wing) | primary fuel-saver — adds thrust on most routes | wind-dependent; never the sole prime mover |
| **Solar deck** (~160 kWp) | hotel/auxiliary load + battery top-up | low areal power; not high-speed main propulsion |
| **Hydrogen PEM FC** (~2×1.2 MW) | electrical prime mover | storage volume, bunkering infra, **green-H₂ required** |
| **LFP battery** (~2 MWh) | peak-shaving + harbour zero-emission manoeuvre | energy density |

The decarbonization claim is evaluated **well-to-wake** (G14): hydrogen made from fossil power is
not zero-emission. `methods/voyage_energy.py` computes the per-source share of a representative
voyage → `out/voyage-energy-report.md`.

## Reference vessel — Nagi 凪 class

Coastal / short-sea, ~3,000 DWT, LOA ~90 m, ~10 kn, IMO MASS Degree 3 (remote, no crew).
Full design in [`data/vessel.edn`](data/vessel.edn).

## 9-cell shipyard line

`steel_block_fabrication` → `grand_block_assembly` → `weld_ndt_inspection` →
**`powertrain_integration`** → `outfitting` → `launch_commissioning` → `sea_trial`, with
`decarbonization_audit` cross-cutting and `class_certification_binder` terminal. R0 = scaffold;
all cells import-clean and raise `RuntimeError` on `.solve()` until Council ratifies R1
(ADR-2606013415, reserved).

## Operational simulation (kami-engine)

Beyond the analytic budget, the Nagi class **actually sails autonomously** in the
physics engine: `40-engine/kami-engine/kami-autodrive/examples/nagi_voyage.rs` runs the
`Autopilot` + `ShipHydro` GNC through a multi-waypoint coastal course while a reduced-order
**zero-emission powertrain** (wind-assist + solar + hydrogen FC + LFP) decides the available
propulsion power each step and books the energy split. There is **no fossil source** — when
the green budget can't meet the commanded thrust the throttle is *power-limited* (the ship
sails slower), never topped up with fuel.

```sh
cargo run -p kami-autodrive --example nagi_voyage      # captured: 20-actors/funadaiku/out/nagi-voyage-sim.txt
cargo test -p kami-autodrive --test nagi_zero_emission_voyage   # 2 tests green
```

Captured run: autonomous arrival through all waypoints, split **hydrogen 84.4% / solar 8.9% /
wind-assist 6.6% / fossil 0.0%** — the same shape as the analytic budget and the propulsion survey.

## Honest R0 boundary

Design + data-model + simulation ONLY. No steel cut, no hull, no FC stack. The voyage energy
budget is a reduced-order analytic model (not CFD/sea-keeping). `ShipHydro` is 3-DOF planar
(not 6-DOF marine CFD), and in the kami-engine demo it is a small-vessel surrogate (8 m/s, ~2 t)
at perception-grid scale — energy *shares* are scale-invariant but the kWh figures are demo-scale.
Nagi is coastal scale; ocean VLCC scale is out of R0–R3 (G12). Robotics
fleet is design-only. All numbers `:representative`. Live yard / bunkering / sea trial is
Council + operator gated (G11/G12).

## Files

- [`manifest.jsonld`](manifest.jsonld) — DID, cells, gates (G1–G14), non-goals (N1–N12), roadmap
- [`CLAUDE.md`](CLAUDE.md) — actor build notes
- [`data/vessel.edn`](data/vessel.edn) · [`data/shipyard.edn`](data/shipyard.edn) · [`data/building.edn`](data/building.edn) · [`data/fleet.kotoba.edn`](data/fleet.kotoba.edn)
- [`products.edn`](products.edn) — Ring-1 catalog (zero-emission freight + Nagi-class build)
- [`src/funadaiku/governor.cljc`](src/funadaiku/governor.cljc) — constitutional Governor: refuses a
  design proposal that violates G7/G8/G12/G13/G14 or N1/N2/N3/N5/N8/N10/N12, and reports the
  provisions a design record cannot decide rather than passing them silently
- [`methods/voyage_energy.py`](methods/voyage_energy.py) — wind/solar/hydrogen energy-budget sim
- ADR: [`/90-docs/adr/2606013400-funadaiku-zero-emission-cargo-shipbuilding-r0.md`](../../90-docs/adr/2606013400-funadaiku-zero-emission-cargo-shipbuilding-r0.md)

Apache 2.0 + etzhayyim Charter Compliance Rider v2.0.
