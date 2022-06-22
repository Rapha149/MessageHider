package de.rapha149.messagehider.version;

import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Wrapper1_18_R1 implements VersionWrapper {

    @Override
    public Class<?> getJsonSyntaxException() {
        return JsonSyntaxException.class;
    }

    @Override
    public ChannelPipeline getPipeline(Player player) {
        return ((CraftPlayer) player).getHandle().b.a.k.pipeline();
    }

    @Override
    public List<Class<?>> getClasses() {
        return Collections.singletonList(PacketPlayOutChat.class);
    }

    @Override
    public String[] getText(Object obj) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        IChatBaseComponent component = packet.b();
        if (component != null) {
            return new String[]{
                    ChatSerializer.a(component),
                    component.getString()
            };
        }

        try {
            Object adventure = packet.getClass().getDeclaredField("adventure$message").get(packet);
            if (adventure != null)
                return getTextFromAdventure(adventure);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        }

        return new String[]{
                ComponentSerializer.toString(packet.components),
                new TextComponent(packet.components).toPlainText()
        };
    }

    @Override
    public UUID getUUID(Object obj) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        return packet.d();
    }

    @Override
    public Object replaceText(Object obj, BaseComponent[] text) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        PacketPlayOutChat newPacket = new PacketPlayOutChat(null, packet.c(), packet.d());
        newPacket.components = text;
        return newPacket;
    }
}
