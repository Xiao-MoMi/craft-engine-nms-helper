package net.momirealms.craftengine.bukkit.nms.v26_3.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.util.Direction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class CustomRotatedBlockProvider implements BlockStateProvider {
    public static final MapCodec<CustomRotatedBlockProvider> CODEC = CustomSimpleStateProvider.CODEC.xmap(
            CustomRotatedBlockProvider::new,
            provider -> provider.provider
    );
    private final CustomSimpleStateProvider provider;

    private CustomRotatedBlockProvider(CustomSimpleStateProvider provider) {
        this.provider = provider;
    }

    @Override
    public @NotNull MapCodec<CustomRotatedBlockProvider> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockState getState(@NotNull LevelAccessor level, @NotNull RandomSource random, @NotNull BlockPos pos) {
        BlockState state = this.provider.getState(level, random, pos);
        if (state instanceof DelegatingBlockState holder) {
            ImmutableBlockState immutableBlockState = holder.blockState();
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return state;
            Property<?> property = immutableBlockState.owner().value().getProperty("axis");
            if (property == null || property.valueClass() != Direction.Axis.class) {
                return (BlockState) immutableBlockState.customBlockState().minecraftState();
            }
            Direction.Axis axis = Direction.Axis.values()[random.nextInt(Direction.Axis.VALUES.length)];
            return (BlockState) immutableBlockState.with((Property<Direction.Axis>)property, axis).customBlockState().minecraftState();
        } else {
            net.minecraft.core.Direction.Axis axis = net.minecraft.core.Direction.Axis.getRandom(random);
            return state.getBlock().defaultBlockState().trySetValue(RotatedPillarBlock.AXIS, axis);
        }
    }
}
