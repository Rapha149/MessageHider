package de.rapha149.messagehider.util;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JsonUtil {

    private static Gson gson = new Gson();

    public static boolean matches(String json1, String json2, boolean regex, int precisionLevel) {
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map1 = gson.fromJson(json1, mapType);
        Map<String, Object> map2 = gson.fromJson(json2, mapType);
        MapDifference<String, Object> difference = Maps.difference(flatten(map1), flatten(map2));

        for (ValueDifference<Object> value : difference.entriesDiffering().values()) {
            if (value.leftValue() instanceof String && value.rightValue() instanceof String) {
                if (regex && ((String) value.rightValue()).matches((String) value.leftValue()))
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
                .flatMap(JsonUtil::flatten)
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
                    .flatMap(JsonUtil::flatten);
        }

        return Stream.of(entry);
    }
}
