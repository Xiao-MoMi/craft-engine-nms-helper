package net.momirealms.craftengine.bukkit.nms.v26_1.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
    public boolean save(@NotNull ValueOutput output) {
        String encodeId = this.getEncodeId(false);
        if (encodeId != null) {
            output.putString("id", encodeId);
            this.saveWithoutId(output, true, false, true);
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
    public int getEntityId() {
        return super.getId();
    }

    @Override
    public void tick() {
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (VersionHelper.hasPaperPatch) {
            this.setPosRaw(x, y, z, true);
        } else {
            this.setPosRaw(x, y, z);
        }
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
    public boolean shouldBeSaved() {
        return false;
    }
}
