package net.momirealms.craftengine.bukkit.nms.v1_21_6;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CollisionBoat extends Boat implements CollisionEntity {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final AABB aabb;
    private final boolean canProjectileHit;

    public CollisionBoat(EntityType<? extends Boat> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean blockBuilding) {
        super(type, world, () -> Items.AIR);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.setInvisible(true);
        this.setPos(new Vec3(x, y, z));
        this.setBoundingBox(aabb);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.blocksBuilding = blockBuilding;
        this.landBoats = true;
        this.noPhysics = true;
    }

    @Override
    public boolean save(@NotNull ValueOutput output) {
        return super.saveAsPassenger(output, true, false, true);
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
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return true;
    }

    @Override
    public void onAboveBubbleColumn(boolean downwards, @NotNull BlockPos pos) {
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
    public boolean hurtServer(@NotNull ServerLevel var1, @NotNull DamageSource var2, float var3) {
        return false;
    }

    @Override
    public void destroy(@NotNull ServerLevel level, @NotNull DamageSource damageSource) {
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
    }

    @Override
    public Leashable.LeashData getLeashData() {
        return null;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void animateHurt(float yaw) {
    }

    @Override
    public double rideHeight(@NotNull EntityDimensions dimensions) {
        return 0;
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 movement) {
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
    }
}
