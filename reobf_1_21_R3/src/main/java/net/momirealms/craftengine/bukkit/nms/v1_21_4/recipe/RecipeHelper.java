package net.momirealms.craftengine.bukkit.nms.v1_21_4.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.recipe.CookingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.remainder.CraftRemainder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeHelper {

    public static Ingredient toMinecraft(net.momirealms.craftengine.core.item.recipe.Ingredient ingredient) {
        if (ingredient == null) {
            return Ingredient.of();
        }
        List<Item> items = new ArrayList<>();
        for (UniqueKey uniqueKey : ingredient.minecraftItems()) {
            Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(uniqueKey.key().namespace(), uniqueKey.key().value()));
            items.add(item);
        }
        return Ingredient.of(items.stream());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Ingredient toMinecraftVisual(net.momirealms.craftengine.core.item.recipe.Ingredient ingredient) {
        if (ingredient == null) {
            return Ingredient.of();
        }
        List itemStacks = BukkitRecipeManager.getIngredientLooks(ingredient);
        return Ingredient.ofStacks(itemStacks);
    }

    public static net.momirealms.craftengine.core.item.recipe.input.CraftingInput toCraftEngine(CraftingInput input) {
        return net.momirealms.craftengine.core.item.recipe.input.CraftingInput
                .of(input.width(), input.height(), input.items().stream().map(it -> UniqueIdItem.of(BukkitItemManager.instance().wrap(it))).toList());
    }

    public static CraftingBookCategory toMinecraft(CraftingRecipeCategory category) {
        if (category == null) return null;
        return CraftingBookCategory.values()[category.ordinal()];
    }

    public static CookingBookCategory toMinecraft(CookingRecipeCategory category) {
        if (category == null) return null;
        return CookingBookCategory.values()[category.ordinal()];
    }

    public static net.minecraft.world.item.ItemStack craftingRemainer(Key recipeId, net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        net.momirealms.craftengine.core.item.Item item = BukkitItemManager.instance().wrap(stack);
        Optional<CustomItem> optionalCustomItem = item.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem customItem = optionalCustomItem.get();
            if (!customItem.isVanillaItem()) {
                CraftRemainder remainder = customItem.settings().craftRemainder();
                if (remainder != null) {
                    net.momirealms.craftengine.core.item.Item remainingItem = remainder.remainder(recipeId, item);
                    return remainingItem == null ? net.minecraft.world.item.ItemStack.EMPTY : (net.minecraft.world.item.ItemStack) remainingItem.getMinecraftItem();
                }
                return net.minecraft.world.item.ItemStack.EMPTY;
            }
        }
        return stack.getItem().getCraftingRemainder();
    }

    @NotNull
    public static NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(Key recipeId, CraftingInput input) {
        NonNullList<net.minecraft.world.item.ItemStack> list = NonNullList.withSize(input.size(), net.minecraft.world.item.ItemStack.EMPTY);
        for (int i = 0; i < list.size(); ++i) {
            list.set(i, RecipeHelper.craftingRemainer(recipeId, input.getItem(i)));
        }
        return list;
    }
}
