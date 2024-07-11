package com.blank038.easygift;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.blank038.easygift.command.EasyGiftCommand;
import com.blank038.easygift.handler.CacheHandler;
import com.blank038.easygift.listen.EasyGiftListener;
import com.blank038.easygift.utils.ScriptUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * @author Blank038
 */
public class EasyGift extends AyPlugin {
    @Getter
    private static EasyGift instance;

    @Override
    public void onEnable() {
        instance = this;
        this.loadConfig();
        this.getCommand("easygift").setExecutor(new EasyGiftCommand());
        Bukkit.getPluginManager().registerEvents(new EasyGiftListener(), this);
    }

    public void loadConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        // 初始化脚本引擎
        ScriptUtil.initScriptEngine();
        // 清理缓存数据
        CacheHandler.clearAllCache();
        // 读取数据
        CacheHandler.init();
    }

    public static String getString(String key, boolean... prefix) {
        String message = instance.getConfig().getString(key, "");
        if (prefix.length > 0 && prefix[0]) {
            message = instance.getConfig().getString("message.prefix") + message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
