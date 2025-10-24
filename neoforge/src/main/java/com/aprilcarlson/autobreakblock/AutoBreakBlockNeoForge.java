package com.aprilcarlson.autobreakblock;

import com.aprilcarlson.autobreakblock.command.AutoBreakCommands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(AutoBreakBlockMod.MOD_ID)
public final class AutoBreakBlockNeoForge {

    public AutoBreakBlockNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onBlockPlaced);
        NeoForge.EVENT_BUS.addListener(this::onLevelTick);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AutoBreakBlockMod::init);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        AutoBreakCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void onBlockPlaced(EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        AutoBreakBlockMod.handleBlockPlaced(serverLevel, event.getPos(), event.getPlacedBlock());
    }

    private void onLevelTick(Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            AutoBreakBlockMod.tick(serverLevel);
        }
    }
}
