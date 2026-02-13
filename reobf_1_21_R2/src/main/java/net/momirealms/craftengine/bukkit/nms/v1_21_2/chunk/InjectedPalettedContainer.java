package net.momirealms.craftengine.bukkit.nms.v1_21_2.chunk;

import com.destroystokyo.paper.antixray.ChunkPacketInfo;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;
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

    public InjectedPalettedContainer(IdMap<T> idList, T object, Strategy paletteProvider, T @Nullable [] presetValues) {
        super(idList, object, paletteProvider, presetValues);
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
        delegated.acquire();
    }

    @Override
    public @NotNull PalettedContainer<T> copy() {
        return delegated.copy();
    }

    @Override
    public void count(@NotNull CountConsumer<T> counter) {
        delegated.count(counter);
    }

    @Override
    public @NotNull T get(int index) {
        return delegated.get(index);
    }

    @Override
    public @NotNull T get(int x, int y, int z) {
        return delegated.get(x, y, z);
    }

    @Override
    public void getAll(@NotNull Consumer<T> action) {
        delegated.getAll(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T getAndSet(int x, int y, int z, @NotNull T value) {
        return (T) WorldStorageInjector.getAndSet(this, x, y, z, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T getAndSetUnchecked(int x, int y, int z, @NotNull T value) {
        return (T) WorldStorageInjector.getAndSetUnchecked(this, x, y, z, value);
    }

    @Override
    public int getSerializedSize() {
        return delegated.getSerializedSize();
    }

    @Override
    public boolean maybeHas(@NotNull Predicate<T> predicate) {
        return delegated.maybeHas(predicate);
    }

    @Override
    public synchronized int onResize(int newBits, @NotNull T object) {
        return delegated.onResize(newBits, object);
    }

    @Override
    public synchronized @NotNull PackedData<T> pack(@NotNull IdMap<T> idList, @NotNull Strategy paletteProvider) {
        return delegated.pack(idList, paletteProvider);
    }

    @Override
    public synchronized void read(@NotNull FriendlyByteBuf buf) {
        delegated.read(buf);
    }

    @Override
    public @NotNull PalettedContainer<T> recreate() {
        return delegated.recreate();
    }

    @Override
    public void release() {
        delegated.release();
    }

    @Override
    public void set(int x, int y, int z, @NotNull T value) {
        delegated.set(x, y, z, value);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        delegated.write(buf);
    }

    @Override
    public synchronized void write(@NotNull FriendlyByteBuf buf, @Nullable ChunkPacketInfo<T> chunkPacketInfo, int chunkSectionIndex) {
        delegated.write(buf, chunkPacketInfo, chunkSectionIndex);
    }
}
