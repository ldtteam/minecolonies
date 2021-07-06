package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

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
    public ActionResultType useOn(final ItemUseContext useContext)
    {
        // if server world, do nothing
        if (useContext.getLevel().isClientSide)
        {
            return ActionResultType.FAIL;
        }

        final PlayerEntity player = useContext.getPlayer();

        final ItemStack scepter = useContext.getPlayer().getItemInHand(useContext.getHand());
        final CompoundNBT compound = scepter.getOrCreateTag();

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
                LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.removehive");
                building.removeHive(pos);
                SoundUtils.playSoundForPlayer((ServerPlayerEntity) player, SoundEvents.NOTE_BLOCK_BELL,
                        (float) SoundUtils.VOLUME * 2, 0.5f);
            }
            else
            {
                if (positions.size() < building.getMaximumHives())
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.addhive");
                    building.addHive(pos);
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                }
                if (positions.size() >= building.getMaximumHives())
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.maxhives");
                    player.inventory.removeItemNoUpdate(player.inventory.selected);
                }
            }
        }
        else
        {
            player.inventory.removeItemNoUpdate(player.inventory.selected);
        }

        return super.useOn(useContext);
    }
}
