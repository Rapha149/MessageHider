package de.rapha149.messagehider.version;

import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Wrapper1_9_R2 implements VersionWrapper {

    private static final Field ADVENTURE_FIELD, COMPONENT_FIELD, TYPE_FIELD;

    static {
        Field adventureField;
        try {
            //noinspection JavaReflectionMemberAccess
            adventureField = PacketPlayOutChat.class.getDeclaredField("adventure$message");
        } catch (NoSuchFieldException e) {
            adventureField = null;
        }
        ADVENTURE_FIELD = adventureField;

        try {
            COMPONENT_FIELD = PacketPlayOutChat.class.getDeclaredField("a");
            COMPONENT_FIELD.setAccessible(true);
            TYPE_FIELD = PacketPlayOutChat.class.getDeclaredField("b");
            TYPE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getJsonSyntaxException() {
        return JsonSyntaxException.class;
    }

    @Override
    public ChannelPipeline getPipeline(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
    }

    @Override
    public List<Class<?>> getClasses() {
        return Collections.singletonList(PacketPlayOutChat.class);
    }

    @Override
    public String[] getText(Object obj) throws IllegalAccessException {
        if (!(obj instanceof PacketPlayOutChat))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        PacketPlayOutChat packet = (PacketPlayOutChat) obj;
        IChatBaseComponent component = (IChatBaseComponent) COMPONENT_FIELD.get(packet);
        if (component != null) {
            return new String[]{
                    ChatSerializer.a(component),
                    component.toPlainText()
            };
        }

        if (ADVENTURE_FIELD != null) {
            try {
                Object adventure = ADVENTURE_FIELD.get(packet);
                if (adventure != null)
                    return getTextFromAdventure(adventure);
            } catch (IllegalAccessException ignore) {
            }
        }

        return new String[]{
                ComponentSerializer.toString(packet.components),
                new TextComponent(packet.components).toPlainText()
        };
    }

    @Override
    public UUID getUUID(Object obj) {
        return null;
    }

    @Override
    public Object replaceText(Object obj, BaseComponent[] text) throws IllegalAccessException {
        if (!(obj instanceof PacketPlayOutChat))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        PacketPlayOutChat newPacket = new PacketPlayOutChat(null, TYPE_FIELD.getByte(obj));
        newPacket.components = text;
        return newPacket;
    }
}
