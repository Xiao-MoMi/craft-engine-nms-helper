package net.momirealms.craftengine.bukkit.nms.v1_21_4.entity;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.core.world.InjectedWorldCallback;
import org.jetbrains.annotations.NotNull;

public class InjectedPaperLevelCallback implements LevelCallback<Entity>, InjectedWorldCallback {
    private static final boolean HAS_TRACKER_ENTITIES = hasTrackerEntities();
    private final LevelCallback<Entity> callback;
    private final ServerEntityLookup entityLookup;

    private static boolean hasTrackerEntities() {
        try {
            ServerEntityLookup.class.getField("trackerEntities");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public InjectedPaperLevelCallback(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        this.entityLookup = (ServerEntityLookup) entityLookup;
        for (Entity entity : entityLookup.getAll()) {
            if (entity instanceof CollisionEntity) {
                callback.onTickingEnd(entity);
                stopTracking(entity);
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
        this.callback.onTrackingStart(entity);
        if (entity instanceof CollisionEntity) {
            stopTracking(entity);
        }
    }

    private void stopTracking(Entity entity) {
        // Untrack through ChunkMap so player pairings and all tracker references are cleared.
        ((ServerLevel) entity.level()).getChunkSource().removeEntity(entity);
        // Some forks backport the ChunkMap tracker list from newer Moonrise versions.
        if (HAS_TRACKER_ENTITIES) {
            this.entityLookup.trackerEntities.remove(entity);
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
