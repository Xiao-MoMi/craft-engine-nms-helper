package net.momirealms.craftengine.bukkit.nms.v1_21_5;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class CollisionInteraction extends Interaction implements CollisionEntity {
    private final AABB aabb;
    private final boolean canProjectileHit;
    private final boolean hardCollision;

    public CollisionInteraction(EntityType<?> type, Level world, double x, double y, double z, AABB aabb, boolean hardCollision, boolean canProjectileHit) {
        super(type, world);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.hardCollision = hardCollision;
        this.setInvisible(true);
        this.setPos(new Vec3(x, y, z));
        this.setBoundingBox(aabb);
    }

    @Override
    public void tick() {
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
        return true;
    }
}
