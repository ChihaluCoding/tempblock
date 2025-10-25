package com.aprilcarlson.autobreakblock.forge;

import com.aprilcarlson.autobreakblock.AutoBreakBlockMod;
import com.aprilcarlson.autobreakblock.command.AutoBreakCommands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoBreakBlockMod.MOD_ID)
public final class AutoBreakBlockForge {

    public AutoBreakBlockForge(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::onCommonSetup);
        RegisterCommandsEvent.BUS.addListener(this::onRegisterCommands);
        EntityPlaceEvent.BUS.addListener(this::onBlockPlaced);
        LevelTickEvent.Post.BUS.addListener(this::onLevelTick);
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

    private void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AutoBreakBlockMod.tick(serverLevel);
    }
}
