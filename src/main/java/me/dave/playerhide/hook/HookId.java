package me.dave.playerhide.hook;

public enum HookId {
    PACKET_EVENTS("packet_events"),
    WORLD_GUARD("world_guard");

    private final String id;

    HookId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
