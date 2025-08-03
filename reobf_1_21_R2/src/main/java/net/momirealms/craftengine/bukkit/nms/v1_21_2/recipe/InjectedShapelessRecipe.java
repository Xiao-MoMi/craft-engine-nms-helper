package net.momirealms.craftengine.bukkit.nms.v1_21_2.recipe;

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

    public InjectedShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe, String group, CraftingBookCategory category, net.minecraft.world.item.ItemStack result, List<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.recipe = recipe;
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe<ItemStack> recipe) {
        List<Ingredient> ingredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraft).toList();
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.result().buildItem(ItemBuildContext.EMPTY).getLiteralObject(),
                ingredients
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
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
