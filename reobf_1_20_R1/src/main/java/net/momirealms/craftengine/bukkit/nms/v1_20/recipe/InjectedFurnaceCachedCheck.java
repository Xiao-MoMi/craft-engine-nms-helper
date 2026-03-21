package net.momirealms.craftengine.bukkit.nms.v1_20.recipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.nms.Clearable;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.ConditionalRecipe;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class InjectedFurnaceCachedCheck<C extends Container, T extends Recipe<C>> implements RecipeManager.CachedCheck<C, T>, Clearable {
    private final RecipeType<T> type;
    // AbstractFurnaceBlockEntity or CampfireBlockEntity.
    private final BlockEntity blockEntity;
    @Nullable
    private ResourceLocation lastRecipe;
    @Nullable
    private ItemStack itemStackCache;
    private boolean isFailure;

    @SuppressWarnings("unchecked")
    public InjectedFurnaceCachedCheck(RecipeType<?> type, BlockEntity blockEntity) {
        this.type = (RecipeType<T>) type;
        this.blockEntity = blockEntity;
    }

    @Override
    public @NotNull Optional<T> getRecipeFor(@NotNull C inventory, @NotNull Level world) {
        ItemStack inputItemStack = inventory.getItem(0);
        // 缓存提前返回
        if (inputItemStack == itemStackCache && isFailure) {
            return Optional.empty();
        }

        // 缓存变动
        if (inputItemStack != itemStackCache) {
            // 更新缓存
            this.itemStackCache = inputItemStack;
            // 查配方
            Item wrapped = CraftEngine.instance().itemManager().wrap(inputItemStack);
            SingleItemInput itemInput = new SingleItemInput(UniqueIdItem.of(wrapped));
            ConditionalRecipe recipe = (ConditionalRecipe) CraftEngine.instance().recipeManager()
                    .recipeByInput(net.momirealms.craftengine.core.item.recipe.RecipeType.SMELTING, itemInput);
            // 没查到, 走原版逻辑, 标记查询成功.
            if (recipe == null) {
                isFailure = false;
            }
            // 查到配方, 检查条件
            else if (recipe.hasCondition()) {
                // 从 PDC 取出需要检查条件的玩家;
                // todo: NamespacedKey 换成 RecipeEventListener.FURNACE_PLAYER_KEY.
                long[] uuidLongs = blockEntity.persistentDataContainer.get(new NamespacedKey("craftengine", "furnace-player"), PersistentDataType.LONG_ARRAY);
                if (uuidLongs != null) {
                    UUID playerUuid = new UUID(uuidLongs[0], uuidLongs[1]);
                    Player conditionPlayer = Bukkit.getPlayer(playerUuid);
                    // 如果玩家不在线, 默认条件失败.
                    if (conditionPlayer == null || !conditionPlayer.isConnected()) {
                        isFailure = true;
                        return Optional.empty();
                    }
                    // 检查配方条件.
                    boolean result = recipe.canUse(PlayerOptionalContext.of(BukkitAdaptor.adapt(conditionPlayer)));
                    if (!result) {
                        isFailure = true;
                        return Optional.empty();
                    }
                    isFailure = false;
                }
            }
        }

        // 默认逻辑
        RecipeManager craftingManager = world.getRecipeManager();
        Optional<Pair<ResourceLocation, T>> optional = craftingManager.getRecipeFor(type, inventory, world, this.lastRecipe);

        if (optional.isPresent()) {
            Pair<ResourceLocation, T> pair = optional.get();
            this.lastRecipe = pair.getFirst();
            return Optional.of(pair.getSecond());
        } else {
            return Optional.empty();
        }
    }

    // 清理缓存
    @Override
    public void clear() {
        this.itemStackCache = null;
    }
}
