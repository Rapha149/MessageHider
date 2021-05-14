package de.rapha149.messagehider.util;

import de.rapha149.messagehider.Updates;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Presets {

    public static FilterData IDLE_TIMEOUT;
    public static FilterData GAMEMODE_CHANGE;
    public static FilterData ONLY_SELF_COMMANDS;

    static {
        GAMEMODE_CHANGE = new FilterData(true, 1, true, false, false,
                loadJson("gamemode_change.json"));

        if (Updates.isBukkitVersionAboveOrEqualTo("1.16")) {
            ONLY_SELF_COMMANDS = new FilterData(true, 1, true, false, true,
                    loadJson("only_self_commands.json"));
        }

        if (Updates.isBukkitVersionAboveOrEqualTo("1.13")) {
            IDLE_TIMEOUT = new FilterData(true, 3, true, false, false,
                    loadJson("idle_timeout_1.13.json"));
        } else if (Updates.isBukkitVersionAboveOrEqualTo("1.8")) {
            IDLE_TIMEOUT = new FilterData(true, 3, true, false, false,
                    loadJson("idle_timeout_1.8.json"));
        } else {
            IDLE_TIMEOUT = new FilterData(false, 0, false, false, false,
                    "commands.setidletimeout.success");
        }
    }

    private static String loadJson(String file) {
        return new BufferedReader(new InputStreamReader(Presets.class.getClassLoader().getResourceAsStream("presets/" + file)))
                .lines().collect(Collectors.joining("\n"));
    }

}
