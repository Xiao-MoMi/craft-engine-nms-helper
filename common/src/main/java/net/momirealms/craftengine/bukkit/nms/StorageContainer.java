package net.momirealms.craftengine.bukkit.nms;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface StorageContainer {

    int containerSize();

    Object getItemStack(int slot);

    Object removeItemStack(int slot, int amount);

    Object removeItemStackNoUpdate(int slot);

    void setItemStack(int slot, @NotNull Object stack);

    int maxItemStackSize();

    void setMaxItemStackSize(int size);

    void setContentsChanged();

    boolean stillValid(@NotNull Object player);

    @NotNull List<?> contents();

    void onOpen(@NotNull HumanEntity player);

    void onClose(@NotNull HumanEntity player);

    List<HumanEntity> getViewers();

    InventoryHolder getOwner();

    boolean canPlaceItem(int slot, @NotNull Object stack);

    void startOpen(@NotNull Object player);

    void stopOpen(@NotNull Object player);

    void contentClear();

    @Nullable Location getLocation();

    boolean contentsIsEmpty();

    int @NotNull [] getSlotsForFace(@NotNull Object direction);

    boolean canPlaceItemThroughFace(int i, @NotNull Object itemStack, @Nullable Object direction);

    boolean canTakeItemThroughFace(int i, @NotNull Object itemStack, @NotNull Object direction);

    void setCanPlaceItem(boolean canPlaceItem);

    void setCanTakeItem(boolean canTakeItem);

    boolean canPlaceItem();

    boolean canTakeItem();

    Object menuType();
}
