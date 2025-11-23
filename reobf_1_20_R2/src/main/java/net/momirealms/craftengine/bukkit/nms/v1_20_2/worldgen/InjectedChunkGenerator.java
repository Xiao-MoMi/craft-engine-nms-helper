package net.momirealms.craftengine.bukkit.nms.v1_20_2.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotWorldConfig;

import java.util.List;
import java.util.Optional;
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
    protected @NotNull Codec<? extends ChunkGenerator> codec() {
        return ChunkGenerator.CODEC;
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

    @Override
    public void createStructures(@NotNull RegistryAccess registryManager,
                                 @NotNull ChunkGeneratorStructureState placementCalculator,
                                 @NotNull StructureManager structureAccessor,
                                 @NotNull ChunkAccess chunk,
                                 @NotNull StructureTemplateManager structureTemplateManager) {
        this.target.createStructures(registryManager, placementCalculator, structureAccessor, chunk, structureTemplateManager);
    }

    @Override
    public @NotNull ChunkGeneratorStructureState createState(@NotNull HolderLookup<StructureSet> holderlookup,
                                                             @NotNull RandomState randomstate,
                                                             long seed,
                                                             @NotNull SpigotWorldConfig conf) {
        return this.target.createState(holderlookup, randomstate, seed, conf);
    }

    @Override
    public @NotNull Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return this.target.getTypeNameForDataFixer();
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> createBiomes(@NotNull Executor executor,
                                                                @NotNull RandomState noiseConfig,
                                                                @NotNull Blender blender,
                                                                @NotNull StructureManager structureAccessor,
                                                                @NotNull ChunkAccess chunk) {
        return this.target.createBiomes(executor, noiseConfig, blender, structureAccessor, chunk);
    }

    @Override
    public @Nullable Pair<BlockPos, Holder<Structure>> findNearestMapStructure(@NotNull ServerLevel world,
                                                                               @NotNull HolderSet<Structure> structures,
                                                                               @NotNull BlockPos center,
                                                                               int radius,
                                                                               boolean skipReferencedStructures) {
        return this.target.findNearestMapStructure(world, structures, center, radius, skipReferencedStructures);
    }

    @Override
    public void addVanillaDecorations(@NotNull WorldGenLevel generatoraccessseed,
                                      @NotNull ChunkAccess ichunkaccess,
                                      @NotNull StructureManager structuremanager) {
        this.target.addVanillaDecorations(generatoraccessseed, ichunkaccess, structuremanager);
    }

    @Override
    public void applyBiomeDecoration(@NotNull WorldGenLevel world,
                                     @NotNull ChunkAccess chunk,
                                     @NotNull StructureManager structureAccessor) {
        this.target.applyBiomeDecoration(world, chunk, structureAccessor);
    }

    @Override
    public void applyBiomeDecoration(@NotNull WorldGenLevel generatoraccessseed,
                                     @NotNull ChunkAccess ichunkaccess,
                                     @NotNull StructureManager structuremanager,
                                     boolean vanilla) {
        this.target.applyBiomeDecoration(generatoraccessseed, ichunkaccess, structuremanager, vanilla);
    }

    @Override
    public int getSpawnHeight(@NotNull LevelHeightAccessor world) {
        return this.target.getSpawnHeight(world);
    }

    @Override
    public @NotNull BiomeSource getBiomeSource() {
        return this.target.getBiomeSource();
    }

    @Override
    public @NotNull WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(@NotNull Holder<Biome> biome,
                                                                               @NotNull StructureManager accessor,
                                                                               @NotNull MobCategory group,
                                                                               @NotNull BlockPos pos) {
        return this.target.getMobsAt(biome, accessor, group, pos);
    }

    @Override
    public void createReferences(@NotNull WorldGenLevel world,
                                 @NotNull StructureManager structureAccessor,
                                 @NotNull ChunkAccess chunk) {
        this.target.createReferences(world, structureAccessor, chunk);
    }

    @Override
    public int getFirstFreeHeight(int x,
                                  int z,
                                  Heightmap.@NotNull Types heightmap,
                                  @NotNull LevelHeightAccessor world,
                                  @NotNull RandomState noiseConfig) {
        return this.target.getFirstFreeHeight(x, z, heightmap, world, noiseConfig);
    }

    @Override
    public int getFirstOccupiedHeight(int x,
                                      int z,
                                      Heightmap.@NotNull Types heightmap,
                                      @NotNull LevelHeightAccessor world,
                                      @NotNull RandomState noiseConfig) {
        return this.target.getFirstOccupiedHeight(x, z, heightmap, world, noiseConfig);
    }
}
