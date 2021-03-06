package de.rapha149.messagehider.version;

import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class Wrapper1_18_R1 implements VersionWrapper {

    private static final Field ADVENTURE_FIELD;

    static {
        Field adventureField;
        try {
            //noinspection JavaReflectionMemberAccess
            adventureField = PacketPlayOutChat.class.getDeclaredField("adventure$message");
        } catch (NoSuchFieldException e) {
            adventureField = null;
        }
        ADVENTURE_FIELD = adventureField;
    }

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
    public Text getText(Object obj) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        if (ADVENTURE_FIELD != null) {
            try {
                Object adventure = ADVENTURE_FIELD.get(obj);
                if (adventure != null)
                    return getTextFromAdventure(adventure);
            } catch (IllegalAccessException ignore) {
            }
        }

        IChatBaseComponent component = packet.b();
        if (component != null) {
            return new Text(ChatSerializer.a(component), component.getString());
        }

        return new Text(ComponentSerializer.toString(packet.components), new TextComponent(packet.components).toPlainText());
    }

    @Override
    public MHPlayer getSender(Object obj) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        return new MHPlayer(packet.d());
    }

    @Override
    public MessageType getMessageType(Object obj) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        return MessageType.getById(packet.c().a());
    }

    @Override
    public Object replaceText(Object obj, Replacement replacement) {
        if (!(obj instanceof PacketPlayOutChat packet))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        ChatMessageType type = replacement.type != null ? ChatMessageType.a((byte) replacement.type.id) : packet.c();
        return new PacketPlayOutChat(ChatSerializer.a(replacement.text), type, packet.d());
    }
}
