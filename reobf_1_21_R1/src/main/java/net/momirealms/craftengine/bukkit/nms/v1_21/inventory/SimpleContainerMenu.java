package net.momirealms.craftengine.bukkit.nms.v1_21.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleContainerMenu extends AbstractContainerMenu {
    private final SimpleStorageContainer container;
    private final SimpleInventoryView view;
    private final AbstractContainerMenu delegate;

    public SimpleContainerMenu(SimpleStorageContainer container, int containerId, Player player, @Nullable Component title) {
        super(container.getMenuType(), containerId);
        this.container = container;
        this.view = new SimpleInventoryView(container, player);
        this.delegate = new ChestMenu(container.getMenuType(), -1, player.getInventory(), container, container.getContainerSize() / 9);
        super.lastSlots = this.delegate.lastSlots;
        super.slots = this.delegate.slots;
        super.remoteSlots = this.delegate.remoteSlots;
        super.dataSlots = this.delegate.dataSlots;
        super.remoteDataSlots = this.delegate.remoteDataSlots;
        if (title != null) this.setTitle(title);
    }

    @Override
    public @NotNull InventoryView getBukkitView() {
        return this.view;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return this.delegate.quickMoveStack(player, slot);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }
}
