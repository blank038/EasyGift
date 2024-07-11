package com.blank038.easygift.utils;

import com.blank038.easygift.EasyGift;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

/**
 * @author Blank038
 */
public class ScriptUtil {
    private static ScriptEngine scriptEngine;

    public static void initScriptEngine() {
        String engineName = EasyGift.getInstance().getConfig().getString("script-engine", "JavaScript");
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R1)) {
            try {
                ScriptEngineFactory factory = (ScriptEngineFactory) Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory").newInstance();
                scriptEngineManager.registerEngineName("nashorn", factory);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                EasyGift.getInstance().getLogger().warning("not found the Nashorn ScriptEngine");
            }
        }
        scriptEngine = scriptEngineManager.getEngineByName(engineName);
    }

    public static boolean detectionCondition(Player player, List<String> conditions) {
        if (scriptEngine == null) {
            EasyGift.getInstance().getLogger().warning("Cannot invoke 'ScriptUtil.detectionCondition', beacuse 'ScriptUtil.scriptEngine' is null");
            return false;
        }
        if (conditions.isEmpty()) {
            return true;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < conditions.size(); i++) {
                if (i + 1 == conditions.size()) {
                    stringBuilder.append(conditions.get(i));
                } else {
                    stringBuilder.append(conditions.get(i)).append(" && ");
                }
            }
            return (boolean) scriptEngine.eval(PlaceholderAPI.setPlaceholders(player, stringBuilder.toString()));
        } catch (Exception e) {
            EasyGift.getInstance().getLogger().severe(" Condition is invalid " + e);
            return false;
        }
    }
}