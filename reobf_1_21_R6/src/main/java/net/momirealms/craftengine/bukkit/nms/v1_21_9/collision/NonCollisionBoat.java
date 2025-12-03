package net.momirealms.craftengine.bukkit.nms.v1_21_9.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NonCollisionBoat extends CollisionBoat {

    public NonCollisionBoat(EntityType<? extends Boat> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean blockBuilding) {
        super(type, world, x, y, z, aabb, canProjectileHit, blockBuilding);
    }

    @Override
    public boolean canCollideWith(@NotNull Entity other) {
        return false;
    }

    @Override
    public boolean canCollideWithBukkit(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return false;
    }

    @Override
    public int getEntityId() {
        return super.getEntityId();
    }
}
