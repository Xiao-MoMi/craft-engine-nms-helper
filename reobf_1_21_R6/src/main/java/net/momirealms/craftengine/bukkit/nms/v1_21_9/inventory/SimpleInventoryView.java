package net.momirealms.craftengine.bukkit.nms.v1_21_9.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftAbstractInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftMenuType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SimpleInventoryView extends CraftAbstractInventoryView {
    private final SimpleStorageContainer container;
    private final CraftInventory inventory;
    private final Player player;
    private String title;

    public SimpleInventoryView(SimpleStorageContainer container, Player player) {
        this.container = container;
        this.inventory = new CraftInventory(container);
        this.player = player;
    }

    @Override
    public @NotNull Inventory getTopInventory() {
        return this.inventory;
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return this.getPlayer().getInventory();
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return this.player.getBukkitEntity();
    }

    @Override
    public @NotNull InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public @NotNull String getTitle() {
        return this.title;
    }

    @Override
    public @NotNull String getOriginalTitle() {
        return this.title;
    }

    @Override
    public void setTitle(@NotNull String title) {
        this.title = title;
        ((ServerPlayer) this.player).connection.send(new ClientboundOpenScreenPacket(
                this.player.containerMenu.containerId, this.container.getMenuType(), Component.literal(this.title)
        ));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable MenuType getMenuType() {
        return CraftMenuType.minecraftToBukkit(this.container.getMenuType());
    }
}
