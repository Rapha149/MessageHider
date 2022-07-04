package de.rapha149.messagehider.version;

import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class Wrapper1_19_R1 implements VersionWrapper {

    private static final Field PLAYER_ADVENTURE_FIELD, SYSTEM_ADVENTURE_FIELD;

    static {
        Field adventureField;
        try {
            //noinspection JavaReflectionMemberAccess
            adventureField = ClientboundPlayerChatPacket.class.getDeclaredField("adventure$message");
            adventureField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            adventureField = null;
        }
        PLAYER_ADVENTURE_FIELD = adventureField;

        try {
            //noinspection JavaReflectionMemberAccess
            adventureField = ClientboundSystemChatPacket.class.getDeclaredField("adventure$content");
            adventureField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            adventureField = null;
        }
        SYSTEM_ADVENTURE_FIELD = adventureField;
    }

    @Override
    public Class<?> getJsonSyntaxException() {
        return JsonSyntaxException.class;
    }

    @Override
    public ChannelPipeline getPipeline(Player player) {
        return ((CraftPlayer) player).getHandle().b.b.m.pipeline();
    }

    @Override
    public List<Class<?>> getClasses() {
        return List.of(ClientboundPlayerChatPacket.class, ClientboundSystemChatPacket.class);
    }

    @Override
    public Text getText(Object obj) {
        if (obj instanceof ClientboundPlayerChatPacket packet) {
            if (PLAYER_ADVENTURE_FIELD != null) {
                try {
                    Object adventure = PLAYER_ADVENTURE_FIELD.get(packet);
                    if (adventure != null)
                        return getTextFromAdventure(adventure);
                } catch (IllegalAccessException ignore) {
                }
            }

            IChatBaseComponent component = packet.d().orElse(packet.c());
            return new Text(ChatSerializer.a(component), component.getString());
        }

        if (obj instanceof ClientboundSystemChatPacket packet) {
            if (SYSTEM_ADVENTURE_FIELD != null) {
                try {
                    Object adventure = SYSTEM_ADVENTURE_FIELD.get(packet);
                    if (adventure != null)
                        return getTextFromAdventure(adventure);
                } catch (IllegalAccessException ignore) {
                }
            }

            return new Text(packet.content(), new TextComponent(ComponentSerializer.parse(packet.content())).toPlainText());
        }

        throw new IllegalArgumentException("Packet is not of type ClientboundPlayerChatPacket or ClientboundSystemChatPacket");
    }

    @Override
    public MHPlayer getSender(Object obj) {
        if (obj instanceof ClientboundPlayerChatPacket packet)
            return new MHPlayer(packet.f().a(), packet.f().b().getString());

        if (obj instanceof ClientboundSystemChatPacket)
            return null;

        throw new IllegalArgumentException("Packet is not of type ClientboundPlayerChatPacket or ClientboundSystemChatPacket");
    }

    @Override
    public MessageType getMessageType(Object obj) {
        if (obj instanceof ClientboundPlayerChatPacket packet)
            return MessageType.getById(packet.e());

        if (obj instanceof ClientboundSystemChatPacket packet)
            return MessageType.getById(packet.c());

        throw new IllegalArgumentException("Packet is not of type ClientboundPlayerChatPacket or ClientboundSystemChatPacket");
    }

    @Override
    public Object replaceText(Object obj, Replacement replacement) {
        Optional<Integer> type = replacement.type != null ? Optional.of(replacement.type.id) : Optional.empty();

        if (obj instanceof ClientboundPlayerChatPacket packet) {
            if (replacement.systemMessage)
                return new ClientboundSystemChatPacket(ComponentSerializer.parse(replacement.text), type.orElse(packet.e()));

            return new ClientboundPlayerChatPacket(packet.c(), Optional.of(ChatSerializer.a(replacement.text)), type.orElse(packet.e()), packet.f(), packet.g(), packet.h());
        }

        if (obj instanceof ClientboundSystemChatPacket packet)
            return new ClientboundSystemChatPacket(ComponentSerializer.parse(replacement.text), type.orElse(packet.c()));

        throw new IllegalArgumentException("Packet is not of type ClientboundPlayerChatPacket or ClientboundSystemChatPacket");
    }
}
