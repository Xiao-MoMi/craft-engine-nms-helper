package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
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
    private final Ingredient target;
    private final Ingredient dye;

    public InjectedDyeRecipe(CustomDyeRecipe recipe,
                             ItemStackTemplate template,
                             CommonInfo commonInfo,
                             CraftingBookInfo craftingBookInfo,
                             Ingredient visualTarget,
                             Ingredient roughTarget,
                             Ingredient visualDye,
                             Ingredient roughDye) {
        super(commonInfo, craftingBookInfo, visualTarget, visualDye, template);
        this.recipe = recipe;
        this.target = roughTarget;
        this.dye = roughDye;
    }

    public static InjectedDyeRecipe of(CustomDyeRecipe recipe) {
        return new InjectedDyeRecipe(
                recipe,
                ItemStackTemplate.fromNonEmptyStack((net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem()),
                new CommonInfo(recipe.showNotification()),
                new CraftingBookInfo(RecipeHelper.toMinecraft(recipe.category()), recipe.group()),
                RecipeHelper.toMinecraftVisual(recipe.target()),
                RecipeHelper.toMinecraft(recipe.target()),
                RecipeHelper.toMinecraftVisual(recipe.dye()),
                RecipeHelper.toMinecraft(recipe.dye())
        );
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input) {
        return (ItemStack) this.recipe.assemble(RecipeHelper.toCraftEngine(input), ItemBuildContext.empty()).minecraftItem();
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        if (!vanillaMatches(input, level)) return false;
        return this.recipe.matches(RecipeHelper.toCraftEngine(input));
    }

    public boolean vanillaMatches(CraftingInput input, Level level) {
        if (input.ingredientCount() < 2) {
            return false;
        } else {
            boolean hasTarget = false;
            boolean hasDyes = false;
            for(int slot = 0; slot < input.size(); ++slot) {
                ItemStack itemStack = input.getItem(slot);
                if (!itemStack.isEmpty()) {
                    if (this.target.test(itemStack)) {
                        if (hasTarget) {
                            return false;
                        }
                        hasTarget = true;
                    } else {
                        if (!this.dye.test(itemStack)) {
                            return false;
                        }
                        hasDyes = true;
                    }
                }
            }
            return hasDyes && hasTarget;
        }
    }

    @Override
    public @NotNull NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return RecipeHelper.getRemainingItems(this.recipe.id(), input);
    }
}
