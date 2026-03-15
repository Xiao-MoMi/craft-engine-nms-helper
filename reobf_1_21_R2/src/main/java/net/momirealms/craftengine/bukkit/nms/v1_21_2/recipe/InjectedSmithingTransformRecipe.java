package net.momirealms.craftengine.bukkit.nms.v1_21_2.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InjectedSmithingTransformRecipe extends SmithingTransformRecipe {
    private final CustomSmithingTransformRecipe recipe;

    public InjectedSmithingTransformRecipe(CustomSmithingTransformRecipe recipe, Optional<Ingredient> template, Optional<Ingredient> base, Optional<Ingredient> addition, net.minecraft.world.item.ItemStack result, boolean copyDataComponents) {
        super(template, base, addition, result, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTransformRecipe of(CustomSmithingTransformRecipe recipe) {
        net.minecraft.world.item.ItemStack result = (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).getMinecraftItem();
        return new InjectedSmithingTransformRecipe(recipe,
                Optional.ofNullable(recipe.template()).map(RecipeHelper::toMinecraft),
                Optional.of(recipe.base()).map(RecipeHelper::toMinecraft),
                Optional.ofNullable(recipe.addition()).map(RecipeHelper::toMinecraft),
                result,
                true
        );
    }

    @Override
    public boolean matches(@NotNull SmithingRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
        if (!vanillaMatches) return false;
        Item template = BukkitItemManager.instance().wrap(input.template());
        Item base = BukkitItemManager.instance().wrap(input.base());
        Item addition = BukkitItemManager.instance().wrap(input.addition());
        SmithingInput smithingInput = new SmithingInput(UniqueIdItem.of(base), UniqueIdItem.of(template), UniqueIdItem.of(addition));
        return this.recipe.matches(smithingInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
