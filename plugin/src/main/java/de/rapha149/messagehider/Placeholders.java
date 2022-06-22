package de.rapha149.messagehider;

import de.rapha149.messagehider.util.Config.FilterData.CommandData;
import de.rapha149.messagehider.util.Util;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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

    public static void replace(List<CommandData> list, UUID sender, UUID receiver, String messageSentPlain, String messageSentJson,
                               String messageReplacedPlain, String messageReplacedJson, List<String> regexGroups) {
        String senderName = sender != null ? (sender.equals(Util.ZERO_UUID) ? "[CONSOLE]" : Bukkit.getOfflinePlayer(sender).getName()) : "[SENDER_NAME]";
        String senderUUID = sender != null ? (sender.equals(Util.ZERO_UUID) ? "[CONSOLE]" : sender.toString()) : "[SENDER_UUID]";
        String receiverName = receiver != null ? Bukkit.getOfflinePlayer(receiver).getName() : "[RECEIVER_NAME]";
        String receiverUUID = receiver != null ? receiver.toString() : "[RECEIVER_UUID]";
        if (MessageHider.getInstance().placeholderAPISupport) {
            Placeholders.senderName = senderName;
            Placeholders.senderUUID = senderUUID;
            Placeholders.receiverName = receiverName;
            Placeholders.receiverUUID = receiverUUID;
            Placeholders.messageSentPlain = messageSentPlain != null ? messageSentPlain : "[SENT_PLAIN_MESSAGE]";
            Placeholders.messageSentJson = messageSentJson != null ? messageSentJson : "[SENT_JSON_MESSAGE]";
            Placeholders.messageReplacedPlain = messageReplacedPlain;
            Placeholders.messageReplacedJson = messageReplacedJson;
            Placeholders.regexGroups = regexGroups;

            OfflinePlayer player = receiver != null ? Bukkit.getOfflinePlayer(receiver) : null;
            list.forEach(command -> command.command = PlaceholderAPI.setPlaceholders(player, command.command));

            Placeholders.senderName = "";
            Placeholders.senderUUID = "";
            Placeholders.receiverName = "";
            Placeholders.receiverUUID = "";
            Placeholders.messageSentPlain = "";
            Placeholders.messageSentJson = "";
            Placeholders.messageReplacedPlain = "";
            Placeholders.messageReplacedJson = "";
            Placeholders.regexGroups = Arrays.asList();
        } else {
            list.forEach(command -> command.command = (command.command
                    .replace("%mh_player_sender_name%", senderName)
                    .replace("%mh_player_sender_uuid%", senderUUID)
                    .replace("%mh_player_receiver_name%", receiverName)
                    .replace("%mh_player_receiver_uuid%", receiverUUID)
                    .replace("%mh_message_sent_plain%", messageSentPlain)
                    .replace("%mh_message_sent_json%", messageSentJson)
                    .replace("%mh_message_replaced_plain%", messageReplacedPlain)
                    .replace("%mh_message_replaced_json%", messageReplacedJson)));
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
                return MessageHider.getInstance().getDescription().getVersion();
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
