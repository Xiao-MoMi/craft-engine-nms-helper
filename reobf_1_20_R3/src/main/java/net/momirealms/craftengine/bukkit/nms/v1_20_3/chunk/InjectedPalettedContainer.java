package net.momirealms.craftengine.bukkit.nms.v1_20_3.chunk;

import com.destroystokyo.paper.antixray.ChunkPacketInfo;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.momirealms.craftengine.bukkit.plugin.injector.WorldStorageInjector;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class InjectedPalettedContainer<T> extends PalettedContainer<T> implements InjectedHolder.Palette {
    private PalettedContainer<T> target;
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
    @SuppressWarnings("unchecked")
    public void setTarget(Object target) {
        this.target = (PalettedContainer<T>) target;
    }

    @Override
    public Object target() {
        return target;
    }

    @Override
    public CEChunk ceChunk() {
        return this.chunk;
    }

    @Override
    public void ceChunk(CEChunk ceChunk) {
        this.chunk = ceChunk;
    }

    @Override
    public CESection ceSection() {
        return this.section;
    }

    @Override
    public void ceSection(CESection ceSection) {
        this.section = ceSection;
    }

    @Override
    public SectionPos cePos() {
        return this.sectionPos;
    }

    @Override
    public void cePos(SectionPos sectionPos) {
        this.sectionPos = sectionPos;
    }

    @Override
    public void acquire() {
        target.acquire();
    }

    @Override
    public @NotNull PalettedContainer<T> copy() {
        return target.copy();
    }

    @Override
    public void count(@NotNull CountConsumer<T> counter) {
        target.count(counter);
    }

    @Override
    public void forEachLocation(@NotNull CountConsumer<T> consumer) {
        target.forEachLocation(consumer);
    }

    @Override
    public @NotNull T get(int index) {
        return target.get(index);
    }

    @Override
    public @NotNull T get(int x, int y, int z) {
        return target.get(x, y, z);
    }

    @Override
    public void getAll(@NotNull Consumer<T> action) {
        target.getAll(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T getAndSet(int x, int y, int z, @NotNull T value) {
        return (T) WorldStorageInjector.GetAndSetInterceptor.INSTANCE.intercept(this, new Object[]{x,y,z,value});
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T getAndSetUnchecked(int x, int y, int z, @NotNull T value) {
        return (T) WorldStorageInjector.GetAndSetUncheckedInterceptor.INSTANCE.intercept(this, new Object[]{x,y,z,value});
    }

    @Override
    public int getSerializedSize() {
        return target.getSerializedSize();
    }

    @Override
    public boolean maybeHas(@NotNull Predicate<T> predicate) {
        return target.maybeHas(predicate);
    }

    @Override
    public synchronized int onResize(int newBits, @NotNull T object) {
        return target.onResize(newBits, object);
    }

    @Override
    public synchronized @NotNull PackedData<T> pack(@NotNull IdMap<T> idList, @NotNull Strategy paletteProvider) {
        return target.pack(idList, paletteProvider);
    }

    @Override
    public synchronized void read(@NotNull FriendlyByteBuf buf) {
        target.read(buf);
    }

    @Override
    public @NotNull PalettedContainer<T> recreate() {
        return target.recreate();
    }

    @Override
    public void release() {
        target.release();
    }

    @Override
    public void set(int x, int y, int z, @NotNull T value) {
        target.set(x, y, z, value);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        target.write(buf);
    }

    @Override
    public synchronized void write(@NotNull FriendlyByteBuf buf, @Nullable ChunkPacketInfo<T> chunkPacketInfo, int chunkSectionIndex) {
        target.write(buf, chunkPacketInfo, chunkSectionIndex);
    }
}
