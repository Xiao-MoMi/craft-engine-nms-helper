package net.momirealms.craftengine.bukkit.nms.v1_21_9.entity;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class InjectedEntityCallbacks implements LevelCallback<Entity> {
    private final LevelCallback<Entity> callback;
    private final ServerEntityLookup entityLookup;

    public InjectedEntityCallbacks(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        this.entityLookup = (ServerEntityLookup) entityLookup;
        for (Entity entity : entityLookup.getAll()) {
            if (entity instanceof CollisionEntity) {
                callback.onTickingEnd(entity);
                entity.moonrise$setTrackedEntity(null);
                this.entityLookup.trackerEntities.remove(entity);
            } else if (entity instanceof Display.ItemDisplay && CraftEngineFurniture.isFurniture(entity.getBukkitEntity())) {
                callback.onTickingEnd(entity);
            }
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
            entityLookup.trackerEntities.remove(entity);
        }
        this.callback.onTrackingStart(entity);
        if (isCollisionEntity) {
            entity.moonrise$setTrackedEntity(null);
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
