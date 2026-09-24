package net.momirealms.craftengine.bukkit.nms.v26_3.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.predicate.DataComponentPredicate;

public record BrewingPotionPredicate(PotionsPredicate predicate) implements DataComponentPredicate {

    @Override
    public boolean test(Item item) {
        return this.predicate.matches((ItemStack) item.minecraftItem());
    }

    @Override
    public void apply(Item item) {
        this.predicate.potions().flatMap(potions -> potions.stream().findFirst()).ifPresent(potion ->
                ((ItemStack) item.minecraftItem()).set(DataComponents.POTION_CONTENTS, new PotionContents(potion)));
    }
}
