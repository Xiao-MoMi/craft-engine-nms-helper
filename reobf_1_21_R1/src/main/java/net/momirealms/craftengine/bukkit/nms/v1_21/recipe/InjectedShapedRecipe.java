package net.momirealms.craftengine.bukkit.nms.v1_21.recipe;

import com.google.common.collect.Maps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InjectedShapedRecipe extends ShapedRecipe {
    private final CustomShapedRecipe recipe;
    private final ShapedRecipePattern roughPattern;

    public InjectedShapedRecipe(CustomShapedRecipe recipe,
                                String group,
                                CraftingBookCategory category,
                                ShapedRecipePattern visualPattern,
                                ShapedRecipePattern roughPattern,
                                net.minecraft.world.item.ItemStack result,
                                boolean showNotification) {
        super(group, category, visualPattern, result, showNotification);
        this.recipe = recipe;
        this.roughPattern = roughPattern;
    }

    public static InjectedShapedRecipe of(CustomShapedRecipe recipe) {
        Map<Character, Ingredient> visualData = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraftVisual));
        ShapedRecipePattern visualPattern = ShapedRecipePattern.of(visualData, recipe.pattern().pattern());
        Map<Character, Ingredient> roughData = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraft));
        ShapedRecipePattern roughPattern = ShapedRecipePattern.of(roughData, recipe.pattern().pattern());
        return new InjectedShapedRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                visualPattern,
                roughPattern,
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem(),
                recipe.showNotification()
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        boolean vanillaMatches = this.roughPattern.matches(input);
        if (!vanillaMatches) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        if (this.recipe.ingredientCountSupport()) {
            this.recipe.takeInput(RecipeHelper.toCraftEngine(input), 1);
        }
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
