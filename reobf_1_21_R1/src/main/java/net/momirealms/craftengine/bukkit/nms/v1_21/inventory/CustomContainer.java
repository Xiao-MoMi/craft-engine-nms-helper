package net.momirealms.craftengine.bukkit.nms.v1_21.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.nms.ContainerMarker;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitContainer;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CustomContainer implements Container, ContainerMarker {
    private final BukkitContainer container;

    public CustomContainer(BukkitContainer container) {
        this.container = container;
    }

    @Override
    public int getContainerSize() {
        return this.container.containerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.container.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return (ItemStack) this.container.getItem(slot).minecraftItem();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        return (ItemStack) this.container.removeItem(slot, count).minecraftItem();
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return (ItemStack) this.container.removeItemNoUpdate(slot).minecraftItem();
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack itemStack) {
        this.container.setItem(slot, ItemStackUtils.wrap(itemStack));
    }

    @Override
    public int getMaxStackSize() {
        return this.container.maxStackSize();
    }

    @Override
    public void setChanged() {
        this.container.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(BukkitAdaptor.adapt((org.bukkit.entity.Player) player.getBukkitEntity()));
    }

    @Override
    public @NotNull List<ItemStack> getContents() {
        List<ItemStack> contents = new ArrayList<>(this.getContainerSize());
        for (Item item : this.container) {
            contents.add((ItemStack) item.minecraftItem());
        }
        return contents;
    }

    @Override
    public void onOpen(@NotNull CraftHumanEntity humanEntity) {
        this.container.onOpen(humanEntity);
    }

    @Override
    public void onClose(@NotNull CraftHumanEntity humanEntity) {
        this.container.onClose(humanEntity);
    }

    @Override
    public @NotNull List<HumanEntity> getViewers() {
        return this.container.getViewers();
    }

    @Override
    public @Nullable InventoryHolder getOwner() {
        return this.container.getOwner();
    }

    @Override
    public void setMaxStackSize(int size) {
        this.container.setMaxStackSize(size);
    }

    @Override
    public void clearContent() {
        this.container.clearContent();
    }

    @Override
    public @NotNull Location getLocation() {
        WorldPosition position = this.container.position();
        if (position == null) {
            return null;
        }
        return LocationUtils.toLocation(position);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack itemStack) {
        return this.container.getMaxStackSize(ItemStackUtils.wrap(itemStack));
    }

    @Override
    public void startOpen(Player player) {
        this.container.startOpen(BukkitAdaptor.adapt((org.bukkit.entity.Player) player.getBukkitEntity()));
    }

    @Override
    public void stopOpen(Player player) {
        this.container.stopOpen(BukkitAdaptor.adapt((org.bukkit.entity.Player) player.getBukkitEntity()));
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack itemStack) {
        return this.container.canPlaceItem(slot, ItemStackUtils.wrap(itemStack));
    }

    @Override
    public boolean canTakeItem(@NotNull Container into, int slot, @NotNull ItemStack itemStack) {
        return this.container.canTakeItem(into, slot, ItemStackUtils.wrap(itemStack));
    }

    @Override
    public boolean hasAnyMatching(@NotNull Predicate<ItemStack> predicate) {
        return this.container.hasAnyMatching(item -> predicate.test((ItemStack) item.minecraftItem()));
    }
}
