package net.momirealms.craftengine.bukkit.nms.v1_21_9.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.momirealms.craftengine.bukkit.api.event.BlockDispenseProjectileEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public class InjectedProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
    private final ProjectileItem projectileItem;
    private final ProjectileItem.DispenseConfig dispenseConfig;

    public InjectedProjectileDispenseBehavior(Item projectile) {
        if (projectile instanceof ProjectileItem projectileItem) {
            this.projectileItem = projectileItem;
            this.dispenseConfig = projectileItem.createDispenseConfig();
        } else {
            String var10002 = String.valueOf(projectile);
            throw new IllegalArgumentException(var10002 + " not instance of " + ProjectileItem.class.getSimpleName());
        }
    }

    public ItemStack execute(BlockSource blockSource, ItemStack item) {
        ServerLevel serverLevel = blockSource.level();
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        Position dispensePosition = this.dispenseConfig.positionFunction().getDispensePosition(blockSource, direction);
        ItemStack singleItemStack = item.copyWithCount(1);
        Block block = CraftBlock.at(serverLevel, blockSource.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
        serverLevel.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return item;
        } else {
            boolean shrink = true;
            if (!event.getItem().equals(craftItem)) {
                shrink = false;
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenseItemBehavior dispenseBehavior = DispenserBlock.getDispenseBehavior(blockSource, eventStack);
                if (dispenseBehavior != DispenseItemBehavior.NOOP && dispenseBehavior != this) {
                    dispenseBehavior.dispense(blockSource, eventStack);
                    return item;
                }
            }

            if (!singleItemStack.isEmpty()) {
                Projectile.Delayed<Projectile> delayed = Projectile.spawnProjectileUsingShootDelayed(this.projectileItem.asProjectile(serverLevel, dispensePosition, CraftItemStack.unwrap(event.getItem()), direction), serverLevel, singleItemStack, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
                Projectile projectile = delayed.projectile();
                projectile.projectileSource = new CraftBlockProjectileSource(blockSource.blockEntity());
                BlockDispenseProjectileEvent e = new BlockDispenseProjectileEvent(block, craftItem, (org.bukkit.entity.Projectile) projectile.getBukkitEntity());
                Bukkit.getPluginManager().callEvent(e);
                delayed.spawn();
            }

            if (shrink) {
                item.shrink(1);
            }

            return item;
        }
    }

    protected void playSound(BlockSource blockSource) {
        blockSource.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), blockSource.pos(), 0);
    }
}
