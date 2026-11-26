# Compact Orbs
[![github](https://img.shields.io/badge/feedback%20&%20issues-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/its-cue/compact-orbs/issues)[![faq](https://img.shields.io/badge/FAQ-black?style=for-the-badge&logoColor=white)](#faq--conflicts)[![installs](http://img.shields.io/endpoint?labelColor=000000&color=08d13e&style=for-the-badge&url=https://api.runelite.net/pluginhub/shields/installs/plugin/compact-orbs)](https://runelite.net/plugin-hub/cue)

![](https://imgur.com/CSLdCAq.gif)

---

# What's New

### v2.0.6: Spec QoL
- Display an orb to track stacks and special attack energy.
    - supported spec types: `Soulreaper Axe`, `Rondache`, and `Spec Energy`
    - customize the tracker orbs color based on supported spec types (can also be applied to the native spec orb)
    - selectable orb size: `Original` or `Small`
        - small options include; `text-only`, `icon-only`, or `both`
    - customize the displayed texts; color, shadow color, and shadow-type (original or outlined)
    - compatible with resource packs
    - drag to reposition
    - **other**:
        - option to hide the native spec bar meter
        - **recommended**: disable the `Regeneration Meter` plugins spec meter when using: `Show spec regen meter`

  ![](https://imgur.com/S1gJAyt.gif)

> inspired by the plugin: [Soulreaper Axe QoL](https://runelite.net/plugin-hub/show/soulreaper-axe-qol) by `seacelery`

---
# Layout

- ### Compact
  - Choose a compact-layout that will be visible when the minimap is hidden:
    - `vertical`: a preset that arranges the orbs vertically
    - `horizontal`: a preset that arranges the orbs horizontally (stacked)
    - `horizontal-wide`: similar to horizontal, but without stacking the data orbs
    - `custom`: user-defined positioning via edit-mode (defaults to `vertical` if none are set)

    ![](https://imgur.com/8iSY5y2.gif)
  
<br/>

- ### Floating
  - An alternative to compact-layouts that utilizes RuneLite's native dragging (`Alt`) to reposition the minimap widgets without restriction.
    - hold `alt` and hover over any minimap widget, then left-click and hold to drag and reposition
    - default arrangement will be kept for any widget that is inside its default `snapcorner` while the minimap is visible
      - any saved positions or orb swaps (via `edit-mode`) will be applied in place of default positioning if available
    - only orbs that are not in the `snapcorner` will be hidden while `Hide orbs with inventory tab` is enabled
    - similar to `compact-layouts`, when the `world-map` or `logout-x` are hidden their hotkey functions are retained
    - `layout` features that are supported:
        - `edit-mode`
        - `prevent-clickthrough`
        - `hide-with-inventory`

<br/>

## Options

### Edit Mode
- Similar to the Prayer/Spellbook plugins `reordering` feature, provides a quick way to `hide / show`, `swap` or `drag & drop`.
    - can be enabled by the `toggle-button` or the `hotkey` if configured
    - the `drag & drop` feature is not supported in any preset-layouts: `vertical`, `horizontal`, `horizontal-wide`

<br/>

### Orb Swapping
- An alternative to moving orbs around without the `drag & drop` feature.
    - Swap an `orb` with another, only supports: `HP`, `PRAYER`, `RUN`, and `SPEC`
    - Supports a different order between compact-view and when the minimap is visible (`vanilla`)
    - if enabled, custom positioning will be disabled
    - cannot be enabled in `custom-layout`

  ![](https://imgur.com/fCI7RHU.png)

<br/>

### Reordering
- Automatically adjusts orb positions based on which orbs are hidden.
    - only applies for `compact-layout`, excluding `custom`

  ![](https://imgur.com/WYMY2hI.gif)

<br/>

### Prevent Clickthrough
- Prevent clicks from passing through certain orbs.
    - supported in any `layout`
    - automatically enabled in `compact-layout` when `orb-swapping` is enabled

  ![](https://imgur.com/8lvJoGk.gif)

<br/>

### Hide with Inventory Tab
- Hide the `compact-layout` when a side panel tab is hidden.
    - does not affect the `detached-minimap`
    - only works in `modern-resizable` display mode

  ![](https://imgur.com/Ezhkz0O.gif)

<br/>

### Show layout when Minimap is Hidden
- While the minimap is visible, arrange the orbs with vanilla positions rather than collapsing them into the snapcorner.
  - any swaps or custom positioning (via `edit-mode`) will be applied if available

  ![](https://imgur.com/oWCSDJn.gif)

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
- Choose where the `toggle-button` will be.

  ![](https://imgur.com/Cytzlbt.png)

<br/>

---

# Detached Minimap
- a detached minimap, only visible while a `compact-layout` is active (can be repositioned separately from the orbs).
    - the `logout-x` will open the logout tab or world switcher

  ![](https://imgur.com/MpxTcDI.gif)

> There is no compatibility with plugins that modify, overlay, or add indicators to the original `vanilla` minimap
> 
> Examples include `Quest Helper`, `Shortest Path`, `Ground Markers`, and `Player Indicators`.

<br/>

### Detach 'Utility' Orbs
- Reposition the utility orbs around the detached minimap, separating them from the current layout
  - utility orbs: [`world-map`, `wiki`, `toggle-button`, `store`, `activity`, `xp`]

  ![](https://imgur.com/yV0XRkB.png)

---

# ❓FAQ / Conflicts

#### Q: Is there a way to prevent the orbs from moving when adjusting window sizes?
- A: You can either use a `snapcorner` and reposition it as needed, or use an `overlay origin`.
  - `overlay origin`: hold `shift` and right-click any overlay to select its relative-to-position

    ![](https://imgur.com/zr5YrZA.gif)

#### Q: I don't see the toggle button, how do I hide the minimap?
- A: Hiding the minimap requires the toggle button or the hotkey to be configured, some window sizes may cause the inventory to obscure the toggle button when set to its `default` position.
  - set the toggle button location to anything but `default` on smaller window sizes [see here](https://github.com/its-cue/compact-orbs#toggle-locations)
  - check that the toggle button config to hide it is not checked

#### Q: Why are some of the orb positions changing when pressing `Alt`?
- A(1): If you're using a compact layout alongside the detached minimap, check if `detach utility orbs` is enabled.

- A(2): Another potential issue, is not releasing the mouse button before `Alt`, as this can cause the position to not save under certain circumstances (rare).
  - the above is actually a core feature, and when releasing `Alt` before the mouse button it allows positioning inside a `snapcorner` without placing it inside of it
  - to alleviate this while repositioning inside the `snapcorner` while avoiding placement, remove the minimap and position it where it would be if it was inside the `snapcorner` - then position the orbs as desired, and re-add the minimap back into the `snapcorner`
  - alternatively, use `edit-mode` while `Show layout when map is hidden` is enabled (and the minimap is visible), to reposition the orbs to the saved positions (removing the need for the above workaround)

<br/>

## Conflicts
- The following plugins are known to be incompatible.
  - `Fixed Resizable Hybrid`
  - `Orb Hider`
  - `Minimap Hider`
  - `Movable Orbs`
  - `Interface Layout`
  - `Vicious Bars`

> **Note**: The list is not exhaustive as there may be other plugins (including newly released ones) which may cause unintended behavior or conflicts even if they are not listed above.
>
> If you are experiencing issues, noticing conflicts with other plugins, or need general help, you can submit an issue [here](https://github.com/its-cue/compact-orbs/issues/new).


