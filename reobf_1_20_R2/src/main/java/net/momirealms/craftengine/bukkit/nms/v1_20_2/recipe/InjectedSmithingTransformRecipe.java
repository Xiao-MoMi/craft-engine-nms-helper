package net.momirealms.craftengine.bukkit.nms.v1_20_2.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import org.jetbrains.annotations.NotNull;

public class InjectedSmithingTransformRecipe extends SmithingTransformRecipe {
    private final CustomSmithingTransformRecipe recipe;

    public InjectedSmithingTransformRecipe(CustomSmithingTransformRecipe recipe, Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);
        this.recipe = recipe;
    }

    public static InjectedSmithingTransformRecipe of(CustomSmithingTransformRecipe recipe) {
        net.minecraft.world.item.ItemStack result = (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem();
        return new InjectedSmithingTransformRecipe(recipe,
                RecipeHelper.toMinecraft(recipe.template()),
                RecipeHelper.toMinecraft(recipe.base()),
                RecipeHelper.toMinecraft(recipe.addition()),
                result
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
