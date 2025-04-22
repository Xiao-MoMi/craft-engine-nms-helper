package net.momirealms.craftengine.bukkit.nms.v1_21;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class NonCollisionInteraction extends CollisionInteraction {

    public NonCollisionInteraction(EntityType<? extends Interaction> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean blockBuilding) {
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
    public boolean canBeCollidedWith() {
        return false;
    }
}
