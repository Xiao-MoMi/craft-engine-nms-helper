package net.momirealms.craftengine.bukkit.nms.v1_20.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class CraftEngineItem extends LootPoolSingletonContainer {
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(new CraftEngineItem.Serializer());
    private final ResourceLocation item;

    public CraftEngineItem(ResourceLocation item, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.item = item;
    }

    @Override
    public @NotNull LootPoolEntryType getType() {
        return TYPE;
    }

    @Override
    public void createItemStack(@NotNull Consumer<ItemStack> consumer, @NotNull LootContext context) {
        Optional<CustomItem<org.bukkit.inventory.ItemStack>> optionalCustomItem = BukkitItemManager.instance().getCustomItem(KeyUtils.resourceLocationToKey(this.item));
        if (optionalCustomItem.isEmpty()) {
            return;
        }
        BukkitServerPlayer serverPlayer = null;
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player) {
            serverPlayer = BukkitAdaptors.adapt((org.bukkit.entity.Player) player.getBukkitEntity());
        }
        consumer.accept((ItemStack) optionalCustomItem.get().buildItem(serverPlayer).getLiteralObject());
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<CraftEngineItem> {
        @Override
        public void serializeCustom(@NotNull JsonObject json, @NotNull CraftEngineItem entry, @NotNull JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("name", entry.item.toString());
        }

        @Override
        protected @NotNull CraftEngineItem deserialize(JsonObject jsonObject,
                                                       @NotNull JsonDeserializationContext jsonDeserializationContext,
                                                       int i,
                                                       int j,
                                                       LootItemCondition @NotNull [] lootItemConditions,
                                                       LootItemFunction @NotNull [] lootItemFunctions) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(jsonObject.get("name").getAsString());
            return new CraftEngineItem(resourceLocation, i, j, lootItemConditions, lootItemFunctions);
        }
    }
}
