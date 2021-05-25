package de.rapha149.messagehider.util;

import de.rapha149.messagehider.Main;
import de.rapha149.messagehider.Updates;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Presets {

    public static FilterData IDLE_TIMEOUT;
    public static FilterData GAMEMODE_CHANGE;
    public static FilterData ONLY_SELF_COMMANDS;
    public static FilterData CONSOLE_COMMANDS;

    static {
        GAMEMODE_CHANGE = new FilterData("gamemode_change", true, 1, true, false,
                false, null, loadJson("gamemode_change.json"), null);

        if (Updates.isBukkitVersionAboveOrEqualTo("1.16")) {
            ONLY_SELF_COMMANDS = new FilterData("only_self_commands", true, 1, true, false,
                    true, null, loadJson("only_self_commands.json"), null);

            CONSOLE_COMMANDS = new FilterData("console_commands", true, 1, true, false,
                    false, null, loadJson("console_commands.json"), null,
                    Arrays.asList(Main.ZERO_UUID.toString()), Arrays.asList(), Arrays.asList(), Arrays.asList());
        }

        if (Updates.isBukkitVersionAboveOrEqualTo("1.13")) {
            IDLE_TIMEOUT = new FilterData("idle_timeout", true, 3, true, false,
                    false, null, loadJson("idle_timeout_1.13.json"), null);
        } else if (Updates.isBukkitVersionAboveOrEqualTo("1.8")) {
            IDLE_TIMEOUT = new FilterData("idle_timeout", true, 3, true, false,
                    false, null, loadJson("idle_timeout_1.8.json"), null);
        } else {
            IDLE_TIMEOUT = new FilterData("idle_timeout", false, 0, false, false,
                    false, null, "commands.setidletimeout.success", null);
        }
    }

    private static String loadJson(String file) {
        return new BufferedReader(new InputStreamReader(Presets.class.getClassLoader().getResourceAsStream("presets/" + file)))
                .lines().collect(Collectors.joining("\n"));
    }

}
