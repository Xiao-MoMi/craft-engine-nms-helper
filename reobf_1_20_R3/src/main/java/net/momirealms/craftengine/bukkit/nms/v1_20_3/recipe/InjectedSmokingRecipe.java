package net.momirealms.craftengine.bukkit.nms.v1_20_3.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomSmokingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.jetbrains.annotations.NotNull;

public class InjectedSmokingRecipe extends SmokingRecipe {
    private final CustomSmokingRecipe recipe;
    private final Ingredient roughIngredient;

    public InjectedSmokingRecipe(CustomSmokingRecipe recipe,
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

    public static InjectedSmokingRecipe of(CustomSmokingRecipe recipe) {
        return new InjectedSmokingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                RecipeHelper.toMinecraftVisual(recipe.ingredient()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).minecraftItem(),
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
