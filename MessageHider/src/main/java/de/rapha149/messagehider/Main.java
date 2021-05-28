package de.rapha149.messagehider;

import de.rapha149.messagehider.Metrics.SimplePie;
import de.rapha149.messagehider.Metrics.SingleLineChart;
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

        Metrics metrics = new Metrics(this, 11112);
        metrics.addCustomChart(new SingleLineChart("idle_timeout", () -> YamlUtil.getPresets().isIdleTimeout() ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("gamemode_change", () -> YamlUtil.getPresets().isGamemodeChange() ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("only_self_commands", () -> YamlUtil.getPresets().isOnlySelfCommands() ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("console_commands", () -> YamlUtil.getPresets().isConsoleCommands() ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("custom_filters_total", () -> YamlUtil.getCustomFilters().size()));
        metrics.addCustomChart(new SimplePie("custom_filters_per_server", () -> String.valueOf(YamlUtil.getCustomFilters().size())));

        new MessageHiderCommand(getCommand("messagehider"));
        manager.registerEvents(events = new Events(), this);

        if (YamlUtil.shouldCheckForUpdates()) {
            String version = Updates.getAvailableVersion(true);
            if (version == null)
                getLogger().info("Your version of this plugin is up to date!");
            else {
                getLogger().warning("There's a new version available for this plugin: " + version + "." +
                        " Download urls:\n- SpigotMC: " + Updates.SPIGOT_URL +
                        "\n- BukkitDev: " + Updates.BUKKIT_URL + " (It may take a few hours until the update is approved there).");
            }
        }

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
