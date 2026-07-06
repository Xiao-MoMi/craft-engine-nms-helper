package net.momirealms.craftengine.bukkit.nms.v26_2.recipe;

import net.minecraft.world.item.ItemStackTemplate;
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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InjectedSmithingTransformRecipe extends SmithingTransformRecipe {
    private final CustomSmithingTransformRecipe recipe;

    public InjectedSmithingTransformRecipe(CustomSmithingTransformRecipe recipe,
                                           CommonInfo commonInfo,
                                           Optional<Ingredient> template,
                                           Ingredient base,
                                           Optional<Ingredient> addition,
                                           ItemStackTemplate result) {
        super(commonInfo, template, base, addition, result);
        this.recipe = recipe;
    }

    public static InjectedSmithingTransformRecipe of(CustomSmithingTransformRecipe recipe) {
        net.minecraft.world.item.ItemStack result = (net.minecraft.world.item.ItemStack) recipe.buildVisualOrActualResult(ItemBuildContext.empty()).minecraftItem();
        return new InjectedSmithingTransformRecipe(recipe,
                new CommonInfo(recipe.showNotification()),
                Optional.ofNullable(recipe.template()).map(RecipeHelper::toMinecraft),
                RecipeHelper.toMinecraft(recipe.base()),
                Optional.ofNullable(recipe.addition()).map(RecipeHelper::toMinecraft),
                new ItemStackTemplate(result.typeHolder(), result.getCount(), result.getComponentsPatch())
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
