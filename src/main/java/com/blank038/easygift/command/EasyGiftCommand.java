package com.blank038.easygift.command;

import com.blank038.easygift.EasyGift;
import com.blank038.easygift.cache.item.PropItemCache;
import com.blank038.easygift.cache.view.ViewCache;
import com.blank038.easygift.handler.CacheHandler;
import com.blank038.easygift.view.GiftView;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 */
public class EasyGiftCommand implements CommandExecutor {
    private final EasyGift plugin = EasyGift.getInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("easygift.admin")) {
            return false;
        }
        if (strings.length == 0) {
            this.plugin.getConfig().getStringList("message.help").forEach((message) -> {
                String colorisy = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(colorisy.replace("%c", s));
            });
            return false;
        }
        switch (strings[0].toLowerCase()) {
            case "give":
                this.give(commandSender, strings);
                break;
            case "save":
                this.save(commandSender, strings);
                break;
            case "forceopen":
                this.forceOpen(commandSender, strings);
                break;
            case "reload":
                this.reload(commandSender);
                break;
            default:
                break;
        }
        return false;
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(EasyGift.getString("message.player-offline", true));
            return;
        }
        if (!CacheHandler.PROP_ITEM_CACHE_MAP.containsKey(args[2])) {
            sender.sendMessage(EasyGift.getString("message.wrong-prop-id", true));
            return;
        }
        int amount = 1;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (Exception ignore) {
        }
        ItemStack itemStack = CacheHandler.PROP_ITEM_CACHE_MAP.get(args[2]).getPropItem();
        itemStack.setAmount(amount);
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("PropItemId", args[2]);
        target.getInventory().addItem(nbtItem.getItem());
        sender.sendMessage(EasyGift.getString("message.give", true).replace("%player%", target.getName()));
    }

    private void reload(CommandSender sender) {
        this.plugin.loadConfig();
        sender.sendMessage(EasyGift.getString("message.reload", true));
    }

    private void save(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(EasyGift.getString("message.wrong-item-id", true));
            return;
        }
        if (CacheHandler.DISPLAY_ITEM_MAP.containsKey(args[1])) {
            sender.sendMessage(EasyGift.getString("message.item-exists", true));
            return;
        }
        ItemStack itemStack = ((Player) sender).getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            sender.sendMessage(EasyGift.getString("message.null-item", true));
            return;
        }
        CacheHandler.DISPLAY_ITEM_MAP.put(args[1], itemStack.clone());
        CacheHandler.saveDisplayItems();
        sender.sendMessage(EasyGift.getString("message.save", true));
    }

    private void forceOpen(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return;
        }
        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(EasyGift.getString("message.player-offline", true));
            return;
        }
        if (!CacheHandler.VIEW_CACHE_MAP.containsKey(args[2])) {
            sender.sendMessage(EasyGift.getString("message.view-not-exists", true));
            return;
        }
        PropItemCache propItem = CacheHandler.PROP_ITEM_CACHE_MAP.get(args[2]);
        ViewCache viewCache = CacheHandler.VIEW_CACHE_MAP.get(propItem.getViewId());
        new GiftView(propItem, viewCache, propItem.getViewParams())
                .setConsume(false)
                .initialize()
                .open(player);
    }
}
