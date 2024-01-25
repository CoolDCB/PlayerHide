package me.dave.playerhide.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.dave.platyutils.hook.Hook;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class WorldGuardHook extends Hook {
    private static StateFlag HIDE_PLAYERS_FLAG;
    private static StateFlag INVISIBLE_BOOTS_PLAYERS_FLAG;

    public WorldGuardHook() {
        super(HookId.WORLD_GUARD.toString());
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("hide-players", false);
            registry.register(flag);
            HIDE_PLAYERS_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("hide-players");
            if (existing instanceof StateFlag) {
                HIDE_PLAYERS_FLAG = (StateFlag) existing;
            }
        }

        try {
            StateFlag flag = new StateFlag("show-boots-only", false);
            registry.register(flag);
            INVISIBLE_BOOTS_PLAYERS_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("show-boots-only");
            if (existing instanceof StateFlag) {
                INVISIBLE_BOOTS_PLAYERS_FLAG = (StateFlag) existing;
            }
        }
    }

    public boolean shouldHide(@NotNull Player player) {
        return isFlagEnabled(player.getWorld(), player.getLocation(), HIDE_PLAYERS_FLAG);
    }

    public boolean shouldShowBootsOnly(@NotNull Player player) {
        return isFlagEnabled(player.getWorld(), player.getLocation(), INVISIBLE_BOOTS_PLAYERS_FLAG);
    }

    public boolean isFlagEnabled(@NotNull World world, @NotNull Location location, @NotNull StateFlag flagType) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return getDefaultState(flagType);

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        List<ProtectedRegion> regions = set.getRegions().stream().sorted(Comparator.comparing(ProtectedRegion::getPriority)).toList();
        if (regions.size() == 0) return getDefaultState(flagType);
        ProtectedRegion region = regions.get(0);
        StateFlag.State flag = region.getFlag(flagType);
        if (flag == null) return getDefaultState(flagType);

        return flag.equals(StateFlag.State.ALLOW);
    }

    private boolean getDefaultState(@NotNull StateFlag flagType) {
        return flagType.getDefault() != null && flagType.getDefault().equals(StateFlag.State.ALLOW);
    }
}
