package net.momirealms.craftengine.bukkit.nms.v26_3.worldgen;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class CustomWeightedStateProvider implements BlockStateProvider {

    public static final MapCodec<CustomWeightedStateProvider> CODEC = WeightedList.nonEmptyCodec(CustomSimpleStateProvider.DIRECT_CODEC)
            .comapFlatMap(CustomWeightedStateProvider::create, (provider) -> provider.weightedList)
            .fieldOf("entries");
    private final WeightedList<CustomSimpleStateProvider> weightedList;

    private CustomWeightedStateProvider(WeightedList<CustomSimpleStateProvider> weightedList) {
        this.weightedList = weightedList;
    }

    private static DataResult<CustomWeightedStateProvider> create(WeightedList<CustomSimpleStateProvider> weightedList) {
        return weightedList.isEmpty() ? DataResult.error(() -> "CustomWeightedStateProvider with no states") : DataResult.success(new CustomWeightedStateProvider(weightedList));
    }

    @Override
    public @NotNull MapCodec<CustomWeightedStateProvider> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockState getState(@NotNull LevelAccessor level, @NotNull RandomSource random, @NotNull BlockPos pos) {
        return this.weightedList.getRandomOrThrow(random).getState(level, random, pos);
    }
}
