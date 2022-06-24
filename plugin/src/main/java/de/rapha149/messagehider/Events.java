package de.rapha149.messagehider;

import de.rapha149.messagehider.util.Config;
import de.rapha149.messagehider.util.Config.FilterData.CommandData.CommandType;
import de.rapha149.messagehider.util.Util;
import de.rapha149.messagehider.util.Util.FilterCheckResult;
import de.rapha149.messagehider.util.Util.FilterCheckResult.FilterStatus;
import de.rapha149.messagehider.version.MHPlayer;
import de.rapha149.messagehider.version.MessageType;
import de.rapha149.messagehider.version.Text;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static de.rapha149.messagehider.util.Util.PREFIX;
import static de.rapha149.messagehider.util.Util.WRAPPER;

public class Events implements Listener {

    public Events() {
        Bukkit.getOnlinePlayers().forEach(this::addHandler);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        addHandler(player);

        if (player.isOp() && Config.get().checkForUpdates) {
            String version = Updates.getAvailableVersion();
            if (version != null) {
                player.sendMessage(PREFIX + "§6There's a new version available for this plugin: §7" + version);
                player.spigot().sendMessage(new ComponentBuilder(PREFIX + "§6You can download it from ")
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
            try {
                WRAPPER.getPipeline(player).remove("MessageHider");
                addHandler(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void removeHandlers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                WRAPPER.getPipeline(player).remove("MessageHider");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addHandler(Player player) {
        ChannelPipeline pipeline;
        try {
            pipeline = WRAPPER.getPipeline(player);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        pipeline.addAfter("packet_handler", "MessageHider", new ChannelDuplexHandler() {

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                try {
                    if (WRAPPER.getClasses().contains(msg.getClass())) {
                        Text text = WRAPPER.getText(msg);
                        String json = text.json;
                        String plain = text.plain;
                        MessageType type = WRAPPER.getMessageType(msg);

                        MHPlayer sender = WRAPPER.getSender(msg);
                        MHPlayer receiver = new MHPlayer(player.getUniqueId());

                        FilterCheckResult result = Util.checkFilters(plain, json, type, sender, receiver);
                        MessageHiderCommand.log(receiver, sender, plain, json, type, result);

                        if (!result.getCommands().isEmpty()) {
                            result.getCommands().forEach(command -> Bukkit.getScheduler().runTaskLater(MessageHider.getInstance(), () -> Bukkit.dispatchCommand(
                                    command.type == CommandType.CONSOLE ? Bukkit.getConsoleSender() : player,
                                    command.command), (int) (command.delay * 20F)));
                        }

                        if (result.getStatus() == FilterStatus.REPLACED) {
                            String replacement = Util.formatReplacementString(result.getReplacement());
                            if (replacement == null)
                                return;
                            msg = WRAPPER.replaceText(msg, replacement);
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
}
