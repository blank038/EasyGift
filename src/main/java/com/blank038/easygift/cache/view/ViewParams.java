package com.blank038.easygift.cache.view;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Blank038
 */
@Getter
public class ViewParams {
    private final String title, slots;

    public ViewParams(ConfigurationSection section) {
        this.title = section.getString("title", "");
        this.slots = section.getString("slots");
    }
}