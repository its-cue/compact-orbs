# Compact Orbs
[![github](https://img.shields.io/badge/feedback%20&%20issues-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/its-cue/compact-orbs/issues)[![faq](https://img.shields.io/badge/FAQ-black?style=for-the-badge&logoColor=white)](#faq--conflicts)[![installs](http://img.shields.io/endpoint?labelColor=000000&color=08d13e&style=for-the-badge&url=https://api.runelite.net/pluginhub/shields/installs/plugin/compact-orbs)](https://runelite.net/plugin-hub/cue)

![](https://imgur.com/iiNx0m3.gif)

<table>
  <tr>
    <td><b><a href="#layout">Layout</a></b></td>
    <td>Choose from a few presets or create a custom layout. <br/> See below for more options</td>
  </tr>

  <tr>
    <td><b><a href="#toggle-button">Toggle Button</a></b></td>
    <td> The eyeball button, has multiple functions: <br/> <code>toggle minimap</code>, <br/> <code>enable edit-mode</code>, <br/> <code>save edit-changes</code>, <br/> <code>reset edit-positions</code>, <br/> <code>toggle detached minimap</code> </td>
  </tr>

  <tr>
    <td><b>Hotkey</b></td>
    <td> A configurable keybind, by default:  <br/> <code>shift + insert</code></td>
  </tr>

  <tr>
    <td><b><a href="#orb-swapping">Orb Swapping</a></b></td>
    <td>Swap an <code>orb</code> with another, supported in: <br/> <code>fixed-mode</code>, <br/> <code>resizable-classic</code>, <br/> <code>resizable-modern</code></td>
  </tr>

  <tr>
    <td><b><a href="#orb-visibility">Orb Visibility</a></b></td>
    <td>Hide / show: <br/> <code>HP</code>, <code>PRAYER</code>, <code>RUN</code>, <code>SPEC</code>, <code>STORE</code>, <code>ACTIVITY</code>, <br/> <code>XP</code>, <code>WORLD-MAP</code>, <code>WIKI</code>, <code>LOGOUT-X</code> </td>
  </tr>

  <tr>
    <td><b><a href="#detached-minimap">Detached Minimap</a></b></td>
    <td>Enable a detached minimap while in <code>compact-view</code>, see limitations below</td>
  </tr>
</table>

<br/>

# Layout
### Types
- Select the compact layout that will be visible when the minimap is hidden: 
    - `vertical`, `horizontal`, `horizontal-wide`, `custom`

    ![](https://imgur.com/8iSY5y2.gif)

<br/>

### Edit Mode
- Can be enabled in either `classic-resizable` or `modern-resizable`
  - the following can be done without navigating to the config panel: `hide / show`, `swap-orbs`
  - additionally in `custom-layout`, it features: `drag & drop`, `reset-positions`

https://github.com/user-attachments/assets/fb19d099-164a-4c27-b268-86bb5162377e

> Caution: `edit-mode` can be cancelled without user input in some cases, resulting in some changes not being saved. (specifically `hide`/`show`)

<br/>

### Reordering
- Enables position rearrangement based on what is hidden
  - not supported in `fixed-mode`, or `custom-layout`

  ![](https://imgur.com/WYMY2hI.gif)

<br/>

### Prevent Clickthrough
- Will prevent clicks from passing through the certain orbs
  - supported in any `layout`, and is automatically enabled while in `compact-layout`, if `orb-swapping` is turned on

  ![](https://imgur.com/8lvJoGk.gif)

<br/>

### Anchor / Offset
- `anchor` location dictates where the preset-layouts are positioned within the container - also dictates direction when `reordering` is enabled
- `vertical-offset` is subtracted from the current position, moving the whole layout if its `anchor` is set to `bottom`

<br/>

---

# Toggle button
- The little eyeball button which indicates if the `minimap` is hidden (red) or visible (green)

### Right-Click
- Require a right-click to interact with the `toggle-button` (left=`disabled`, right=`enabled`)

  ![](https://imgur.com/rZyamSg.png)

### Toggle Locations

  ![](https://imgur.com/Cytzlbt.png)

<br/>

---

# Orb Swapping
- Swap an `orb` with another, only supports: `HP`, `PRAYER`, `RUN`, and `SPEC`
- Supports different ordering between compact-view and when the minimap is visible (`vanilla`)

  ![](https://imgur.com/fCI7RHU.png)

> Note: since `edit-mode` does not work in `fixed-mode` this option has been left accessible via the config panel

---

# Orb Visibility
- Hide or show any listed orb; the `logout-x`(Esc) and `world-map`(Ctrl-M) will retain hotkey function even if hidden

https://github.com/user-attachments/assets/27feee3c-b658-4ae9-a9bb-5d1af8dcd5d5

> Note: since `edit-mode` does not work in `fixed-mode` this option has been left accessible via the config panel

---

# Detached Minimap
- a separate detached minimap, only visible while a compact layout is active (movable)
    - `Logout-X`: will open the logout tab or world switcher
  
  ![](https://imgur.com/MpxTcDI.gif)
  
> There is no compatibility with plugins that modify, overlay, or add indicators to the original `vanilla` minimap
>>- Examples include `Quest Helper`, `Shortest Path`, `Ground Markers`, and `Player Indicators`.

---

# ❓FAQ / Conflicts

#### Q: Are there any ways to prevent the orbs from moving when changing the window size?
 - A: you can use either an anchor point (now movable) or you can set an `origin` for the minimap - dragging the minimap with `alt`, then `shift-click` when positioned where you want it will let you select where you want the minimap to be anchored to, relative to the `origin`.

#### Q: Can you add [orb] to the detached minimap?
 - A: no, since the implementation of that feature would go against the plugin-hub guidelines

## Conflicts:
The following plugins have been flagged to not work, or conflict with this plugin;

- `Fixed Resizable Hybrid`
- `Orb Hider`
- `Minimap Hider`
- `Movable Orbs`
