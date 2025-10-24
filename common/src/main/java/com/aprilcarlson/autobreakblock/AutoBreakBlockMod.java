package com.aprilcarlson.autobreakblock;

import com.aprilcarlson.autobreakblock.platform.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for shared mod logic.
 */
public final class AutoBreakBlockMod {

    public static final String MOD_ID = "autobreakblock";
    public static final String MOD_NAME = "Auto Break Block";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static final AutoBreakScheduler SCHEDULER = new AutoBreakScheduler();
    private static final Map<ResourceKey<Level>, List<PendingPlacement>> PENDING_PLACEMENTS = new HashMap<>();

    private AutoBreakBlockMod() {
    }

    public static void init() {
        ConfigManager.load();
        LOGGER.info("Initialised on {} ({})", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static void handleBlockPlaced(ServerLevel level, BlockPos pos, BlockState state) {
        scheduleForState(level, pos, state);
    }

    public static void tick(ServerLevel level) {
        processPendingPlacements(level);
        SCHEDULER.tick(level);
    }

    public static void queuePlacement(ServerLevel level, BlockPos pos, BlockState predictedState) {
        synchronized (PENDING_PLACEMENTS) {
            PENDING_PLACEMENTS
                    .computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                    .add(new PendingPlacement(pos.immutable(), predictedState.getBlock()));
        }
    }

    public static AutoBreakScheduler getScheduler() {
        return SCHEDULER;
    }

    private static void processPendingPlacements(ServerLevel level) {
        List<PendingPlacement> placements;
        synchronized (PENDING_PLACEMENTS) {
            placements = PENDING_PLACEMENTS.remove(level.dimension());
        }
        if (placements == null || placements.isEmpty()) {
            return;
        }

        for (PendingPlacement placement : placements) {
            BlockPos pos = placement.pos();
            if (!level.hasChunkAt(pos)) {
                queuePlacement(level, pos, level.getBlockState(pos));
                continue;
            }
            BlockState current = level.getBlockState(pos);
            if (current.isAir() || current.getBlock() != placement.block()) {
                continue;
            }
            scheduleForState(level, pos, current);
        }
    }

    private static void scheduleForState(ServerLevel level, BlockPos pos, BlockState state) {
        if (!ConfigManager.getConfig().isEnabled()) {
            return;
        }
        if (!ConfigManager.isTargetBlock(state)) {
            return;
        }

        int delay = ConfigManager.getConfig().getDelaySeconds();
        SCHEDULER.schedule(level, pos, state, delay);
    }

    private record PendingPlacement(BlockPos pos, Block block) {
    }
}

