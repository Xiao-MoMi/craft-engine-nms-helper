package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import com.google.common.collect.Maps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InjectedShapedRecipe extends ShapedRecipe {
    private final CustomShapedRecipe recipe;
    private final ShapedRecipePattern roughPattern;

    public InjectedShapedRecipe(CustomShapedRecipe recipe,
                                ItemStackTemplate template,
                                CommonInfo commonInfo,
                                CraftingBookInfo craftingBookInfo,
                                ShapedRecipePattern visualPattern,
                                ShapedRecipePattern roughPattern) {
        super(commonInfo, craftingBookInfo, visualPattern, template);
        this.recipe = recipe;
        this.roughPattern = roughPattern;
    }

    public static InjectedShapedRecipe of(CustomShapedRecipe recipe) {
        Map<Character, Ingredient> visual = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraftVisual));
        ShapedRecipePattern visualPattern = ShapedRecipePattern.of(visual, recipe.pattern().pattern());
        Map<Character, Ingredient> actual = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraft));
        ShapedRecipePattern actualPattern = ShapedRecipePattern.of(actual, recipe.pattern().pattern());
        return new InjectedShapedRecipe(
                recipe,
                ItemStackTemplate.fromNonEmptyStack((net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem()), new CommonInfo(recipe.showNotification()), new CraftingBookInfo(RecipeHelper.toMinecraft(recipe.category()), recipe.group()), visualPattern, actualPattern
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
        if (this.recipe.ingredientCountSupport()) {
            this.recipe.takeInput(RecipeHelper.toCraftEngine(input), 1);
        }
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }
}
