package com.aprilcarlson.autobreakblock;

import com.aprilcarlson.autobreakblock.ConfigManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public final class AutoBreakBlockFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AutoBreakBlockMod.init();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(world instanceof ServerLevel serverLevel)) {
                return InteractionResult.PASS;
            }
            if (!ConfigManager.getConfig().isEnabled()) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return InteractionResult.PASS;
            }
            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hitResult);
            BlockState predictedState = blockItem.getBlock().defaultBlockState();
            if (!ConfigManager.isTargetBlock(predictedState)) {
                return InteractionResult.PASS;
            }
            AutoBreakBlockMod.queuePlacement(serverLevel, context.getClickedPos(), predictedState);
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(AutoBreakBlockMod::tick);
    }
}


