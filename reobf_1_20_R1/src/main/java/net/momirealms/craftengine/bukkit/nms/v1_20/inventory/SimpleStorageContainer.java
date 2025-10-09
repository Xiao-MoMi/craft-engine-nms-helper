package net.momirealms.craftengine.bukkit.nms.v1_20.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.block.entity.BlockEntityHolder;
import net.momirealms.craftengine.bukkit.nms.StorageContainer;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class SimpleStorageContainer implements WorldlyContainer, StorageContainer {
    private final NonNullList<ItemStack> items;
    private final List<HumanEntity> viewers;
    private final InventoryHolder owner;
    private final int[] slots;
    private final BlockEntity blockEntity;
    private int maxStack = MAX_STACK;
    private boolean canPlaceItem;
    private boolean canTakeItem;
    private MenuType<?> menuType;

    public SimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem) {
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        this.viewers = new ObjectArrayList<>();
        this.owner = owner;
        this.slots = IntStream.range(0, size).toArray();
        this.canPlaceItem = canPlaceItem;
        this.canTakeItem = canTakeItem;
        if (owner instanceof BlockEntityHolder blockEntityHolder) {
            this.blockEntity = blockEntityHolder.blockEntity();
        } else {
            this.blockEntity = null;
        }
    }

    @Override
    public int containerSize() {
        return this.getContainerSize();
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public @NotNull Object getItemStack(int slot) {
        return this.getItem(slot);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public @NotNull Object removeItemStack(int slot, int amount) {
        return this.removeItem(slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = this.getItem(slot);
        ItemStack result;
        if (stack == ItemStack.EMPTY) return stack;
        if (stack.getCount() <= amount) {
            this.setItem(slot, ItemStack.EMPTY);
            result = stack;
        } else {
            result = CraftItemStack.copyNMSStack(stack, amount);
            stack.shrink(amount);
        }
        this.setChanged();
        return result;
    }

    @Override
    public @NotNull Object removeItemStackNoUpdate(int slot) {
        return this.removeItemNoUpdate(slot);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.getItem(slot);
        ItemStack result;
        if (stack == ItemStack.EMPTY) return stack;
        if (stack.getCount() <= 1) {
            this.setItem(slot, ItemStack.EMPTY);
            result = stack;
        } else {
            result = CraftItemStack.copyNMSStack(stack, 1);
            stack.shrink(1);
        }
        return result;
    }

    @Override
    public void setItemStack(int slot, @NotNull Object stack) {
        this.setItem(slot, (ItemStack) stack);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.items.set(slot, stack);
        if (stack != ItemStack.EMPTY && this.getMaxStackSize() > 0 && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int maxItemStackSize() {
        return this.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStack;
    }

    @Override
    public void setMaxItemStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public void setContentsChanged() {
        this.setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@NotNull Object player) {
        return this.stillValid((Player) player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return true;
        BukkitServerPlayer serverPlayer = BukkitCraftEngine.instance().adapt(((ServerPlayer) player).getBukkitEntity());
        if (serverPlayer == null) return false;
        return serverPlayer.canInteractWithBlock(this.blockEntity.pos(), 4);
    }

    @Override
    public @NotNull List<ItemStack> contents() {
        return this.getContents();
    }

    @Override
    public @NotNull List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(@NotNull HumanEntity player) {
        this.onOpen((CraftHumanEntity) player);
    }

    @Override
    public void onOpen(@NotNull CraftHumanEntity player) {
        this.viewers.add(player);
    }

    @Override
    public void onClose(@NotNull HumanEntity player) {
        this.onClose((CraftHumanEntity) player);
    }

    @Override
    public void onClose(@NotNull CraftHumanEntity player) {
        this.viewers.remove(player);
    }

    @Override
    public @NotNull List<HumanEntity> getViewers() {
        return this.viewers;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull Object stack) {
        return this.canPlaceItem(slot, (ItemStack) stack);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return true;
    }

    @Override
    public void startOpen(@NotNull Object player) {
        this.startOpen((Player) player);
    }

    @Override
    public void startOpen(@NotNull Player player) {
    }

    @Override
    public void stopOpen(@NotNull Object player) {
        this.stopOpen((Player) player);
    }

    @Override
    public void stopOpen(@NotNull Player player) {
    }

    @Override
    public void contentClear() {
        this.items.clear();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public @NotNull Location getLocation() {
        if (this.blockEntity == null) return null;
        return new Location(
                (World) this.blockEntity.world().world().platformWorld(),
                this.blockEntity.pos().x(),
                this.blockEntity.pos().y(),
                this.blockEntity.pos().z()
        );
    }

    @Override
    public boolean contentsIsEmpty() {
        return this.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        Iterator<ItemStack> iterator = this.items.iterator();

        ItemStack itemstack;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            itemstack = iterator.next();
        } while (itemstack.isEmpty());

        return false;
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Object direction) {
        return this.getSlotsForFace((Direction) direction);
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        return this.slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NotNull Object itemStack, @Nullable Object direction) {
        return this.canPlaceItemThroughFace(i, (ItemStack) itemStack, (Direction) direction);
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @NotNull Object itemStack, @NotNull Object direction) {
        return this.canTakeItemThroughFace(i, (ItemStack) itemStack, (Direction) direction);
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @NotNull ItemStack itemStack, @NotNull Direction direction) {
        return this.canTakeItem;
    }

    @Override
    public void setCanPlaceItem(boolean canPlaceItem) {
        this.canPlaceItem = canPlaceItem;
    }

    @Override
    public void setCanTakeItem(boolean canTakeItem) {
        this.canTakeItem = canTakeItem;
    }

    @Override
    public boolean canPlaceItem() {
        return this.canPlaceItem;
    }

    @Override
    public boolean canTakeItem() {
        return this.canTakeItem;
    }

    public MenuType<?> getMenuType() {
        if (this.menuType == null) {
            this.menuType = switch (getContainerSize()) {
                case 9 -> MenuType.GENERIC_9x1;
                case 18 -> MenuType.GENERIC_9x2;
                case 27 -> MenuType.GENERIC_9x3;
                case 36 -> MenuType.GENERIC_9x4;
                case 45 -> MenuType.GENERIC_9x5;
                case 54 -> MenuType.GENERIC_9x6;
                default -> throw new IllegalArgumentException("Unsupported custom inventory size " + getContainerSize());
            };
        }
        return this.menuType;
    }

    @Override
    public Object menuType() {
        return getMenuType();
    }
}
