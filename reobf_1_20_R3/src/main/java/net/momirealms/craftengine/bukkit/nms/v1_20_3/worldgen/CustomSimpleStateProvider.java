package net.momirealms.craftengine.bukkit.nms.v1_20_3.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.Map;

@SuppressWarnings("unchecked")
public class CustomSimpleStateProvider extends BlockStateProvider {
    public static final Codec<CustomSimpleStateProvider> STRING_CODEC = ExtraCodecs.NON_EMPTY_STRING
            .xmap(CustomSimpleStateProvider::new, p -> p.name);
    public static final MapCodec<CustomSimpleStateProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.STRING.fieldOf("Name").forGetter(p -> p.name),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .optionalFieldOf("Properties", Map.of())
                            .forGetter(p -> p.properties)
            ).apply(instance, CustomSimpleStateProvider::new)
    );
    public static final Codec<CustomSimpleStateProvider> DIRECT_CODEC = Codec.either(
            STRING_CODEC,
            MAP_CODEC.codec()
    ).xmap(
            either -> either.map(
                    stringProvider -> stringProvider,
                    mapProvider -> mapProvider
            ),
            provider -> provider.properties.isEmpty() ? Either.left(provider) : Either.right(provider)
    );
    public static final Codec<CustomSimpleStateProvider> CODEC = DIRECT_CODEC.fieldOf("state").codec();
    public static final BlockStateProviderType<CustomSimpleStateProvider> TYPE;

    static {
        try {
            TYPE = ReflectionUtils.setAccessible(BlockStateProviderType.class.getDeclaredConstructor(Codec.class)).newInstance(CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final String name;
    private final Map<String, String> properties;
    private BlockStateWrapper cached;

    private CustomSimpleStateProvider(String name) {
        this.name = name;
        this.properties = Map.of();
    }

    private CustomSimpleStateProvider(String name, Map<String, String> properties) {
        this.name = name;
        this.properties = properties;
    }

    @Override
    protected @NotNull BlockStateProviderType<?> type() {
        return TYPE;
    }

    @Override
    public @NotNull BlockState getState(@NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        if (this.cached != null) return (BlockState) this.cached.literalObject();
        BlockStateWrapper deserialized = CraftEngine.instance().blockManager().createBlockState(this.name);
        if (deserialized == null) return Blocks.STONE.defaultBlockState();
        for (Map.Entry<String, String> entry : this.properties.entrySet()) {
            deserialized = deserialized.withProperty(entry.getKey(), entry.getValue());
        }
        this.cached = deserialized;
        return (BlockState) deserialized.literalObject();
    }
}
