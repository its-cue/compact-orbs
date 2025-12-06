# Compact Orbs

**tl;dr:** Choose a layout, and tweak it to your preference

---

![preview](https://imgur.com/E4MHR1c.png)

# Position the minimap Orbs into a Compact Layout

- choose from a `vertical` or `horizontal` layout preset
- configure the layout position with `top`/`bottom` and `left`/`right`
- swap the `hp`, `prayer`, `run`, or `spec` orbs
- hide or show the `minimap` & `compass` via quick-toggle buttons or configurable `hotkey`
- hide or show any `orb`, `wiki`, `logout-X`, or `world-map`
- layouts dynamically reposition UI elements/orbs based on the chosen `direction` and `layout` type

> **Note:** Orb hiding & swapping are supported in all display modes, while layouts are only supported in
`resizable-classic` & `resizable-modern`

---

<details>
<summary>Features</summary>

---

## Customizable Positioning

- `vertical` & `horizontal` layouts allow different positioning configurations
- `direction` options
    - `left` / `right` – positions the layout to the left or right
    - `top` / `bottom` – positions the layout at the top or bottom
- in `vertical` layout, `top` & `bottom` also control top-down/bottom-up ordering of UI elements (`disable reordering`
  &cross; )
- in `horizontal` layout, `left` & `right` control left-to-right, or right-to-left ordering (`disable reordering`
  &cross; )
- `disable reordering` removes the dynamic offset for hidden orbs when enabled
- `leave-empty-space` prevents anchoring non-row/column UI elements to the orbs, allowing "floating" gaps

### Top vs Bottom

![topVbottom](https://imgur.com/fHpIB0p.png)

### Left vs Right

![leftVright](https://imgur.com/TfjXKY0.png)

---

### What Does "Leave Empty Space" Do?

Prevents non-row/column UI elements from adjusting position, creating gaps depending on hidden orbs

![empty_space](https://imgur.com/8dHMRsM.png)

---

For example, a configured layout could leverage `reordering` while enabling `leave-empty-space`

- `horizontal` &check;, `vertical-left` &check;
- `disable reordering` &cross;, `leave-empty-space` &check;

![empty_example](https://imgur.com/RS9hUZo.png)

---

### Additional Layout Examples

![layout_example](https://imgur.com/MKy82nt.png)

> Tip: Hold `Alt` to move the container, `Shift+Alt + Click` resets the container its original position

---

## Interactive Toggle Buttons

- `minimap` & `compass` buttons hide/show their corresponding UI elements (compass only works when minimap is hidden)
- buttons can be hidden independently via config or hotkey
- default hotkey: `shift+insert`, can be reconfigured
- option to override hotkey to show the minimap instead of the buttons (`toggle minimap via hotkey`)

![toggle_button](https://imgur.com/nDjUhTQ.png)

`toggle-position` options (only when the minimap is visible)

1. `default` – original location at the bottom of the minimap container
2. `below-map` – behind the `store` orb, below minimap (adjusts to `run` if `store` is hidden)
3. `above-xp` – above the `xp-drops` orb near the compass
4. `below-x` – below where `logout-X` would appear

---

## Orbs

- hide any `orb`, `wiki`, `logout-X`, or `world-map` (if visible via in-game settings)
- hotkeys still work when orbs are hidden (`esc` for `logout-X`, `ctrl+m` for `world-map`)
- swap the `hp`, `prayer`, `run`, & `spec` orbs, hidden targets still receive swaps

![orb_swap](https://imgur.com/6nENyKE.png)

</details>

---

<details>
<summary>Configuration</summary>

---

Side panel configurations should handle all customization needs. However, if an orb does not hide correctly - make sure
the corresponding in-game setting is enabled

> **Note:** if `disable reordering`&cross; and `leave-empty-space`&cross;, you may need to reflect in-game settings to
> the configs, since those features rely on config for proper alignment
>
>> in-game setting : `show store button` &cross;
>> - apply `hide store orb` &check; in the config panel

---

### Store

![show_store](https://imgur.com/8kLOyFC.png)

---

### Activity

![show_activity](https://imgur.com/FeMTGEL.png)

---

### Wiki

![show_wiki](https://imgur.com/ViegXYk.png)

> Note: The wiki plugin replaces the vanilla banner, both are supported! (may need to refresh by opening/closing a tab)

</details>

---

<details>
<summary>Gallery</summary>

---

## In-Game Preview

![gallery](https://imgur.com/dCSMmWZ.png)

## Default Layout

![gallery](https://imgur.com/78NSzbN.png)

</details>

---