package net.momirealms.craftengine.bukkit.nms.v1_20_5;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class CollisionInteraction extends Interaction implements CollisionEntity {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final AABB aabb;
    private final boolean canProjectileHit;

    public CollisionInteraction(EntityType<? extends Interaction> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean blockBuilding) {
        super(type, world);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.setInvisible(true);
        this.setPos(new Vec3(x, y, z));
        this.setBoundingBox(aabb);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.blocksBuilding = blockBuilding;
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
}
