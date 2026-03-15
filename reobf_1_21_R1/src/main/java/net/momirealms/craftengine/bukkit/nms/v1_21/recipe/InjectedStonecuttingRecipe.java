package net.momirealms.craftengine.bukkit.nms.v1_21.recipe;

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
import org.jetbrains.annotations.NotNull;

public class InjectedStonecuttingRecipe extends StonecutterRecipe {
    private final CustomStoneCuttingRecipe recipe;

    public InjectedStonecuttingRecipe(CustomStoneCuttingRecipe recipe, String group, Ingredient ingredient, ItemStack result) {
        super(group, ingredient, result);
        this.recipe = recipe;
    }

    public static InjectedStonecuttingRecipe of(CustomStoneCuttingRecipe recipe) {
        return new InjectedStonecuttingRecipe(recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.ingredient()),
                (ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).getMinecraftItem()
        );
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
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
