package net.momirealms.craftengine.bukkit.nms.v1_21.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomSimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
    public static final CustomSimpleBlockFeature INSTANCE = new CustomSimpleBlockFeature();

    private CustomSimpleBlockFeature() {
        super(SimpleBlockConfiguration.CODEC);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<SimpleBlockConfiguration> context) {
        SimpleBlockConfiguration simpleBlockConfiguration = context.config();
        WorldGenLevel worldGenLevel = context.level();
        BlockPos blockPos = context.origin();
        BlockState state = simpleBlockConfiguration.toPlace().getState(context.random(), blockPos);
        if (state.canSurvive(worldGenLevel, blockPos)) {
            Optional<ImmutableBlockState> optionalCustomBlockState = BlockStateUtils.getOptionalCustomBlockState(state);
            if (optionalCustomBlockState.isPresent()) {
                ImmutableBlockState blockState = optionalCustomBlockState.get();
                BlockBehavior behavior = blockState.behavior();
                if (behavior.hasMultiState(blockState)) {
                    if (!behavior.canPlaceMultiState(new WorldGenAccess(worldGenLevel), LocationUtils.fromBlockPos(blockPos), blockState)) {
                        return false;
                    }
                    try {
                        worldGenLevel.setBlock(blockPos, state, 2);
                        behavior.placeMultiState(state.getBlock(), new Object[]{worldGenLevel, blockPos, state, null, ItemStack.EMPTY});
                        return true;
                    } catch (Throwable t) {
                        CraftEngine.instance().logger().warn("Failed to run placeMultiState", t);
                        return false;
                    }
                } else {
                    worldGenLevel.setBlock(blockPos, state, 2);
                }
            } else {
                if (state.getBlock() instanceof DoublePlantBlock) {
                    if (!worldGenLevel.isEmptyBlock(blockPos.above())) {
                        return false;
                    }
                    DoublePlantBlock.placeAt(worldGenLevel, state, blockPos, 2);
                } else {
                    worldGenLevel.setBlock(blockPos, state, 2);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
