package net.momirealms.craftengine.bukkit.nms.v1_20_5.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
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

    @Override
    public @NotNull BlockState withProperty(String propertyName, String value) {
        Property<?> property = this.blockState.getBlock().getStateDefinition().getProperty(propertyName);
        BlockState finalState = this.blockState;
        if (property != null) {
            Optional<?> optionalValue = property.getValue(value);
            if (optionalValue.isPresent()) {
                finalState = setValue(finalState, property, optionalValue.get());
            }
        }
        return finalState;
    }

    @Override
    public @NotNull BlockState cycleProperty(String propertyName, boolean backwards) {
        Property<?> property = this.blockState.getBlock().getStateDefinition().getProperty(propertyName);
        if (property == null) return this.blockState;
        return cycleState(this.blockState, property, backwards);
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean backwards) {
        return backwards ? blockState.setValue(property, MiscUtils.findPreviousInIterable(property.getPossibleValues(), blockState.getValue(property))) : blockState.cycle(property);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState setValue(BlockState blockState, Property<T> property, Object value) {
        return blockState.setValue(property, (T) value);
    }
}
