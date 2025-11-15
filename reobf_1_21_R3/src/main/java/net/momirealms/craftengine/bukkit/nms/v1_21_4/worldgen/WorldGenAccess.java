package net.momirealms.craftengine.bukkit.nms.v1_21_4.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.world.BlockAccessor;
import net.momirealms.craftengine.core.world.WorldHeight;

public class WorldGenAccess implements BlockAccessor {
    private final WorldGenLevel level;
    private WorldHeight worldHeight;

    public WorldGenAccess(WorldGenLevel worldGenLevel) {
        this.level = worldGenLevel;
    }

    @Override
    public BlockStateWrapper getBlockState(int x, int y, int z) {
        return BlockStateUtils.toBlockStateWrapper(this.level.getBlockState(new BlockPos(x, y, z)));
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockStateWrapper stateWrapper, int flags) {
        this.level.setBlock(new BlockPos(x, y, z), (BlockState) stateWrapper.literalObject(), flags);
    }

    @Override
    public WorldHeight worldHeight() {
        if (this.worldHeight == null) {
            this.worldHeight = WorldHeight.create(this.level.getMinY(), this.level.getHeight());
        }
        return this.worldHeight;
    }
}
