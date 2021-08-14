package de.rapha149.messagehider;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Placeholders {

    private static Map<Player, String[]> messages = new HashMap<>();

    public static List<String> replace(Player player, List<String> list, String messageSent, String messageReplaced) {
        if (Main.getInstance().placeholderAPISupport) {
            messages.put(player, new String[]{messageSent, messageReplaced});
            List<String> result = PlaceholderAPI.setPlaceholders(player, list);
            messages.remove(player);
            return result;
        } else {
            List<String> result = new ArrayList<>();
            list.forEach(str -> result.add(str.replace("%player_name%", player.getName())
                    .replace("%player_uuid%", player.getUniqueId().toString())
                    .replace("%mh_message_sent%", messageSent)
                    .replace("%mh_message_replaced%", messageReplaced)));
            return result;
        }
    }

    public static void registerPlaceholderExpansion() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "mh";
            }

            @Override
            public @NotNull String getAuthor() {
                return "Rapha149";
            }

            @Override
            public @NotNull String getVersion() {
                return Main.getInstance().getDescription().getVersion();
            }

            @Override
            public boolean persist() {
                return true;
            }

            @Override
            public boolean canRegister() {
                return true;
            }

            @Override
            public String onPlaceholderRequest(Player player, String params) {
                if (params.matches("message_(sent|replaced)")) {
                    if (messages.containsKey(player))
                        return messages.get(player)[params.matches("message_sent") ? 0 : 1];
                    else
                        return "";
                }
                return null;
            }
        }.register();
    }
}
