package me.dave.playerhide.visibility;

import me.dave.playerhide.PlayerHide;
import me.dave.playerhide.hook.HookId;
import me.dave.playerhide.hook.PacketEventsHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class VisibilityManager {
    private final HashMap<UUID, VisibilityState> visibilityMap;

    public VisibilityManager() {
        visibilityMap = new HashMap<>();
    }

    public VisibilityState getState(UUID uuid) {
        return visibilityMap.getOrDefault(uuid, VisibilityState.SHOWN);
    }

    public void changeState(Player player, VisibilityState newState) {
        UUID uuid = player.getUniqueId();
        VisibilityState oldState = visibilityMap.getOrDefault(uuid, VisibilityState.SHOWN);
        visibilityMap.put(uuid, newState);
        if (newState != oldState) {
            switch (newState) {
                case SHOWN -> {
                    if (oldState.equals(VisibilityState.HIDDEN)) {
                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                            if (onlinePlayer != player) {
                                onlinePlayer.showPlayer(PlayerHide.getInstance(), player);
                            }
                        });
                    } else if (oldState.equals(VisibilityState.BOOTS_ONLY)) {
                        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                        onlinePlayers.remove(player);

                        PlayerHide.getInstance().getHook(HookId.PACKET_EVENTS.toString()).ifPresent(hook -> {
                            PacketEventsHook packetEventsHook = ((PacketEventsHook) hook);
                            packetEventsHook.syncPlayerFlags(onlinePlayers, player);
                            packetEventsHook.syncEquipmentPackets(onlinePlayers, player);
                        });
                    }
                }
                case BOOTS_ONLY -> {
                    if (oldState.equals(VisibilityState.HIDDEN)) {
                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                            if (onlinePlayer != player) {
                                onlinePlayer.showPlayer(PlayerHide.getInstance(), player);
                            }
                        });
                    }

                    Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                    onlinePlayers.remove(player);

                    PlayerHide.getInstance().getHook(HookId.PACKET_EVENTS.toString()).ifPresent(hook -> {
                        PacketEventsHook packetEventsHook = ((PacketEventsHook) hook);
                        packetEventsHook.setInvisible(onlinePlayers, player);
                        packetEventsHook.showBootsOnly(onlinePlayers, player);
                    });
                }
                case HIDDEN -> {
                    if (oldState.equals(VisibilityState.BOOTS_ONLY)) {
                        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                        onlinePlayers.remove(player);

                        PlayerHide.getInstance().getHook(HookId.PACKET_EVENTS.toString()).ifPresent(hook -> {
                            PacketEventsHook packetEventsHook = ((PacketEventsHook) hook);
                            packetEventsHook.syncPlayerFlags(onlinePlayers, player);
                            packetEventsHook.syncEquipmentPackets(onlinePlayers, player);
                        });
                    }

                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        if (onlinePlayer != player) {
                            onlinePlayer.hidePlayer(PlayerHide.getInstance(), player);
                        }
                    });
                }
            }
        }
    }
}
