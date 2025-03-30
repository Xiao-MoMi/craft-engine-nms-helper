package net.momirealms.craftengine.bukkit.nms.v1_21_2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class CollisionInteraction extends Interaction implements CollisionEntity {
    private final AABB aabb;
    private final boolean canProjectileHit;
    private final boolean hardCollision;

    public CollisionInteraction(EntityType<?> type, Level world, AABB aabb, boolean hardCollision, boolean canProjectileHit) {
        super(type, world);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.hardCollision = hardCollision;
        this.setInvisible(true);
    }

    @Override
    public void tick() {
    }

    @NotNull
    @Override
    protected AABB makeBoundingBox() {
        return this.aabb;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.canProjectileHit;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity other) {
        return true;
    }

    @Override
    public boolean canCollideWithBukkit(@NotNull Entity entity) {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.hardCollision;
    }
}
