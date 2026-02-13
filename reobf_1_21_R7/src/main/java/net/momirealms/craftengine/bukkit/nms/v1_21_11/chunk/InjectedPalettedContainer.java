package net.momirealms.craftengine.bukkit.nms.v1_21_11.chunk;

import io.papermc.paper.antixray.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import net.momirealms.craftengine.bukkit.plugin.injector.WorldStorageInjector;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class InjectedPalettedContainer<T> extends PalettedContainer<T> implements InjectedStorage.Palette {
    public PalettedContainer<T> delegated;
    private CESection section;
    private CEChunk chunk;
    private SectionPos sectionPos;
    private boolean isActive;

    public InjectedPalettedContainer(T object, Strategy<T> paletteProvider, T @Nullable [] presetValues) {
        super(object, paletteProvider, presetValues);
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setActive(boolean b) {
        this.isActive = b;
    }

    @Override
    public synchronized void write(@NotNull FriendlyByteBuf buffer, @Nullable ChunkPacketInfo<T> chunkPacketInfo, int chunkSectionIndex) {
        this.delegated.write(buffer, chunkPacketInfo, chunkSectionIndex);
    }

    @Override
    public Object delegated() {
        return this.delegated;
    }

    @Override
    public CESection section() {
        return this.section;
    }

    @Override
    public void setSection(CESection ceSection) {
        this.section = ceSection;
    }

    @Override
    public CEChunk chunk() {
        return this.chunk;
    }

    @Override
    public void setChunk(CEChunk ceChunk) {
        this.chunk = ceChunk;
    }

    @Override
    public SectionPos pos() {
        return this.sectionPos;
    }

    @Override
    public void setPos(SectionPos sectionPos) {
        this.sectionPos = sectionPos;
    }

    @Override
    public void acquire() {
        this.delegated.acquire();
    }

    @Override
    public @NotNull PalettedContainer<T> copy() {
        return this.delegated.copy();
    }

    @Override
    public void count(@NotNull CountConsumer<T> counter) {
        this.delegated.count(counter);
    }

    @Override
    public @NotNull T get(int index) {
        return this.delegated.get(index);
    }

    @Override
    public @NotNull T get(int x, int y, int z) {
        return this.delegated.get(x, y, z);
    }

    public T getVirtual(int index) {
        return this.delegated.get(index);
    }

    public T getVirtual(int x, int y, int z) {
        return this.delegated.get(x, y, z);
    }

    @Override
    public void getAll(@NotNull Consumer<T> action) {
        this.delegated.getAll(action);
    }

    @Override
    public @NotNull T getAndSet(int x, int y, int z, @NotNull T value) {
        T old = this.delegated.getAndSet(x, y, z, value);
        if (this.isActive) {
            WorldStorageInjector.compareAndUpdateBlockState(x, y, z, value, old, this);
        }
        return old;
    }

    @Override
    public @NotNull T getAndSetUnchecked(int x, int y, int z, @NotNull T value) {
        T old = this.delegated.getAndSetUnchecked(x, y, z, value);
        if (this.isActive) {
            WorldStorageInjector.compareAndUpdateBlockState(x, y, z, value, old, this);
        }
        return old;
    }

    @Override
    public int getSerializedSize() {
        return this.delegated.getSerializedSize();
    }

    @Override
    public int bitsPerEntry() {
        return this.delegated.bitsPerEntry();
    }

    @Override
    public boolean maybeHas(@NotNull Predicate<T> predicate) {
        return this.delegated.maybeHas(predicate);
    }

    @Override
    public synchronized int onResize(int newBits, @NotNull T object) {
        return this.delegated.onResize(newBits, object);
    }

    @Override
    public synchronized @NotNull PalettedContainerRO.PackedData<T> pack(@NotNull Strategy<T> paletteProvider) {
        return this.delegated.pack(paletteProvider);
    }

    @Override
    public synchronized void read(@NotNull FriendlyByteBuf buf) {
        this.delegated.read(buf);
    }

    @Override
    public @NotNull PalettedContainer<T> recreate() {
        return this.delegated.recreate();
    }

    @Override
    public void release() {
        this.delegated.release();
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        this.delegated.write(buf);
    }

    @Override
    public void set(int x, int y, int z, @NotNull T value) {
        this.delegated.set(x, y, z, value);
    }
}
