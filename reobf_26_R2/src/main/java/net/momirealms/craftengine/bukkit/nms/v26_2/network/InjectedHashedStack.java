package net.momirealms.craftengine.bukkit.nms.v26_2.network;

import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record InjectedHashedStack(HashedStack hashedStack, Player player) implements HashedStack {

    @Override
    public boolean matches(@NotNull ItemStack stack, HashedPatchMap.@NotNull HashGenerator hashGenerator) {
        if (this.hashedStack instanceof HashedStack.ActualItem actualItem && actualItem.count() != stack.getCount()) return false;
        if (!stack.isEmpty()) {
            Optional<org.bukkit.inventory.ItemStack> optional = BukkitItemManager.instance().s2c(stack.copy().getBukkitStack(), this.player);
            if (optional.isPresent()) {
                stack = ((CraftItemStack) optional.get()).handle;
            }
        }
        return this.hashedStack.matches(stack, hashGenerator);
    }
}
