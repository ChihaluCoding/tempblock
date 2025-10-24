package com.aprilcarlson.autobreakblock;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.aprilcarlson.autobreakblock.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Manages reading and writing the shared JSON config.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "autobreakblock.json";

    private static AutoBreakConfig config;
    private static Block cachedTargetBlock;

    private ConfigManager() {
    }

    public static synchronized void load() {
        Path path = getConfigPath();
        AutoBreakConfig loaded = null;

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                loaded = GSON.fromJson(reader, AutoBreakConfig.class);
            } catch (Exception ex) {
                AutoBreakBlockMod.LOGGER.warn("Failed to read config, falling back to defaults", ex);
            }
        }

        if (loaded == null) {
            loaded = AutoBreakConfig.createDefault();
        }

        loaded.validate();
        config = loaded;
        resolveTargetBlock(config.getTargetBlock()).ifPresentOrElse(
                block -> cachedTargetBlock = block,
                () -> {
                    AutoBreakBlockMod.LOGGER.warn("Unknown target block id '{}', using minecraft:stone instead", config.getTargetBlock());
                    config.setTargetBlock("minecraft:stone");
                    cachedTargetBlock = Blocks.STONE;
                }
        );
        save(); // Write back to ensure defaults persist
    }

    public static synchronized AutoBreakConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }

    public static synchronized boolean isTargetBlock(BlockState state) {
        if (config == null) {
            load();
        }
        return cachedTargetBlock != null && state.is(cachedTargetBlock);
    }

    public static synchronized boolean toggleEnabled() {
        AutoBreakConfig cfg = getConfig();
        cfg.setEnabled(!cfg.isEnabled());
        save();
        return cfg.isEnabled();
    }

    public static synchronized void setEnabled(boolean enabled) {
        AutoBreakConfig cfg = getConfig();
        cfg.setEnabled(enabled);
        save();
    }

    public static synchronized boolean setTargetBlock(String blockId) {
        AutoBreakConfig cfg = getConfig();
        Optional<Block> resolved = resolveTargetBlock(blockId);
        if (resolved.isEmpty()) {
            return false;
        }
        cfg.setTargetBlock(blockId);
        cachedTargetBlock = resolved.get();
        AutoBreakBlockMod.getScheduler().clear();
        save();
        return true;
    }

    public static synchronized boolean isValidBlockId(String blockId) {
        return resolveTargetBlock(blockId).isPresent();
    }

    public static synchronized void setDelaySeconds(int seconds) {
        AutoBreakConfig cfg = getConfig();
        cfg.setDelaySeconds(Math.max(0, seconds));
        save();
    }

    public static synchronized void save() {
        if (config == null) {
            return;
        }

        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ex) {
            AutoBreakBlockMod.LOGGER.error("Failed to write config file {}", path, ex);
        }
    }

    private static Path getConfigPath() {
        return Services.PLATFORM.getConfigDirectory().resolve(CONFIG_FILE);
    }

    private static Optional<Block> resolveTargetBlock(String blockId) {
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        if (id == null) {
            return Optional.empty();
        }
        return BuiltInRegistries.BLOCK.getOptional(id);
    }
}

