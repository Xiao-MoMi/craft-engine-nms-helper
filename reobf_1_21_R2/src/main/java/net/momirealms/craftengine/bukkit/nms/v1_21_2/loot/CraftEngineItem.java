package net.momirealms.craftengine.bukkit.nms.v1_21_2.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CraftEngineItem extends LootPoolSingletonContainer {
    public static final MapCodec<CraftEngineItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ResourceLocation.CODEC.fieldOf("name").forGetter(lootItem -> lootItem.name))
                    .and(singletonFields(instance))
                    .apply(instance, CraftEngineItem::new)
    );
    public static final LootPoolEntryType TYPE = new LootPoolEntryType(CODEC);
    private final ResourceLocation name;

    public CraftEngineItem(ResourceLocation name, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
    }

    @Override
    protected void createItemStack(@NotNull Consumer<ItemStack> consumer, @NotNull LootContext lootContext) {
        Optional<CustomItem> optionalCustomItem = BukkitItemManager.instance().getCustomItem(KeyUtils.identifierToKey(this.name));
        if (optionalCustomItem.isEmpty()) {
            return;
        }
        BukkitServerPlayer serverPlayer = null;
        if (lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            serverPlayer = BukkitAdaptor.adapt((org.bukkit.entity.Player) player.getBukkitEntity());
        }
        consumer.accept((ItemStack) optionalCustomItem.get().buildItem(serverPlayer).getMinecraftItem());
    }

    @Override
    public @NotNull LootPoolEntryType getType() {
        return TYPE;
    }
}
