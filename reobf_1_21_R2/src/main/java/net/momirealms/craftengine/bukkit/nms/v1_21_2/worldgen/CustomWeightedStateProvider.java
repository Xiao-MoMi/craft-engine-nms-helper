package net.momirealms.craftengine.bukkit.nms.v1_21_2.worldgen;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class CustomWeightedStateProvider extends BlockStateProvider {
    public static final MapCodec<CustomWeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(CustomSimpleStateProvider.DIRECT_CODEC)
            .comapFlatMap(CustomWeightedStateProvider::create, (provider) -> provider.weightedList)
            .fieldOf("entries");
    public static final BlockStateProviderType<CustomWeightedStateProvider> TYPE;

    static {
        try {
            TYPE = ReflectionUtils.setAccessible(BlockStateProviderType.class.getDeclaredConstructor(MapCodec.class)).newInstance(CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final SimpleWeightedRandomList<CustomSimpleStateProvider> weightedList;

    private CustomWeightedStateProvider(SimpleWeightedRandomList<CustomSimpleStateProvider> weightedList) {
        this.weightedList = weightedList;
    }

    private static DataResult<CustomWeightedStateProvider> create(SimpleWeightedRandomList<CustomSimpleStateProvider> weightedList) {
        return weightedList.isEmpty() ? DataResult.error(() -> "CustomWeightedStateProvider with no states") : DataResult.success(new CustomWeightedStateProvider(weightedList));
    }

    @Override
    protected @NotNull BlockStateProviderType<?> type() {
        return TYPE;
    }

    @Override
    public @NotNull BlockState getState(@NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        return this.weightedList.getRandomValue(randomSource).orElseThrow(IllegalStateException::new).getState(randomSource, blockPos);
    }
}
