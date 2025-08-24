package net.momirealms.craftengine.bukkit.nms.v1_21_2.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.momirealms.craftengine.core.block.VanillaBlockStateWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class BlockStateWrapper implements VanillaBlockStateWrapper {
    private final BlockState blockState;

    public BlockStateWrapper(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public Collection<String> properties() {
        return this.blockState.getProperties().stream().map(Property::getName).collect(Collectors.toList());
    }

    @Override
    public @Nullable String getProperty(String s) {
        Property<?> property = this.blockState.getBlock().getStateDefinition().getProperty(s);
        if (property == null) {
            return null;
        }
        return this.blockState.getValue(property).toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean hasProperty(String s) {
        return this.blockState.getBlock().getStateDefinition().getProperty(s) != null;
    }

    @Override
    public Object literalObject() {
        return this.blockState;
    }
}
