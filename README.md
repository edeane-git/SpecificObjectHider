# Specific Object Hider

RuneLite plugin that allows players to hide right-click menu options for specific game object instances at exact world coordinates.

Designed to eliminate misclicks on annoying or obstructive scenery while keeping other instances of the same object fully usable.

## Features

### Precise Object Hiding
- Right-click any object in game and select **"Hide all options"** to block interaction with that exact tile instance.
- Automatically captures the object ID and `WorldPoint` coordinates so other objects sharing the same ID elsewhere in the world remain clickable.
- Hides all primary actions, use-item targets, and examine options for target objects.

### Side Panel Management
- **Visual List:** View all currently hidden objects with their names, IDs, and world coordinates (`X, Y, Plane`).
- **Temporary Reveal:** Click the eye icon next to an object (or globally at the top) to temporarily show its menu options without deleting the rule.
- **Easy Deletion:** Delete individual object rules via a safe confirmation popup menu.
- **RuneScape Native UI:** Clean panel design matching RuneLite’s built-in interface aesthetic.

### Menu Safety & Customization
- **Configurable Side Panel:** Toggle the sidebar panel on or off in the plugin settings depending on your preference.
- **Menu Entry Toggle:** Optionally hide the right-click "Hide all options" entry if you want to lock down your current hidden objects list and avoid accidental additions.

## Usage

1. Enable the plugin in your RuneLite Plugin Hub settings.
2. Right-click an object you want to block (e.g., a specific rock, door, or decorative object).
3. Click **"Hide all options"**.
4. Open the **Specific Object Hider** side panel on the right sidebar to review, toggle, or remove any hidden objects.

## Development

Requirements: JDK 11+.

```bash
./gradlew run
```

## License

BSD-2-Clause (same as the RuneLite example plugin).