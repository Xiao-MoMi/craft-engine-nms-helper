package net.momirealms.craftengine.bukkit.nms.v1_21_5.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InjectedShapelessRecipe extends ShapelessRecipe {
    private final CustomShapelessRecipe<ItemStack> recipe;
    private final List<Ingredient> visualIngredients;
    private final net.minecraft.world.item.ItemStack result;
    private PlacementInfo placementInfo;

    public InjectedShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe,
                                   String group,
                                   CraftingBookCategory category,
                                   net.minecraft.world.item.ItemStack result,
                                   List<Ingredient> visualIngredients,
                                   List<Ingredient> roughIngredients) {
        super(group, category, result, roughIngredients);
        this.recipe = recipe;
        this.visualIngredients = visualIngredients;
        this.result = result;
    }

    public static InjectedShapelessRecipe of(CustomShapelessRecipe<ItemStack> recipe) {
        List<Ingredient> visualIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraftVisual).toList();
        List<Ingredient> roughIngredients = recipe.ingredientsInUse().stream().map(RecipeHelper::toMinecraft).toList();
        return new InjectedShapelessRecipe(
                recipe,
                recipe.group(),
                RecipeHelper.toMinecraft(recipe.category()),
                (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.EMPTY).getLiteralObject(),
                visualIngredients,
                roughIngredients
        );
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
        if (!vanillaMatches) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return RecipeHelper.getRemainingItems(input);
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.visualIngredients);
        }
        return this.placementInfo;
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(this.visualIngredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE))
        );
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
