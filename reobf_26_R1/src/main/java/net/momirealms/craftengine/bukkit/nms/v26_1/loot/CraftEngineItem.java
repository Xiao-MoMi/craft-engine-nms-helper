package net.momirealms.craftengine.bukkit.nms.v26_1.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.ItemDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CraftEngineItem extends LootPoolSingletonContainer {
    public static final MapCodec<CraftEngineItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Identifier.CODEC.fieldOf("name").forGetter(lootItem -> lootItem.name))
                    .and(singletonFields(instance))
                    .apply(instance, CraftEngineItem::new)
    );
    private final Identifier name;

    public CraftEngineItem(Identifier name, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
    }

    @Override
    public @NotNull MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    @Override
    public void createItemStack(@NotNull Consumer<ItemStack> consumer, @NotNull LootContext lootContext) {
        Optional<ItemDefinition> optionalItemDefinition = BukkitItemManager.instance().getItemDefinition(KeyUtils.identifierToKey(this.name));
        if (optionalItemDefinition.isEmpty()) {
            return;
        }
        BukkitServerPlayer serverPlayer = null;
        if (lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            serverPlayer = BukkitAdaptor.adapt((org.bukkit.entity.Player) player.getBukkitEntity());
        }
        consumer.accept((ItemStack) optionalItemDefinition.get().buildItem(serverPlayer).minecraftItem());
    }
}
