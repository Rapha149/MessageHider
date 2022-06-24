package de.rapha149.messagehider.version;

public enum MessageType {

    CHAT(0),
    SYSTEM(1),
    GAME_INFO(2),
    SAY(3),
    MSG(4),
    TEAMMSG(5),
    EMOTE(6),
    TELLRAW(7);

    private final int id;

    MessageType(int id) {
        this.id = id;
    }

    public static MessageType getById(int id) {
        for (MessageType type : values())
            if (type.id == id)
                return type;
        return null;
    }
}
