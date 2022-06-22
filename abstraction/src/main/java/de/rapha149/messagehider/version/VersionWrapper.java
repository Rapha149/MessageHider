package de.rapha149.messagehider.version;

import io.netty.channel.ChannelPipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface VersionWrapper {

    Class<?> getJsonSyntaxException();

    ChannelPipeline getPipeline(Player player) throws Exception;

    List<Class<?>> getClasses();

    String[] getText(Object obj) throws Exception;

    default String[] getTextFromAdventure(Object obj) {
        if(!(obj instanceof Component))
            throw new IllegalArgumentException("Object is not of type Component");

        String json = GsonComponentSerializer.gson().serialize((Component) obj);
        return new String[] {
                json,
                new TextComponent(ComponentSerializer.parse(json)).toPlainText()
        };
    }

    UUID getUUID(Object obj) throws Exception;

    Object replaceText(Object obj, BaseComponent[] text) throws Exception;
}
