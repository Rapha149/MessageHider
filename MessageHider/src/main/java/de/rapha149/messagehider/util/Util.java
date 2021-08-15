package de.rapha149.messagehider.util;

import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.rapha149.messagehider.Main;
import de.rapha149.messagehider.Placeholders;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData.CommandData;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData.MessageData;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData.PlayerData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Util {

    private static Gson gson = new Gson();

    public static String replaceGroups(String input, List<String> groups) {
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

    public static BaseComponent[] formatReplacementString(String replacement) {
        String colorized = ChatColor.translateAlternateColorCodes('&', replacement);
        if (colorized.startsWith("{") && colorized.endsWith("}"))
            try {
                return ComponentSerializer.parse(colorized);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        else
            return new ComponentBuilder(colorized).create();
    }

    private static String[] getReplacementForCommand(String replacement, List<String> regexGroups) {
        String colorized = ChatColor.translateAlternateColorCodes('&', replacement);
        if (regexGroups != null)
            colorized = replaceGroups(colorized, regexGroups);

        if (colorized.startsWith("{") && colorized.endsWith("}")) {
            try {
                String plain = new TextComponent(ComponentSerializer.parse(colorized)).toPlainText();
                return new String[]{plain, colorized};
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        } else
            return new String[]{colorized, colorized};
    }

    public static FilterCheckResult checkFilters(boolean breakIfFound, String plain, String json, UUID sender, UUID receiver, String... filterIds) {
        int ignored = 0;
        int filtered = 0;
        List<String> filteredIds = new ArrayList<>();
        List<String> ids = Arrays.asList(filterIds);
        List<String> usedIds = new ArrayList<>();
        boolean hidden = false;
        String replacement = null;
        List<CommandData> commands = new ArrayList<>();

        for (FilterData filter : YamlUtil.getFilters()) {
            if (filter.getMessage() != null) {
                String id = filter.getId();
                if (ids.isEmpty() || (id != null && ids.contains(id))) {
                    usedIds.add(id);

                    PlayerData players = filter.getPlayers();
                    
                    if (sender != null) {
                        String name = sender.equals(Main.ZERO_UUID) ? "<console>" : Bukkit.getPlayer(sender).getName();
                        if (!Bukkit.getOnlineMode()) {
                            if (!players.getSenders().isEmpty() && !players.getSenders().contains(sender.toString()) && !players.getSenders().contains(name))
                                continue;
                            if (players.getExcludedSenders().contains(name))
                                continue;
                        } else {
                            if (!players.getSenderUUIDs().isEmpty() && !players.getSenderUUIDs().contains(sender))
                                continue;
                            if (players.getExcludedSenderUUIDs().contains(sender))
                                continue;
                        }
                    }

                    if (receiver != null) {
                        String name = receiver.equals(Main.ZERO_UUID) ? "<console>" : Bukkit.getPlayer(receiver).getName();
                        if (!Bukkit.getOnlineMode()) {
                            if (!players.getReceivers().isEmpty() && !players.getReceivers().contains(receiver.toString()) && !players.getReceivers().contains(name))
                                continue;
                            if (players.getExcludedReceivers().contains(name))
                                continue;
                        } else {
                            if (!players.getReceiverUUIDs().isEmpty() && !players.getReceiverUUIDs().contains(receiver))
                                continue;
                            if (players.getExcludedReceiverUUIDs().contains(receiver))
                                continue;
                        }

                        if (sender != null && sender.equals(receiver) && filter.isOnlyHideForOtherPlayers())
                            continue;
                    }

                    MessageData message = filter.getMessage();
                    boolean regex = message.isRegex();
                    boolean ignoreCase = message.isIgnoreCase();
                    String filterMessage = message.getText();
                    String replace = message.getReplacement();
                    boolean onlyExecuteCommands = filter.isOnlyExecuteCommands();
                    List<CommandData> cmds = new ArrayList<>(filter.getCommands());
                    if (message.getJson().isEnabled()) {
                        if (json != null) {
                            JsonResult result = Util.jsonMatches(filterMessage, json, regex, ignoreCase, message.getJson().getJsonPrecisionLevel());
                            if (result.matches) {
                                if (!cmds.isEmpty()) {
                                    String[] replacements = replace != null ? getReplacementForCommand(replace, result.groups) : new String[]{plain, json};
                                    Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], regex ? result.groups : Arrays.asList());
                                    commands.addAll(cmds);
                                }

                                filtered++;
                                if (id != null)
                                    filteredIds.add(id);

                                if (!onlyExecuteCommands) {
                                    if (replace == null)
                                        hidden = true;

                                    if (replacement == null && replace != null) {
                                        if (regex)
                                            replacement = replaceGroups(replace, result.groups);
                                        else
                                            replacement = replace;
                                    }

                                    if (breakIfFound && replacement != null)
                                        break;
                                }
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
                                    String[] replacements = replace != null ? getReplacementForCommand(replace, groups) : new String[]{plain, json};
                                    Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], groups);
                                    commands.addAll(cmds);
                                }

                                filtered++;
                                if (id != null)
                                    filteredIds.add(id);

                                if (!onlyExecuteCommands) {
                                    if (replace == null)
                                        hidden = true;

                                    if (replacement == null && replace != null)
                                        replacement = replaceGroups(replace, groups);

                                    if (breakIfFound && replacement != null)
                                        break;
                                }
                            }
                        } else if (ignoreCase ? plain.equalsIgnoreCase(filterMessage) : plain.equals(filterMessage)) {
                            if (!cmds.isEmpty()) {
                                String[] replacements = replace != null ? getReplacementForCommand(replace, null) : new String[]{plain, json};
                                Placeholders.replace(cmds, sender, receiver, plain, json, replacements[0], replacements[1], Arrays.asList());
                                commands.addAll(cmds);
                            }

                            filtered++;
                            if (id != null)
                                filteredIds.add(id);

                            if (!onlyExecuteCommands) {
                                if (replace == null)
                                    hidden = true;

                                if (replacement == null && replace != null)
                                    replacement = replace;

                                if (breakIfFound && replacement != null)
                                    break;
                            }
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
        List<String> groups = regex ? new ArrayList<>() : null;

        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map1 = gson.fromJson(json1, mapType);
        Map<String, Object> map2 = gson.fromJson(json2, mapType);
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
        return map.entrySet()
                .stream()
                .flatMap(Util::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) entry.getValue();
            return properties.entrySet()
                    .stream()
                    .flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
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
        private String replacement;
        private List<CommandData> commands;

        private FilterCheckResult(int filteredCount, List<String> filteredIds, int ignored, List<String> notFoundIds,
                                  boolean hidden, String replacement, List<CommandData> commands) {
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

        public String getReplacement() {
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
