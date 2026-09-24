package net.momirealms.craftengine.bukkit.nms.v26_3.entity;

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

    public InjectedProjectileDispenseBehavior(Item item) {
        if (item instanceof ProjectileItem projectileItem) {
            this.projectileItem = projectileItem;
            this.dispenseConfig = projectileItem.createDispenseConfig();
        } else {
            String var10002 = String.valueOf(item);
            throw new IllegalArgumentException(var10002 + " not instance of " + ProjectileItem.class.getSimpleName());
        }
    }

    public ItemStack execute(BlockSource source, ItemStack dispensed) {
        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = this.dispenseConfig.positionFunction().getDispensePosition(source, direction);
        ItemStack singleItemStack = dispensed.copyWithCount(1);
        Block block = CraftBlock.at(level, source.pos());
        org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
        level.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return dispensed;
        } else {
            boolean shrink = true;
            if (!event.getItem().equals(craftItem)) {
                shrink = false;
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenseItemBehavior dispenseBehavior = DispenserBlock.getDispenseBehavior(source, eventStack);
                if (dispenseBehavior != DispenseItemBehavior.NOOP && dispenseBehavior != this) {
                    dispenseBehavior.dispense(source, eventStack);
                    return dispensed;
                }
            }

            if (!singleItemStack.isEmpty()) {
                Projectile.Delayed<Projectile> delayed = Projectile.spawnProjectileUsingShootDelayed(this.projectileItem.asProjectile(level, position, CraftItemStack.unwrap(event.getItem()), direction), level, singleItemStack, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
                Projectile projectile = delayed.projectile();
                projectile.projectileSource = new CraftBlockProjectileSource(source.blockEntity());
                BlockDispenseProjectileEvent e = new BlockDispenseProjectileEvent(block, craftItem, (org.bukkit.entity.Projectile) projectile.getBukkitEntity());
                Bukkit.getPluginManager().callEvent(e);
                delayed.spawn();
            }

            if (shrink) {
                dispensed.shrink(1);
            }

            return dispensed;
        }
    }

    protected void playSound(BlockSource source) {
        source.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), source.pos(), 0);
    }
}
