package com.blank038.easygift.cache.item;

import com.blank038.easygift.cache.view.ViewParams;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
public class PropItemCache {
    private final String propId, viewId;
    private final ViewParams viewParams;
    private final int rewardCount;
    private final List<String> conditions = new ArrayList<>();
    private final Map<String, PropButton> propButtonMap = new HashMap<>();
    private final ItemStack propItem;

    public PropItemCache(String propId, ConfigurationSection section) {
        this.propId = propId;
        this.viewId = section.getString("view-id");
        this.viewParams = new ViewParams(section.getConfigurationSection("view-params"));
        this.rewardCount = section.getInt("reward-count");
        // 读取物品
        ConfigurationSection propSec = section.getConfigurationSection("item");
        this.propItem = new ItemStack(Material.valueOf(propSec.getString("type")));
        this.propItem.setAmount(propSec.getInt("amount"));
        this.propItem.setDurability((short) propSec.getInt("data"));
        ItemMeta itemMeta = this.propItem.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', propSec.getString("name")));
        List<String> lore = new ArrayList<>(propSec.getStringList("lore"));
        lore.replaceAll((s) -> ChatColor.translateAlternateColorCodes('&', s));
        itemMeta.setLore(lore);
        this.propItem.setItemMeta(itemMeta);
        // 读取条件
        this.conditions.addAll(section.getStringList("conditions"));
        // 读取按钮
        if (section.contains("buttons")) {
            ConfigurationSection buttonSec = section.getConfigurationSection("buttons");
            buttonSec.getKeys(false).forEach((k) -> {
                this.propButtonMap.put(k, new PropButton(k, buttonSec.getConfigurationSection(k)));
            });
        }
    }

    public ItemStack getPropItem() {
        return this.propItem.clone();
    }
}
