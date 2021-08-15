package de.rapha149.messagehider;

import de.rapha149.messagehider.util.ReflectionUtil;
import de.rapha149.messagehider.util.ReflectionUtil.Param;
import de.rapha149.messagehider.util.Util;
import de.rapha149.messagehider.util.Util.FilterCheckResult;
import de.rapha149.messagehider.util.Util.FilterCheckResult.FilterStatus;
import de.rapha149.messagehider.util.YamlUtil;
import de.rapha149.messagehider.util.YamlUtil.YamlData.FilterData.CommandData.CommandType;
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
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§3Click here to view the plugin on SpigotMC.").create()))
                        .append(" §6or ").reset()
                        .append("§3BukkitDev").event(new ClickEvent(ClickEvent.Action.OPEN_URL, Updates.BUKKIT_URL))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§3Click here to view the plugin on BukkitDev." +
                                                                                                "\n§7It may take a few hours until the update is approved there.").create()))
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
                    if (msg.getClass() == ReflectionUtil.getClass(true, "PacketPlayOutChat", "network.protocol.game")) {
                        boolean adventure = false;
                        String json = null;
                        String plain = null;
                        Object component = ReflectionUtil.getField(msg, "a");
                        if (component != null) {
                            json = (String) ReflectionUtil.invokeStaticMethod(
                                    ReflectionUtil.getClass(true, "IChatBaseComponent$ChatSerializer", "network.chat"),
                                    "a", new Param(ReflectionUtil.getClass(true, "IChatBaseComponent", "network.chat"), component));
                            plain = (String) ReflectionUtil.invokeMethod(component, ReflectionUtil.TO_PLAIN_TEXT);
                        } else {
                            try {
                                msg.getClass().getDeclaredField("adventure$message");
                                adventure = true;

                                component = ReflectionUtil.getField(msg, "adventure$message");
                                if(component != null) {
                                    json = (String) ReflectionUtil.invokeMethod(ReflectionUtil.invokeStaticMethod(
                                            ReflectionUtil.getClass("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer"), "gson"),
                                            "serialize", new Param(ReflectionUtil.getClass("net.kyori.adventure.text.Component"), component));
                                    plain = new TextComponent(ComponentSerializer.parse(json)).toPlainText();
                                }
                            } catch (NoSuchFieldException ignore) {
                            }
                        }
                        if (component == null) {
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
                        UUID receiver = player.getUniqueId();

                        FilterCheckResult result = Util.checkFilters(true, plain, json, sender, receiver);
                        MessageHiderCommand.log(receiver, sender, plain, json, result);

                        if (!result.getCommands().isEmpty()) {
                            result.getCommands().forEach(command -> Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Bukkit.dispatchCommand(
                                    command.getType() == CommandType.CONSOLE ? Bukkit.getConsoleSender(): player,
                                    command.getCommand()), (int) (command.getDelay() * 20F)));
                        }

                        if (result.getStatus() == FilterStatus.REPLACED) {
                            ReflectionUtil.setField(msg, "a", null);
                            if (adventure)
                                ReflectionUtil.setField(msg, "adventure$message", null);
                            BaseComponent[] replacement = Util.formatReplacementString(result.getReplacement());
                            if (replacement != null)
                                ReflectionUtil.setField(msg, "components", replacement);
                            else
                                return;
                        } else if (result.getStatus() == FilterStatus.HIDDEN)
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
        return ((Channel) ReflectionUtil.getFieldFromType(
                ReflectionUtil.getFieldFromType(
                        ReflectionUtil.getFieldFromType(
                                ReflectionUtil.invokeMethod(player,
                                        "getHandle"),
                                ReflectionUtil.getClass(true, "PlayerConnection", "server.network")),
                        ReflectionUtil.getClass(true, "NetworkManager", "network")),
                Channel.class)).pipeline();
    }
}
