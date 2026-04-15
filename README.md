# Compact Orbs
[![github](https://img.shields.io/badge/feedback%20&%20issues-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/its-cue/compact-orbs/issues)[![faq](https://img.shields.io/badge/FAQ-black?style=for-the-badge&logoColor=white)](#faq)[![installs](http://img.shields.io/endpoint?labelColor=000000&color=08d13e&style=for-the-badge&url=https://api.runelite.net/pluginhub/shields/installs/plugin/compact-orbs)](https://runelite.net/plugin-hub/cue)

<table>
  <tr>
    <td colspan="2" style="text-align: center;">
      <img src="https://imgur.com/fvzsaOP.gif" alt="preview"/>
    </td>
  </tr>

  <tr>
    <td style="text-align: center;"><b><a href="#layout">Layout</a></b></td>
    <td>Select & modify a compact layout: <code>Vertical</code>/<code>Horizontal</code></td>
  </tr>

  <tr>
    <td style="text-align: center;"><b>Hotkey</b></td>
    <td>Use/set a hotkey to show/hide the <code>Minimap</code> & <code>Compass</code> toggle buttons,<br/> or reconfigure it to show/hide the <code>Minimap</code> (<i>hotkey</i>: <code>ctrl + insert</code>)</td>
  </tr>

  <tr>
    <td style="text-align: center;"><b><a href="#swapping">Orb Swapping</a></b></td>
    <td>Switch an <code>Orb</code> with another  </td>
  </tr>

  <tr>
    <td style="text-align: center;"><b><a href="#visibility">Orb Visibility</a></b></td>
    <td>Hide any <code>Orb</code>, including the <code>Logout-X</code>, <code>Worldmap</code>, &amp; <code>Xp</code></td>
  </tr>

  <tr>
    <td style="text-align: center;"><b><a href="#overlay">Minimap Overlay</a></b></td>
    <td>Display a <code>Vanilla</code> minimap while in compact view</td>
  </tr>
</table>

<details id="layout">
<summary>⚙️ Layout</summary>

## Toggle location
- choose where the toggle button is displayed when the `Minimap` is visible
    - 1 `Default`: original position, below the wiki banner
    - 2 `Above Xp`: above the XP orb
    - 3 `Below Map`: centered below the minimap, to the right of the `Store` orb
    - 4 `Below X`: positioned in the top-right corner, just below the `Logout-X`

      ![](https://imgur.com/nDjUhTQ.png)

## Hide toggle buttons
- selectively hide the `Minimap` or `Compass` toggle buttons (eyes)

## Layout
- select the desired compact layout
    - `Vertical` (default): displays orbs stacked vertically similar to the mobile client
    - `Horizontal`: displays `Data` orbs stacked horizontally in 2 rows

      ![](https://imgur.com/E4MHR1c.png)

## Direction
- dictates which direction orbs are positioned in the container or when reordering due to any being hidden (direction is not limited to the layout type)
    - `Horizontal`
        - `Top`: set the anchor point to the top of the container (displayed as top-down) & will shift orbs upward based on orbs hidden above
        - `Bottom`: set the anchor point to the bottom of the container (displayed as bottom-up) & will shift orbs downward based on orbs hidden below
      
      ![](https://imgur.com/fHpIB0p.png)

    - `Vertical`
        - `Left`: set the reorder anchor point to the left, & will shift orbs left based on orbs hidden before it
        - `Right`: set the reorder anchor point to the right, & will shift orbs right based on orbs hidden before it

      ![](https://imgur.com/TfjXKY0.png)

## Reordering
- orbs by default will reorder based on `Layout` & `Direction`, however this can be toggled off
    - `leave empty space`: preserves the space between orbs, removes snapping for a floating effect

      ![](https://imgur.com/8dHMRsM.png)

<br/>


<table>
  <tr>
    <td colspan="2" style="text-align: center;">
      Example configuration keeping <code>reordering</code> & enabling <code>leave-empty-space</code>>
    </td>
  </tr>

  <tr>
    <td>

✅ `horizontal`

✅ `vertical-left`

❌ `disable-reordering`

✅ `leave-empty-space`

  </td>
    <td><img src="https://imgur.com/RS9hUZo.png" alt="preview"/></td>
  </tr>
</table>


## Clickthrough
- Prevents clicking through the `Hp`, `Prayer`, `Run`, &`Spec` orbs while in compact view
- removes the `Walk here` menu option
- slightly increases the non-clickable area around each orb (does **_not_** increase the size of the clickable button)

</details>

<details id="swapping">
<summary>🔄️ Orb Swapping</summary>

## Swapping
- each `slot` is populated with the corresponding `orb` in the config
- `slot`'s must have a unique orb, since its position never changes (only the `orb` that is displayed)
- supported across `fixed`, `resizable-classic`, or `resizable-modern` display modes
    - `Compact slots`: display order when in compact view
    - `Vanilla slots`: display order when the minimap is visible

![](https://imgur.com/6nENyKE.png)

</details>

<details id="visibility">
<summary>👁️ Orb Visibility</summary>

## Orb hiding
- any listed orb can be hidden (read [In-game settings](#in-game-settings))
- supported across `fixed`, `resizable-classic`, or `resizable-modern` display modes
    - `WorldMap`(`ctrl+M`) & the `Logout-X` will work when hidden (so long as the hotkey options are enabled in-game)
    - `Gridmaster` was for the temporary Gridmaster league (left in the event it returns)

      ![](https://imgur.com/RPYnWuj.gif)

## In-game settings
- to hide/show the `Store`/`Wiki`/`Activity` the below settings must be `enabled` ✔️

  ![](https://imgur.com/XwpxyQz.png)

_Note_: in the Wiki plugin, the config `Show wiki button under minimap` cannot be enabled at the same time as `Hide Wiki banner`, enabling either will disable the other if also enabled

![](https://imgur.com/Dm8HVb8.png)

</details>

<details id="overlay">
<summary>🗺️ Minimap Overlay</summary>

- a separate detached minimap, only visible while a compact layout is active (movable)
    - `Logout-X`: will open the logout tab or world switcher

      ![](https://imgur.com/xpjxwy6.png)

- `Restrictions`: there is no compatibility with plugins that modify, overlay, or add indicators to the original `Vanilla` minimap (since it's not the `Vanilla` minimap)
    - for ex;
        - `Quest Helper` - hint arrow & lines
        - `Shortest Path` - minimap tile path
        - `Player Indicators` - minimap names
        - `NPC Indicators` - minimap names
        - `Ground Markers` - minimap tiles
        - `Player-owned House` - teleport icons
        - `Runecraft` - abyss rift icons
        - `Barrows` - hill names
        - `Implings` - minimap names
        - etc.

> Disclaimer: this feature is experimental, and could be subject to change/removal depending on the host interface (StatBoostsHud) staying official client only
</details>

<details id="faq">
<summary>❓ FAQ / Conflicts</summary>

## FAQ

### Q: Any ways to prevent the orbs from moving around when the window size changes?
- A: here's a few things that helped me -
    - avoid setting the Runelite window to `maximize` in the top right (next to the close window X)
    - in the Runelite config side panel, try setting `Resize type` to `Keep game size` (so long as the window doesn't expand past your monitors size)
    - setting the dimensions for the `Game size`, as well as `Lock window size`

### Q: Can the orbs be hidden without using the compact layout?
- A: yep, swapping works as well

### Q: Can you add [orb] to the detached minimap?
- A: there are no plans to include any functional versions of the following orbs: [`Xp`, `Worldmap`, `Hp`, `Prayer`, `Run`, `Spec`, `Wiki`, `Store`, `Activity`]

## Conflicts
Since this modifies the minimap visibility, there are a few plugins that have been flagged as conflicts (enabling any will disable this plugin & vice versa)

- `Fixed Resizable Hybrid`
- `Orb Hider`
- `Minimap Hider`

</details>