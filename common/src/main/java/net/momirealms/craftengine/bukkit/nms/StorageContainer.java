package net.momirealms.craftengine.bukkit.nms;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface StorageContainer {

    int containerSize();

    Object getItemAtSlot(int slot);

    Object removeItemAtSlot(int slot, int amount);

    Object removeItemNoUpdateAtSlot(int slot);

    void setItem(int slot, @NotNull Object stack);

    int maxStackSize();

    void maxStackSize(int size);

    void setContentsChanged();

    boolean stillValid(@NotNull Object player);

    @NotNull List<?> contents();

    void onOpen(@NotNull HumanEntity player);

    void onClose(@NotNull HumanEntity player);

    List<HumanEntity> viewers();

    InventoryHolder owner();

    boolean canPlaceItem(int slot, @NotNull Object stack);

    void startOpen(@NotNull Object player);

    void stopOpen(@NotNull Object player);

    void clearContents();

    @Nullable Location location();

    boolean contentsIsEmpty();

    int @NotNull [] getSlotsForFace(@NotNull Object direction);

    boolean canPlaceItemThroughFace(int i, @NotNull Object itemStack, @Nullable Object direction);

    boolean canTakeItemThroughFace(int i, @NotNull Object itemStack, @NotNull Object direction);

    void setCanPlaceItem(boolean canPlaceItem);

    void setCanTakeItem(boolean canTakeItem);

    boolean canPlaceItem();

    boolean canTakeItem();

    void onContentsChanged(Consumer<StorageContainer> onContentsChanged);
}
