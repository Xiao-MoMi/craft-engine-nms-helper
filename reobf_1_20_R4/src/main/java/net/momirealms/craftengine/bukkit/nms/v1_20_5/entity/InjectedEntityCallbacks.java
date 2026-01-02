package net.momirealms.craftengine.bukkit.nms.v1_20_5.entity;

import com.destroystokyo.paper.util.maplist.ReferenceList;
import io.papermc.paper.chunk.system.entity.EntityLookup;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FoliaReflections;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InjectedEntityCallbacks implements LevelCallback<Entity> {
    private final LevelCallback<Entity> callback;
    private final EntityLookup entityLookup;
    @Nullable
    private ReferenceList<Entity> loadedEntities;
    @Nullable
    private ReferenceList<Entity> toProcessTrackingUnloading;

    public InjectedEntityCallbacks(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        this.entityLookup = entityLookup;
        if (VersionHelper.isFolia()) {
            foliaInit();
            return;
        }
        for (Entity entity : entityLookup.getAll()) {
            if (entity instanceof CollisionEntity) {
                callback.onTickingEnd(entity);
                entity.tracker = null;
                entityLookup.world.chunkSource.chunkMap.entityMap.remove(entity.getId());
            } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                callback.onTickingEnd(entity);
            }
        }
    }

    private void foliaInit() {
        this.loadedEntities = getLoadedEntities(entityLookup);
        this.toProcessTrackingUnloading = getToProcessTrackingUnloading(entityLookup);
        if (this.loadedEntities == null || this.toProcessTrackingUnloading == null) {
            for (Entity entity : entityLookup.getAll()) {
                if (entity instanceof CollisionEntity) {
                    this.callback.onTickingEnd(entity);
                    entity.tracker = null;
                } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                    this.callback.onTickingEnd(entity);
                }
            }
        } else {
            for (Entity entity : entityLookup.getAll()) {
                if (entity instanceof CollisionEntity) {
                    this.callback.onTickingEnd(entity);
                    entity.tracker = null;
                    if (this.loadedEntities.remove(entity)) {
                        this.toProcessTrackingUnloading.add(entity);
                    }
                } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                    this.callback.onTickingEnd(entity);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static ReferenceList<Entity> getLoadedEntities(EntityLookup entityLookup) {
        try {
            Object worldData = FoliaReflections.methodHandle$Level$getCurrentWorldData.invokeExact(entityLookup.world);
            if (worldData == null) return null;
            return (ReferenceList<Entity>) FoliaReflections.methodHandle$RegionizedWorldData$loadedEntitiesGetter.invokeExact(worldData);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static ReferenceList<Entity> getToProcessTrackingUnloading(EntityLookup entityLookup) {
        try {
            Object worldData = FoliaReflections.methodHandle$Level$getCurrentWorldData.invokeExact(entityLookup.world);
            if (worldData == null) return null;
            return (ReferenceList<Entity>) FoliaReflections.methodHandle$RegionizedWorldData$toProcessTrackingUnloadingGetter.invokeExact(worldData);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreated(@NotNull Entity entity) {
        this.callback.onCreated(entity);
    }

    @Override
    public void onDestroyed(@NotNull Entity entity) {
        this.callback.onDestroyed(entity);
    }

    @Override
    public void onTickingStart(@NotNull Entity entity) {
        if (entity instanceof CollisionEntity || entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) return;
        this.callback.onTickingStart(entity);
    }

    @Override
    public void onTickingEnd(@NotNull Entity entity) {
        this.callback.onTickingEnd(entity);
    }

    @Override
    public void onTrackingStart(@NotNull Entity entity) {
        this.callback.onTrackingStart(entity);
        if (entity instanceof CollisionEntity) {
            entity.tracker = null;
            if (VersionHelper.isFolia()) {
                foliaRemoveCollisionEntity(entity);
                return;
            }
            this.entityLookup.world.chunkSource.chunkMap.entityMap.remove(entity.getId());
        }
    }

    private void foliaRemoveCollisionEntity(Entity entity) {
        if (this.loadedEntities == null) {
            this.loadedEntities = getLoadedEntities(entityLookup);
        }
        if (this.toProcessTrackingUnloading == null) {
            this.toProcessTrackingUnloading = getToProcessTrackingUnloading(entityLookup);
        }
        if (this.loadedEntities != null && this.toProcessTrackingUnloading != null && this.loadedEntities.remove(entity)) {
            this.toProcessTrackingUnloading.add(entity);
        }
    }

    @Override
    public void onTrackingEnd(@NotNull Entity entity) {
        this.callback.onTrackingEnd(entity);
    }

    @Override
    public void onSectionChange(@NotNull Entity entity) {
        this.callback.onSectionChange(entity);
    }
}
