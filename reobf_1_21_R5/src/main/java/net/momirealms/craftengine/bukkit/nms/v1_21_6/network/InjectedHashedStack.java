package net.momirealms.craftengine.bukkit.nms.v1_21_6.network;

import net.minecraft.core.Holder;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record InjectedHashedStack(Holder<Item> item, int count, HashedPatchMap components, Player player) implements HashedStack {

    @Override
    public boolean matches(ItemStack stack, HashedPatchMap.@NotNull HashGenerator hashGenerator) {
        if (this.count != stack.getCount()) return false;
        Optional<org.bukkit.inventory.ItemStack> optionalItemStack = BukkitItemManager.instance().s2c(stack.getBukkitStack(), this.player);
        ItemStack matcheItemStack = stack;
        if (optionalItemStack.isPresent()) {
            matcheItemStack = ((CraftItemStack) optionalItemStack.get()).handle;
        }
        return this.item.equals(matcheItemStack.getItemHolder()) && this.components.matches(matcheItemStack.getComponentsPatch(), hashGenerator);
    }
}
