package net.momirealms.craftengine.bukkit.nms.v1_20.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.core.item.BuildableItem;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.CookingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeHelper {

    public static Ingredient toMinecraft(net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack> ingredient) {
        if (ingredient == null) {
            return Ingredient.EMPTY;
        }
        List<Item> items = new ArrayList<>();
        for (UniqueKey uniqueKey : ingredient.minecraftItems()) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(uniqueKey.key().namespace(), uniqueKey.key().value()));
            items.add(item);
        }
        return Ingredient.of(items.toArray(new Item[0]));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Ingredient toMinecraftVisual(net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack> ingredient) {
        if (ingredient == null) {
            return Ingredient.EMPTY;
        }
        List itemStacks = BukkitRecipeManager.getIngredientLooks(ingredient.items());
        net.minecraft.world.item.ItemStack[] exactItems = new net.minecraft.world.item.ItemStack[itemStacks.size()];
        for (int i = 0; i < itemStacks.size(); i++) {
            exactItems[i] = (net.minecraft.world.item.ItemStack) itemStacks.get(i);
        }
        Ingredient nmsIngredient = Ingredient.of(itemStacks.stream());
        nmsIngredient.exact = true;
        nmsIngredient.itemStacks = exactItems;
        return nmsIngredient;
    }

    public static net.momirealms.craftengine.core.item.recipe.input.CraftingInput<ItemStack> toCraftEngine(CraftingContainer input) {
        return net.momirealms.craftengine.core.item.recipe.input.CraftingInput
                .of(input.getWidth(), input.getHeight(), input.getItems().stream().map(it -> UniqueIdItem.of(BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(it)))).toList());
    }

    public static CraftingBookCategory toMinecraft(CraftingRecipeCategory category) {
        if (category == null) return null;
        return CraftingBookCategory.values()[category.ordinal()];
    }

    public static CookingBookCategory toMinecraft(CookingRecipeCategory category) {
        if (category == null) return null;
        return CookingBookCategory.values()[category.ordinal()];
    }

    public static net.minecraft.world.item.ItemStack craftingRemainer(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        net.momirealms.craftengine.core.item.Item<ItemStack> item = BukkitItemManager.instance().wrap(CraftItemStack.asCraftMirror(stack));
        Optional<CustomItem<ItemStack>> optionalCustomItem = item.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            if (!customItem.isVanillaItem()) {
                Key remainder = customItem.settings().craftRemainder();
                if (remainder != null) {
                    Optional<? extends BuildableItem<ItemStack>> optionalBuildableItem = BukkitItemManager.instance().getBuildableItem(remainder);
                    if (optionalBuildableItem.isPresent()) {
                        return (net.minecraft.world.item.ItemStack) optionalBuildableItem.get().buildItem(ItemBuildContext.EMPTY).getLiteralObject();
                    }
                }
                return net.minecraft.world.item.ItemStack.EMPTY;
            }
        }
        Item remainingItem = stack.getItem().getCraftingRemainingItem();
        return remainingItem == null ? net.minecraft.world.item.ItemStack.EMPTY : new net.minecraft.world.item.ItemStack(remainingItem);
    }

    @NotNull
    public static NonNullList<net.minecraft.world.item.ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<net.minecraft.world.item.ItemStack> list = NonNullList.withSize(input.getContainerSize(), net.minecraft.world.item.ItemStack.EMPTY);
        for (int i = 0; i < list.size(); ++i) {
            list.set(i, RecipeHelper.craftingRemainer(input.getItem(i)));
        }
        return list;
    }
}
