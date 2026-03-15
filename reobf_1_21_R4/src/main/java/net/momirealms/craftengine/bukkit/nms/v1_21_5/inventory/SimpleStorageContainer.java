package net.momirealms.craftengine.bukkit.nms.v1_21_5.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.entity.BlockEntityHolder;
import net.momirealms.craftengine.bukkit.nms.StorageContainer;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
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
    private Consumer<StorageContainer> onContentsChanged;

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
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public int containerSize() {
        return this.items.size();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public @NotNull ItemStack getItemAtSlot(int slot) {
        return this.items.get(slot);
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
    public @NotNull ItemStack removeItemAtSlot(int slot, int amount) {
        return this.removeItem(slot, amount);
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
    public @NotNull ItemStack removeItemNoUpdateAtSlot(int slot) {
        return this.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, @NotNull Object stack) {
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
    public int getMaxStackSize() {
        return this.maxStack;
    }

    @Override
    public int maxStackSize() {
        return this.maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public void maxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public void setChanged() {
        if (this.onContentsChanged != null) {
            this.onContentsChanged.accept(this);
        }
    }

    @Override
    public void setContentsChanged() {
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Object player) {
        return this.stillValid((Player) player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return true;
        BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(((ServerPlayer) player).getBukkitEntity());
        if (serverPlayer == null) return false;
        return serverPlayer.canInteractWithBlock(this.blockEntity.pos(), 4);
    }

    @Override
    public @NotNull List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public @NotNull List<ItemStack> contents() {
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
    public @NotNull List<HumanEntity> viewers() {
        return this.viewers;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner;
    }

    @Override
    public InventoryHolder owner() {
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
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void clearContents() {
        this.items.clear();
    }

    @Override
    public @Nullable Location getLocation() {
        if (this.blockEntity == null) return null;
        return new Location(
                (World) this.blockEntity.world().world().platformWorld(),
                this.blockEntity.pos().x(),
                this.blockEntity.pos().y(),
                this.blockEntity.pos().z()
        );
    }

    @Override
    public @Nullable Location location() {
        return this.getLocation();
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
    public boolean contentsIsEmpty() {
        return this.isEmpty();
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

    @Override
    public void onContentsChanged(Consumer<StorageContainer> onContentsChanged) {
        this.onContentsChanged = onContentsChanged;
    }
}
