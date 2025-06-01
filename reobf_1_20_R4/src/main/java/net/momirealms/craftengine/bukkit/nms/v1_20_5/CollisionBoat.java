package net.momirealms.craftengine.bukkit.nms.v1_20_5;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class CollisionBoat extends Boat implements CollisionEntity {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final AABB aabb;
    private final boolean canProjectileHit;

    public CollisionBoat(EntityType<? extends Boat> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean blockBuilding) {
        super(type, world);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.setInvisible(true);
        this.setPos(new Vec3(x, y, z));
        this.setBoundingBox(aabb);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.blocksBuilding = blockBuilding;
        this.landBoats = true;
    }

    @Override
    public boolean save(@NotNull CompoundTag tag) {
        String s = this.getEncodeId();
        if (s != null) {
            tag.putString("id", s);
            this.saveWithoutId(tag, false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        super.remove(RemovalReason.DISCARDED);
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public void tick() {
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z, true);
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

    @Override
    public void onAboveBubbleCol(boolean downwards) {
    }

    @Override
    public void push(@NotNull Entity entity) {
    }

    @Override
    public void positionRider(@NotNull Entity passenger, Entity.@NotNull MoveFunction callback) {
    }

    @Override
    public boolean canAddPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public int getMaxPassengers() {
        return 0;
    }

    @Override
    public boolean isUnderWater() {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public void destroy(@NotNull DamageSource source) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void animateHurt(float yaw) {
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 movement) {
    }
}
