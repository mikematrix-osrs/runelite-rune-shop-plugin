# Rune Shop Calculator

A RuneLite side-panel plugin that calculates the total GP cost of buying runes from OSRS shops, accounting for **price inflation per unit** and **world hopping**.

## Features

- Select any combination of the 12 buyable runes
- Enter a target quantity using shorthand (`10k`, `1.5m`, or plain numbers)
- Configurable inflation rate — each unit purchased increases the shop price by a small percentage, resetting when you hop worlds
- **Pack buying** support for Air, Water, Earth, Fire, Mind, and Chaos runes — toggle the pack icon on a rune row to switch between buying individual runes and packs of 100
- Output table shows total cost, worlds needed, and average GP per rune
- Rune icons are click-to-toggle; clicking a rune icon or clearing its quantity field deselects it

## How It Works

OSRS rune shops use dynamic pricing — the more you buy in one session, the more each subsequent rune costs. When you hop to a new world the price resets. This plugin simulates that mechanic using a configurable inflation percentage (default **0.1%** per unit) to give you an accurate total cost estimate before you start hopping.

## Installation

Available on the [RuneLite Plugin Hub](https://runelite.net/plugin-hub). Search for **Rune Shop Calculator**.
