# CargoHologram

---

This mod is focused at early and middle game stages at
technical mod-packs or even end game stage at vanilla style
mod-packs. At these stages when players have staffs of items 
but lack a good way to manage them automatically. This mod
provides tools to improve your life. You will not open and close
all the containers just to take or store items anymore

## Introduction

---

### Build-in Blur Background support

All UI in this mod all have adjustable blur background, you can configure this
by build in commands or the `UI configure stick` which will be given to player
when you login in at first time.

If you installed `Modern UI` and you want to use its blur style, you can configure
this in config file

### Cargo Storage

Container which is added by this mod, it can set priority,
display item and `TraitFilterItem`.

What this container differs others is that, the more strict
(less type item can be stored) the more items it can store.
In another world, the less item types it can store, the more
count it can. Support hud display configuration

### Cargo Inserter

will Transform its contents into linked `Cargo Storage` by their
filters and priority by manually click the transform button or when
you close the ui. Items will be dropped if it can't be stored anymore.
support hud linked block display and in ui adjustable highlight

### Cargo Monitor

will scan player centered containers and display them. support search 
by mod self or sync with jei search. Items will also display which block
with its location and which slot it is in. You can also click to take it
into inventory remotely.

### Cargo Crafter

advanced version of `Cargo Monitor`, additional support craft remotely.
support jei quick specific recipe

### Item Trait

This is intended items into several human friendly items, currently
we have these types

| name           | meaning                                |
|----------------|----------------------------------------|
| Placeable      | can be placed as a block               |
| FluidContainer | can contain fluids                     |
| Enchanted      | have been enchanted or not             |
| MaxEnchanted   | every enchantment is max level         |
| Custom Name    | be named by name tag                   |
| Damaged        | not have full durability               |
| Damageable     | have durability                        |
| Equipable      | work as armor                          |
| FurnaceFuel    | can be used as furnace fuel            |
| ItemIdentity   | is same item without check nbt         |
| ItemGroup      | under the same creative tab            |
| ItemTag        | have specific item tag                 |
| ModId          | added by which mod                     |
| Color          | used for dye                           |
| EquipSlot      | item can be equipped on where          |
| ToolType       | generally, blocks will be mined faster |

### TraitFilterItem

used to store a list of Item Trait, support:
`PASS_ANY`, `PASS_ALL`, `PASS_NONE`

## Also

This mod may lack some balance, you can configure some stuffs in
config files or use other mod to adjust recipes. If they can't satisfy you,
welcome to open an issue or a pull-request. Just keep your manners please.