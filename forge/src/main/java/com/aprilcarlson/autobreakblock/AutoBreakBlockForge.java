package com.aprilcarlson.autobreakblock;

import com.aprilcarlson.autobreakblock.command.AutoBreakCommands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoBreakBlockMod.MOD_ID)
public final class AutoBreakBlockForge {

    public AutoBreakBlockForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        AutoBreakBlockMod.init();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AutoBreakCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onBlockPlaced(EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        AutoBreakBlockMod.handleBlockPlaced(serverLevel, event.getPos(), event.getPlacedBlock());
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        if (event.level instanceof ServerLevel serverLevel) {
            AutoBreakBlockMod.tick(serverLevel);
        }
    }
}
