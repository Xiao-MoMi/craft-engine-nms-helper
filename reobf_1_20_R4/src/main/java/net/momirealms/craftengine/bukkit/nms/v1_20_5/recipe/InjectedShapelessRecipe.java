package net.momirealms.craftengine.bukkit.nms.v1_20_5.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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

    public InjectedShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe, String group, CraftingBookCategory category, net.minecraft.world.item.ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.recipe = recipe;
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe<ItemStack> recipe) {
        List<net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack>> ingredients = recipe.ingredientsInUse();
        NonNullList<Ingredient> data = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); i++) {
            data.set(i, RecipeHelper.toMinecraft(ingredients.get(i)));
        }
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.result().buildItem(ItemBuildContext.EMPTY).getLiteralObject(),
                data
        );
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, @NotNull Level world) {
        boolean vanillaMatches = super.matches(inventory, world);
        if (!vanillaMatches) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(inventory));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingContainer inventory) {
        return RecipeHelper.getRemainingItems(inventory);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
