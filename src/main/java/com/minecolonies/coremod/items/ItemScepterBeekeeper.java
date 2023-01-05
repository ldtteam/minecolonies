package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;

import java.util.Collection;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

import net.minecraft.world.item.Item.Properties;

/**
 * Beekeeper Scepter Item class. Used to give tasks to Beekeeper.
 */
public class ItemScepterBeekeeper extends AbstractItemMinecolonies
{
    /**
     * BeekeeperScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterBeekeeper(final Properties properties)
    {
        super("scepterbeekeeper", properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(final UseOnContext useContext)
    {
        // if server world, do nothing
        if (useContext.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final Player player = useContext.getPlayer();

        final ItemStack scepter = useContext.getPlayer().getItemInHand(useContext.getHand());
        final CompoundTag compound = scepter.getOrCreateTag();

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), useContext.getLevel());
        final BlockPos hutPos = BlockPosUtil.read(compound, TAG_POS);
        final IBuilding hut = colony.getBuildingManager().getBuilding(hutPos);
        final BuildingBeekeeper building = (BuildingBeekeeper) hut;

        if (useContext.getLevel().getBlockState(useContext.getClickedPos()).getBlock() instanceof BeehiveBlock)
        {

            final Collection<BlockPos> positions = building.getHives();

            final BlockPos pos = useContext.getClickedPos();
            if (positions.contains(pos))
            {
                MessageUtils.format(TOOL_BEEHIVE_SCEPTER_REMOVE_HIVE).sendTo(useContext.getPlayer());
                building.removeHive(pos);
                SoundUtils.playSoundForPlayer((ServerPlayer) player, SoundEvents.NOTE_BLOCK_BELL.get(), (float) SoundUtils.VOLUME * 2, 0.5f);
            }
            else
            {
                if (positions.size() < building.getMaximumHives())
                {
                    MessageUtils.format(TOOL_BEEHIVE_SCEPTER_ADD_HIVE).sendTo(useContext.getPlayer());
                    building.addHive(pos);
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                }
                if (positions.size() >= building.getMaximumHives())
                {
                    MessageUtils.format(TOOL_BEEHIVE_SCEPTER_MAX_HIVES).sendTo(useContext.getPlayer());
                    player.getInventory().removeItemNoUpdate(player.getInventory().selected);
                }
            }
        }
        else
        {
            player.getInventory().removeItemNoUpdate(player.getInventory().selected);
        }

        return super.useOn(useContext);
    }
}
