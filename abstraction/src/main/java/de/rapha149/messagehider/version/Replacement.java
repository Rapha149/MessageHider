package de.rapha149.messagehider.version;

import java.util.Optional;

public class Replacement {

    public boolean enabled;
    public String text;
    public MessageType type;
    public boolean systemMessage;

    public Replacement() {
        enabled = false;
        text = null;
        type = null;
        systemMessage = false;
    }

    public Replacement(boolean enabled, String text, MessageType type, boolean systemMessage) {
        this.enabled = enabled;
        this.text = text;
        this.type = type;
        this.systemMessage = systemMessage;
    }

    public Replacement withText(String text, String fallback) {
        return new Replacement(enabled, Optional.ofNullable(text).orElse(fallback), type, systemMessage);
    }

    public Replacement withFallback(String fallback) {
        if (text == null)
            return new Replacement(enabled, fallback, type, systemMessage);
        return this;
    }
}