package net.momirealms.craftengine.bukkit.nms.v1_21_2.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTrimRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InjectedSmithingTrimRecipe extends SmithingTrimRecipe {
    private final CustomSmithingTrimRecipe recipe;

    public InjectedSmithingTrimRecipe(CustomSmithingTrimRecipe recipe, Optional<Ingredient> template, Optional<Ingredient> base, Optional<Ingredient> addition, boolean copyDataComponents) {
        super(template, base, addition, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTrimRecipe of(CustomSmithingTrimRecipe recipe) {
        return new InjectedSmithingTrimRecipe(recipe,
                Optional.of(recipe.template()).map(RecipeHelper::toMinecraft),
                Optional.of(recipe.base()).map(RecipeHelper::toMinecraft),
                Optional.of(recipe.addition()).map(RecipeHelper::toMinecraft),
                true
        );
    }

    @Override
    public boolean matches(@NotNull SmithingRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
        if (!vanillaMatches) return false;
        Item template = BukkitItemManager.instance().wrap(input.template());
        Item base = BukkitItemManager.instance().wrap(input.base());
        Item addition = BukkitItemManager.instance().wrap(input.addition());
        SmithingInput smithingInput = new SmithingInput(UniqueIdItem.of(base), UniqueIdItem.of(template), UniqueIdItem.of(addition));
        return this.recipe.matches(smithingInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
