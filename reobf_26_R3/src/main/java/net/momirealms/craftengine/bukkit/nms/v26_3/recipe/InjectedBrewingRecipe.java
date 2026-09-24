package net.momirealms.craftengine.bukkit.nms.v26_3.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomBrewingRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InjectedBrewingRecipe extends BrewingRecipe {
    private final CustomBrewingRecipe recipe;

    public InjectedBrewingRecipe(CustomBrewingRecipe recipe, PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        super(input, reagent, output);
        this.recipe = recipe;
    }

    public static InjectedBrewingRecipe of(CustomBrewingRecipe recipe) {
        return new InjectedBrewingRecipe(
                recipe,
                new PotionIngredient(RecipeHelper.toMinecraft(recipe.container()), Optional.empty()),
                new PotionIngredient(RecipeHelper.toMinecraft(recipe.ingredient()), Optional.empty()),
                ItemStackTemplate.fromNonEmptyStack((ItemStack) recipe.result(ItemBuildContext.empty()).minecraftItem())
        );
    }

    @Override
    public boolean matches(@NotNull BrewingInput input) {
        if (!super.matches(input)) return false;
        return this.recipe.matches(new net.momirealms.craftengine.core.item.recipe.input.BrewingInput<>(
                UniqueIdItem.of(BukkitItemManager.instance().wrap(input.input())),
                UniqueIdItem.of(BukkitItemManager.instance().wrap(input.reagent()))
        ));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull BrewingInput input) {
        return (ItemStack) this.recipe.result(ItemBuildContext.empty()).minecraftItem();
    }
}
