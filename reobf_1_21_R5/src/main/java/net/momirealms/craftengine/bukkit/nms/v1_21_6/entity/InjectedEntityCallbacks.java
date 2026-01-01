package net.momirealms.craftengine.bukkit.nms.v1_21_6.entity;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurnitureManager;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class InjectedEntityCallbacks implements LevelCallback<Entity> {
    private final LevelCallback<Entity> callback;

    public InjectedEntityCallbacks(LevelCallback<Entity> callback, EntityLookup entityLookup) {
        this.callback = callback;
        for (Entity entity : entityLookup.getAll()) {
            if (entity instanceof CollisionEntity || entity instanceof Display.ItemDisplay && BukkitFurnitureManager.instance().isFurnitureMetaEntity(entity.getId())) {
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
        if (entity instanceof CollisionEntity || entity instanceof Display.ItemDisplay && BukkitFurnitureManager.instance().isFurnitureMetaEntity(entity.getId())) return;
        this.callback.onTickingStart(entity);
    }

    @Override
    public void onTickingEnd(@NotNull Entity entity) {
        this.callback.onTickingEnd(entity);
    }

    @Override
    public void onTrackingStart(@NotNull Entity entity) {
        this.callback.onTrackingStart(entity);
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
