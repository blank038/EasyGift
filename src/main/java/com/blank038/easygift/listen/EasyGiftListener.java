package com.blank038.easygift.listen;

import com.blank038.easygift.EasyGift;
import com.blank038.easygift.cache.item.PropItemCache;
import com.blank038.easygift.cache.view.ViewCache;
import com.blank038.easygift.handler.CacheHandler;
import com.blank038.easygift.view.GiftView;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blank038
 */
public class EasyGiftListener implements Listener {
    private final Map<String, Long> cooldown = new HashMap<>();

    public EasyGiftListener() {
        Runnable runnable = () -> cooldown.entrySet().removeIf((entry) -> System.currentTimeMillis() > entry.getValue());
        Bukkit.getScheduler().runTaskTimer(EasyGift.getInstance(), runnable, 20L, 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (System.currentTimeMillis() <= this.cooldown.getOrDefault(player.getName(), 0L)) {
            return;
        }
        this.cooldown.put(player.getName(), System.currentTimeMillis() + 300L);
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("PropItemId")) {
            return;
        }
        String propId = nbtItem.getString("PropItemId");
        if (!CacheHandler.PROP_ITEM_CACHE_MAP.containsKey(propId)) {
            return;
        }
        event.setCancelled(true);
        PropItemCache propItem = CacheHandler.PROP_ITEM_CACHE_MAP.get(propId);
        ViewCache viewCache = CacheHandler.VIEW_CACHE_MAP.get(propItem.getViewId());
        new GiftView(propItem, viewCache, propItem.getViewParams())
                .setConsume(true)
                .initialize()
                .open(player);
    }
}
