package net.momirealms.craftengine.bukkit.nms.v1_20.entity;

import com.destroystokyo.paper.util.maplist.ReferenceList;
import io.papermc.paper.chunk.system.entity.EntityLookup;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FoliaReflections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InjectedFoliaEntityCallbacks implements LevelCallback<Entity> {
    private final LevelCallback<Entity> callback;
    private final EntityLookup entityLookup;
    @Nullable
    private ReferenceList<Entity> loadedEntities;
    @Nullable
    private ReferenceList<Entity> toProcessTrackingUnloading;

    public InjectedFoliaEntityCallbacks(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        this.entityLookup = entityLookup;
        this.loadedEntities = getLoadedEntities();
        this.toProcessTrackingUnloading = getToProcessTrackingUnloading();
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
    private ReferenceList<Entity> getLoadedEntities() {
        try {
            Object worldData = FoliaReflections.methodHandle$Level$getCurrentWorldData.invokeExact(this.entityLookup.world);
            if (worldData == null) return null;
            return (ReferenceList<Entity>) FoliaReflections.methodHandle$RegionizedWorldData$loadedEntitiesGetter.invokeExact(worldData);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private ReferenceList<Entity> getToProcessTrackingUnloading() {
        try {
            Object worldData = FoliaReflections.methodHandle$Level$getCurrentWorldData.invokeExact(this.entityLookup.world);
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
            if (this.loadedEntities == null) {
                this.loadedEntities = getLoadedEntities();
            }
            if (this.toProcessTrackingUnloading == null) {
                this.toProcessTrackingUnloading = getToProcessTrackingUnloading();
            }
            if (this.loadedEntities != null && this.toProcessTrackingUnloading != null && this.loadedEntities.remove(entity)) {
                this.toProcessTrackingUnloading.add(entity);
            }
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
