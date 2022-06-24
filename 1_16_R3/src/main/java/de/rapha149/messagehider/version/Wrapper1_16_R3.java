package de.rapha149.messagehider.version;

import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Wrapper1_16_R3 implements VersionWrapper {

    private static final Field ADVENTURE_FIELD, COMPONENT_FIELD, UUID_FIELD;

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
            UUID_FIELD = PacketPlayOutChat.class.getDeclaredField("c");
            UUID_FIELD.setAccessible(true);
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
    public Text getText(Object obj) throws IllegalAccessException {
        if (!(obj instanceof PacketPlayOutChat))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        if (ADVENTURE_FIELD != null) {
            try {
                Object adventure = ADVENTURE_FIELD.get(obj);
                if (adventure != null)
                    return getTextFromAdventure(adventure);
            } catch (IllegalAccessException ignore) {
            }
        }

        PacketPlayOutChat packet = (PacketPlayOutChat) obj;
        IChatBaseComponent component = (IChatBaseComponent) COMPONENT_FIELD.get(packet);
        if (component != null)
            return new Text(ChatSerializer.a(component), component.getString());

        return new Text(ComponentSerializer.toString(packet.components), new TextComponent(packet.components).toPlainText());
    }

    @Override
    public MHPlayer getSender(Object obj) throws IllegalAccessException {
        if (!(obj instanceof PacketPlayOutChat))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        return new MHPlayer((UUID) UUID_FIELD.get(obj));
    }

    @Override
    public Object replaceText(Object obj, String json) throws IllegalAccessException {
        if (!(obj instanceof PacketPlayOutChat))
            throw new IllegalArgumentException("Packet is not of type PacketPlayOutChat");

        PacketPlayOutChat packet = (PacketPlayOutChat) obj;
        return new PacketPlayOutChat(ChatSerializer.a(json), packet.d(), (UUID) UUID_FIELD.get(packet));
    }
}
