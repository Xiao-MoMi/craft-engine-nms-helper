package net.momirealms.craftengine.bukkit.nms.v1_21_6.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedShapelessRecipe extends ShapelessRecipe {
    private final CustomShapelessRecipe<ItemStack> recipe;
    private final ShapelessRecipe companionRecipe;

    public InjectedShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe,
                                   String group,
                                   CraftingBookCategory category,
                                   net.minecraft.world.item.ItemStack result,
                                   List<Ingredient> visualIngredients,
                                   List<Ingredient> roughIngredients) {
        super(group, category, result, visualIngredients);
        this.recipe = recipe;
        this.companionRecipe = new ShapelessRecipe(group, category, result, roughIngredients);
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe<ItemStack> recipe) {
        List<Ingredient> visualIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraftVisual).toList();
        List<Ingredient> roughIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraft).toList();
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).getLiteralObject(),
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
        if (this.recipe.takeAdditionalIngredients()) {
            this.recipe.takeAdditionalIngredients(RecipeHelper.toCraftEngine(input), 1);
        }
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
