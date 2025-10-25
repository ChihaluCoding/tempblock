package com.aprilcarlson.autobreakblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

/**
 * Lightweight scheduler that keeps track of blocks waiting to be removed.
 */
public final class AutoBreakScheduler {

    private final Map<ResourceKey<Level>, List<BlockRemovalTask>> scheduledTasks = new HashMap<>();

    public synchronized void schedule(ServerLevel level, BlockPos pos, BlockState state, int delaySeconds) {
        if (delaySeconds <= 0) {
            level.destroyBlock(pos, true);
            return;
        }

        long executeTick = level.getGameTime() + delaySeconds * 20L;
        BlockRemovalTask task = new BlockRemovalTask(pos.immutable(), state, executeTick);
        scheduledTasks
                .computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                .add(task);
    }

    public synchronized void tick(ServerLevel level) {
        List<BlockRemovalTask> tasks = scheduledTasks.get(level.dimension());
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        AutoBreakConfig config = ConfigManager.getConfig();
        long currentTick = level.getGameTime();
        Iterator<BlockRemovalTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            BlockRemovalTask task = iterator.next();

            if (task.executeTick > currentTick) {
                continue;
            }

            if (!config.isEnabled()) {
                iterator.remove();
                continue;
            }

            if (!level.isLoaded(task.position)) {
                continue;
            }

            BlockState currentState = level.getBlockState(task.position);

            if (!ConfigManager.isTargetBlock(currentState)) {
                iterator.remove();
                continue;
            }

            if (!currentState.equals(task.expectedState)) {
                iterator.remove();
                continue;
            }

            level.destroyBlock(task.position, true);
            iterator.remove();
        }

        if (tasks.isEmpty()) {
            scheduledTasks.remove(level.dimension());
        }
    }

    public synchronized void clear() {
        scheduledTasks.clear();
    }

    private record BlockRemovalTask(BlockPos position, BlockState expectedState, long executeTick) {
    }
}
