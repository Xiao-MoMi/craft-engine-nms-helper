package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
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
    private final Ingredient roughInput;

    public InjectedSmokingRecipe(CustomSmokingRecipe recipe,
                                 ItemStackTemplate template,
                                 CommonInfo commonInfo,
                                 CookingBookInfo cookingBookInfo,
                                 Ingredient visualIngredient,
                                 Ingredient roughIngredient,
                                 float experience,
                                 int cookingTime) {
        super(commonInfo, cookingBookInfo, visualIngredient, template, experience, cookingTime);
        this.recipe = recipe;
        this.roughInput = roughIngredient;
    }

    public static InjectedSmokingRecipe of(CustomSmokingRecipe recipe) {
        return new InjectedSmokingRecipe(recipe,
                ItemStackTemplate.fromNonEmptyStack((ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).minecraftItem()), new CommonInfo(recipe.showNotification()),
                new AbstractCookingRecipe.CookingBookInfo(RecipeHelper.toMinecraft(recipe.category()), recipe.group()),
                RecipeHelper.toMinecraftVisual(recipe.ingredient()),
                RecipeHelper.toMinecraft(recipe.ingredient()),
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
