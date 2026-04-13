package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomDyeRecipe;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedDyeRecipe extends DyeRecipe {
    private final CustomDyeRecipe recipe;

    public InjectedDyeRecipe(CustomDyeRecipe recipe,
                             ItemStackTemplate template,
                             CommonInfo commonInfo,
                             CraftingBookInfo craftingBookInfo,
                             Ingredient visualTarget,
                             Ingredient visualDye) {
        super(commonInfo, craftingBookInfo, visualTarget, visualDye, template);
        this.recipe = recipe;
    }

    public static InjectedDyeRecipe of(CustomDyeRecipe recipe) {
        return new InjectedDyeRecipe(
                recipe,
                ItemStackTemplate.fromNonEmptyStack((net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem()),
                new CommonInfo(recipe.showNotification()),
                new CraftingBookInfo(RecipeHelper.toMinecraft(recipe.category()), recipe.group()),
                RecipeHelper.toMinecraftVisual(recipe.target()),
                RecipeHelper.toMinecraftVisual(recipe.dye())
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }
}
