package net.momirealms.craftengine.bukkit.nms.v1_20_3.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
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
    private final NonNullList<Ingredient> roughIngredients;
    private boolean isMatching;

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

    private boolean vanillaMatches(CraftingContainer inventory, Level world) {
        StackedContents sc = new StackedContents();
        sc.initialize(this);
        int i = 0;
        for(int j = 0; j < inventory.getContainerSize(); ++j) {
            net.minecraft.world.item.ItemStack itemstack = inventory.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                sc.accountStack(itemstack, 1);
            }
        }
        if (i != this.roughIngredients.size()) {
            return false;
        }
        return sc.canCraft(this, null);
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
    public boolean matches(@NotNull CraftingContainer inventory, @NotNull Level world) {
        this.isMatching = true;
        boolean vanillaMatches = this.vanillaMatches(inventory, world);
        this.isMatching = false;
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
