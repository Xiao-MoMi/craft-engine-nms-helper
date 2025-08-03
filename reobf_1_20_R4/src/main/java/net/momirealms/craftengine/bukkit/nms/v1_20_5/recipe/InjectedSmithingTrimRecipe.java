package net.momirealms.craftengine.bukkit.nms.v1_20_5.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
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

public class InjectedSmithingTrimRecipe extends SmithingTrimRecipe {
    private final CustomSmithingTrimRecipe<ItemStack> recipe;

    public InjectedSmithingTrimRecipe(CustomSmithingTrimRecipe<ItemStack> recipe, Ingredient template, Ingredient base, Ingredient addition, boolean copyDataComponents) {
        super(template, base, addition, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTrimRecipe of(CustomSmithingTrimRecipe<ItemStack> recipe) {
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
        Item<ItemStack> template = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(inventory.getItem(0)));
        Item<ItemStack> base = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(inventory.getItem(1)));
        Item<ItemStack> addition = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(inventory.getItem(2)));
        SmithingInput<ItemStack> smithingInput = new SmithingInput<>(UniqueIdItem.of(base), UniqueIdItem.of(template), UniqueIdItem.of(addition));
        return this.recipe.matches(smithingInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
