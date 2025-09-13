package net.momirealms.craftengine.bukkit.nms.v1_21_2.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InjectedSmithingTransformRecipe extends SmithingTransformRecipe {
    private final CustomSmithingTransformRecipe<ItemStack> recipe;

    public InjectedSmithingTransformRecipe(CustomSmithingTransformRecipe<ItemStack> recipe, Optional<Ingredient> template, Optional<Ingredient> base, Optional<Ingredient> addition, net.minecraft.world.item.ItemStack result, boolean copyDataComponents) {
        super(template, base, addition, result, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTransformRecipe of(CustomSmithingTransformRecipe<ItemStack> recipe) {
        net.minecraft.world.item.ItemStack result = (net.minecraft.world.item.ItemStack) recipe.result().buildItem(ItemBuildContext.empty()).getLiteralObject();
        return new InjectedSmithingTransformRecipe(recipe,
                Optional.ofNullable(recipe.template()).map(RecipeHelper::toMinecraft),
                Optional.ofNullable(recipe.base()).map(RecipeHelper::toMinecraft),
                Optional.ofNullable(recipe.addition()).map(RecipeHelper::toMinecraft),
                result,
                true
        );
    }

    @Override
    public boolean matches(@NotNull SmithingRecipeInput input, @NotNull Level level) {
        boolean vanillaMatches = super.matches(input, level);
        if (!vanillaMatches) return false;
        Item<ItemStack> template = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(input.template()));
        Item<ItemStack> base = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(input.base()));
        Item<ItemStack> addition = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(input.addition()));
        SmithingInput<ItemStack> smithingInput = new SmithingInput<>(UniqueIdItem.of(base), UniqueIdItem.of(template), UniqueIdItem.of(addition));
        return this.recipe.matches(smithingInput);
    }

    @Override
    public boolean showNotification() {
        return this.recipe.showNotification();
    }
}
