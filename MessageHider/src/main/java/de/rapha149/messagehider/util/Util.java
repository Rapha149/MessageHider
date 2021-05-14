package de.rapha149.messagehider.util;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rapha149.messagehider.Main;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Util {

    private static Gson gson = new Gson();

    public static FilterCheckResult checkFilters(boolean breakIfFound, String plain, String json, UUID sender, UUID receiver, String... filterIds) {
        int ignored = 0;
        int hidden = 0;
        List<String> hiddenIds = new ArrayList<>();
        List<String> ids = Arrays.asList(filterIds);
        List<String> usedIds = new ArrayList<>();

        for (FilterData filter : YamlUtil.getFilters()) {
            String id = filter.getId();
            if (ids.isEmpty() || (id != null && ids.contains(id))) {
                usedIds.add(id);

                if (sender != null) {
                    String name = sender.equals(Main.ZERO_UUID) ? "<console>" : Bukkit.getPlayer(sender).getName();
                    if (!Bukkit.getOnlineMode()) {
                        if (!filter.getSenders().isEmpty() && !filter.getSenders().contains(sender.toString()) && !filter.getSenders().contains(name))
                            continue;
                        if (filter.getExcludedSenders().contains(name))
                            continue;
                    } else {
                        if (!filter.getSenderUUIDs().isEmpty() && !filter.getSenderUUIDs().contains(sender))
                            continue;
                        if (filter.getExcludedSenderUUIDs().contains(sender))
                            continue;
                    }
                }

                if (receiver != null) {
                    String name = receiver.equals(Main.ZERO_UUID) ? "<console>" : Bukkit.getPlayer(receiver).getName();
                    if (!Bukkit.getOnlineMode()) {
                        if (!filter.getReceivers().isEmpty() && !filter.getReceivers().contains(receiver.toString()) && !filter.getReceivers().contains(name))
                            continue;
                        if (filter.getExcludedReceivers().contains(name))
                            continue;
                    } else {
                        if (!filter.getReceiverUUIDs().isEmpty() && !filter.getReceiverUUIDs().contains(receiver))
                            continue;
                        if (filter.getExcludedReceiverUUIDs().contains(receiver))
                            continue;
                    }

                    if (sender != null && sender.equals(receiver) && filter.isOnlyHideForOtherPlayers())
                        continue;
                }

                boolean regex = filter.isRegex();
                boolean ignoreCase = filter.isIgnoreCase();
                String filterMessage = filter.getMessage();
                if (filter.isJson()) {
                    if (json != null) {
                        if (Util.jsonMatches(filterMessage, json, regex, ignoreCase, filter.getJsonPrecisionLevel())) {
                            hidden++;
                            if (id != null)
                                hiddenIds.add(id);
                            if (breakIfFound)
                                break;
                        }
                    } else
                        ignored++;
                } else if (plain != null) {
                    if (regex) {
                        if (Pattern.compile(filterMessage, ignoreCase ? Pattern.CASE_INSENSITIVE : 0).matcher(plain).matches()) {
                            hidden++;
                            if (id != null)
                                hiddenIds.add(id);
                            if (breakIfFound)
                                break;
                        }
                    } else if (ignoreCase ? plain.equalsIgnoreCase(filterMessage) : plain.equals(filterMessage)) {
                        hidden++;
                        if (id != null)
                            hiddenIds.add(id);
                        if (breakIfFound)
                            break;
                    }
                } else
                    ignored++;
            }
        }

        List<String> notFoundIds = new ArrayList<>(ids);
        notFoundIds.removeAll(usedIds);
        return new FilterCheckResult(hidden, hiddenIds, ignored, notFoundIds);
    }

    public static boolean jsonMatches(String json1, String json2, boolean regex, boolean ignoreCase, int precisionLevel) {
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map1 = gson.fromJson(json1, mapType);
        Map<String, Object> map2 = gson.fromJson(json2, mapType);
        MapDifference<String, Object> difference = Maps.difference(flatten(map1), flatten(map2));

        for (ValueDifference<Object> value : difference.entriesDiffering().values()) {
            if (value.leftValue() instanceof String && value.rightValue() instanceof String) {
                if (regex && Pattern.compile((String) value.leftValue(), ignoreCase ? Pattern.CASE_INSENSITIVE : 0).matcher((String) value.rightValue()).matches())
                    continue;
                if (ignoreCase && ((String) value.leftValue()).equalsIgnoreCase((String) value.rightValue()))
                    continue;
                if (value.leftValue().equals("<ignore>"))
                    continue;
            }

            return false;
        }

        switch (precisionLevel) {
            case 3:
                return difference.entriesOnlyOnLeft().isEmpty() && difference.entriesOnlyOnRight().isEmpty();
            case 2:
                return difference.entriesOnlyOnLeft().isEmpty() && difference.entriesOnlyOnRight().entrySet().stream()
                        .noneMatch(entry -> !(entry.getValue() instanceof Boolean) || (Boolean) entry.getValue());
            case 1:
                return difference.entriesOnlyOnLeft().isEmpty();
            case 0:
                return true;
        }
        return false;
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

    public static class FilterCheckResult {

        private boolean hidden;
        private int hiddenCount;
        private List<String> hiddenIds;
        private int ignored;
        List<String> notFoundIds;

        private FilterCheckResult(int hiddenCount, List<String> hiddenIds, int ignored, List<String> notFoundIds) {
            hidden = hiddenCount > 0;
            this.hiddenCount = hiddenCount;
            this.hiddenIds = hiddenIds;
            this.ignored = ignored;
            this.notFoundIds = notFoundIds;
        }

        public boolean isHidden() {
            return hidden;
        }

        public int getHiddenCount() {
            return hiddenCount;
        }

        public List<String> getHiddenIds() {
            return hiddenIds;
        }

        public int getIgnored() {
            return ignored;
        }

        public List<String> getNotFoundIds() {
            return notFoundIds;
        }
    }
}
