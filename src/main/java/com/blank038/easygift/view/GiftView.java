package com.blank038.easygift.view;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.easygift.EasyGift;
import com.blank038.easygift.cache.item.PropButton;
import com.blank038.easygift.cache.item.PropItemCache;
import com.blank038.easygift.cache.view.ViewCache;
import com.blank038.easygift.cache.view.ViewParams;
import com.blank038.easygift.handler.CacheHandler;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Blank038
 */
public class GiftView {
    private final PropItemCache itemCache;
    private final ViewCache viewCache;
    private final ViewParams params;
    private final List<String> selector = new ArrayList<>();
    private boolean consume;
    private GuiModel model;

    public GiftView(PropItemCache itemCache, ViewCache viewCache, ViewParams params) {
        this.itemCache = itemCache;
        this.viewCache = viewCache;
        this.params = params;
    }

    public GiftView initialize() {
        String title = this.viewCache.getTitle().replace("%title%", params.getTitle());
        this.model = new GuiModel(title, this.viewCache.getSize());
        this.model.registerListener(EasyGift.getInstance());
        this.initializeDisplayItems();
        this.initializePropItems();
        this.model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getInventory()) {
                return;
            }
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return;
            }
            Player clicker = (Player) e.getWhoClicked();
            NBTItem nbtItem = new NBTItem(itemStack);
            if (nbtItem.hasTag("GiftViewAction")) {
                String action = nbtItem.getString("GiftViewAction");
                switch (action) {
                    case "confirm":
                        this.confirmRewards(clicker);
                        break;
                    case "cancel":
                        clicker.closeInventory();
                        break;
                    default:
                        break;
                }
            } else if (nbtItem.hasTag("GiftButtonId")) {
                String buttonId = nbtItem.getString("GiftButtonId");
                if (this.selector.contains(buttonId)) {
                    this.selector.remove(buttonId);
                    itemStack.removeEnchantment(Enchantment.DAMAGE_ALL);
                } else if (this.selector.size() >= this.itemCache.getRewardCount()) {
                    clicker.sendMessage(EasyGift.getString("message.too-much", true));
                } else {
                    this.selector.add(buttonId);
                    itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                }
            }
        });
        return this;
    }

    public GiftView setConsume(boolean consume) {
        this.consume = consume;
        return this;
    }

    private void initializePropItems() {
        Integer[] slots = CommonUtil.formatSlots(Optional.ofNullable(this.params.getSlots()).orElse(this.viewCache.getSlots()));
        PropButton[] buttons = this.itemCache.getPropButtonMap().values().toArray(new PropButton[0]);
        for (int i = 0; i < buttons.length && i < slots.length; i++) {
            PropButton button = buttons[i];
            if (!CacheHandler.DISPLAY_ITEM_MAP.containsKey(button.getDisplayItem())) {
                continue;
            }
            ItemStack clone = CacheHandler.DISPLAY_ITEM_MAP.get(button.getDisplayItem()).clone();
            ItemMeta itemMeta = clone.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            clone.setItemMeta(itemMeta);
            // 设置 NBT 标签
            NBTItem nbtItem = new NBTItem(clone);
            nbtItem.setString("GiftButtonId", button.getButtonId());
            this.model.setItem(slots[i], nbtItem.getItem());
        }
    }

    private void initializeDisplayItems() {
        this.viewCache.getDisplayItems().forEach((k, v) -> {
            for (int i : CommonUtil.formatSlots(k)) {
                this.model.setItem(i, v);
            }
        });
    }

    private void confirmRewards(Player clicker) {
        if (this.selector.size() != this.itemCache.getRewardCount()) {
            clicker.sendMessage(EasyGift.getString("message.insufficient", true));
            return;
        }
        Runnable runnable = () -> {
            clicker.closeInventory();
            List<String> opCommands = new ArrayList<>();
            this.selector.forEach((buttonId) -> {
                PropButton button = this.itemCache.getPropButtonMap().get(buttonId);
                if (button.isGotten() && button.getDisplayItem() != null
                        && CacheHandler.DISPLAY_ITEM_MAP.containsKey(button.getDisplayItem())) {
                    ItemStack displayItem = CacheHandler.DISPLAY_ITEM_MAP.get(button.getDisplayItem());
                    clicker.getInventory().addItem(displayItem);
                }
                button.getCommands().forEach((command) -> {
                    if (command.startsWith("console:")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(8));
                    } else {
                        opCommands.add(command);
                    }
                });
            });
            boolean isOp = clicker.isOp();
            try {
                clicker.setOp(true);
                opCommands.forEach((command) -> Bukkit.dispatchCommand(clicker, command.replace("%player%", clicker.getName())));
            } finally {
                clicker.setOp(isOp);
            }
        };
        if (!consume) {
            runnable.run();
            return;
        }
        ItemStack itemStack = clicker.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            clicker.sendMessage(EasyGift.getString("message.pls-held-item", true));
            return;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasTag("PropItemId")) {
            String propId = nbtItem.getString("PropItemId");
            if (!propId.equals(this.itemCache.getPropId())) {
                clicker.sendMessage(EasyGift.getString("message.pls-held-item", true));
                return;
            }
            int amount = itemStack.getAmount();
            if (amount == 1) {
                clicker.getInventory().setItemInMainHand(null);
            } else {
                itemStack.setAmount(amount - 1);
            }
            runnable.run();
            clicker.sendMessage(EasyGift.getString("message.gotten", true));
        } else {
            clicker.sendMessage(EasyGift.getString("message.pls-held-item", true));
        }
    }

    public void open(Player player) {
        this.model.openInventory(player);
    }
}
