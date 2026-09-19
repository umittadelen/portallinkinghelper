# Portal Linking Helper

A client-side Minecraft mod that helps you find the matching Nether and Overworld coordinates for a portal.

The mod can also create and remove waypoints through supported waypoint mods.

## Features

- Convert Overworld coordinates to Nether coordinates
- Convert Nether coordinates to Overworld coordinates
- Use your current position or enter coordinates manually
- Create and remove portal waypoints when a supported waypoint mod is installed
- Works without a waypoint mod

## Commands

With `Xaero's Minimap` mod:

```text
/locateportal add <name> [x y z]
/locateportal remove <name>
```

Without a supported mod:

```text
/locateportal
/locateportal [x y z]
```

If coordinates are not given, your current position is used.

The command only works in the Overworld and Nether.

## Examples

* `/locateportal add basePortal`:
Creates a portal link using your current position.
* `/locateportal add basePortal 100 64 200`:
Creates a portal link at the coordinate `X100 Y64 Z200`.
* `/locateportal remove basePortal`:
Removes the portal link named `basePortal`.

## Requirements
* [Fabric API](https://modrinth.com/mod/fabric-api)

## License

MIT License. See [LICENSE](https://github.com/umittadelen/portallinkinghelper/blob/master/LICENSE).

# Supported Waypoint Mods

## Xaero's Minimap

Xaero's Minimap is currently supported as an optional integration.
You can use the coordinate commands without any other supported mod installed. If it is installed, their corresponding commands will be automatically added to runtime.

> [!NOTE]
> More waypoint integrations or features may/will be added in the future.