package net.momirealms.craftengine.bukkit.nms;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

@SuppressWarnings("unchecked")
public class Impl1_20_r4 extends FastNMS {

    @Override
    public Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState) {
        PalettedContainer<BlockState> pc = (PalettedContainer<BlockState>) palettedContainer;
        return pc.getAndSet(x, y, z, (BlockState) blockState);
    }
}
