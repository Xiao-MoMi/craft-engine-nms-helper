package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedShapelessRecipe extends ShapelessRecipe {
    private final CustomShapelessRecipe recipe;
    private final ShapelessRecipe companionRecipe;

    public InjectedShapelessRecipe(CustomShapelessRecipe recipe,
                                   String group,
                                   CraftingBookCategory category,
                                   net.minecraft.world.item.ItemStack result,
                                   List<Ingredient> visualIngredients,
                                   List<Ingredient> roughIngredients) {
        CommonInfo commonInfo = new CommonInfo(recipe.showNotification());
        CraftingRecipe.CraftingBookInfo bookInfo = new CraftingRecipe.CraftingBookInfo(category, group);
        ItemStackTemplate template = ItemStackTemplate.fromNonEmptyStack(result);
        this.recipe = recipe;
        this.companionRecipe = new ShapelessRecipe(commonInfo, bookInfo, template, roughIngredients);
        super(commonInfo, bookInfo, template, visualIngredients);
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe recipe) {
        List<Ingredient> visualIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraftVisual).toList();
        List<Ingredient> roughIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraft).toList();
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).getMinecraftItem(),
                visualIngredients,
                roughIngredients
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        boolean vanillaMatches = this.companionRecipe.matches(input, level);
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

}
