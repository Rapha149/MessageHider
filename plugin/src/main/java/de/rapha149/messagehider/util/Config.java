package de.rapha149.messagehider.util;

import de.rapha149.messagehider.MessageHider;
import org.bukkit.ChatColor;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {

    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile("[\\w\\d]+");
    private static final int CONFIG_VERSION = 2;

    private static File file;
    private static Yaml yaml;
    private static Config config;

    public static void load() throws IOException {
        Logger logger = MessageHider.getInstance().getLogger();

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(new CustomClassLoaderConstructor(MessageHider.getInstance().getClass().getClassLoader()), new Representer(), options);

        file = new File(MessageHider.getInstance().getDataFolder(), "config.yml");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (file.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line != null && line.startsWith("# version=" + CONFIG_VERSION))
                config = yaml.loadAs(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8), Config.class);
            else {
                File renamed;
                int number = 0;
                while (true) {
                    renamed = new File(MessageHider.getInstance().getDataFolder(), "config.yml.old" + (number > 0 ? number : ""));
                    if (!renamed.exists())
                        break;
                    number++;
                }
                file.renameTo(renamed);
                config = new Config();
                logger.severe("Your config was outdated so it got regenerated. " +
                              "The old config was renamed to \"" + renamed.getName() + "\". " +
                              "Please recreate custom filters by using \"/messagehider create\".");
            }
        } else
            config = new Config();
        save();

        Util.PREFIX = config.translatedPrefix;

        Iterator<FilterData> iterator = config.filters.iterator();
        while (iterator.hasNext()) {
            FilterData filter = iterator.next();
            String id = filter.id;
            if (id != null) {
                if (!ALLOWED_CHARS_PATTERN.matcher(id).matches()) {
                    logger.warning("Filter ids must only contain letters, numbers and underscores. (Wrong id: \"" + id + "\")");
                    iterator.remove();
                    continue;
                }
            }

            String filterSpecification = id != null ? "the filter " + id : "a filter";
            if (filter.message.json.enabled) {
                try {
                    new JSONObject(filter.message.text);
                } catch (JSONException e) {
                    logger.warning("You got a json error in '" + filter.message + "' (Message of " + filterSpecification + ")");
                    iterator.remove();
                    continue;
                }
            }

            if (filter.message.replacement != null) {
                String replacement = filter.message.replacement;
                if (replacement.startsWith("{") && replacement.endsWith("}")) {
                    try {
                        new JSONObject(replacement);
                    } catch (JSONException e) {
                        logger.warning("You got a json error in '" + filter.message + "' (Replacement of " + filterSpecification + ")");
                        iterator.remove();
                    }
                }
            }
        }

        List<String> ids = new ArrayList<>();
        getFilters().forEach(filter -> {
            if (filter.id != null) {
                if (!ids.contains(filter.id))
                    ids.add(filter.id);
                else
                    logger.warning("You have duplicate filter ids: \"" + filter.id + "\"");
            }
        });

        PresetsData presets = config.presets;
        if (presets.gamemodeChange && Presets.GAMEMODE_CHANGE == null)
            logger.warning("You enabled the preset 'gamemodeChange' but it is not suitable for this server version.");
        if (presets.onlySelfCommands && Presets.ONLY_SELF_COMMANDS == null)
            logger.warning("You enabled the preset 'onlySelfCommands' but it is not suitable for this server version.");
        if (presets.consoleCommands && Presets.CONSOLE_COMMANDS == null)
            logger.warning("You enabled the preset 'consoleCommands' but it is not suitable for this server version.");
    }

    public static void save() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()))) {
            writer.write("# version=" + CONFIG_VERSION + " DO NOT CHANGE THIS LINE\n" + yaml.dumpAsMap(config));
        }
    }

    public static Config get() {
        return config;
    }

    public static List<FilterData> getFilters() {
        List<FilterData> filters = new ArrayList<>(config.filters);

        PresetsData presets = config.presets;
        if (presets.idleTimeout)
            filters.add(Presets.IDLE_TIMEOUT);
        if (presets.gamemodeChange && Presets.GAMEMODE_CHANGE != null)
            filters.add(Presets.GAMEMODE_CHANGE);
        if (presets.onlySelfCommands && Presets.ONLY_SELF_COMMANDS != null)
            filters.add(Presets.ONLY_SELF_COMMANDS);
        if (presets.consoleCommands && Presets.CONSOLE_COMMANDS != null)
            filters.add(Presets.CONSOLE_COMMANDS);

        return filters;
    }

    public static List<FilterData> getCustomFiltersByIds(List<String> ids) {
        return config.filters.stream().filter(filter -> filter.id != null && ids.contains(filter.id)).collect(Collectors.toList());
    }

    public static void addFilter(FilterData filter) throws IOException {
        config.messageFilters.add(filter);
        config.filters.add(filter);
        save();
    }

    public boolean checkForUpdates = true;
    private String prefix = "&8[&cMH&8] ";
    public PresetsData presets = new PresetsData();
    private List<FilterData> messageFilters = new ArrayList<>();

    public transient String translatedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
    public transient List<FilterData> filters = new ArrayList<>();

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        translatedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public List<FilterData> getMessageFilters() {
        return messageFilters;
    }

    public void setMessageFilters(List<FilterData> messageFilters) {
        this.messageFilters = messageFilters;
        filters = new ArrayList<>(messageFilters);
        filters.sort((f1, f2) -> {
            if (f1.priority == null && f2.priority == null)
                return 0;
            if (f1.priority != null && f2.priority == null)
                return -1;
            if (f1.priority == null && f2.priority != null)
                return 1;
            return f1.priority.compareTo(f2.priority);
        });
    }

    public static class PresetsData {

        public boolean idleTimeout = false;
        public boolean gamemodeChange = false;
        public boolean onlySelfCommands = false;
        public boolean consoleCommands = false;
    }

    public static class FilterData {

        public String id;
        public MessageData message;
        public boolean onlyHideForOtherPlayers;
        public boolean onlyExecuteCommands;
        public boolean stopAfter;
        public Integer priority;
        public List<CommandData> commands;
        public TargetsData targets;

        public FilterData() {
            id = null;
            message = new MessageData();
            onlyHideForOtherPlayers = false;
            onlyExecuteCommands = false;
            stopAfter = false;
            priority = null;
            commands = new ArrayList<>();
            targets = new TargetsData();
        }

        public FilterData(String id, MessageData message, boolean onlyHideForOtherPlayers,
                          boolean onlyExecuteCommands, boolean stopAfter, Integer priority, List<CommandData> commands) {
            this.id = id;
            this.message = message;
            this.onlyHideForOtherPlayers = onlyHideForOtherPlayers;
            this.onlyExecuteCommands = onlyExecuteCommands;
            this.stopAfter = stopAfter;
            this.priority = priority;
            this.commands = commands;
            this.targets = new TargetsData();
        }

        public FilterData(String id, MessageData message, boolean onlyHideForOtherPlayers,
                          boolean onlyExecuteCommands, boolean stopAfter, Integer priority, List<CommandData> commands,
                          TargetsData targets) {
            this.id = id;
            this.message = message;
            this.onlyHideForOtherPlayers = onlyHideForOtherPlayers;
            this.onlyExecuteCommands = onlyExecuteCommands;
            this.stopAfter = stopAfter;
            this.priority = priority;
            this.commands = commands;
            this.targets = targets;
        }

        public static class MessageData {

            public String text;
            public String replacement;
            public boolean ignoreCase;
            public boolean regex;
            public JsonData json;

            public MessageData() {
                text = "";
                replacement = null;
                ignoreCase = false;
                regex = false;
                json = new JsonData();
            }

            public MessageData(String text, String replacement, boolean ignoreCase, boolean regex, JsonData json) {
                this.text = text;
                this.replacement = replacement;
                this.ignoreCase = ignoreCase;
                this.regex = regex;
                this.json = json;
            }

            public static class JsonData {

                public boolean enabled;
                public int jsonPrecisionLevel;

                public JsonData() {
                    enabled = false;
                    jsonPrecisionLevel = 2;
                }

                public JsonData(boolean enabled, int jsonPrecisionLevel) {
                    this.enabled = enabled;
                    this.jsonPrecisionLevel = jsonPrecisionLevel;
                }
            }
        }

        public static class CommandData {

            public String command = "cmd";
            public CommandType type = CommandType.CONSOLE;
            public float delay = 0;

            public CommandData() {
            }

            public CommandData(CommandData commandData) {
                this.command = commandData.command;
                this.type = commandData.type;
                this.delay = commandData.delay;
            }

            public enum CommandType {
                CONSOLE, PLAYER
            }
        }

        public static class TargetsData {

            public List<String> senders;
            public List<String> excludedSenders;
            public List<String> receivers;
            public List<String> excludedReceivers;

            public TargetsData() {
                senders = new ArrayList<>();
                excludedSenders = new ArrayList<>();
                receivers = new ArrayList<>();
                excludedReceivers = new ArrayList<>();
            }

            public TargetsData(List<String> senders, List<String> excludedSenders, List<String> receivers, List<String> excludedReceivers) {
                this.senders = senders;
                this.excludedSenders = excludedSenders;
                this.receivers = receivers;
                this.excludedReceivers = excludedReceivers;
            }
        }
    }
}
