package net.momirealms.craftengine.bukkit.nms.v1_21_4.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.bukkit.world.gen.CraftEngineFeatures;
import net.momirealms.craftengine.bukkit.world.gen.InjectedChunkGenerator;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotWorldConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InjectedCustomChunkGenerator extends ChunkGenerator implements InjectedChunkGenerator {
    private final ChunkGenerator target;
    private final CEWorld world;
    private long lastUpdateFeatureTime;
    private CraftEngineFeatures features;

    public InjectedCustomChunkGenerator(CEWorld world, ChunkGenerator target) {
        super(target.getBiomeSource());
        this.target = target;
        this.world = world;
    }

    @Nullable
    private CraftEngineFeatures getFeatures(WorldGenLevel level) {
        if (!BukkitWorldManager.instance().hasCustomFeatures()) {
            return null;
        }
        if (this.lastUpdateFeatureTime != BukkitWorldManager.instance().lastReloadFeatureTime) {
            this.features = BukkitWorldManager.instance().fetchFeatures(level.getLevel());
            this.lastUpdateFeatureTime = BukkitWorldManager.instance().lastReloadFeatureTime;
        }
        return this.features;
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
                             @NotNull ChunkAccess chunkAccess) {
        this.target.applyCarvers(worldGenRegion, seed, randomState, biomeManager, structureManager, chunkAccess);
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
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Blender blender,
                                                                 @NotNull RandomState randomState,
                                                                 @NotNull StructureManager structureManager,
                                                                 @NotNull ChunkAccess chunkAccess) {
        BukkitWorldManager.instance().handleChunkGenerate(this.world, ChunkPos.of(chunkAccess.locX, chunkAccess.locZ), chunkAccess);
        return this.target.fillFromNoise(blender, randomState, structureManager, chunkAccess);
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
    public @NotNull ChunkGeneratorStructureState createState(@NotNull HolderLookup<StructureSet> holderlookup,
                                                             @NotNull RandomState randomstate,
                                                             long seed,
                                                             @NotNull SpigotWorldConfig conf) {
        return this.target.createState(holderlookup, randomstate, seed, conf);
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
    public void addVanillaDecorations(@NotNull WorldGenLevel level,
                                      @NotNull ChunkAccess chunkAccess,
                                      @NotNull StructureManager structuremanager) {
        this.target.addVanillaDecorations(level, chunkAccess, structuremanager);
    }

    @Override
    public void applyBiomeDecoration(@NotNull WorldGenLevel level,
                                     @NotNull ChunkAccess chunkAccess,
                                     @NotNull StructureManager structureAccessor) {
        this.target.applyBiomeDecoration(level, chunkAccess, structureAccessor);
        CraftEngineFeatures ceFeatures = getFeatures(level);
        if (ceFeatures != null && !ceFeatures.features.isEmpty()) {
            SectionPos sectionPos = SectionPos.of(chunkAccess.getPos(), level.getMinSectionY());
            Set<Holder<Biome>> biomeSet = new ObjectArraySet<>();
            net.minecraft.world.level.ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach((chunkPos) -> {
                ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
                for (LevelChunkSection section : chunk.getSections()) {
                    PalettedContainerRO<Holder<Biome>> biomePalette = section.getBiomes();
                    Objects.requireNonNull(biomeSet);
                    biomePalette.getAll(biomeSet::add);
                }
            });
            biomeSet.retainAll(this.biomeSource.possibleBiomes());
            Set<Integer> featureSet = new HashSet<>();
            for (Holder<Biome> biome : biomeSet) {
                ResourceLocation identifier = ((Holder.Reference<Biome>) biome).key().location();
                List<Integer> byBiome = ceFeatures.getFeatureIdsByBiome(Key.of(identifier.getNamespace(), identifier.getPath()));
                featureSet.addAll(byBiome);
            }
            if (!featureSet.isEmpty()) {
                BlockPos blockPos = sectionPos.origin();
                WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
                for (Integer feature : featureSet) {
                    PlacedFeature placedFeature = (PlacedFeature) ceFeatures.getFeatureById(feature).feature;
                    placedFeature.place(level, this, worldgenRandom, blockPos);
                }
            }
        }
    }

    @Override
    public void applyBiomeDecoration(@NotNull WorldGenLevel level,
                                     @NotNull ChunkAccess chunkAccess,
                                     @NotNull StructureManager structuremanager,
                                     boolean vanilla) {
        this.target.applyBiomeDecoration(level, chunkAccess, structuremanager, vanilla);
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

    @Override
    public void validate() {
        this.target.validate();
    }

    @Override
    public @NotNull Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return this.target.getTypeNameForDataFixer();
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> createBiomes(@NotNull RandomState noiseConfig,
                                                                @NotNull Blender blender,
                                                                @NotNull StructureManager structureAccessor,
                                                                @NotNull ChunkAccess chunk) {
        return this.target.createBiomes(noiseConfig, blender, structureAccessor, chunk);
    }

    @Override
    public void createStructures(@NotNull RegistryAccess registryAccess,
                                 @NotNull ChunkGeneratorStructureState structureState,
                                 @NotNull StructureManager structureManager,
                                 @NotNull ChunkAccess chunk,
                                 @NotNull StructureTemplateManager structureTemplateManager,
                                 @NotNull ResourceKey<Level> level) {
        this.target.createStructures(registryAccess, structureState, structureManager, chunk, structureTemplateManager, level);
    }
}
