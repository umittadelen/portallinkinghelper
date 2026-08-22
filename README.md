# Xaero's Portal Locater

A client-side Fabric mod that creates linked Nether/Overworld
waypoints in Xaero's Minimap.

---

## Usage

```text
/locateportal add <name> [x y z]
/locateportal remove <name>
```

If coordinates are omitted, your current position is used. The
command only works while standing in the Overworld or the Nether.

### Examples:

`/locateportal add basePortal` creates a waypoint named `P_basePortal` at your current position.

`/locateportal add basePortal 100 64 200` creates a waypoint named `P_basePortal` at `100 64 200`.

`/locateportal remove basePortal` removes the `P_basePortal` waypoints from both the Overworld and the Nether.

If a waypoint with that name already exists, a number is appended (e.g. `P_basePortal_2`) to avoid overwriting it.

---

## Requirements
- Minecraft 1.21.1
- Fabric
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)

---

## License

MIT License. See LICENSE.

---

## Disclaimer

This project is not affiliated with or endorsed by Xaero.
Xaero's Minimap is a separate dependency and remains the property
of its respective author.