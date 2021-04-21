package de.rapha149.messagehider;

import de.rapha149.messagehider.util.ReflectionUtil;
import de.rapha149.messagehider.util.YamlUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;

public class Main extends JavaPlugin {

    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static Main instance;
    private Events events;

    @Override
    public void onEnable() {
        instance = this;
        PluginManager manager = getServer().getPluginManager();
        try {
            YamlUtil.load();
        } catch (IOException e) {
            e.printStackTrace();
            manager.disablePlugin(this);
            return;
        }

        if (!ReflectionUtil.load()) {
            getLogger().severe("Could not load ReflectionUtil. Plugin will be disabled.");
            manager.disablePlugin(this);
            return;
        }

        new MessageHiderCommand(getCommand("messagehider"));
        manager.registerEvents(events = new Events(), this);
        getLogger().info("Plugin successfully enabled!");
    }

    @Override
    public void onDisable() {
        events.removeHandlers();
        getLogger().info("Plugin disabled.");
    }

    public static Main getInstance() {
        return instance;
    }
}
