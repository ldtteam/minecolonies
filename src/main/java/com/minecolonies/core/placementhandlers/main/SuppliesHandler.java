package com.minecolonies.core.placementhandlers.main;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.component.SupplyData;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.MineColonies;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.ProgressTranslationConstants.PROGRESS_SUPPLY_CHEST_PLACED;

public class SuppliesHandler implements ISurvivalBlueprintHandler
{
    public static final String ID = MOD_ID + ":supplies";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Component getDisplayName()
    {
        // this should never actually be visible
        return Component.translatableEscape("com.minecolonies.coremod.supplies.placement");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canHandle(final Blueprint blueprint, final ClientLevel clientLevel, final Player player, final BlockPos blockPos, final RotationMirror rotMir)
    {
        return false;
    }

    @Override
    public void handle(
            final Blueprint blueprint,
            final String packName,
            final String blueprintPath,
            final boolean clientPack,
            final Level world,
            final Player playerArg,
            final BlockPos blockPos,
            final RotationMirror rotMir)
    {
        if (clientPack || !StructurePacks.hasPack(packName))
        {
            MessageUtils.format(NO_CUSTOM_CAMPS).sendTo(playerArg);
            SoundUtils.playErrorSound(playerArg, playerArg.blockPosition());
            return;
        }

        final ServerPlayer player = (ServerPlayer) playerArg;

        blueprint.setRotationMirror(rotMir, world);

        if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) > 0 && !MineColonies.getConfig().getServer().allowInfiniteSupplyChests.get()
                && !isFreeInstantPlacementMH(player) && !player.isCreative())
        {
            MessageUtils.format(WARNING_SUPPLY_CHEST_ALREADY_PLACED).sendTo(player);
            SoundUtils.playErrorSound(player, player.blockPosition());
            return;
        }

        Predicate<ItemStack> searchPredicate = stack -> !stack.isEmpty();
        if (blueprintPath.contains("supplyship"))
        {
            searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyChest), true, false));
        }
        if (blueprintPath.contains("supplycamp"))
        {
            searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyCamp), true, false));
        }

        if (isFreeInstantPlacementMH(player))
        {
            searchPredicate = searchPredicate.and(stack -> SupplyData.readFromItemStack(stack).instantPlacement());
        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(new InvWrapper(player.getInventory()), searchPredicate);

        if (slot != -1 && !ItemStackUtils.isEmpty(player.getInventory().removeItemNoUpdate(slot)))
        {
            if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) < 1)
            {
                MessageUtils.format(PROGRESS_SUPPLY_CHEST_PLACED).sendTo(player);
                player.awardStat(Stats.ITEM_USED.get(ModItems.supplyChest), 1);
                AdvancementTriggers.PLACE_SUPPLY.get().trigger(player);
            }

            SoundUtils.playSuccessSound(player, player.blockPosition());

            StructurePlacementUtils.loadAndPlaceStructureWithRotation(player.level(), blueprint, blockPos, rotMir, true, player);
        }
        else
        {
            MessageUtils.format(WARNING_REMOVING_SUPPLY_CHEST).sendTo(player);
        }
    }

    /**
     * Whether the itemstack used allows a free placement.
     *
     * @param playerEntity the player to check
     * @return whether the itemstack used allows a free placement.
     */
    private boolean isFreeInstantPlacementMH(ServerPlayer playerEntity)
    {
        final ItemStack mhItem = playerEntity.getMainHandItem();
        return !ItemStackUtils.isEmpty(mhItem) && SupplyData.readFromItemStack(mhItem).instantPlacement();
    }
}
