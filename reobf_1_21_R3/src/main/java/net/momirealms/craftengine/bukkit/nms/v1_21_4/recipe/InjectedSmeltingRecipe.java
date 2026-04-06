package net.momirealms.craftengine.bukkit.nms.v1_21_4.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomSmeltingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.jetbrains.annotations.NotNull;

public class InjectedSmeltingRecipe extends SmeltingRecipe {
    private final CustomSmeltingRecipe recipe;
    private final Ingredient roughInput;

    public InjectedSmeltingRecipe(CustomSmeltingRecipe recipe,
                                  String group,
                                  CookingBookCategory category,
                                  Ingredient visualIngredient,
                                  Ingredient roughIngredient,
                                  ItemStack result,
                                  float experience,
                                  int cookingTime) {
        super(group, category, visualIngredient, result, experience, cookingTime);
        this.recipe = recipe;
        this.roughInput = roughIngredient;
    }

    public static InjectedSmeltingRecipe of(CustomSmeltingRecipe recipe) {
        return new InjectedSmeltingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                RecipeHelper.toMinecraftVisual(recipe.ingredient()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).minecraftItem(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    public boolean vanillaMatches(SingleRecipeInput input, Level level) {
        return this.roughInput.test(input.item());
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = this.vanillaMatches(input, level);
        if (!vanillaMatches) return false;
        Item wrapped = BukkitItemManager.instance().wrap(input.item());
        SingleItemInput singleItemInput = new SingleItemInput(UniqueIdItem.of(wrapped));
        return this.recipe.matches(singleItemInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
