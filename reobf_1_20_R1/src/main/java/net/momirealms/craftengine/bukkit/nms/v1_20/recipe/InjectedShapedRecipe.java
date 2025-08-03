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
    private final CustomShapedRecipe<ItemStack> recipe;

    public InjectedShapedRecipe(CustomShapedRecipe<ItemStack> recipe, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, net.minecraft.world.item.ItemStack result, boolean showNotification) {
        super(new ResourceLocation(recipe.id().namespace(), recipe.id().value()), group, category, width, height, ingredients, result, showNotification);
        this.recipe = recipe;
    }

    public static InjectedShapedRecipe of(CustomShapedRecipe<ItemStack> recipe) {
        String[] shape = recipe.pattern().pattern();
        int width = shape[0].length();
        NonNullList<Ingredient> data = NonNullList.withSize(shape.length * width, Ingredient.EMPTY);
        for (int i = 0; i < shape.length; ++i) {
            String row = shape[i];
            for (int j = 0; j < row.length(); ++j) {
                data.set(i * width + j, RecipeHelper.toMinecraft(recipe.pattern().ingredients().get(row.charAt(j))));
            }
        }
        return new InjectedShapedRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                width,
                shape.length,
                data,
                (net.minecraft.world.item.ItemStack) recipe.result().buildItem(ItemBuildContext.EMPTY).getLiteralObject(),
                recipe.showNotification()
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
