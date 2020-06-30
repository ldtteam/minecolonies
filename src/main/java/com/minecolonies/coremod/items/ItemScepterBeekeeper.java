package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
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
        super("scepterbeekeeper", properties.maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext useContext)
    {
        // if server world, do nothing
        if (useContext.getWorld().isRemote)
        {
            return ActionResultType.FAIL;
        }

        final PlayerEntity player = useContext.getPlayer();

        final ItemStack scepter = useContext.getPlayer().getHeldItem(useContext.getHand());
        final CompoundNBT compound = scepter.getOrCreateTag();

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), useContext.getWorld());
        final BlockPos hutPos = BlockPosUtil.read(compound, TAG_POS);
        final IBuilding hut = colony.getBuildingManager().getBuilding(hutPos);
        final BuildingBeekeeper building = (BuildingBeekeeper) hut;

        if (useContext.getWorld().getBlockState(useContext.getPos()).getBlock() instanceof BeehiveBlock)
        {

            final Collection<BlockPos> positions = building.getHives();

            final BlockPos pos = useContext.getPos();
            if (positions.contains(pos))
            {
                LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.removehive");
                building.removeHive(pos);
            }
            else
            {
                if (positions.size() < building.getMaximumHives())
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.addhive");
                    building.addHive(pos);
                }
                if (positions.size() >= building.getMaximumHives())
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.maxhives");
                    player.inventory.removeStackFromSlot(player.inventory.currentItem);
                }
            }
        }
        else
        {
            player.inventory.removeStackFromSlot(player.inventory.currentItem);
        }

        return super.onItemUse(useContext);
    }
}
