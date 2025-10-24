package com.aprilcarlson.autobreakblock.fabric;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aprilcarlson.autobreakblock.AutoBreakConfig;
import com.aprilcarlson.autobreakblock.ConfigManager;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FabricConfigScreen {

    private FabricConfigScreen() {
    }

    public static Screen create(Screen parent) {
        AutoBreakConfig config = ConfigManager.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.autobreakblock.title"));
        builder.setSavingRunnable(ConfigManager::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.autobreakblock.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.autobreakblock.enabled"),
                        config.isEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ConfigManager::setEnabled)
                .build());

        general.addEntry(createBlockEntry(entryBuilder, config));

        general.addEntry(entryBuilder.startIntField(
                        Component.translatable("config.autobreakblock.delay"),
                        config.getDelaySeconds())
                .setMin(0)
                .setDefaultValue(5)
                .setSaveConsumer(ConfigManager::setDelaySeconds)
                .setTooltip(Component.translatable("config.autobreakblock.delay.tooltip"))
                .build());

        return builder.build();
    }

    private static AbstractConfigListEntry<String> createBlockEntry(ConfigEntryBuilder entryBuilder, AutoBreakConfig config) {
        List<String> blockIds = BuiltInRegistries.BLOCK.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        DropdownMenuBuilder<String> builder = entryBuilder.startStringDropdownMenu(
                Component.translatable("config.autobreakblock.target_block"),
                config.getTargetBlock());
        builder.setSelections(blockIds);
        builder.setSuggestionMode(true);
        builder.setDefaultValue("minecraft:stone");
        builder.setTooltipSupplier(value -> Optional.of(new Component[]{
                Component.translatable("config.autobreakblock.target_block.tooltip")
        }));
        builder.setErrorSupplier(value -> ConfigManager.isValidBlockId(value)
                ? Optional.empty()
                : Optional.of(Component.translatable("config.autobreakblock.target_block.error", value)));
        builder.setSaveConsumer(value -> ConfigManager.setTargetBlock(value));
        return builder.build();
    }
}
