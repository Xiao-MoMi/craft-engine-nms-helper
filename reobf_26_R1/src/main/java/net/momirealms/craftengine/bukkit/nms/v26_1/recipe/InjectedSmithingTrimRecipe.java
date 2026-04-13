package net.momirealms.craftengine.bukkit.nms.v26_1.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InjectedSmithingTrimRecipe extends SmithingTrimRecipe {
    private final CustomSmithingTrimRecipe recipe;

    public InjectedSmithingTrimRecipe(CustomSmithingTrimRecipe recipe,
                                      CommonInfo commonInfo,
                                      Ingredient template,
                                      Ingredient base,
                                      Ingredient addition,
                                      Holder<TrimPattern> pattern,
                                      boolean copyDataComponents) {
        super(commonInfo, template, base, addition, pattern, copyDataComponents);
        this.recipe = recipe;
    }

    public static InjectedSmithingTrimRecipe of(CustomSmithingTrimRecipe recipe) {
        Registry<TrimPattern> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        Key pattern = Objects.requireNonNull(recipe.pattern(), "pattern should not be null");
        return new InjectedSmithingTrimRecipe(recipe,
                new CommonInfo(recipe.showNotification()),
                RecipeHelper.toMinecraft(recipe.template()),
                RecipeHelper.toMinecraft(recipe.base()),
                RecipeHelper.toMinecraft(recipe.addition()),
                registry.get(Identifier.fromNamespaceAndPath(pattern.namespace(), pattern.value()))
                        .orElseThrow(() -> new NullPointerException("Pattern " + recipe.pattern() + " doesn't exist.")),
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

}
