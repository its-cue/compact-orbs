# Compact Orbs
[![github](https://img.shields.io/badge/feedback%20&%20issues-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/its-cue/compact-orbs/issues)[![faq](https://img.shields.io/badge/FAQ-black?style=for-the-badge&logoColor=white)](#faq--conflicts)[![installs](http://img.shields.io/endpoint?labelColor=000000&color=08d13e&style=for-the-badge&url=https://api.runelite.net/pluginhub/shields/installs/plugin/compact-orbs)](https://runelite.net/plugin-hub/cue)

![](https://imgur.com/iiNx0m3.gif)

|                                           |                                                                                       |
|-------------------------------------------|---------------------------------------------------------------------------------------|
| **[Layout](#layout)**                     | Choose from a few presets or create a custom layout.<br/>See below for more           |
| **[Toggle Button](#toggle-button)**       | Switches between `vanilla` or `compact` view.<br/>Also used to enter/exit `edit-mode` |
| **Hotkey**                                | A configurable keybind, by default: `shift + insert`                                  |
| **[Detached Minimap](#detached-minimap)** | Enable a detached minimap while in `compact-view`.<br/>See below for more             |

<br/>

# Layout
### Types
- Choose a compact layout that will be visible when the minimap is hidden: 
    - `vertical`, `horizontal`, `horizontal-wide`, `custom`

    ![](https://imgur.com/8iSY5y2.gif)

<br/>

### Edit Mode
- Similar to the Prayer/Spellbook plugins `reordering` feature, provides a quick way to `hide / show`, `swap` or `drag & drop`.
  - can be enabled by the `toggle-button` or the `hotkey` if configured
  - the `drag & drop` feature is not supported in any preset-layouts: `vertical`, `horizontal`, `horizontal-wide`

https://github.com/user-attachments/assets/fb19d099-164a-4c27-b268-86bb5162377e

#### Orb Swapping
- An alternative to moving orbs around without the `drag & drop` feature.
  - Swap an `orb` with another, only supports: `HP`, `PRAYER`, `RUN`, and `SPEC`
  - Supports a different order between compact-view and when the minimap is visible (`vanilla`)
  - if enabled, custom positioning will be disabled
  - cannot be enabled in `custom-layout`

  ![](https://imgur.com/fCI7RHU.png)

#### Orb Visibility
- Hide or show any available `orb`; hotkey function is retained when the `logout-x`(Esc) and `world-map`(Ctrl-M) are hidden

https://github.com/user-attachments/assets/27feee3c-b658-4ae9-a9bb-5d1af8dcd5d5

<br/>

### Reordering
- Automatically adjusts orb positions based on which orbs are hidden.
  - only applies for `compact-layout`, excluding `custom`

  ![](https://imgur.com/WYMY2hI.gif)

<br/>

### Prevent Clickthrough
- Prevent clicks from passing through the certain orbs.
  - supported in any `layout`
  - automatically enabled in `compact-layout` when `orb-swapping` is enabled

  ![](https://imgur.com/8lvJoGk.gif)

<br/>

### Hide with Inventory tab
- Hide the `compact-layout` when a side panel tab is hidden.
  - does not affect the `detached-minimap`
  - only works in `modern-resizable` display mode
  
  ![](https://imgur.com/Ezhkz0O.gif)

<br/>

### Anchor / Offset
- `anchor` determines where preset-layouts are positioned within the container and the direction used by `reordering`.

- `vertical-offset` adjusts the layout by the specified value if its `anchor` is set to `bottom`.

<br/>

---

# Toggle button
- Switch between a visible minimap and the selected `compact-layout`.
  - right-click to enable/leave `edit-mode`, or show/hide the `detached` minimap while in `compact-layout`

### Right-Click
- Require a right-click to interact with the `toggle-button` (left=`disabled`, right=`enabled`).

  ![](https://imgur.com/rZyamSg.png)

### Toggle Locations
- Choose where the `toggle-button` will be if `orb-swapping` is enabled.

  ![](https://imgur.com/Cytzlbt.png)

<br/>

---

# Detached Minimap
- a detached minimap, only visible while a `compact-layout` is active (can be repositioned separately from the orbs).
    - the `logout-x` will open the logout tab or world switcher
  
  ![](https://imgur.com/MpxTcDI.gif)
  
> There is no compatibility with plugins that modify, overlay, or add indicators to the original `vanilla` minimap
>>- Examples include `Quest Helper`, `Shortest Path`, `Ground Markers`, and `Player Indicators`.

---

# ❓FAQ / Conflicts

#### Q: Are there any ways to prevent the orbs from moving when changing the window size?
 - A: you can use either an anchor point (now movable) or you can set an `origin` for the minimap - dragging the minimap with `alt`, then `shift-click` when positioned where you want it will let you select where you want the minimap to be anchored to, relative to the `origin`.

#### Q: Can you add [orb] to the detached minimap?
 - A: [#36](https://github.com/its-cue/compact-orbs/issues/36)

## Conflicts:
The following plugins have been flagged to not work, or conflicts, with this plugin;

- `Fixed Resizable Hybrid`
- `Orb Hider`
- `Minimap Hider`
- `Movable Orbs`
