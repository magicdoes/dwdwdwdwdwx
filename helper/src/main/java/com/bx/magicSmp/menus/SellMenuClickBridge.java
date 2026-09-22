package com.bx.magicSmp.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Defers protected SellMenu button actions until after the InventoryClickEvent
 * has completely finished. Opening another inventory from inside the original
 * click event can leave a client-side/real cursor copy of the GUI icon.
 */
public final class SellMenuClickBridge {
    private SellMenuClickBridge() {}

    public static void schedule(SellMenu menu, int slot, Player player, ClickType click) {
        if (menu == null || player == null) return;
        menu.plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) return;
            menu.handleClick(slot, player, click);
            player.updateInventory();
        });
    }
}
