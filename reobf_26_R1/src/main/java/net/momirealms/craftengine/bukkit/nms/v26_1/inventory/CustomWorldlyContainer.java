package net.momirealms.craftengine.bukkit.nms.v26_1.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.world.BukkitContainer;
import net.momirealms.craftengine.core.world.WorldlyContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CustomWorldlyContainer extends CustomContainer implements net.minecraft.world.WorldlyContainer {
    private final WorldlyContainer container;

    public CustomWorldlyContainer(WorldlyContainer container) {
        super((BukkitContainer) container);
        this.container = container;
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        return this.container.getSlotsForFace(DirectionUtils.fromNMSDirection(direction));
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return this.container.canPlaceItemThroughFace(slot, ItemStackUtils.wrap(itemStack), direction == null ? null : DirectionUtils.fromNMSDirection(direction));
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack itemStack, @NotNull Direction direction) {
        return this.container.canTakeItemThroughFace(slot, ItemStackUtils.wrap(itemStack), DirectionUtils.fromNMSDirection(direction));
    }
}
