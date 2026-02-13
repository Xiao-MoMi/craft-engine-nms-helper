package net.momirealms.craftengine.bukkit.nms.v1_21_4.chunk;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.momirealms.craftengine.bukkit.plugin.injector.WorldStorageInjector;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import org.jetbrains.annotations.NotNull;

public class InjectedLevelChunkSection extends LevelChunkSection implements InjectedStorage.Section {
    private CESection section;
    private CEChunk chunk;
    private SectionPos sectionPos;
    private boolean isActive;
    private final boolean canRecalcBlockCounts;

    public InjectedLevelChunkSection(PalettedContainer<BlockState> states, PalettedContainer<Holder<Biome>> biomes) {
        super(states, biomes);
        this.canRecalcBlockCounts = true;
    }

    @Override
    public void recalcBlockCounts() {
        if (this.canRecalcBlockCounts) {
            super.recalcBlockCounts();
        }
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setActive(boolean b) {
        this.isActive = b;
    }

    @Override
    public CESection section() {
        return this.section;
    }

    @Override
    public void setSection(CESection ceSection) {
        this.section = ceSection;
    }

    @Override
    public CEChunk chunk() {
        return this.chunk;
    }

    @Override
    public void setChunk(CEChunk ceChunk) {
        this.chunk = ceChunk;
    }

    @Override
    public SectionPos pos() {
        return this.sectionPos;
    }

    @Override
    public void setPos(SectionPos sectionPos) {
        this.sectionPos = sectionPos;
    }

//    @Override
//    public @NotNull LevelChunkSection copy() {
//        return new InjectedLevelChunkSection(this.states.copy(), getBiomes().copy(), this.section, this.chunk, this.sectionPos);
//    }

    @Override
    @NotNull
    public BlockState setBlockState(int x, int y, int z, @NotNull BlockState state, boolean useLocks) {
        return (BlockState) WorldStorageInjector.setBlockState(this, x, y, z, state, useLocks);
    }
}
