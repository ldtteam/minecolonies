package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.client.gui.questlog.WindowQuestLogInProgress;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_QUEST_LOG_NEED_COLONY;

/**
 * Class describing the quest log item.
 */
public class ItemQuestLog extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    public static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the quest log item.
     *
     * @param properties the properties.
     */
    public ItemQuestLog(final Item.Properties properties)
    {
        super("questlog", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    @NotNull
    public InteractionResult useOn(final UseOnContext ctx)
    {
        final ItemStack questLog = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundTag compound = checkForCompound(questLog);
        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof AbstractTileEntityColonyBuilding buildingEntity && buildingEntity.getBuilding() instanceof BuildingTownHall)
        {
            compound.putInt(TAG_COLONY, buildingEntity.getColonyId());
            if (!ctx.getLevel().isClientSide)
            {
                MessageUtils.format(COM_MINECOLONIES_QUEST_LOG_NEED_COLONY, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(compound, ctx.getLevel(), ctx.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(
      final Level worldIn,
      final Player playerIn,
      final InteractionHand hand)
    {
        final ItemStack questLog = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide)
        {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, questLog);
        }

        openWindow(checkForCompound(questLog), worldIn, playerIn);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, questLog);
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param questLog the quest log item to check for.
     * @return the compound of the quest log.
     */
    private static CompoundTag checkForCompound(final ItemStack questLog)
    {
        if (!questLog.hasTag())
        {
            questLog.setTag(new CompoundTag());
        }
        return questLog.getTag();
    }

    /**
     * Opens the quest log window if there is a valid colony linked
     *
     * @param compound the item compound
     * @param player   the player entity opening the window
     */
    private static void openWindow(CompoundTag compound, Level world, Player player)
    {
        if (compound.contains(TAG_COLONY))
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY), world.dimension());
            if (colonyView != null)
            {
                new WindowQuestLogInProgress(colonyView).open();
            }
        }
        else
        {
            player.displayClientMessage(Component.translatable(TranslationConstants.COM_MINECOLONIES_QUEST_LOG_NEED_COLONY), true);
        }
    }
}

