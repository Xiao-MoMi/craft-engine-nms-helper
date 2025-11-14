package net.momirealms.craftengine.bukkit.nms.v1_21_4.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class CustomSimpleStateProvider extends BlockStateProvider {
    public static final MapCodec<CustomSimpleStateProvider> CODEC = ExtraCodecs.NON_EMPTY_STRING.fieldOf("state")
            .xmap(CustomSimpleStateProvider::new, (provider) -> provider.state);
    public static final BlockStateProviderType<CustomSimpleStateProvider> TYPE;

    static {
        try {
            TYPE = ReflectionUtils.setAccessible(BlockStateProviderType.class.getDeclaredConstructor(MapCodec.class)).newInstance(CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final String state;
    private BlockStateWrapper cached;

    public CustomSimpleStateProvider(String state) {
        this.state = state;
    }

    @Override
    protected @NotNull BlockStateProviderType<?> type() {
        return TYPE;
    }

    @Override
    public @NotNull BlockState getState(@NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        if (this.cached != null) {
            return (BlockState) this.cached.literalObject();
        }
        BlockStateWrapper deserialized = CraftEngine.instance().blockManager().createBlockState(this.state);
        if (deserialized != null) {
            this.cached = deserialized;
            return (BlockState) deserialized.literalObject();
        }
        return Blocks.STONE.defaultBlockState();
    }
}
