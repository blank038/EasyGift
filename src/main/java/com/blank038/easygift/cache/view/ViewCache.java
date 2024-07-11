package com.blank038.easygift.cache.view;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Blank038
 */
@Getter
public class ViewCache {
    private final String title, slots;
    private final int size;
    private final Map<String, ItemStack> displayItems = new HashMap<>();

    public ViewCache(FileConfiguration data) {
        this.title = data.getString("title");
        this.slots = data.getString("slots");
        this.size = data.getInt("size");
        // 解析物品
        if (data.contains("items")) {
            ConfigurationSection itemSection = data.getConfigurationSection("items");
            itemSection.getKeys(false).forEach((k) -> {
                ConfigurationSection is = itemSection.getConfigurationSection(k);
                ItemStack itemStack = new ItemStack(Material.valueOf(is.getString("type")));
                itemStack.setAmount(is.getInt("amount"));
                itemStack.setDurability((short) is.getInt("data"));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', is.getString("name")));
                List<String> lore = new ArrayList<>(is.getStringList("lore"));
                lore.replaceAll((s) -> ChatColor.translateAlternateColorCodes('&', s));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                if (is.contains("action")) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    nbtItem.setString("GiftViewAction", is.getString("action"));
                    itemStack = nbtItem.getItem();
                }
                this.displayItems.put(is.getString("slot"), itemStack);
            });
        }
    }
}
