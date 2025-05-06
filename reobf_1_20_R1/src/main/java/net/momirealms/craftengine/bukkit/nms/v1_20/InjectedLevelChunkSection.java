package net.momirealms.craftengine.bukkit.nms.v1_20;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.jetbrains.annotations.NotNull;

public class InjectedLevelChunkSection extends LevelChunkSection implements InjectedHolder.Section {
    private CESection section;
    private CEChunk chunk;
    private SectionPos sectionPos;

    public InjectedLevelChunkSection(PalettedContainer<BlockState> states, PalettedContainer<Holder<Biome>> biomes) {
        super(states, biomes);
    }

    @Override
    public CEChunk ceChunk() {
        return this.chunk;
    }

    @Override
    public void ceChunk(CEChunk ceChunk) {
        this.chunk = ceChunk;
    }

    @Override
    public SectionPos cePos() {
        return this.sectionPos;
    }

    @Override
    public void cePos(SectionPos sectionPos) {
        this.sectionPos = sectionPos;
    }

    @Override
    public CESection ceSection() {
        return this.section;
    }

    @Override
    public void ceSection(CESection ceSection) {
        this.section = ceSection;
    }

    @Override
    @NotNull
    public BlockState setBlockState(int x, int y, int z, @NotNull BlockState state, boolean useLocks) {
        try {
            return (BlockState) BukkitInjector.SetBlockStateInterceptor.INSTANCE.intercept(this, new Object[]{x, y, z, state, useLocks}, () -> super.setBlockState(x, y, z, state, useLocks));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
