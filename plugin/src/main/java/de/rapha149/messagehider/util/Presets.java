package de.rapha149.messagehider.util;

import de.rapha149.messagehider.Updates;
import de.rapha149.messagehider.util.Config.FilterData;
import de.rapha149.messagehider.util.Config.FilterData.MessageData;
import de.rapha149.messagehider.util.Config.FilterData.MessageData.JsonData;
import de.rapha149.messagehider.util.Config.FilterData.TargetsData;

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
        GAMEMODE_CHANGE = new FilterData("gamemode_change", new MessageData(loadJson("gamemode_change.json"),
                null, false, null, true, new JsonData(true, 1)),
                false, false, false, null, Arrays.asList());

        if (Updates.isBukkitVersionAboveOrEqualTo("1.16")) {
            ONLY_SELF_COMMANDS = new FilterData("only_self_commands", new MessageData(loadJson("only_self_commands.json"),
                    null, false, null, true, new JsonData(true, 1)),
                    true, false, false, null, Arrays.asList());

            CONSOLE_COMMANDS = new FilterData("console_commands", new MessageData(loadJson("console_commands.json"),
                    null, false, null, true, new JsonData(true, 1)),
                    false, false, false, null, Arrays.asList(),
                    new TargetsData(Arrays.asList(Util.ZERO_UUID.toString()), Arrays.asList(), Arrays.asList(), Arrays.asList()));
        }

        if (Updates.isBukkitVersionAboveOrEqualTo("1.13")) {
            IDLE_TIMEOUT = new FilterData("idle_timeout", new MessageData(loadJson("idle_timeout_1.13.json"),
                    null, false, null, true, new JsonData(true, 3)),
                    true, false, false, null, Arrays.asList());
        } else if (Updates.isBukkitVersionAboveOrEqualTo("1.8")) {
            IDLE_TIMEOUT = new FilterData("idle_timeout", new MessageData(loadJson("idle_timeout_1.8.json"),
                    null, false, null, true, new JsonData(true, 3)),
                    true, false, false, null, Arrays.asList());
        } else {
            IDLE_TIMEOUT = new FilterData("idle_timeout", new MessageData("commands.setidletimeout.success",
                    null, false, null, false, new JsonData(false, 0)),
                    false, false, false, null, Arrays.asList());
        }
    }

    private static String loadJson(String file) {
        return new BufferedReader(new InputStreamReader(Presets.class.getClassLoader().getResourceAsStream("presets/" + file)))
                .lines().collect(Collectors.joining("\n"));
    }

}
