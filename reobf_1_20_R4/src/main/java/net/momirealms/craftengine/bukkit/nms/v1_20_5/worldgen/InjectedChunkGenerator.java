package net.momirealms.craftengine.bukkit.nms.v1_20_5.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class InjectedChunkGenerator extends ChunkGenerator {
    private final ChunkGenerator target;
    private final CEWorld world;

    public InjectedChunkGenerator(CEWorld world, ChunkGenerator target) {
        super(target.getBiomeSource());
        this.target = target;
        this.world = world;
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return MapCodec.assumeMapUnsafe(ChunkGenerator.CODEC);
    }

    @Override
    public void applyCarvers(@NotNull WorldGenRegion worldGenRegion,
                             long seed,
                             @NotNull RandomState randomState,
                             @NotNull BiomeManager biomeManager,
                             @NotNull StructureManager structureManager,
                             @NotNull ChunkAccess chunkAccess,
                             GenerationStep.@NotNull Carving carving) {
        this.target.applyCarvers(worldGenRegion, seed, randomState, biomeManager, structureManager, chunkAccess, carving);
    }

    @Override
    public void buildSurface(@NotNull WorldGenRegion worldGenRegion,
                             @NotNull StructureManager structureManager,
                             @NotNull RandomState randomState,
                             @NotNull ChunkAccess chunkAccess) {
        this.target.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
    }

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion worldGenRegion) {
        this.target.spawnOriginalMobs(worldGenRegion);
    }

    @Override
    public int getGenDepth() {
        return this.target.getGenDepth();
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Executor executor,
                                                                 @NotNull Blender blender,
                                                                 @NotNull RandomState randomState,
                                                                 @NotNull StructureManager structureManager,
                                                                 @NotNull ChunkAccess chunkAccess) {
        BukkitWorldManager.instance().handleChunkGenerate(this.world, ChunkPos.of(chunkAccess.locX, chunkAccess.locZ), chunkAccess);
        return this.target.fillFromNoise(executor, blender, randomState, structureManager, chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return this.target.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return this.target.getMinY();
    }

    @Override
    public int getBaseHeight(int x,
                             int z,
                             Heightmap.@NotNull Types types,
                             @NotNull LevelHeightAccessor levelHeightAccessor,
                             @NotNull RandomState randomState) {
        return this.target.getBaseHeight(x, z, types, levelHeightAccessor, randomState);
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x,
                                              int z,
                                              @NotNull LevelHeightAccessor levelHeightAccessor,
                                              @NotNull RandomState randomState) {
        return this.target.getBaseColumn(x, z, levelHeightAccessor, randomState);
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> list,
                                   @NotNull RandomState randomState,
                                   @NotNull BlockPos blockPos) {
        this.target.addDebugScreenInfo(list, randomState, blockPos);
    }
}
