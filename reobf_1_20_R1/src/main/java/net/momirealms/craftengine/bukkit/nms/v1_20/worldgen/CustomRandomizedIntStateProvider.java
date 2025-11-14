package net.momirealms.craftengine.bukkit.nms.v1_20.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class CustomRandomizedIntStateProvider extends BlockStateProvider {
    public static final MapCodec<CustomRandomizedIntStateProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance ->
                    instance.group(
                            BlockStateProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source),
                            Codec.STRING.fieldOf("property").forGetter(provider -> provider.propertyName),
                            IntProvider.CODEC.fieldOf("values").forGetter(provider -> provider.values)
                    ).apply(instance, CustomRandomizedIntStateProvider::new)
    );
    public static final Codec<CustomRandomizedIntStateProvider> CODEC = MAP_CODEC.codec();
    public static final BlockStateProviderType<CustomRandomizedIntStateProvider> TYPE;

    static {
        try {
            TYPE = ReflectionUtils.setAccessible(BlockStateProviderType.class.getDeclaredConstructor(Codec.class)).newInstance(CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final BlockStateProvider source;
    private final String propertyName;
    @Nullable
    private net.minecraft.world.level.block.state.properties.IntegerProperty property;
    @Nullable
    private IntegerProperty customProperty;
    private final IntProvider values;

    private CustomRandomizedIntStateProvider(BlockStateProvider source, String propertyName, IntProvider values) {
        this.source = source;
        this.propertyName = propertyName;
        this.values = values;
    }

    @Override
    protected @NotNull BlockStateProviderType<?> type() {
        return TYPE;
    }

    @Override
    public @NotNull BlockState getState(@NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        BlockState state = this.source.getState(randomSource, blockPos);
        if (state instanceof DelegatingBlockState holder) {
            ImmutableBlockState immutableBlockState = holder.blockState();
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return state;
            if (this.customProperty == null || !immutableBlockState.contains(this.customProperty)) {
                IntegerProperty integerProperty = findCustomProperty(immutableBlockState, this.propertyName);
                if (integerProperty == null) return state;
                this.customProperty = integerProperty;
            }
            return (BlockState) immutableBlockState.with(this.customProperty, this.values.sample(randomSource)).customBlockState().literalObject();
        } else {
            if (this.property == null || !state.hasProperty(this.property)) {
                net.minecraft.world.level.block.state.properties.IntegerProperty integerProperty = findProperty(state, this.propertyName);
                if (integerProperty == null) return state;
                this.property = integerProperty;
            }
            return state.setValue(this.property, this.values.sample(randomSource));
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
