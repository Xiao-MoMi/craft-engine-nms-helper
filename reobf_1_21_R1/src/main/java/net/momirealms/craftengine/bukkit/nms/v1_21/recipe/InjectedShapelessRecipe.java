package net.momirealms.craftengine.bukkit.nms.v1_21.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import net.momirealms.craftengine.core.item.recipe.PlacementInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedShapelessRecipe extends ShapelessRecipe {
    private final CustomShapelessRecipe<ItemStack> recipe;
    private final NonNullList<Ingredient> roughIngredients;
    private boolean isMatching = false;

    public InjectedShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe,
                                   String group,
                                   CraftingBookCategory category,
                                   net.minecraft.world.item.ItemStack result,
                                   NonNullList<Ingredient> visualIngredients,
                                   NonNullList<Ingredient> roughIngredients) {
        super(group, category, result, visualIngredients);
        this.recipe = recipe;
        this.roughIngredients = roughIngredients;
    }

    @SuppressWarnings("DuplicatedCode")
    public static InjectedShapelessRecipe of(CustomShapelessRecipe<ItemStack> recipe) {
        List<net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack>> visualIngredients = recipe.ingredientsInUse();
        NonNullList<Ingredient> visualData = NonNullList.withSize(visualIngredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < visualIngredients.size(); i++) {
            visualData.set(i, RecipeHelper.toMinecraftVisual(visualIngredients.get(i)));
        }
        List<net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack>> roughIngredients = recipe.ingredientsInUse();
        NonNullList<Ingredient> roughData = NonNullList.withSize(roughIngredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < roughIngredients.size(); i++) {
            roughData.set(i, RecipeHelper.toMinecraft(roughIngredients.get(i)));
        }
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.EMPTY).getLiteralObject(),
                visualData,
                roughData
        );
    }

    private boolean vanillaMatches(CraftingInput input, Level world) {
        if (input.ingredientCount() != this.roughIngredients.size()) {
            return false;
        } else if (input.size() == 1 && this.roughIngredients.size() == 1) {
            return this.roughIngredients.getFirst().test(input.getItem(0));
        } else {
            input.stackedContents().initializeExtras(this, input);
            this.isMatching = true;
            boolean canCraft = input.stackedContents().canCraft(this, null);
            this.isMatching = false;
            input.stackedContents().resetExtras();
            return canCraft;
        }
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (this.isMatching) {
            return this.roughIngredients;
        } else {
            return super.getIngredients();
        }
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        boolean vanillaMatches = this.vanillaMatches(input, level);
        if (!vanillaMatches) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return RecipeHelper.getRemainingItems(input);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
