package net.momirealms.craftengine.bukkit.nms.v26_2.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedShapelessRecipe extends ShapelessRecipe {
    private final CustomShapelessRecipe recipe;
    private final ShapelessRecipe companionRecipe;

    public InjectedShapelessRecipe(CustomShapelessRecipe recipe,
                                   ItemStackTemplate template,
                                   CommonInfo commonInfo,
                                   CraftingRecipe.CraftingBookInfo craftingBookInfo,
                                   List<Ingredient> visualIngredients,
                                   List<Ingredient> roughIngredients) {
        super(commonInfo, craftingBookInfo, template, visualIngredients);
        this.recipe = recipe;
        this.companionRecipe = new ShapelessRecipe(commonInfo, craftingBookInfo, template, roughIngredients);
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe recipe) {
        List<Ingredient> visualIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraftVisual).toList();
        List<Ingredient> roughIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraft).toList();
        return new InjectedShapelessRecipe(
                recipe,
                ItemStackTemplate.fromNonEmptyStack((net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem()),
                new CommonInfo(recipe.showNotification()),
                new CraftingBookInfo(RecipeHelper.toMinecraft(recipe.category()), recipe.group()),
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
