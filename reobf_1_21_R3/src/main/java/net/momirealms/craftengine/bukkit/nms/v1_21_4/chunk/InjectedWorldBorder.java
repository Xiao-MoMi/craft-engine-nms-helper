package net.momirealms.craftengine.bukkit.nms.v1_21_4.chunk;

import net.minecraft.server.level.ServerLevel;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldHeight;
import net.momirealms.craftengine.core.world.WorldHolder;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.CraftWorldBorder;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class InjectedWorldBorder extends CraftWorldBorder implements BukkitWorld, WorldHolder {
    private final WeakReference<CraftWorld> craftWorld;
    private final WeakReference<ServerLevel> serverLevel;
    private final WorldHeight worldHeight;
    private final UUID uuid;
    private final String name;
    @Nullable
    private CEWorld ceWorld;

    public InjectedWorldBorder(CraftWorld craftWorld) {
        super(craftWorld);
        this.craftWorld = new WeakReference<>(craftWorld);
        this.serverLevel = new WeakReference<>(craftWorld.getHandle());
        this.worldHeight = WorldHeight.create(craftWorld.getMinHeight(), craftWorld.getMaxHeight() - craftWorld.getMinHeight());
        this.uuid = craftWorld.getUID();
        this.name = craftWorld.getName();
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
        return this.serverLevel.get();
    }

    @Override
    public Object platformWorld() {
        return this.craftWorld.get();
    }

    @Override
    public WorldHeight worldHeight() {
        return this.worldHeight;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }
}
