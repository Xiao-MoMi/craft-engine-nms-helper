package net.momirealms.craftengine.bukkit.nms.v1_20_5.entity;

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
        if (item instanceof ProjectileItem projectileitem) {
            this.projectileItem = projectileitem;
            this.dispenseConfig = projectileitem.createDispenseConfig();
        } else {
            String s = String.valueOf(item);
            throw new IllegalArgumentException(s + " not instance of " + ProjectileItem.class.getSimpleName());
        }
    }

    public ItemStack execute(BlockSource pointer, ItemStack stack) {
        ServerLevel worldserver = pointer.level();
        Direction enumdirection = pointer.state().getValue(DispenserBlock.FACING);
        Position iposition = this.dispenseConfig.positionFunction().getDispensePosition(pointer, enumdirection);
        ItemStack itemstack1 = stack.copyWithCount(1);
        Block block = CraftBlock.at(worldserver, pointer.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(enumdirection.getStepX(), enumdirection.getStepY(), enumdirection.getStepZ()));
        if (!DispenserBlock.eventFired) {
            worldserver.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            return stack;
        } else {
            boolean shrink = true;
            if (!event.getItem().equals(craftItem)) {
                shrink = false;
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenseItemBehavior idispensebehavior = DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
                if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                    idispensebehavior.dispense(pointer, eventStack);
                    return stack;
                }
            }

            Projectile iprojectile = this.projectileItem.asProjectile(worldserver, iposition, CraftItemStack.unwrap(event.getItem()), enumdirection);
            this.projectileItem.shoot(iprojectile, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
            iprojectile.projectileSource = new CraftBlockProjectileSource(pointer.blockEntity());
            worldserver.addFreshEntity(iprojectile);
            BlockDispenseProjectileEvent e = new BlockDispenseProjectileEvent(block, craftItem, (org.bukkit.entity.Projectile) iprojectile.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(e);
            if (shrink) {
                stack.shrink(1);
            }

            return stack;
        }
    }

    protected void playSound(BlockSource pointer) {
        pointer.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), pointer.pos(), 0);
    }
}
