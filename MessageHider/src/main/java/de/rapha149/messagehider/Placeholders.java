package de.rapha149.messagehider;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders {

    private static String senderName = "";
    private static String senderUUID = "";
    private static String receiverName = "";
    private static String receiverUUID = "";
    private static String messageSentPlain = "";
    private static String messageSentJson = "";
    private static String messageReplacedPlain = "";
    private static String messageReplacedJson = "";
    private static List<String> regexGroups = Arrays.asList();

    public static List<String> replace(List<String> list, UUID sender, UUID receiver, String messageSentPlain, String messageSentJson,
                                       String messageReplacedPlain, String messageReplacedJson, List<String> regexGroups) {
        String senderName = sender != null ? (sender.equals(Main.ZERO_UUID) ? "[CONSOLE]" : Bukkit.getOfflinePlayer(sender).getName()) : "[SENDER_NAME]";
        String senderUUID = sender != null ? (sender.equals(Main.ZERO_UUID) ? "[CONSOLE]" : sender.toString()) : "[SENDER_UUID]";
        String receiverName = receiver != null ? Bukkit.getOfflinePlayer(receiver).getName() : "[RECEIVER_NAME]";
        String receiverUUID = receiver != null ? receiver.toString() : "[RECEIVER_UUID]";
        if (Main.getInstance().placeholderAPISupport) {
            Placeholders.senderName = senderName;
            Placeholders.senderUUID = senderUUID;
            Placeholders.receiverName = receiverName;
            Placeholders.receiverUUID = receiverUUID;
            Placeholders.messageSentPlain = messageSentPlain;
            Placeholders.messageSentJson = messageSentJson;
            Placeholders.messageReplacedPlain = messageReplacedPlain;
            Placeholders.messageReplacedJson = messageReplacedJson;
            Placeholders.regexGroups = regexGroups;
            List<String> result = PlaceholderAPI.setPlaceholders(receiver != null ? Bukkit.getOfflinePlayer(receiver) : null, list);

            Placeholders.senderName = "";
            Placeholders.senderUUID = "";
            Placeholders.receiverName = "";
            Placeholders.receiverUUID = "";
            Placeholders.messageSentPlain = "";
            Placeholders.messageSentJson = "";
            Placeholders.messageReplacedPlain = "";
            Placeholders.messageReplacedJson = "";
            Placeholders.regexGroups = Arrays.asList();
            return result;
        } else {
            List<String> result = new ArrayList<>();
            list.forEach(str -> result.add(str.replace("%mh_player_sender_name%", senderName)
                    .replace("%mh_player_sender_uuid%", senderUUID)
                    .replace("%mh_player_receiver_name%", receiverName)
                    .replace("%mh_player_receiver_uuid%", receiverUUID)
                    .replace("%mh_message_sent_plain%", messageSentPlain)
                    .replace("%mh_message_sent_json%", messageSentJson)
                    .replace("%mh_message_replaced_plain%", messageReplacedPlain)
                    .replace("%mh_message_replaced_json%", messageReplacedJson)));
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
            public String onRequest(OfflinePlayer player, String params) {
                if (params.equals("player_sender_name"))
                    return senderName;
                if (params.equals("player_sender_uuid"))
                    return senderUUID;
                if (params.equals("player_receiver_name"))
                    return receiverName;
                if (params.equals("player_receiver_uuid"))
                    return receiverUUID;
                if (params.equals("message_sent_plain"))
                    return messageSentPlain;
                if (params.equals("message_sent_json"))
                    return messageSentJson;
                if (params.equals("message_replaced_plain"))
                    return messageReplacedPlain;
                if (params.equals("message_replaced_json"))
                    return messageReplacedJson;
                Matcher matcher = Pattern.compile("regex_(\\d+)").matcher(params);
                if(matcher.matches()) {
                    int group = Integer.parseInt(matcher.group(1));
                    return group < regexGroups.size() ? regexGroups.get(group) : "";
                }
                return null;
            }
        }.register();
    }
}
