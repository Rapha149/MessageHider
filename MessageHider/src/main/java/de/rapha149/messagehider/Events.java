package de.rapha149.messagehider;

import de.rapha149.messagehider.util.JsonUtil;
import de.rapha149.messagehider.util.ReflectionUtil;
import de.rapha149.messagehider.util.ReflectionUtil.Param;
import de.rapha149.messagehider.util.YamlUtil;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData;
import io.netty.channel.*;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Pattern;

public class Events implements Listener {

    public Events() {
        Bukkit.getOnlinePlayers().forEach(this::addHandler);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        addHandler(player);

        if (player.isOp() && YamlUtil.shouldCheckForUpdates()) {
            String version = Updates.getAvailableVersion();
            if (version != null) {
                player.sendMessage(YamlUtil.getPrefix() + "§6There's a new version available for this plugin: §7" + version);
                player.spigot().sendMessage(new ComponentBuilder(YamlUtil.getPrefix() + "§6You can download it from ")
                        .append("§3SpigotMC").event(new ClickEvent(ClickEvent.Action.OPEN_URL, Updates.SPIGOT_URL))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§3Click here to view the plugin on SpigotMC").create()))
                        .append(" §6or ").reset()
                        .append("§3BukkitDev")/*.event(new ClickEvent(ClickEvent.Action.OPEN_URL, Updates.BUKKIT_URL))*/
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Not available yet").create()))
                        .append("§6.").create());
            }
        }
    }

    public void reloadHandlers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            getPipeline(player).remove("MessageHider");
            addHandler(player);
        });
    }

    public void removeHandlers() {
        Bukkit.getOnlinePlayers().forEach(player -> getPipeline(player).remove("MessageHider"));
    }

    private void addHandler(Player player) {
        getPipeline(player).addAfter("packet_handler", "MessageHider", new ChannelDuplexHandler() {

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                try {
                    if (msg.getClass() == ReflectionUtil.getClass(true, "PacketPlayOutChat")) {
                        Object component = ReflectionUtil.getField(msg, "a");
                        String json;
                        String plain;
                        if (component != null) {
                            json = (String) ReflectionUtil.invokeStaticMethod(true, "IChatBaseComponent$ChatSerializer",
                                    "a", new Param(true, "IChatBaseComponent", component));
                            plain = (String) ReflectionUtil.invokeMethod(component, ReflectionUtil.TO_PLAIN_TEXT);
                        } else {
                            BaseComponent[] components = (BaseComponent[]) ReflectionUtil.getField(msg, "components");
                            json = ComponentSerializer.toString(components);
                            plain = new TextComponent(components).toPlainText();
                        }

                        UUID sender = null;
                        for (Field field : msg.getClass().getDeclaredFields()) {
                            if (field.getType() == UUID.class) {
                                field.setAccessible(true);
                                sender = (UUID) field.get(msg);
                                break;
                            }
                        }

                        boolean hidden = false;
                        UUID receiver = player.getUniqueId();
                        for (FilterData filter : YamlUtil.getFilters()) {
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

                            boolean regex = filter.isRegex();
                            boolean ignoreCase = filter.isIgnoreCase();
                            String filterMessage = filter.getMessage();
                            if (filter.isJson()) {
                                if (JsonUtil.matches(filterMessage, json, regex, ignoreCase, filter.getJsonPrecisionLevel())) {
                                    hidden = true;
                                    break;
                                }
                            } else if (regex) {
                                if (Pattern.compile(filterMessage, ignoreCase ? Pattern.CASE_INSENSITIVE : 0).matcher(plain).matches()) {
                                    hidden = true;
                                    break;
                                }
                            } else if (ignoreCase ? plain.equalsIgnoreCase(filterMessage) : plain.equals(filterMessage)) {
                                hidden = true;
                                break;
                            }
                        }

                        MessageHiderCommand.log(receiver, sender, plain, json, hidden);
                        if (hidden)
                            return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                super.write(ctx, msg, promise);
            }
        });
    }

    private ChannelPipeline getPipeline(Player player) {
        return ((Channel) ReflectionUtil.getField(
                ReflectionUtil.getField(
                        ReflectionUtil.getField(
                                ReflectionUtil.invokeMethod(player,
                                        "getHandle"),
                                "playerConnection"),
                        "networkManager"),
                "channel")).pipeline();
    }
}
