package net.momirealms.craftengine.bukkit.nms.v1_20_3.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class BiomeFilter extends PlacementFilter {
    private final Predicate<Key> filter;

    public BiomeFilter(Predicate<Key> filter) {
        this.filter = filter;
    }

    @Override
    protected boolean shouldPlace(@NotNull PlacementContext context,
                                  @NotNull RandomSource random,
                                  @NotNull BlockPos pos) {
        Holder<Biome> biome = context.getLevel().getBiome(pos);
        ResourceLocation identifier = ((Holder.Reference<Biome>) biome).key().location();
        Key biomeId = new Key(identifier.getNamespace(), identifier.getPath());
        return this.filter.test(biomeId);
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return PlacementModifierType.BIOME_FILTER;
    }
}
