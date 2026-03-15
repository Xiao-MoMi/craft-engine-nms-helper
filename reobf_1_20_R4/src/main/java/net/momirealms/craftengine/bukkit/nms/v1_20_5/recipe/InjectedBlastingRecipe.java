package net.momirealms.craftengine.bukkit.nms.v1_20_5.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomBlastingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.jetbrains.annotations.NotNull;

public class InjectedBlastingRecipe extends BlastingRecipe {
    private final CustomBlastingRecipe recipe;
    private final Ingredient roughIngredient;

    public InjectedBlastingRecipe(CustomBlastingRecipe recipe,
                                  String group,
                                  CookingBookCategory category,
                                  Ingredient visualIngredient,
                                  Ingredient roughIngredient,
                                  ItemStack result,
                                  float experience,
                                  int cookingTime) {
        super(group, category, visualIngredient, result, experience, cookingTime);
        this.recipe = recipe;
        this.roughIngredient = roughIngredient;
    }

    public static InjectedBlastingRecipe of(CustomBlastingRecipe recipe) {
        return new InjectedBlastingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                RecipeHelper.toMinecraftVisual(recipe.ingredient()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).getMinecraftItem(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    @Override
    public boolean matches(@NotNull Container inventory, @NotNull Level world) {
        boolean vanillaMatches = this.roughIngredient.test(inventory.getItem(0));
        if (!vanillaMatches) return false;
        Item wrapped = BukkitItemManager.instance().wrap(inventory.getItem(0));
        SingleItemInput singleItemInput = new SingleItemInput(UniqueIdItem.of(wrapped));
        return this.recipe.matches(singleItemInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
