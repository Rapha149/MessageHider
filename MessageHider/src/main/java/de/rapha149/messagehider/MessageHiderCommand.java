package de.rapha149.messagehider;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.rapha149.messagehider.util.Util;
import de.rapha149.messagehider.util.Util.FilterCheckResult;
import de.rapha149.messagehider.util.Util.FilterCheckResult.FilterStatus;
import de.rapha149.messagehider.util.YamlUtil;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MessageHiderCommand implements CommandExecutor, TabCompleter {

    private static final SimpleDateFormat format = new SimpleDateFormat("[dd.MM.yy HH:mm:ss]");
    private static Map<UUID, File> logging;

    public MessageHiderCommand(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
        logging = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 1 && args[0].toLowerCase().matches("reload|log|create|check")) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("messagehider.reload")) {
                        try {
                            YamlUtil.load();
                            sender.sendMessage(YamlUtil.getPrefix() + "§2The config was reloaded.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage(YamlUtil.getPrefix() + "§cAn error occurred.");
                        }
                    } else
                        sender.sendMessage(YamlUtil.getPrefix() + "§cYou don't have enough permissions for this.");
                    break;
                case "log":
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.hasPermission("messagehider.log")) {
                            if (args.length >= 2 && args[1].toLowerCase().matches("start|stop")) {
                                UUID uuid = player.getUniqueId();
                                switch (args[1].toLowerCase()) {
                                    case "start":
                                        if (!logging.containsKey(uuid)) {
                                            File file = new File(Main.getInstance().getDataFolder(), "logs/" + player.getName() +
                                                    "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".log");
                                            if (!file.getParentFile().exists())
                                                file.getParentFile().mkdirs();

                                            logging.put(uuid, file);
                                            write(uuid, format.format(new Date()) + "\nStarted logging");
                                            player.sendMessage(YamlUtil.getPrefix() + "§aYou are now logging messages sent to you.");
                                            player.spigot().sendMessage(new ComponentBuilder("§2Use ")
                                                    .append("§7/mh log stop").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mh log stop"))
                                                    .append(" §2to stop logging.").reset().create());
                                        } else
                                            player.sendMessage(YamlUtil.getPrefix() + "§6You are already logging messages.");
                                        break;
                                    case "stop":
                                        if (logging.containsKey(uuid)) {
                                            player.sendMessage(YamlUtil.getPrefix() + "§bStopped logging." +
                                                    "\n§3Logged messages are located in §7" + logging.get(uuid).getParentFile().getPath() + "§3.");
                                            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                                write(uuid, format.format(new Date()) + "\nStopped logging\n");
                                                logging.remove(uuid);
                                            });
                                        } else
                                            player.sendMessage(YamlUtil.getPrefix() + "§6You weren't logging messages.");
                                        break;
                                }
                            } else
                                player.sendMessage(YamlUtil.getPrefix() + "§cPlease use §7/" + alias + " log <start|stop>§c.");
                        } else
                            player.sendMessage(YamlUtil.getPrefix() + "§cYou don't have enough permissions for this.");
                    } else
                        sender.sendMessage(YamlUtil.getPrefix() + "§cOnly possible for players.");
                    break;
                case "create":
                    if (sender.hasPermission("messagehider.create")) {
                        try {
                            YamlUtil.addFilter(new FilterData());
                            sender.sendMessage(YamlUtil.getPrefix() + "§2An empty filter was created.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage(YamlUtil.getPrefix() + "§cAn error occurred.");
                        }
                    } else
                        sender.sendMessage(YamlUtil.getPrefix() + "§cYou don't have enough permissions for this.");
                    break;
                case "check":
                    if (sender.hasPermission("messagehider.check")) {
                        if (args.length >= 4 && args[1].toLowerCase().matches("json|plain")) {
                            boolean json = args[1].equalsIgnoreCase("json");
                            String[] filterIds;
                            if (args[2].equals("-"))
                                filterIds = new String[0];
                            else
                                filterIds = args[2].split(",");
                            String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                            if (json) {
                                try {
                                    if (!new JsonParser().parse(message).isJsonObject())
                                        throw new JsonParseException("");
                                } catch (JsonParseException e) {
                                    sender.sendMessage(YamlUtil.getPrefix() + "§cYour json is not valid.");
                                    return true;
                                }
                            }

                            FilterCheckResult result = Util.checkFilters(false, json ? null : message, json ? message : null, null, null, filterIds);
                            StringBuilder sb = new StringBuilder(YamlUtil.getPrefix() + "§2Result:");
                            if (!result.getNotFoundIds().isEmpty())
                                sb.append("\n§7  - §6These filter ids were not found: §e" + String.join("§8, §e", result.getNotFoundIds()));
                            if (result.getIgnored() > 0)
                                sb.append("\n§7  - §e" + result.getIgnored() + " §6filter" + (result.getIgnored() == 1 ? " was" : "s were") + " ignored because they were in " + (json ? "plain" : "json") + " text.");

                            sb.append("\n§7  - §bThe message would ");
                            if (result.getStatus() == FilterStatus.HIDDEN)
                                sb.append("be §4hidden§7.");
                            else if (result.getStatus() == FilterStatus.REPLACED)
                                sb.append("be §ereplaced§7.");
                            else
                                sb.append("remain §anormal§7.");

                            if (!result.getHiddenIds().isEmpty())
                                sb.append("\n§7  - §bThe filters that would have cancelled the message: §3" + String.join("§8, §3", result.getHiddenIds()));
                            else
                                sb.append("\n§7  - §bNo filters with ids cancelled the message.");
                            sb.append("\n§7  - §3" + result.getHiddenCount() + " §btotal filter" + (result.getHiddenCount() == 1 ? "" : "s") +
                                    " cancelled the message. (Including filters without ids)");
                            if (result.getReplacement() != null)
                                sb.append("\n§7  - §bThe message would be replaced by:" +
                                        (!(sender instanceof Player) ? "\n§f    " + result.getReplacement() : ""));
                            sender.sendMessage(sb.toString());

                            if (result.getReplacement() != null && sender instanceof Player) {
                                BaseComponent[] replacement = Util.formatReplacementString(result.getReplacement());
                                if (replacement != null)
                                    sender.spigot().sendMessage(replacement);
                            }
                        } else
                            sender.sendMessage(YamlUtil.getPrefix() + "§cPlease use §7/" + alias + " check <json|plain> <Filter ids> <Message>§c.");
                    } else
                        sender.sendMessage(YamlUtil.getPrefix() + "§cYou don't have enough permissions for this.");
                    break;
            }
        } else
            sender.sendMessage(YamlUtil.getPrefix() + "§cPlease use §7/" + alias + " <reload|log|create|check>§c.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("messagehider.reload"))
                list.add("reload");
            if (sender.hasPermission("messagehider.log"))
                list.add("log");
            if (sender.hasPermission("messagehider.create"))
                list.add("create");
            if (sender.hasPermission("messagehider.check"))
                list.add("check");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("log") && sender.hasPermission("messagehider.log"))
                list.addAll(Arrays.asList("start", "stop"));
            if (args[0].equalsIgnoreCase("check") && sender.hasPermission("messagehider.check"))
                list.addAll(Arrays.asList("json", "plain"));
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("check") && sender.hasPermission("messagehider.check")) {
                if (args[2].isEmpty() || args[2].equals("-"))
                    list.add("-");

                List<String> ids = YamlUtil.getFilters().stream().map(FilterData::getId).filter(Objects::nonNull).collect(Collectors.toList());
                int index = args[2].lastIndexOf(',');
                if (ids.contains(args[2].substring(index + 1)))
                    list.add(args[2] + ",");
                else {
                    String input = args[2].substring(0, index == -1 ? 0 : index);
                    YamlUtil.getFilters().stream().map(FilterData::getId)
                            .filter(Objects::nonNull).map(id -> input + (input.isEmpty() ? "" : ",") + id).forEach(list::add);
                }
            }
        }

        if (list != null) {
            String arg = args[args.length - 1].toLowerCase();
            List<String> completions = new ArrayList<>();
            list.stream().filter(s -> s.toLowerCase().startsWith(arg)).forEach(completions::add);
            return completions;
        }
        return Arrays.asList();
    }

    public static void log(UUID uuid, UUID sender, String plain, String json, FilterCheckResult result) {
        StringBuilder sb = new StringBuilder(format.format(new Date()));
        if (result.getStatus() == FilterStatus.HIDDEN)
            sb.append(" (Hidden)");
        if (result.getStatus() == FilterStatus.REPLACED)
            sb.append(" (Replaced)");
        if (sender != null)
            sb.append("\nSent from: " + (sender.equals(Main.ZERO_UUID) ? "<console>" : sender));
        sb.append("\nPlain: " + plain + "\nJSON: " + json);
        if (result.getReplacement() != null)
            sb.append("\nReplaced by: " + result.getReplacement());

        write(uuid, sb.toString());
    }

    private static void write(UUID uuid, String str) {
        if (logging.containsKey(uuid)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(logging.get(uuid), true))) {
                writer.write(str + "\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
