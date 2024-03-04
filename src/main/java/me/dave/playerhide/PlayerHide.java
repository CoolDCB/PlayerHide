package me.dave.playerhide;

import me.dave.platyutils.plugin.SpigotPlugin;
import me.dave.playerhide.hook.HookId;
import me.dave.playerhide.hook.PacketEventsHook;
import me.dave.playerhide.hook.WorldGuardHook;
import me.dave.playerhide.listeners.PlayerListener;
import me.dave.playerhide.visibility.VisibilityManager;
import me.dave.playerhide.visibility.VisibilityState;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class PlayerHide extends SpigotPlugin {
    private static PlayerHide plugin;
    private VisibilityManager visibilityManager;
    private BukkitTask heartbeat;

    @Override
    public void onLoad() {
        plugin = this;

        addHook("WorldGuard", () -> registerHook(new WorldGuardHook()), false);
    }

    @Override
    public void onEnable() {
        visibilityManager = new VisibilityManager();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        addHook("packetevents", () -> registerHook(new PacketEventsHook()));

        heartbeat = Bukkit.getScheduler().runTaskTimer(this, () -> {
            WorldGuardHook worldGuardHook = (WorldGuardHook) getHook(HookId.WORLD_GUARD.toString()).orElse(null);
            PacketEventsHook packetEventsHook = (PacketEventsHook) getHook(HookId.PACKET_EVENTS.toString()).orElse(null);
            if (worldGuardHook == null || packetEventsHook == null) {
                return;
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (worldGuardHook.shouldHide(player)) {
                    visibilityManager.changeState(player, VisibilityState.HIDDEN);
                } else if (worldGuardHook.shouldShowBootsOnly(player)) {
                    visibilityManager.changeState(player, VisibilityState.BOOTS_ONLY);
                } else {
                    visibilityManager.changeState(player, VisibilityState.SHOWN);
                }
            });
        }, 0, 5);

    }

    @Override
    public void onDisable() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    private void addHook(String pluginName, Runnable runnable, boolean requireEnabled) {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin var5 = pluginManager.getPlugin(pluginName);
        if (var5 instanceof JavaPlugin hookPlugin) {
            if (!requireEnabled || hookPlugin.isEnabled()) {
                this.getLogger().info("Found plugin \"" + pluginName + "\". Enabling " + pluginName + " support.");
                runnable.run();
            }
        }
    }

    public static PlayerHide getInstance() {
        return plugin;
    }
}
