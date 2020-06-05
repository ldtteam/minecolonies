package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Beekeeper Scepter Item class. Used to give tasks to Beekeeper.
 */
public class ItemScepterBeekeeper extends AbstractItemMinecolonies
{
    private static final String NBT_HIVE_POS = Constants.MOD_ID + ":" + "hives";

    /**
     * BeekeeperScepter constructor. Sets max stack to 1, like other tools.
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

        final ItemStack scepter = useContext.getPlayer().getHeldItem(useContext.getHand());
        final CompoundNBT compound = scepter.getOrCreateTag();

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), useContext.getWorld());
        final BlockPos hutPos = BlockPosUtil.read(compound, TAG_POS);
        final IBuilding hut = colony.getBuildingManager().getBuilding(hutPos);
        final BuildingBeekeeper building = (BuildingBeekeeper) hut;

        if (useContext.getWorld().getBlockState(useContext.getPos()).getBlock() instanceof BeehiveBlock)
        {

            final Collection<BlockPos> positions = readPositions(compound);

            final BlockPos pos = useContext.getPos();
            if (positions.contains(pos))
            {
                LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.removehive");
                positions.remove(pos);
            }
            else
            {
                if (positions.size() < building.getMaximumHives())
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.addhive");
                    positions.add(pos);
                }
                else
                {
                    LanguageHandler.sendPlayerMessage(useContext.getPlayer(), "item.minecolonies.scepterbeekeeper.maxhives");
                    save(useContext.getPlayer(), building, compound);
                }
            }
            writePositions(positions, compound);
        }
        else
        {
            save(useContext.getPlayer(), building, compound);
        }

        return super.onItemUse(useContext);
    }

    private static void save(final PlayerEntity player, final BuildingBeekeeper building, final CompoundNBT compound)
    {
        LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterbeekeeper.done");
        building.setHives(readPositions(compound));
        player.inventory.removeStackFromSlot(player.inventory.currentItem);
    }

    private static void writePositions(final Collection<BlockPos> positions, final CompoundNBT compound)
    {
        compound.put(NBT_HIVE_POS, positions.stream()
                                     .map(NBTUtil::writeBlockPos)
                                     .collect(NBTUtils.toListNBT()));
    }

    @NotNull
    private static Set<BlockPos> readPositions(final CompoundNBT compound)
    {
        return NBTUtils.streamCompound(compound.getList(NBT_HIVE_POS, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND))
                 .map(NBTUtil::readBlockPos)
                 .collect(Collectors.toSet());
    }
}
