package me.dave.playerhide.listeners;

import me.dave.playerhide.PlayerHide;
import me.dave.playerhide.visibility.VisibilityManager;
import me.dave.playerhide.visibility.VisibilityState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        VisibilityManager visibilityManager = PlayerHide.getInstance().getVisibilityManager();
        Player player = event.getPlayer();

        Bukkit.getOnlinePlayers().stream()
            .filter(onlinePlayer -> visibilityManager.getState(onlinePlayer.getUniqueId()) == VisibilityState.HIDDEN)
            .forEach(onlinePlayer -> onlinePlayer.hidePlayer(PlayerHide.getInstance(), player));
    }
}
