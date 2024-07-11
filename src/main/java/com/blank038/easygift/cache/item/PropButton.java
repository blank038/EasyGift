package com.blank038.easygift.cache.item;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
@Getter
public class PropButton {
    private final String buttonId, displayItem;
    private final List<String> commands = new ArrayList<>();
    private final boolean gotten;

    public PropButton(String buttonId, ConfigurationSection section) {
        this.buttonId = buttonId;
        this.displayItem = section.getString("display");
        this.gotten = section.getBoolean("gotten");
        this.commands.addAll(section.getStringList("commands"));
    }
}
