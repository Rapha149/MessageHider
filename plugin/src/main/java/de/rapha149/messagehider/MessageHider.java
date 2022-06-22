package de.rapha149.messagehider;

import de.rapha149.messagehider.Metrics.SimplePie;
import de.rapha149.messagehider.Metrics.SingleLineChart;
import de.rapha149.messagehider.util.Config;
import de.rapha149.messagehider.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static de.rapha149.messagehider.util.Util.WRAPPER;

public class MessageHider extends JavaPlugin {

    private static MessageHider instance;
    private Events events;
    public boolean placeholderAPISupport;

    @Override
    public void onEnable() {
        instance = this;

        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        try {
            WRAPPER = (VersionWrapper) Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + nmsVersion).newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Failed to load support for server version " + nmsVersion, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MessageHider does not support the server version \"" + nmsVersion + "\"", e);
        }

        PluginManager manager = getServer().getPluginManager();
        try {
            Config.load();
        } catch (IOException e) {
            e.printStackTrace();
            manager.disablePlugin(this);
            return;
        }

        Metrics metrics = new Metrics(this, 11112);
        metrics.addCustomChart(new SingleLineChart("idle_timeout", () -> Config.get().presets.idleTimeout ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("gamemode_change", () -> Config.get().presets.gamemodeChange ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("only_self_commands", () -> Config.get().presets.onlySelfCommands ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("console_commands", () -> Config.get().presets.consoleCommands ? 1 : 0));
        metrics.addCustomChart(new SingleLineChart("custom_filters_total", () -> Config.get().filters.size()));
        metrics.addCustomChart(new SimplePie("custom_filters_per_server", () -> String.valueOf(Config.get().filters.size())));

        new MessageHiderCommand(getCommand("messagehider"));
        manager.registerEvents(events = new Events(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPISupport = true;
            Placeholders.registerPlaceholderExpansion();
            getLogger().info("PlaceholderAPI support was enabled.");
        }

        if (Config.get().checkForUpdates) {
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

    public static MessageHider getInstance() {
        return instance;
    }
}
