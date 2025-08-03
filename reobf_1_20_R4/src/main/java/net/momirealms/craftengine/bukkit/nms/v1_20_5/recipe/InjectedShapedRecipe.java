package net.momirealms.craftengine.bukkit.nms.v1_20_5.recipe;

import com.google.common.collect.Maps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InjectedShapedRecipe extends ShapedRecipe {
    private final CustomShapedRecipe<ItemStack> recipe;

    public InjectedShapedRecipe(CustomShapedRecipe<ItemStack> recipe, String group, CraftingBookCategory category, ShapedRecipePattern pattern, net.minecraft.world.item.ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.recipe = recipe;
    }

    public static InjectedShapedRecipe of(CustomShapedRecipe<ItemStack> recipe) {
        Map<Character, Ingredient> data = Maps.transformValues(recipe.pattern().ingredients(), (RecipeHelper::toMinecraft));
        ShapedRecipePattern pattern = ShapedRecipePattern.of(data, recipe.pattern().pattern());
        return new InjectedShapedRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                pattern,
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
