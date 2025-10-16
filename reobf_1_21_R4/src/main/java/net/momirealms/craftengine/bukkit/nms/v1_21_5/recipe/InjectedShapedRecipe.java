package net.momirealms.craftengine.bukkit.nms.v1_21_5.recipe;

import com.google.common.collect.Maps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InjectedShapedRecipe extends ShapedRecipe {
    private final CustomShapedRecipe<ItemStack> recipe;
    private final ShapedRecipePattern roughPattern;

    public InjectedShapedRecipe(CustomShapedRecipe<ItemStack> recipe,
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

    public static InjectedShapedRecipe of(CustomShapedRecipe<ItemStack> recipe) {
        Map<Character, Ingredient> visual = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraftVisual));
        ShapedRecipePattern visualPattern = ShapedRecipePattern.of(visual, recipe.pattern().pattern());
        Map<Character, Ingredient> actual = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraft));
        ShapedRecipePattern actualPattern = ShapedRecipePattern.of(actual, recipe.pattern().pattern());
        return new InjectedShapedRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                visualPattern,
                actualPattern,
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).getLiteralObject(),
                recipe.showNotification()
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        // 先进行粗略匹配
        boolean vanillaMatches = this.roughPattern.matches(input);
        if (!vanillaMatches) return false;
        // 再进行细节匹配
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
