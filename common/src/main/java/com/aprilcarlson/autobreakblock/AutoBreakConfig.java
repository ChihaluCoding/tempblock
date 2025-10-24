package com.aprilcarlson.autobreakblock;

/**
 * POJO representing the persistent configuration for Auto Break Block.
 */
public final class AutoBreakConfig {

    private boolean enabled = true;
    private String targetBlock = "minecraft:stone";
    private int delaySeconds = 5;

    public static AutoBreakConfig createDefault() {
        return new AutoBreakConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTargetBlock() {
        return targetBlock;
    }

    public void setTargetBlock(String targetBlock) {
        this.targetBlock = targetBlock;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    /**
     * Normalises incoming data to ensure the config contains valid values.
     */
    public void validate() {
        if (delaySeconds < 0) {
            delaySeconds = 0;
        }
        if (targetBlock == null || targetBlock.isBlank()) {
            targetBlock = "minecraft:stone";
        }
    }
}
