package net.momirealms.craftengine.bukkit.nms.v1_20.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class CustomRotatedBlockProvider extends BlockStateProvider {
    public static final Codec<CustomRotatedBlockProvider> CODEC = CustomSimpleStateProvider.CODEC.xmap(
            CustomRotatedBlockProvider::new,
            provider -> provider.provider
    );
    public static final BlockStateProviderType<CustomRotatedBlockProvider> TYPE;

    static {
        try {
            TYPE = ReflectionUtils.setAccessible(BlockStateProviderType.class.getDeclaredConstructor(Codec.class)).newInstance(CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final CustomSimpleStateProvider provider;

    private CustomRotatedBlockProvider(CustomSimpleStateProvider provider) {
        this.provider = provider;
    }

    @Override
    protected @NotNull BlockStateProviderType<?> type() {
        return TYPE;
    }

    @Override
    public @NotNull BlockState getState(@NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        BlockState state = this.provider.getState(randomSource, blockPos);
        if (state instanceof DelegatingBlockState holder) {
            ImmutableBlockState immutableBlockState = holder.blockState();
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return state;
            Property<?> property = immutableBlockState.owner().value().getProperty("axis");
            if (property == null || property.valueClass() != Direction.Axis.class) {
                return (BlockState) immutableBlockState.customBlockState().literalObject();
            }
            Direction.Axis axis = Direction.Axis.values()[randomSource.nextInt(Direction.Axis.VALUES.length)];
            return (BlockState) immutableBlockState.with((Property<Direction.Axis>)property, axis).customBlockState().literalObject();
        } else {
            net.minecraft.core.Direction.Axis axis = net.minecraft.core.Direction.Axis.getRandom(randomSource);
            return state.getBlock().defaultBlockState().trySetValue(RotatedPillarBlock.AXIS, axis);
        }
    }
}
