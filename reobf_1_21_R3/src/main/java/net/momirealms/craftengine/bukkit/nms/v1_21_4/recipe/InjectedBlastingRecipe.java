package net.momirealms.craftengine.bukkit.nms.v1_21_4.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomBlastingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

public class InjectedBlastingRecipe extends BlastingRecipe {
    private final CustomBlastingRecipe<org.bukkit.inventory.ItemStack> recipe;

    public InjectedBlastingRecipe(CustomBlastingRecipe<org.bukkit.inventory.ItemStack> recipe, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
        this.recipe = recipe;
    }

    public static InjectedBlastingRecipe of(CustomBlastingRecipe<org.bukkit.inventory.ItemStack> recipe) {
        return new InjectedBlastingRecipe(recipe,
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
