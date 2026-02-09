package net.momirealms.craftengine.bukkit.nms.v1_21_9.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTrimRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InjectedSmithingTrimRecipe extends SmithingTrimRecipe {
    private final CustomSmithingTrimRecipe<ItemStack> recipe;

    public InjectedSmithingTrimRecipe(CustomSmithingTrimRecipe<ItemStack> recipe, Ingredient template, Ingredient base, Ingredient addition, Holder<TrimPattern> pattern, boolean copyDataComponents) {
        super(template, base, addition, pattern, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTrimRecipe of(CustomSmithingTrimRecipe<ItemStack> recipe) {
        Registry<TrimPattern> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        Key pattern = Objects.requireNonNull(recipe.pattern(), "pattern should not be null");
        return new InjectedSmithingTrimRecipe(recipe,
                RecipeHelper.toMinecraft(recipe.template()),
                RecipeHelper.toMinecraft(recipe.base()),
                RecipeHelper.toMinecraft(recipe.addition()),
                registry.get(ResourceLocation.fromNamespaceAndPath(pattern.namespace(), pattern.value()))
                        .orElseThrow(() -> new NullPointerException("Pattern " + recipe.pattern() + " doesn't exist.")),
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
