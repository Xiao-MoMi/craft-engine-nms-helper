package net.momirealms.craftengine.bukkit.nms.v1_20.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTrimRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import org.jetbrains.annotations.NotNull;

public class InjectedSmithingTrimRecipe extends SmithingTrimRecipe {
    private final CustomSmithingTrimRecipe recipe;

    public InjectedSmithingTrimRecipe(CustomSmithingTrimRecipe recipe, Ingredient template, Ingredient base, Ingredient addition, boolean copyDataComponents) {
        super(new ResourceLocation(recipe.id().namespace(), recipe.id().value()), template, base, addition, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTrimRecipe of(CustomSmithingTrimRecipe recipe) {
        return new InjectedSmithingTrimRecipe(recipe,
                RecipeHelper.toMinecraft(recipe.template()),
                RecipeHelper.toMinecraft(recipe.base()),
                RecipeHelper.toMinecraft(recipe.addition()),
                true
        );
    }

    @Override
    public boolean matches(@NotNull Container inventory, @NotNull Level world) {
        boolean vanillaMatches = super.matches(inventory, world);
        if (!vanillaMatches) return false;
        Item template = BukkitItemManager.instance().wrap(inventory.getItem(0));
        Item base = BukkitItemManager.instance().wrap(inventory.getItem(1));
        Item addition = BukkitItemManager.instance().wrap(inventory.getItem(2));
        SmithingInput smithingInput = new SmithingInput(UniqueIdItem.of(base), UniqueIdItem.of(template), UniqueIdItem.of(addition));
        return this.recipe.matches(smithingInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
