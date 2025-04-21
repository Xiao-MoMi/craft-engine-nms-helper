package net.momirealms.craftengine.bukkit.nms.v1_20;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import org.jetbrains.annotations.NotNull;

public class CollisionShulker extends Shulker implements CollisionEntity {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final AABB aabb;
    private final boolean canProjectileHit;
    private final boolean canCollide;

    public CollisionShulker(EntityType<? extends Shulker> type, Level world, double x, double y, double z, AABB aabb, boolean canProjectileHit, boolean canCollide, boolean blockBuilding) {
        super(type, world);
        this.aabb = aabb;
        this.canProjectileHit = canProjectileHit;
        this.setInvisible(true);
        this.setPos(new Vec3(x, y, z));
        this.setBoundingBox(aabb);
        this.setInvulnerable(true);
        this.setNoAi(true);
        this.setSilent(true);
        this.canCollide = canCollide;
        this.blocksBuilding = blockBuilding;
    }

    @Override
    public boolean save(@NotNull CompoundTag tag) {
        String s = this.getEncodeId();
        if (s != null) {
            tag.putString("id", s);
            this.saveWithoutId(tag);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
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
        return this.canCollide;
    }

    @Override
    public boolean canCollideWithBukkit(@NotNull Entity entity) {
        return this.canCollide;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.canCollide;
    }
}
