package net.momirealms.craftengine.bukkit.nms.v1_20.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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
                                   String group,
                                   CraftingBookCategory category,
                                   net.minecraft.world.item.ItemStack result,
                                   NonNullList<Ingredient> visualIngredients,
                                   NonNullList<Ingredient> roughIngredients) {
        super(new ResourceLocation(recipe.id().namespace(), recipe.id().value()), group, category, result, visualIngredients);
        this.recipe = recipe;
        this.companionRecipe = new ShapelessRecipe(new ResourceLocation(recipe.id().namespace(), recipe.id().value()), group, category, result, roughIngredients);
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe recipe) {
        List<net.momirealms.craftengine.core.item.recipe.Ingredient> visualIngredients = recipe.ingredientsInUse();
        NonNullList<Ingredient> visualData = NonNullList.withSize(visualIngredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < visualIngredients.size(); i++) {
            visualData.set(i, RecipeHelper.toMinecraftVisual(visualIngredients.get(i)));
        }
        List<net.momirealms.craftengine.core.item.recipe.Ingredient> roughIngredients = recipe.ingredientsInUse();
        NonNullList<Ingredient> roughData = NonNullList.withSize(roughIngredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < roughIngredients.size(); i++) {
            roughData.set(i, RecipeHelper.toMinecraft(roughIngredients.get(i)));
        }
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem(),
                visualData,
                roughData
        );
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, @NotNull Level world) {
        boolean vanillaMatches = this.companionRecipe.matches(inventory, world);
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
