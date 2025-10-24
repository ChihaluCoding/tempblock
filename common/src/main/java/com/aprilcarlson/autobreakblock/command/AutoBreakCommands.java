package com.aprilcarlson.autobreakblock.command;

import com.aprilcarlson.autobreakblock.AutoBreakBlockMod;
import com.aprilcarlson.autobreakblock.ConfigManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AutoBreakCommands {

    private static final DynamicCommandExceptionType INVALID_BLOCK = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.autobreak.block.invalid", id));

    private AutoBreakCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("autobreak")
                .then(Commands.literal("toggle")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            boolean enabled = ConfigManager.toggleEnabled();
                            ctx.getSource().sendSuccess(() -> Component.translatable(
                                    enabled ? "commands.autobreak.toggle.enabled" : "commands.autobreak.toggle.disabled"),
                                    true);
                            if (!enabled) {
                                AutoBreakBlockMod.getScheduler().clear();
                            }
                            return 1;
                        }))
                .then(Commands.literal("block")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                .executes(ctx -> {
                                    BlockInput input = BlockStateArgument.getBlock(ctx, "block");
                                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(input.getState().getBlock());
                                    if (id == null || !ConfigManager.setTargetBlock(id.toString())) {
                                        throw INVALID_BLOCK.create(id);
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.translatable(
                                            "commands.autobreak.block.set", id), true);
                                    return 1;
                                })))
                .then(Commands.literal("delay")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                    ConfigManager.setDelaySeconds(seconds);
                                    ctx.getSource().sendSuccess(() -> Component.translatable(
                                            "commands.autobreak.delay.set", seconds), true);
                                    return 1;
                                })))
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            var config = ConfigManager.getConfig();
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.autobreak.status.enabled", config.isEnabled()),
                                    false);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.autobreak.status.block", config.getTargetBlock()),
                                    false);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.autobreak.status.delay", config.getDelaySeconds()),
                                    false);
                            return 1;
                        })));
    }

    public static Component createMissingPermissionMessage() {
        return Component.translatable("commands.autobreak.permission");
    }

    public static CommandSyntaxException unknownBlock(ResourceLocation id) {
        return INVALID_BLOCK.create(id);
    }
}
