package net.momirealms.craftengine.bukkit.nms.v1_21_5;

import io.papermc.paper.antixray.ChunkPacketInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class InjectedLevelChunkSection extends LevelChunkSection {
    private final LevelChunkSection target;

    public InjectedLevelChunkSection(PalettedContainer<BlockState> states, PalettedContainer<Holder<Biome>> biomes, LevelChunkSection target) {
        super(states, biomes);
        this.target = target;
    }

    @Override
    public void acquire() {
        this.target.acquire();
    }

    @Override
    public @NotNull LevelChunkSection copy() {
        return this.target.copy();
    }

    @Override
    public void fillBiomesFromNoise(@NotNull BiomeResolver biomeResolver, Climate.@NotNull Sampler climateSampler, int x, int y, int z) {
        this.target.fillBiomesFromNoise(biomeResolver, climateSampler, x, y, z);
    }

    @Override
    public @NotNull PalettedContainerRO<Holder<Biome>> getBiomes() {
        return this.target.getBiomes();
    }

    @Override
    public @NotNull BlockState getBlockState(int x, int y, int z) {
        return this.target.getBlockState(x, y, z);
    }

    @Override
    public @NotNull FluidState getFluidState(int x, int y, int z) {
        return this.target.getFluidState(x, y, z);
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return this.target.getNoiseBiome(x, y, z);
    }

    @Override
    public int getSerializedSize() {
        return this.target.getSerializedSize();
    }

    @Override
    public @NotNull PalettedContainer<BlockState> getStates() {
        return this.target.getStates();
    }

    @Override
    public boolean hasOnlyAir() {
        return this.target.hasOnlyAir();
    }

    @Override
    public boolean isRandomlyTicking() {
        return this.target.isRandomlyTicking();
    }

    @Override
    public boolean isRandomlyTickingBlocks() {
        return this.target.isRandomlyTickingBlocks();
    }

    @Override
    public boolean isRandomlyTickingFluids() {
        return this.target.isRandomlyTickingFluids();
    }

    @Override
    public boolean maybeHas(@NotNull Predicate<BlockState> predicate) {
        return this.target.maybeHas(predicate);
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buffer) {
        this.target.read(buffer);
    }

    @Override
    public void readBiomes(@NotNull FriendlyByteBuf buffer) {
        this.target.readBiomes(buffer);
    }

    @Override
    public void recalcBlockCounts() {
        if (target == null) return;
        this.target.recalcBlockCounts();
    }

    @Override
    public void release() {
        this.target.release();
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Holder<Biome> biome) {
        this.target.setBiome(x, y, z, biome);
    }

    @Override
    public @NotNull BlockState setBlockState(int x, int y, int z, @NotNull BlockState state) {
        return this.target.setBlockState(x, y, z, state);
    }

    @Override
    public @NotNull BlockState setBlockState(int x, int y, int z, @NotNull BlockState state, boolean useLocks) {
        return this.target.setBlockState(x, y, z, state, useLocks);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        this.target.write(buffer);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer, @NotNull ChunkPacketInfo<BlockState> chunkPacketInfo, int chunkSectionIndex) {
        this.target.write(buffer, chunkPacketInfo, chunkSectionIndex);
    }
}
