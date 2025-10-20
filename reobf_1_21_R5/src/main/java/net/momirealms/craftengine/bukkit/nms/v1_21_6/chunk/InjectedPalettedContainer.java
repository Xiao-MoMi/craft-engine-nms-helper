package net.momirealms.craftengine.bukkit.nms.v1_21_6.chunk;

import io.papermc.paper.antixray.ChunkPacketInfo;
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
    public synchronized void write(@NotNull FriendlyByteBuf buffer, @Nullable ChunkPacketInfo<T> chunkPacketInfo, int chunkSectionIndex) {
        this.target.write(buffer, chunkPacketInfo, chunkSectionIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setTarget(Object target) {
        this.target = (PalettedContainer<T>) target;
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
    public Object target() {
        return target;
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
        this.target.acquire();
    }

    @Override
    public @NotNull PalettedContainer<T> copy() {
        return this.target.copy();
    }

    @Override
    public void count(@NotNull CountConsumer<T> counter) {
        this.target.count(counter);
    }

    @Override
    public @NotNull T get(int index) {
        return this.target.get(index);
    }

    @Override
    public @NotNull T get(int x, int y, int z) {
        return this.target.get(x, y, z);
    }

    public T getVirtual(int index) {
        return this.target.get(index);
    }

    public T getVirtual(int x, int y, int z) {
        return this.target.get(x, y, z);
    }

    @Override
    public void getAll(@NotNull Consumer<T> action) {
        this.target.getAll(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T getAndSet(int x, int y, int z, @NotNull T value) {
        return (T) WorldStorageInjector.GetAndSetInterceptor.INSTANCE.intercept(this, new Object[]{x,y,z,value});
    }
    
    @Override
    public @NotNull T getAndSetUnchecked(int x, int y, int z, @NotNull T value) {
        return this.target.getAndSetUnchecked(x, y, z, value);
    }

    @Override
    public int getSerializedSize() {
        return this.target.getSerializedSize();
    }

    @Override
    public boolean maybeHas(@NotNull Predicate<T> predicate) {
        return this.target.maybeHas(predicate);
    }

    @Override
    public synchronized int onResize(int newBits, @NotNull T object) {
        return this.target.onResize(newBits, object);
    }

    @Override
    public synchronized @NotNull PackedData<T> pack(@NotNull IdMap<T> idList, @NotNull Strategy paletteProvider) {
        return this.target.pack(idList, paletteProvider);
    }

    @Override
    public synchronized void read(@NotNull FriendlyByteBuf buf) {
        this.target.read(buf);
    }

    @Override
    public @NotNull PalettedContainer<T> recreate() {
        return this.target.recreate();
    }

    @Override
    public void release() {
        this.target.release();
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        this.target.write(buf);
    }

    @Override
    public void set(int x, int y, int z, @NotNull T value) {
        this.target.set(x, y, z, value);
    }
}
