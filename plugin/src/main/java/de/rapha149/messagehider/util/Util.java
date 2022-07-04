package de.rapha149.messagehider.util;

import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import de.rapha149.messagehider.Placeholders;
import de.rapha149.messagehider.util.Config.FilterData;
import de.rapha149.messagehider.util.Config.FilterData.CommandData;
import de.rapha149.messagehider.util.Config.FilterData.MessageData;
import de.rapha149.messagehider.util.Config.FilterData.TargetsData;
import de.rapha149.messagehider.version.MHPlayer;
import de.rapha149.messagehider.version.MessageType;
import de.rapha149.messagehider.version.Replacement;
import de.rapha149.messagehider.version.VersionWrapper;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.JSONObject;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Util {

    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static VersionWrapper WRAPPER;
    public static String PREFIX = "";

    public static String replaceGroups(String input, List<String> groups) {
        if (input == null)
            return null;

        StringBuilder sb = new StringBuilder();
        Matcher matcher = Pattern.compile("(?<!\\$)\\$(\\d)").matcher(input);
        int current = 0;
        while (matcher.find()) {
            sb.append(input, current, matcher.start());
            current = matcher.end();
            int group = Integer.parseInt(matcher.group(1));
            sb.append(group < groups.size() ? groups.get(group) : matcher.group());
        }
        return sb + input.substring(current);
    }

    public static String formatReplacementString(String replacement) {
        if (replacement == null)
            return null;

        String colorized = ChatColor.translateAlternateColorCodes('&', replacement);
        if ((colorized.startsWith("{") && colorized.endsWith("}")) ||
            (colorized.startsWith("[") && colorized.endsWith("]"))) {
            return colorized;
        } else {
            JSONObject json = new JSONObject();
            json.put("text", colorized);
            return json.toString();
        }
    }

    private static String[] getReplacementForCommand(String replacement, List<String> regexGroups) {
        String colorized = ChatColor.translateAlternateColorCodes('&', replacement);
        if (regexGroups != null)
            colorized = replaceGroups(colorized, regexGroups);

        if (colorized.startsWith("{") && colorized.endsWith("}")) {
            try {
                String plain = new TextComponent(ComponentSerializer.parse(colorized)).toPlainText();
                return new String[]{plain, colorized};
            } catch (Exception e) {
                if (e.getClass() == WRAPPER.getJsonSyntaxException()) {
                    e.printStackTrace();
                    return null;
                }
                throw e;
            }
        } else
            return new String[]{colorized, colorized};
    }

    public static FilterCheckResult checkFilters(String plain, String json, MessageType type, MHPlayer sender, MHPlayer receiver, String... filterIds) {
        int ignored = 0;
        int filtered = 0;
        List<String> filteredIds = new ArrayList<>();
        List<String> ids = Arrays.asList(filterIds);
        List<String> usedIds = new ArrayList<>();
        boolean hidden = false;
        Replacement replacement = null;
        List<CommandData> commands = new ArrayList<>();

        for (FilterData filter : Config.getFilters()) {
            if (filter.message != null) {
                String id = filter.id;
                if (ids.isEmpty() || (id != null && ids.contains(id))) {
                    usedIds.add(id);

                    TargetsData players = filter.targets;
                    if (sender != null) {
                        String name = sender.name;
                        String representation = sender.representation;
                        if (Bukkit.getOnlineMode()) {
                            if (!players.senders.isEmpty() && !players.senders.contains(representation) && !players.senders.contains(name))
                                continue;
                            if (players.excludedSenders.contains(representation) || players.excludedSenders.contains(name))
                                continue;
                        } else {
                            if (!players.senders.isEmpty() && !players.senders.contains(representation))
                                continue;
                            if (players.excludedSenders.contains(representation))
                                continue;
                        }
                    }

                    if (receiver != null) {
                        String name = receiver.name;
                        String representation = receiver.representation;
                        if (Bukkit.getOnlineMode()) {
                            if (!players.receivers.isEmpty() && !players.receivers.contains(representation) && !players.receivers.contains(name))
                                continue;
                            if (players.excludedReceivers.contains(representation) || players.excludedReceivers.contains(name))
                                continue;
                        } else {
                            if (!players.receivers.isEmpty() && !players.receivers.contains(representation))
                                continue;
                            if (players.excludedReceivers.contains(representation))
                                continue;
                        }

                        if (sender != null && sender.equals(receiver) && filter.onlyHideForOtherPlayers)
                            continue;
                    }

                    MessageData message = filter.message;
                    if (message.type != null && message.type != type)
                        continue;

                    String filterMessage = message.text;
                    Replacement replace = message.replace;
                    boolean ignoreCase = message.ignoreCase;
                    boolean regex = message.regex;
                    boolean onlyExecuteCommands = filter.onlyExecuteCommands;
                    boolean stopAfter = filter.stopAfter;
                    List<CommandData> cmds = filter.commands.stream().map(CommandData::new).collect(Collectors.toList());
                    if (message.json.enabled) {
                        if (json != null) {
                            JsonResult result = Util.jsonMatches(filterMessage, json, regex, ignoreCase, message.json.jsonPrecisionLevel);
                            if (result.matches) {
                                if (!cmds.isEmpty()) {
                                    String[] replacements = replace.text != null ? getReplacementForCommand(replace.text, result.groups) : new String[]{plain, json};
                                    Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], regex ? result.groups : Arrays.asList());
                                    commands.addAll(cmds);
                                }

                                filtered++;
                                if (id != null)
                                    filteredIds.add(id);

                                if (!onlyExecuteCommands && !hidden && replacement == null) {
                                    if (!replace.enabled)
                                        hidden = true;
                                    else
                                        replacement = regex ? replace.withText(replaceGroups(replace.text, result.groups), json) : replace;
                                }

                                if (stopAfter)
                                    break;
                            }
                        } else
                            ignored++;
                    } else if (plain != null) {
                        if (regex) {
                            Matcher matcher = Pattern.compile(filterMessage, ignoreCase ? Pattern.CASE_INSENSITIVE : 0).matcher(plain);
                            if (matcher.matches()) {
                                List<String> groups = new ArrayList<>();
                                for (int i = 0; i <= matcher.groupCount(); i++)
                                    groups.add(matcher.group(i));

                                if (!cmds.isEmpty()) {
                                    String[] replacements = replace.text != null ? getReplacementForCommand(replace.text, groups) : new String[]{plain, json};
                                    Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], groups);
                                    commands.addAll(cmds);
                                }

                                filtered++;
                                if (id != null)
                                    filteredIds.add(id);

                                if (!onlyExecuteCommands && !hidden && replacement == null) {
                                    if (!replace.enabled)
                                        hidden = true;
                                    else
                                        replacement = replace.withText(replaceGroups(replace.text, groups), json);
                                }

                                if (stopAfter)
                                    break;
                            }
                        } else if (ignoreCase ? plain.equalsIgnoreCase(filterMessage) : plain.equals(filterMessage)) {
                            if (!cmds.isEmpty()) {
                                String[] replacements = replace.text != null ? getReplacementForCommand(replace.text, null) : new String[]{plain, json};
                                Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], Arrays.asList());
                                commands.addAll(cmds);
                            }

                            filtered++;
                            if (id != null)
                                filteredIds.add(id);

                            if (!onlyExecuteCommands && !hidden && replacement == null) {
                                if (!replace.enabled)
                                    hidden = true;
                                else
                                    replacement = replace.withFallback(json);
                            }

                            if (stopAfter)
                                break;
                        }
                    } else
                        ignored++;
                }
            }
        }

        List<String> notFoundIds = new ArrayList<>(ids);
        notFoundIds.removeAll(usedIds);
        return new FilterCheckResult(filtered, filteredIds, ignored, notFoundIds, hidden, replacement, commands);
    }

    public static JsonResult jsonMatches(String json1, String json2, boolean regex, boolean ignoreCase, int precisionLevel) {
        List<String> groups = regex ? new ArrayList<>(Arrays.asList(json1)) : null;

        Map<String, Object> map1 = new JSONObject(json1).toMap();
        Map<String, Object> map2 = new JSONObject(json2).toMap();
        MapDifference<String, Object> difference = Maps.difference(flatten(map1), flatten(map2), new Equivalence<Object>() {
            @Override
            protected boolean doEquivalent(Object a, Object b) {
                if (a instanceof String && b instanceof String) {
                    if (regex) {
                        Matcher matcher = Pattern.compile((String) a).matcher((String) b);
                        if (matcher.matches()) {
                            for (int i = 1; i <= matcher.groupCount(); i++)
                                groups.add(matcher.group(i));
                            return true;
                        }
                    }
                    if (ignoreCase && ((String) a).equalsIgnoreCase((String) b))
                        return true;
                    if (a.equals("<ignore>"))
                        return true;
                }
                return a.equals(b);
            }

            @Override
            protected int doHash(Object o) {
                return o.hashCode();
            }
        });

        if (!difference.entriesDiffering().isEmpty())
            return new JsonResult(false);

        switch (precisionLevel) {
            case 3:
                return new JsonResult(difference.entriesOnlyOnLeft().isEmpty() && difference.entriesOnlyOnRight().isEmpty(), groups);
            case 2:
                return new JsonResult(difference.entriesOnlyOnLeft().isEmpty() && difference.entriesOnlyOnRight().entrySet().stream()
                        .noneMatch(entry -> !(entry.getValue() instanceof Boolean) || (Boolean) entry.getValue()), groups);
            case 1:
                return new JsonResult(difference.entriesOnlyOnLeft().isEmpty(), groups);
            case 0:
                return new JsonResult(true, groups);
        }
        return new JsonResult(false);
    }

    private static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream().flatMap(Util::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {
        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) entry.getValue();
            return properties.entrySet().stream()
                    .flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size()).mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
                    .flatMap(Util::flatten);
        }

        return Stream.of(entry);
    }

    private static class JsonResult {

        private boolean matches;
        private List<String> groups;

        public JsonResult(boolean matches) {
            this.matches = matches;
            groups = null;
        }

        private JsonResult(boolean matches, List<String> groups) {
            this.matches = matches;
            this.groups = matches ? groups : null;
        }
    }

    public static class FilterCheckResult {

        private FilterStatus status;
        private int filteredCount;
        private List<String> filteredIds;
        private int ignored;
        List<String> notFoundIds;
        private Replacement replacement;
        private List<CommandData> commands;

        private FilterCheckResult(int filteredCount, List<String> filteredIds, int ignored, List<String> notFoundIds,
                                  boolean hidden, Replacement replacement, List<CommandData> commands) {
            status = hidden ? FilterStatus.HIDDEN : (replacement != null ? FilterStatus.REPLACED : FilterStatus.NORMAL);
            this.filteredCount = filteredCount;
            this.filteredIds = filteredIds;
            this.ignored = ignored;
            this.notFoundIds = notFoundIds;
            this.replacement = replacement;
            this.commands = commands;
        }

        public FilterStatus getStatus() {
            return status;
        }

        public int getFilteredCount() {
            return filteredCount;
        }

        public List<String> getFilteredIds() {
            return filteredIds;
        }

        public int getIgnored() {
            return ignored;
        }

        public List<String> getNotFoundIds() {
            return notFoundIds;
        }

        public Replacement getReplacement() {
            return replacement;
        }

        public List<CommandData> getCommands() {
            return commands;
        }

        public enum FilterStatus {
            NORMAL, HIDDEN, REPLACED
        }
    }
}
