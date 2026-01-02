package net.momirealms.craftengine.bukkit.nms;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public final class FoliaReflections {
    private FoliaReflections() {}

    public static final Class<?> clazz$RegionizedWorldData = ReflectionUtils.getClazz(
            "io.papermc.paper.threadedregions.RegionizedWorldData"
    );

    public static final Class<?> clazz$ReferenceList = ReflectionUtils.getClazz(
            "ca.spottedleaf.moonrise.common.list.ReferenceList",
            "com.destroystokyo.paper.util.maplist.ReferenceList"
    );

    public static final MethodHandle methodHandle$Level$getCurrentWorldData;
    public static final MethodHandle methodHandle$RegionizedWorldData$trackerEntitiesGetter;
    public static final MethodHandle methodHandle$RegionizedWorldData$loadedEntitiesGetter;
    public static final MethodHandle methodHandle$RegionizedWorldData$toProcessTrackingUnloadingGetter;

    static {
        if (VersionHelper.isFolia()) {
            try {
                Method method$Level$getCurrentWorldData = Objects.requireNonNull(ReflectionUtils.getDeclaredMethod(
                        CoreReflections.clazz$Level, clazz$RegionizedWorldData, new String[]{"getCurrentWorldData"}
                ));
                methodHandle$Level$getCurrentWorldData = ReflectionUtils.unreflectMethod(method$Level$getCurrentWorldData)
                        .asType(MethodType.methodType(Object.class, VersionHelper.isOrAbove1_21() ? CoreReflections.clazz$Level : CoreReflections.clazz$ServerLevel));
                if (VersionHelper.isOrAbove1_21()) {
                    Field field$RegionizedWorldData$trackerEntities = Objects.requireNonNull(ReflectionUtils.getDeclaredField(
                            clazz$RegionizedWorldData, "trackerEntities"
                    ));
                    methodHandle$RegionizedWorldData$trackerEntitiesGetter = ReflectionUtils.unreflectGetter(field$RegionizedWorldData$trackerEntities)
                            .asType(MethodType.methodType(clazz$ReferenceList, Object.class));
                    methodHandle$RegionizedWorldData$loadedEntitiesGetter = null;
                    methodHandle$RegionizedWorldData$toProcessTrackingUnloadingGetter = null;
                } else {
                    Field field$RegionizedWorldData$loadedEntities = Objects.requireNonNull(ReflectionUtils.getDeclaredField(
                            clazz$RegionizedWorldData, "loadedEntities"
                    ));
                    Field field$RegionizedWorldData$toProcessTrackingUnloading = Objects.requireNonNull(ReflectionUtils.getDeclaredField(
                            clazz$RegionizedWorldData, "toProcessTrackingUnloading"
                    ));
                    methodHandle$RegionizedWorldData$loadedEntitiesGetter = ReflectionUtils.unreflectGetter(field$RegionizedWorldData$loadedEntities)
                            .asType(MethodType.methodType(clazz$ReferenceList, Object.class));
                    methodHandle$RegionizedWorldData$toProcessTrackingUnloadingGetter = ReflectionUtils.unreflectGetter(field$RegionizedWorldData$toProcessTrackingUnloading)
                            .asType(MethodType.methodType(clazz$ReferenceList, Object.class));
                    methodHandle$RegionizedWorldData$trackerEntitiesGetter = null;
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            methodHandle$RegionizedWorldData$trackerEntitiesGetter = null;
            methodHandle$Level$getCurrentWorldData = null;
            methodHandle$RegionizedWorldData$loadedEntitiesGetter = null;
            methodHandle$RegionizedWorldData$toProcessTrackingUnloadingGetter = null;
        }
    }
}
