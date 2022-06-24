package de.rapha149.messagehider.version;

import org.bukkit.Bukkit;

import java.util.UUID;

public class MHPlayer {

    private static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public final UUID uuid;
    public final String name;
    public final String representation;

    public MHPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.representation = uuid.toString();
    }

    public MHPlayer(UUID uuid) {
        this.uuid = uuid;
        this.name = uuid.equals(ZERO_UUID) ? "<console>" : Bukkit.getOfflinePlayer(uuid).getName();
        this.representation = uuid.equals(ZERO_UUID) ? "<console>" : uuid.toString();
    }
}
