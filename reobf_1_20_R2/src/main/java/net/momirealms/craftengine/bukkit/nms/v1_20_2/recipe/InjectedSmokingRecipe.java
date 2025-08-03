package net.momirealms.craftengine.bukkit.nms.v1_20_2.recipe;

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
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

public class InjectedSmokingRecipe extends SmokingRecipe {
    private final CustomSmokingRecipe<org.bukkit.inventory.ItemStack> recipe;

    public InjectedSmokingRecipe(CustomSmokingRecipe<org.bukkit.inventory.ItemStack> recipe, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
        this.recipe = recipe;
    }

    public static InjectedSmokingRecipe of(CustomSmokingRecipe<org.bukkit.inventory.ItemStack> recipe) {
        return new InjectedSmokingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.EMPTY).getLiteralObject(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    @Override
    public boolean matches(@NotNull Container inventory, @NotNull Level world) {
        boolean vanillaMatches = super.matches(inventory, world);
        if (!vanillaMatches) return false;
        Item<org.bukkit.inventory.ItemStack> wrapped = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(inventory.getItem(0)));
        SingleItemInput<org.bukkit.inventory.ItemStack> singleItemInput = new SingleItemInput<>(UniqueIdItem.of(wrapped));
        return this.recipe.matches(singleItemInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
