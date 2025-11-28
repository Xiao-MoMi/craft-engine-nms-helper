package net.momirealms.craftengine.bukkit.nms.v1_21_11.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomStoneCuttingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

public class InjectedStonecuttingRecipe extends StonecutterRecipe {
    private final CustomStoneCuttingRecipe<org.bukkit.inventory.ItemStack> recipe;

    public InjectedStonecuttingRecipe(CustomStoneCuttingRecipe<org.bukkit.inventory.ItemStack> recipe, String group, Ingredient ingredient, ItemStack result) {
        super(group, ingredient, result);
        this.recipe = recipe;
    }

    public static InjectedStonecuttingRecipe of(CustomStoneCuttingRecipe<org.bukkit.inventory.ItemStack> recipe) {
        return new InjectedStonecuttingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (net.minecraft.world.item.ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).getLiteralObject()
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
