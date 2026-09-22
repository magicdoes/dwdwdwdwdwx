# MagicSMP Sell Pages Fix

Keeps the multiplier progression page and Back button.

Fix: the original SellMenu opened progression/worth menus directly inside the InventoryClickEvent after cancelling the GUI-control click. This build defers the protected button action until after the click event finishes, matching the safer handling already used by MagicSMP's other protected menus. This prevents multiplier display icons from being transferred/ghosted to the cursor/inventory when navigating Progression -> Back -> another multiplier.

Baseline: the supplied MagicSMP(5).jar.
Output: build/MagicSMP.jar
