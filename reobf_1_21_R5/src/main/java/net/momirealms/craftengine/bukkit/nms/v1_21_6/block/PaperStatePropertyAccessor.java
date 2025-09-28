package net.momirealms.craftengine.bukkit.nms.v1_21_6.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaperStatePropertyAccessor implements StatePropertyAccessor {
    private final BlockState blockState;

    public PaperStatePropertyAccessor(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public Collection<String> getPropertyNames() {
        return this.blockState.getProperties().stream().map(Property::getName).collect(Collectors.toList());
    }

    @Override
    public String getPropertyValueAsString(String s) {
        Property<?> property = this.blockState.getBlock().getStateDefinition().getProperty(s);
        if (property == null) {
            return null;
        }
        return this.blockState.getValue(property).toString().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPropertyValue(String s) {
        Property<?> property = this.blockState.getBlock().getStateDefinition().getProperty(s);
        if (property == null) {
            return null;
        }
        return (T) this.blockState.getValue(property);
    }

    @Override
    public boolean hasProperty(String s) {
        return this.blockState.getBlock().getStateDefinition().getProperty(s) != null;
    }
}
