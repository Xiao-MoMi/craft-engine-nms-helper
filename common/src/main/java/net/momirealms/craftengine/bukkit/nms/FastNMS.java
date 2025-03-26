package net.momirealms.craftengine.bukkit.nms;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public abstract class FastNMS {
    public static final FastNMS INSTANCE = instance();

    private static FastNMS instance() {
        String classSuffix = getImplSuffix();
        try {
            Class<?> clazz = Class.forName("net.momirealms.craftengine.bukkit.nms.Impl" + classSuffix);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (FastNMS) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize craftengine nms helper", e);
        }
    }

    private static @NotNull String getImplSuffix() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String classSuffix;
        switch (bukkitVersion) {
            case "1.21.4" -> classSuffix = "1_21_r3";
            case "1.21.2", "1.21.3" -> classSuffix = "1_21_r2";
            case "1.21", "1.21.1" -> classSuffix = "1_21_r1";
            case "1.20.5", "1.20.6" -> classSuffix = "1_20_r4";
            case "1.20.3", "1.20.4" -> classSuffix = "1_20_r3";
            case "1.20.2" -> classSuffix = "1_20_r2";
            case "1.20", "1.20.1" -> classSuffix = "1_20_r1";
            default -> throw new UnsupportedVersionException();
        }
        return classSuffix;
    }

    public abstract Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState);
}
