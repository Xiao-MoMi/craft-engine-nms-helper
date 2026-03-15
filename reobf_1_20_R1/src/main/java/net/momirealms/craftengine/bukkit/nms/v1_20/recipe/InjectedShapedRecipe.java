package net.momirealms.craftengine.bukkit.nms.v1_20.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InjectedShapedRecipe extends ShapedRecipe {
    private final CustomShapedRecipe recipe;
    private final NonNullList<Ingredient> roughIngredients;

    public InjectedShapedRecipe(CustomShapedRecipe recipe,
                                String group,
                                CraftingBookCategory category,
                                int width,
                                int height,
                                NonNullList<Ingredient> visualIngredients,
                                NonNullList<Ingredient> roughIngredients,
                                net.minecraft.world.item.ItemStack result,
                                boolean showNotification) {
        super(new ResourceLocation(recipe.id().namespace(), recipe.id().value()), group, category, width, height, visualIngredients, result, showNotification);
        this.recipe = recipe;
        this.roughIngredients = roughIngredients;
    }

    public static InjectedShapedRecipe of(CustomShapedRecipe recipe) {
        String[] shape = recipe.pattern().pattern();
        int width = shape[0].length();
        NonNullList<Ingredient> visualData = NonNullList.withSize(shape.length * width, Ingredient.EMPTY);
        for (int i = 0; i < shape.length; ++i) {
            String row = shape[i];
            for (int j = 0; j < row.length(); ++j) {
                visualData.set(i * width + j, RecipeHelper.toMinecraftVisual(recipe.pattern().ingredients().get(row.charAt(j))));
            }
        }
        NonNullList<Ingredient> roughData = NonNullList.withSize(shape.length * width, Ingredient.EMPTY);
        for (int i = 0; i < shape.length; ++i) {
            String row = shape[i];
            for (int j = 0; j < row.length(); ++j) {
                roughData.set(i * width + j, RecipeHelper.toMinecraft(recipe.pattern().ingredients().get(row.charAt(j))));
            }
        }
        return new InjectedShapedRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                width,
                shape.length,
                visualData,
                roughData,
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).getMinecraftItem(),
                recipe.showNotification()
        );
    }

    private boolean vanillaMatches(CraftingContainer inventory, Level world) {
        for (int i = 0; i <= inventory.getWidth() - this.getWidth(); ++i) {
            for (int j = 0; j <= inventory.getHeight() - this.getHeight(); ++j) {
                if (this.matches(inventory, i, j, true)) {
                    return true;
                }
                if (this.matches(inventory, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(CraftingContainer inv, int offsetX, int offsetY, boolean flipped) {
        for (int k = 0; k < inv.getWidth(); ++k) {
            for (int l = 0; l < inv.getHeight(); ++l) {
                int i1 = k - offsetX;
                int j1 = l - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (i1 >= 0 && j1 >= 0 && i1 < this.getWidth() && j1 < this.getHeight()) {
                    if (flipped) {
                        ingredient = this.roughIngredients.get(this.getWidth() - i1 - 1 + j1 * this.getWidth());
                    } else {
                        ingredient = this.roughIngredients.get(i1 + j1 * this.getWidth());
                    }
                }
                if (!ingredient.test(inv.getItem(k + l * inv.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, @NotNull Level world) {
        boolean vanillaMatches = this.vanillaMatches(inventory, world);
        if (!vanillaMatches) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(inventory));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingContainer inventory) {
        if (this.recipe.ingredientCountSupport()) {
            this.recipe.takeInput(RecipeHelper.toCraftEngine(inventory), 1);
        }
        return RecipeHelper.getRemainingItems(this.recipe.id(), inventory);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
