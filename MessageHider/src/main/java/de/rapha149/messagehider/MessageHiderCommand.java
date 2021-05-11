package de.rapha149.messagehider;

import de.rapha149.messagehider.util.YamlUtil;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

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
        if (args.length >= 1 && args[0].toLowerCase().matches("log|create|reload")) {
            switch (args[0].toLowerCase()) {
                case "log":
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.hasPermission("messagehider.log")) {
                            if (args.length >= 2 && args[1].toLowerCase().matches("start|stop")) {
                                UUID uuid = player.getUniqueId();
                                switch (args[1].toLowerCase()) {
                                    case "start":
                                        if (!logging.containsKey(uuid)) {
                                            File file = new File(Main.getInstance().getDataFolder(), "logs/" + player.getName() + ".log");
                                            if (!file.getParentFile().exists())
                                                file.getParentFile().mkdirs();
                                            if(file.exists())
                                                file.delete();

                                            logging.put(uuid, file);
                                            write(uuid, format.format(new Date()) + "\nStarted logging");
                                            player.sendMessage("§aYou are now logging messages sent to you." +
                                                    "\n§2Use §7/" + alias + " stop §2to stop logging.");
                                        } else
                                            player.sendMessage("§6You are already logging messages.");
                                        break;
                                    case "stop":
                                        if (logging.containsKey(uuid)) {
                                            player.sendMessage("§bStopped logging." +
                                                    "\n§3Logged messages can be found in §7" + logging.get(uuid).getPath() + "§3.");
                                            write(uuid, format.format(new Date()) + "\nStopped logging\n");
                                            logging.remove(uuid);
                                        } else
                                            player.sendMessage("§6You weren't logging messages.");
                                        break;
                                }
                            } else
                                player.sendMessage("§cPlease use §7/" + alias + " log <start|stop>§c.");
                        } else
                            player.sendMessage("§cYou don't have enough permissions for this.");
                    } else
                        sender.sendMessage("§cOnly possible for players.");
                    break;
                case "create":
                    if (sender.hasPermission("messagehider.create")) {
                        try {
                            YamlUtil.addFilter(new FilterData());
                            sender.sendMessage("§2An empty filter was created.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("§cAn error occurred.");
                        }
                    } else
                        sender.sendMessage("§cYou don't have enough permissions for this.");
                    break;
                case "reload":
                    if (sender.hasPermission("messagehider.reload")) {
                        try {
                            YamlUtil.load();
                            sender.sendMessage("§2The config was reloaded.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("§cAn error occurred.");
                        }
                    } else
                        sender.sendMessage("§cYou don't have enough permissions for this.");
                    break;
            }
        } else
            sender.sendMessage(YamlUtil.getPrefix() + "§cPlease use §7/" + alias + " <log|create|reload>§c.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("messagehider.log"))
                list.add("log");
            if (sender.hasPermission("messagehider.create"))
                list.add("create");
            if (sender.hasPermission("messagehider.reload"))
                list.add("reload");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("log") && sender.hasPermission("messagehider.log"))
                list.addAll(Arrays.asList("start", "stop"));
        }

        if (list != null) {
            String arg = args[args.length - 1].toLowerCase();
            List<String> completions = new ArrayList<>();
            list.stream().filter(s -> s.toLowerCase().startsWith(arg)).forEach(completions::add);
            return completions;
        }
        return Arrays.asList();
    }

    public static void log(UUID uuid, UUID sender, String plain, String json, boolean hidden) {
        write(uuid, format.format(new Date()) + (hidden ? " (Hidden)" : "") +
                (sender != null ? "\nSent from: " + (sender.equals(Main.ZERO_UUID) ? "<console>" : sender) : "") +
                "\nPlain: " + plain + "\nJSON: " + json);
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
