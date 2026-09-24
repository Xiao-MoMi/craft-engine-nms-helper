package net.momirealms.craftengine.bukkit.nms.v26_3.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class CustomRandomizedIntStateProvider implements BlockStateProvider {
    public static final MapCodec<CustomRandomizedIntStateProvider> CODEC = RecordCodecBuilder.mapCodec(
            instance ->
                    instance.group(
                            BlockStateProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source),
                            Codec.STRING.fieldOf("property").forGetter(provider -> provider.propertyName),
                            IntProviders.CODEC.fieldOf("values").forGetter(provider -> provider.values)
                    ).apply(instance, CustomRandomizedIntStateProvider::new)
    );
    private final Holder<BlockStateProvider> source;
    private final String propertyName;
    @Nullable
    private net.minecraft.world.level.block.state.properties.IntegerProperty property;
    @Nullable
    private IntegerProperty customProperty;
    private final IntProvider values;

    private CustomRandomizedIntStateProvider(Holder<BlockStateProvider> source, String propertyName, IntProvider values) {
        this.source = source;
        this.propertyName = propertyName;
        this.values = values;
    }

    @Override
    public @NotNull MapCodec<CustomRandomizedIntStateProvider> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockState getState(@NotNull LevelAccessor level, @NotNull RandomSource random, @NotNull BlockPos pos) {
        BlockState state = this.source.value().getState(level, random, pos);
        if (state instanceof DelegatingBlockState holder) {
            ImmutableBlockState immutableBlockState = holder.blockState();
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return state;
            if (this.customProperty == null || !immutableBlockState.contains(this.customProperty)) {
                IntegerProperty integerProperty = findCustomProperty(immutableBlockState, this.propertyName);
                if (integerProperty == null) return state;
                this.customProperty = integerProperty;
            }
            return (BlockState) immutableBlockState.with(this.customProperty, this.values.sample(random)).customBlockState().minecraftState();
        } else {
            if (this.property == null || !state.hasProperty(this.property)) {
                net.minecraft.world.level.block.state.properties.IntegerProperty integerProperty = findProperty(state, this.propertyName);
                if (integerProperty == null) return state;
                this.property = integerProperty;
            }
            return state.setValue(this.property, this.values.sample(random));
        }
    }

    @Nullable
    private static net.minecraft.world.level.block.state.properties.IntegerProperty findProperty(BlockState state, String propertyName) {
        return state.getProperties().stream()
                .filter(property -> property.getName().equals(propertyName))
                .filter(property -> property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty)
                .map(property -> (net.minecraft.world.level.block.state.properties.IntegerProperty) property)
                .findAny()
                .orElse(null);
    }

    @Nullable
    private static IntegerProperty findCustomProperty(ImmutableBlockState state, String propertyName) {
        return state.getProperties().stream()
                .filter(property -> property.name().equals(propertyName))
                .filter(property -> property.valueClass() == Integer.class)
                .map(property -> (IntegerProperty) property)
                .findAny()
                .orElse(null);
    }
}
