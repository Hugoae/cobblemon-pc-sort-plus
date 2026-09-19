# Cobblemon PC Sort+

Tag Cobblemon PC boxes, then sort. A box tagged Water and High IV only takes Water Pokémon that also meet that box's IV cut. If two boxes both match, the more specific one wins, then the stricter IV threshold, then the lowest box number.

NeoForge and Fabric port of XAI's Fabric Cobble PC Sort. The original stays on `cobblepcsort`. This project is `cobblemonpcsortplus` (CurseForge / Modrinth slug: `cobblemon-pc-sort-plus`).

Minecraft **1.21.1**, Cobblemon **1.8.0+**. Use the JAR for your loader. Client and server both need the mod.

- **NeoForge 21.1.250** — Kotlin for Forge 5.3+
- **Fabric** — Fabric API + fabric-language-kotlin

## Sorting

Routing tags on a box are combined with AND. Dual-type Fire/Water matches a Fire+Water box; a Fire-only Pokémon does not.

Full boxes overflow to the next match, then to Fallback. Source and Protect never receive Pokémon.

- **Source** — Sort (R) only moves Pokémon that start in these boxes.
- **Protect** — ignored on read and write.
- **Fallback** — used when no matching destination has a free slot. Fallback+Water is still a Water box, not a dump.

**Sort all** walks the whole PC (skipping Protect), then orders each occupied box by Name, Pokédex, Level, or None.

## Tags

Two pages, dots to switch, no arrow keys (Cobblemon already uses those for boxes).

Page 1: Source, Protect, Fallback, High IV, Shiny, Alpha, Legendary, Mythical, 18 types.

Page 2: Male, Female, Hidden ability, Held item, Fossil, Pseudo-legendary, Ultra Beast, Paradox, Baby, Regional, Level 1, Level 100, generations 1–9, sizes XS–XL.

The Tags button lights up when the open box has tags. Clear removes every tag on that box. High IV is per box (`IV ≥ n/6`).

## Controls

Toolbar under the PC: Tags, Sort, Sort all, Undo.

Shortcuts apply on the PC screen only, and are ignored while a box name or the filter is focused:

| Key | Action   |
| --- | -------- |
| R   | Sort     |
| T   | Sort all |
| Z   | Undo     |

Results go to the action bar. The mod does not write to chat.

## Undo

One level: Pokémon go back to the slots they had before the last sort. The snapshot is stored in the world save, so it survives logout and a restart of that world. Sorting again replaces it; Undo consumes it.

## Files

- Profiles: `config/cobblemonpcsortplus/profiles/<uuid>.json`
- Undo: `<world>/cobblemonpcsortplus/undo/<uuid>.json`

## Build

```bash
./gradlew build
```

JARs:

- `neoforge/build/libs/cobblemonpcsortplus-neoforge-1.21.1-1.0.1.jar`
- `fabric/build/libs/cobblemonpcsortplus-fabric-1.21.1-1.0.1.jar`

Regenerating the tag panel texture: `python tools/generate_tag_panel.py` (requires Pillow).

## License

MIT. Original Fabric mod © XAI. This fork © xhugo. GUI sprites adapted from [Cobblemon Interface](https://modrinth.com/resourcepack/cobblemon-interface) by VinnyStalck (MIT).
