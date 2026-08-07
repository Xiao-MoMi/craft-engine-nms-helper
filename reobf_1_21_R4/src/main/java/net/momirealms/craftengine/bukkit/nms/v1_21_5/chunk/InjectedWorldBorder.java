package net.momirealms.craftengine.bukkit.nms.v1_21_5.chunk;

import net.minecraft.server.level.ServerLevel;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldHeight;
import net.momirealms.craftengine.core.world.WorldHolder;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.CraftWorldBorder;
import org.jetbrains.annotations.Nullable;

public class InjectedWorldBorder extends CraftWorldBorder implements BukkitWorld, WorldHolder {
    private final CraftWorld craftWorld;
    private final ServerLevel serverLevel;
    private final WorldHeight worldHeight;
    @Nullable
    private CEWorld ceWorld;

    public InjectedWorldBorder(CraftWorld craftWorld) {
        super(craftWorld);
        this.craftWorld = craftWorld;
        this.serverLevel = craftWorld.getHandle();
        this.worldHeight = WorldHeight.create(craftWorld.getMinHeight(), craftWorld.getMaxHeight() - craftWorld.getMinHeight());
    }

    @Override
    public void setStorageWorld(CEWorld ceWorld) {
        this.ceWorld = ceWorld;
    }

    @Override
    public CEWorld storageWorld() {
        return this.ceWorld;
    }

    @Override
    public Object minecraftWorld() {
        return this.serverLevel;
    }

    @Override
    public Object platformWorld() {
        return this.craftWorld;
    }

    @Override
    public WorldHeight worldHeight() {
        return this.worldHeight;
    }
}
