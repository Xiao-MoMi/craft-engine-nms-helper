package net.momirealms.craftengine.bukkit.nms.v1_21_6.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomCampfireRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

public class InjectedCampfireCookingRecipe extends CampfireCookingRecipe {
    private final CustomCampfireRecipe<org.bukkit.inventory.ItemStack> recipe;

    public InjectedCampfireCookingRecipe(CustomCampfireRecipe<org.bukkit.inventory.ItemStack> recipe, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
        this.recipe = recipe;
    }

    public static InjectedCampfireCookingRecipe of(CustomCampfireRecipe<org.bukkit.inventory.ItemStack> recipe) {
        return new InjectedCampfireCookingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.EMPTY).getLiteralObject(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
        if (!vanillaMatches) return false;
        Item<org.bukkit.inventory.ItemStack> wrapped = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(input.item()));
        SingleItemInput<org.bukkit.inventory.ItemStack> singleItemInput = new SingleItemInput<>(UniqueIdItem.of(wrapped));
        return this.recipe.matches(singleItemInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
