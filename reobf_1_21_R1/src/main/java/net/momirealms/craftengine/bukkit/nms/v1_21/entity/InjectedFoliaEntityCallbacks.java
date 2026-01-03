package net.momirealms.craftengine.bukkit.nms.v1_21.entity;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup;
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
    private final ServerEntityLookup entityLookup;
    @Nullable
    private ReferenceList<Entity> trackerEntities;

    public InjectedFoliaEntityCallbacks(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        this.entityLookup = (ServerEntityLookup) entityLookup;
        this.trackerEntities = getTrackerEntities();
        if (this.trackerEntities == null) {
            for (Entity entity : entityLookup.getAll()) {
                if (entity instanceof CollisionEntity) {
                    this.callback.onTickingEnd(entity);
                    entity.moonrise$setTrackedEntity(null);
                } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                    this.callback.onTickingEnd(entity);
                }
            }
        } else {
            for (Entity entity : entityLookup.getAll()) {
                if (entity instanceof CollisionEntity) {
                    this.callback.onTickingEnd(entity);
                    entity.moonrise$setTrackedEntity(null);
                    this.trackerEntities.remove(entity);
                } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                    this.callback.onTickingEnd(entity);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private ReferenceList<Entity> getTrackerEntities() {
        try {
            Object worldData = FoliaReflections.methodHandle$Level$getCurrentWorldData.invokeExact(this.entityLookup.world);
            if (worldData == null) return null;
            return (ReferenceList<Entity>) FoliaReflections.methodHandle$RegionizedWorldData$trackerEntitiesGetter.invokeExact(worldData);
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
        boolean isCollisionEntity = entity instanceof CollisionEntity;
        if (isCollisionEntity) {
            if (this.trackerEntities == null) {
                this.trackerEntities = getTrackerEntities();
            }
            if (this.trackerEntities != null) {
                this.trackerEntities.remove(entity);
            }
            this.callback.onTrackingStart(entity);
            entity.moonrise$setTrackedEntity(null);
        } else  {
            this.callback.onTrackingStart(entity);
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
