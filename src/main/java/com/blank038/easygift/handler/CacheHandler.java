package com.blank038.easygift.handler;

import com.blank038.easygift.EasyGift;
import com.blank038.easygift.cache.item.PropItemCache;
import com.blank038.easygift.cache.view.ViewCache;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class CacheHandler {
    public static final Map<String, PropItemCache> PROP_ITEM_CACHE_MAP = new HashMap<>();
    public static final Map<String, ViewCache> VIEW_CACHE_MAP = new HashMap<>();
    public static final Map<String, ItemStack> DISPLAY_ITEM_MAP = new HashMap<>();

    public static void clearAllCache() {
        PROP_ITEM_CACHE_MAP.clear();
        VIEW_CACHE_MAP.clear();
        if (!DISPLAY_ITEM_MAP.isEmpty()) {
            saveDisplayItems();
        }
        DISPLAY_ITEM_MAP.clear();
    }

    public static void init() {
        File itemFolder = new File(EasyGift.getInstance().getDataFolder(), "items"),
                viewFolder = new File(EasyGift.getInstance().getDataFolder(), "views");
        if (!itemFolder.exists()) {
            EasyGift.getInstance().saveResource("items/example.yml", "items/example.yml");
        }
        if (!viewFolder.exists()) {
            EasyGift.getInstance().saveResource("views/exampleView.yml", "views/exampleView.yml");
        }
        for (File file : itemFolder.listFiles()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            data.getKeys(false).forEach((k) -> PROP_ITEM_CACHE_MAP.put(k, new PropItemCache(k, data.getConfigurationSection(k))));
        }
        for (File file : viewFolder.listFiles()) {
            String key = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            VIEW_CACHE_MAP.put(key, new ViewCache(data));
        }
        EasyGift.getInstance().saveResource("display.yml", "display.yml", false, (file) -> {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            data.getKeys(false).forEach((key) -> DISPLAY_ITEM_MAP.put(key, data.getItemStack(key)));
        });
    }

    public static void saveDisplayItems() {
        File file = new File(EasyGift.getInstance().getDataFolder(), "display.yml");
        FileConfiguration data = new YamlConfiguration();
        DISPLAY_ITEM_MAP.forEach(data::set);
        try {
            data.save(file);
        } catch (IOException e) {
            EasyGift.getInstance().getLogger().log(Level.WARNING, e, () -> "存储展示物品出现异常.");
        }
    }
}
