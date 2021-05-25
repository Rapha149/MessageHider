package de.rapha149.messagehider.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.rapha149.messagehider.Main;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import de.rapha149.messagehider.util.YamlUtil.YamlData.PresetsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YamlUtil {

    private static final List<Character> allowedCharsForIds;

    static {
        List<Character> list = new ArrayList<>();
        for (int i = 'a'; i <= 'z'; i++)
            list.add((char) i);
        for (int i = 'A'; i <= 'Z'; i++)
            list.add((char) i);
        for (int i = '0'; i <= '9'; i++)
            list.add((char) i);
        list.add('_');
        allowedCharsForIds = Collections.unmodifiableList(list);
    }

    private static File file;
    private static Yaml yaml;
    private static YamlData data;
    private static Map<String, UUID> uuidCache = new HashMap<>();

    public static void load() throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(new CustomClassLoaderConstructor(Main.getInstance().getClass().getClassLoader()), new Representer(), options);

        file = new File(Main.getInstance().getDataFolder(), "config.yml");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (file.exists())
            data = yaml.loadAs(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), YamlData.class);
        else
            data = new YamlData();
        save();

        JsonParser parser = new JsonParser();
        Iterator<FilterData> iterator = data.filters.iterator();
        while (iterator.hasNext()) {
            FilterData filter = iterator.next();
            String id = filter.id;
            if (id != null) {
                boolean removed = false;
                for (char c : id.toCharArray()) {
                    if (!allowedCharsForIds.contains(c)) {
                        Main.getInstance().getLogger().warning("Filter ids must only contain letters, numbers and underscores. (Wrong id: \"" + id + "\")");
                        iterator.remove();
                        removed = true;
                    }
                }
                if (removed)
                    continue;
            }

            String filterSpecification = id != null ? "the filter" + id : "a filter";
            if (filter.json) {
                try {
                    if(!parser.parse(filter.message).isJsonObject())
                        throw new JsonParseException("");
                } catch (JsonParseException e) {
                    Main.getInstance().getLogger().warning("You got a json error in '" + filter.message + "' (Message of " + filterSpecification + ")");
                    iterator.remove();
                    continue;
                }
            }

            if(filter.replacement != null) {
                String replacement = filter.getReplacement();
                if(replacement.startsWith("{") && replacement.endsWith("}")) {
                    try {
                        if(!parser.parse(filter.message).isJsonObject())
                            throw new JsonParseException("");
                    } catch (JsonParseException e) {
                        Main.getInstance().getLogger().warning("You got a json error in '" + filter.message + "' (Replacement of " + filterSpecification + ")");
                        iterator.remove();
                    }
                }
            }
        }

        List<String> ids = new ArrayList<>();
        getFilters().forEach(filter -> {
            if(filter.id != null) {
                if (!ids.contains(filter.id))
                    ids.add(filter.id);
                else
                    Main.getInstance().getLogger().warning("You have duplicate filter ids: \"" + filter.id + "\"");
            }
        });

        PresetsData presets = data.presets;
        if (presets.gamemodeChange && Presets.GAMEMODE_CHANGE == null)
            Main.getInstance().getLogger().warning("You enabled the preset 'gamemodeChange' but it is not suitable for this server version.");
        if (presets.onlySelfCommands && Presets.ONLY_SELF_COMMANDS == null)
            Main.getInstance().getLogger().warning("You enabled the preset 'onlySelfCommands' but it is not suitable for this server version.");
        if (presets.consoleCommands && Presets.CONSOLE_COMMANDS == null)
            Main.getInstance().getLogger().warning("You enabled the preset 'consoleCommands' but it is not suitable for this server version.");

        uuidCache.clear();
    }

    private static void save() throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(yaml.dumpAsMap(data));
        writer.flush();
        writer.close();
    }

    public static List<FilterData> getFilters() {
        List<FilterData> filters = new ArrayList<>(data.filters);

        PresetsData presets = data.presets;
        if (presets.idleTimeout)
            filters.add(Presets.IDLE_TIMEOUT);
        if (presets.gamemodeChange && Presets.GAMEMODE_CHANGE != null)
            filters.add(Presets.GAMEMODE_CHANGE);
        if (presets.onlySelfCommands && Presets.ONLY_SELF_COMMANDS != null)
            filters.add(Presets.ONLY_SELF_COMMANDS);
        if(presets.consoleCommands && Presets.CONSOLE_COMMANDS != null)
            filters.add(Presets.CONSOLE_COMMANDS);

        return filters;
    }

    public static void addFilter(FilterData filter) throws IOException {
        data.messageFilters.add(filter);
        data.filters.add(filter);
        save();
    }

    public static boolean shouldCheckForUpdates() {
        return data.checkForUpdates;
    }

    public static String getPrefix() {
        return data.translatedPrefix;
    }

    public static PresetsData getPresets() {
        return data.presets;
    }

    public static List<FilterData> getCustomFilters() {
        return data.filters;
    }

    public static class YamlData {

        private boolean checkForUpdates;
        private String prefix;
        private PresetsData presets;
        private List<FilterData> messageFilters;

        private transient String translatedPrefix;
        private transient List<FilterData> filters;

        public YamlData() {
            checkForUpdates = true;
            prefix = "&8[&cMH&8] ";
            translatedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
            presets = new PresetsData();
            messageFilters = new ArrayList<>();
            filters = new ArrayList<>();
        }

        public boolean isCheckForUpdates() {
            return checkForUpdates;
        }

        public void setCheckForUpdates(boolean checkForUpdates) {
            this.checkForUpdates = checkForUpdates;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
            translatedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
        }

        public PresetsData getPresets() {
            return presets;
        }

        public void setPresets(PresetsData presets) {
            this.presets = presets;
        }

        public List<FilterData> getMessageFilters() {
            return messageFilters;
        }

        public void setMessageFilters(List<FilterData> messageFilters) {
            this.messageFilters = messageFilters;
            filters = new ArrayList<>(messageFilters);
            filters.sort((f1, f2) -> {
                if(f1.priority == null && f2.priority == null)
                    return 0;
                if(f1.priority != null && f2.priority == null)
                    return -1;
                if(f1.priority == null && f2.priority != null)
                    return 1;
                return f1.priority.compareTo(f2.priority);
            });
        }

        public static class PresetsData {

            private boolean idleTimeout;
            private boolean gamemodeChange;
            private boolean onlySelfCommands;
            private boolean consoleCommands;

            public PresetsData() {
                idleTimeout = false;
                gamemodeChange = false;
                onlySelfCommands = false;
                consoleCommands = false;
            }

            public boolean isIdleTimeout() {
                return idleTimeout;
            }

            public void setIdleTimeout(boolean idleTimeout) {
                this.idleTimeout = idleTimeout;
            }

            public boolean isGamemodeChange() {
                return gamemodeChange;
            }

            public void setGamemodeChange(boolean gamemodeChange) {
                this.gamemodeChange = gamemodeChange;
            }

            public boolean isOnlySelfCommands() {
                return onlySelfCommands;
            }

            public void setOnlySelfCommands(boolean onlySelfCommands) {
                this.onlySelfCommands = onlySelfCommands;
            }

            public boolean isConsoleCommands() {
                return consoleCommands;
            }

            public void setConsoleCommands(boolean consoleCommands) {
                this.consoleCommands = consoleCommands;
            }
        }

        public static class FilterData {

            private String id;
            private boolean json;
            private int jsonPrecisionLevel;
            private boolean regex;
            private boolean ignoreCase;
            private boolean onlyHideForOtherPlayers;
            private Integer priority;
            private List<String> senders;
            private List<String> excludedSenders;
            private List<String> receivers;
            private List<String> excludedReceivers;
            private String message;
            private String replacement;

            private transient List<UUID> senderUUIDs;
            private transient List<UUID> excludedSenderUUIDs;
            private transient List<UUID> receiverUUIDs;
            private transient List<UUID> excludedReceiverUUIDs;

            public FilterData() {
                id = null;
                json = false;
                jsonPrecisionLevel = 2;
                regex = false;
                ignoreCase = false;
                onlyHideForOtherPlayers = false;
                priority = null;
                senders = new ArrayList<>();
                excludedSenders = new ArrayList<>();
                receivers = new ArrayList<>();
                excludedReceivers = new ArrayList<>();
                message = "";
                replacement = null;

                senderUUIDs = new ArrayList<>();
                excludedSenderUUIDs = new ArrayList<>();
                receiverUUIDs = new ArrayList<>();
                excludedReceiverUUIDs = new ArrayList<>();
            }

            public FilterData(String id, boolean json, int jsonPrecisionLevel, boolean regex, boolean ignoreCase,
                              boolean onlyHideForOtherPlayers, Integer priority, String message, String replacement) {
                this.id = id;
                this.json = json;
                this.jsonPrecisionLevel = jsonPrecisionLevel;
                this.regex = regex;
                this.ignoreCase = ignoreCase;
                this.onlyHideForOtherPlayers = onlyHideForOtherPlayers;
                this.priority = priority;
                this.senders = new ArrayList<>();
                this.excludedSenders = new ArrayList<>();
                this.receivers = new ArrayList<>();
                this.excludedReceivers = new ArrayList<>();
                this.message = message;
                this.replacement = replacement;

                senderUUIDs = new ArrayList<>();
                excludedSenderUUIDs = new ArrayList<>();
                receiverUUIDs = new ArrayList<>();
                excludedReceiverUUIDs = new ArrayList<>();
            }

            public FilterData(String id, boolean json, int jsonPrecisionLevel, boolean regex, boolean ignoreCase,
                              boolean onlyHideForOtherPlayers, Integer priority, String message, String replacement,
                              List<String> senders, List<String> excludedSenders, List<String> receivers, List<String> excludedReceivers) {
                this.id = id;
                this.json = json;
                this.jsonPrecisionLevel = jsonPrecisionLevel;
                this.regex = regex;
                this.ignoreCase = ignoreCase;
                this.onlyHideForOtherPlayers = onlyHideForOtherPlayers;
                this.priority = priority;
                this.senders = senders;
                this.excludedSenders = excludedSenders;
                this.receivers = receivers;
                this.excludedReceivers = excludedReceivers;
                this.message = message;
                this.replacement = replacement;

                senderUUIDs = YamlUtil.getUUIDs(senders);
                excludedSenderUUIDs = YamlUtil.getUUIDs(excludedSenders);
                receiverUUIDs = YamlUtil.getUUIDs(receivers);
                excludedReceiverUUIDs = YamlUtil.getUUIDs(excludedReceivers);
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public boolean isJson() {
                return json;
            }

            public void setJson(boolean json) {
                this.json = json;
            }

            public int getJsonPrecisionLevel() {
                return jsonPrecisionLevel;
            }

            public void setJsonPrecisionLevel(int jsonPrecisionLevel) {
                this.jsonPrecisionLevel = jsonPrecisionLevel;
            }

            public boolean isRegex() {
                return regex;
            }

            public void setRegex(boolean regex) {
                this.regex = regex;
            }

            public boolean isIgnoreCase() {
                return ignoreCase;
            }

            public void setIgnoreCase(boolean ignoreCase) {
                this.ignoreCase = ignoreCase;
            }

            public boolean isOnlyHideForOtherPlayers() {
                return onlyHideForOtherPlayers;
            }

            public void setOnlyHideForOtherPlayers(boolean onlyHideForOtherPlayers) {
                this.onlyHideForOtherPlayers = onlyHideForOtherPlayers;
            }

            public Integer getPriority() {
                return priority;
            }

            public void setPriority(Integer priority) {
                this.priority = priority;
            }

            public List<String> getSenders() {
                return senders;
            }

            public void setSenders(List<String> senders) {
                this.senders = senders;
                senderUUIDs = YamlUtil.getUUIDs(senders);
            }

            public List<String> getExcludedSenders() {
                return excludedSenders;
            }

            public void setExcludedSenders(List<String> excludedSenders) {
                this.excludedSenders = excludedSenders;
                excludedSenderUUIDs = YamlUtil.getUUIDs(excludedSenders);
            }

            public List<String> getReceivers() {
                return receivers;
            }

            public void setReceivers(List<String> receivers) {
                this.receivers = receivers;
                receiverUUIDs = YamlUtil.getUUIDs(receivers);
            }

            public List<String> getExcludedReceivers() {
                return excludedReceivers;
            }

            public void setExcludedReceivers(List<String> excludedReceivers) {
                this.excludedReceivers = excludedReceivers;
                excludedReceiverUUIDs = YamlUtil.getUUIDs(excludedReceivers);
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            public String getReplacement() {
                return replacement;
            }

            public void setReplacement(String replacement) {
                this.replacement = replacement;
            }

            public List<UUID> getSenderUUIDs() {
                return senderUUIDs;
            }

            public List<UUID> getExcludedSenderUUIDs() {
                return excludedSenderUUIDs;
            }

            public List<UUID> getReceiverUUIDs() {
                return receiverUUIDs;
            }

            public List<UUID> getExcludedReceiverUUIDs() {
                return excludedReceiverUUIDs;
            }
        }
    }

    private static List<UUID> getUUIDs(List<String> players) {
        List<UUID> uuids = new ArrayList<>();
        players.forEach(player -> {
            try {
                uuids.add(UUID.fromString(player));
            } catch (IllegalArgumentException e) {
                if (Bukkit.getOnlineMode()) {
                    if (uuidCache.containsKey(player))
                        uuids.add(uuidCache.get(player));
                    else {
                        UUID uuid = null;
                        if (player.equals("<console>"))
                            uuid = Main.ZERO_UUID;
                        else if (!player.isEmpty()) {
                            try {
                                uuid = getUUID(player);
                                if (uuid == null)
                                    Main.getInstance().getLogger().warning(player + " is not a valid player.");
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } else
                            Main.getInstance().getLogger().warning("You specified an empty player name.");

                        if (uuid != null) {
                            uuidCache.put(player, uuid);
                            uuids.add(uuid);
                        }
                    }
                }
            }
        });
        return uuids;
    }

    private static UUID getUUID(String name) throws IOException {
        URLConnection conn = new URL("https://www.mc-heads.net/minecraft/profile/" + URLEncoder.encode(name, "UTF-8")).openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:86.0) Gecko/20100101 Firefox/86.0");
        conn.connect();
        JsonElement root = new JsonParser().parse(new InputStreamReader(conn.getInputStream()));
        if (root.isJsonObject()) {
            StringBuilder sb = new StringBuilder(root.getAsJsonObject().get("id").getAsString());
            sb.insert(20, '-');
            sb.insert(16, '-');
            sb.insert(12, '-');
            sb.insert(8, '-');
            return UUID.fromString(sb.toString());
        } else
            return null;
    }
}
