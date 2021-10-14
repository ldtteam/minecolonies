package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.client.gui.huts.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Building class for the Barracks.
 */
public class BuildingBarracks extends AbstractBuilding
{
    /**
     * Name of our building's Schematics.
     */
    private static final String SCHEMATIC_NAME = "barracks";

    /**
     * Max hut level of the Barracks.
     */
    private static final int BARRACKS_HUT_MAX_LEVEL = 5;

    /**
     * The tag to store the tower list to NBT.
     */
    private static final String TAG_TOWERS = "towers";

    /**
     * The list of barracksTowers.
     */
    private final List<BlockPos> towers = new ArrayList<>();

    /**
     * The goldcost for spies
     */
    public static int SPIES_GOLD_COST = 5;

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    public BuildingBarracks(@NotNull final IColony colony, final BlockPos pos)
    {
        super(colony, pos);
        keepX.put((stack) -> stack.getItem() == Items.GOLD_INGOT, new Tuple<>(STACKSIZE, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return BARRACKS_HUT_MAX_LEVEL;
    }

    @Override
    public void onDestroyed()
    {
        final World world = getColony().getWorld();

        if (world != null)
        {
            for (final BlockPos tower : towers)
            {
                world.setBlockAndUpdate(tower, Blocks.AIR.defaultBlockState());
            }
        }
        super.onDestroyed();
        colony.getBuildingManager().guardBuildingChangedAt(this, 0);
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        colony.getBuildingManager().guardBuildingChangedAt(this, newLevel);
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == ModBlocks.blockHutBarracksTower)
        {
            final IBuilding building = getColony().getBuildingManager().getBuilding(pos);
            if (building instanceof BuildingBarracksTower)
            {
                building.setStyle(this.getStyle());
                ((BuildingBarracksTower) building).addBarracks(getPosition());
                if (!towers.contains(pos))
                {
                    towers.add(pos);
                }
            }
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        if (colony.getWorld().isClientSide)
        {
            return;
        }

        if (colony.getRaiderManager().isRaided())
        {
            if (!colony.getRaiderManager().areSpiesEnabled())
            {
                if (InventoryUtils.tryRemoveStackFromItemHandler(this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null), new ItemStack(Items.GOLD_INGOT, SPIES_GOLD_COST)))
                {
                    colony.getRaiderManager().setSpiesEnabled(true);
                    colony.markDirty();
                }
            }
        }
        else
        {
            colony.getRaiderManager().setSpiesEnabled(false);
        }
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        if (newLevel <= 0)
        {
            return 0;
        }

        // tower levels must all be 4+ to get increased radius of 3 
        int barracksClaimRadius = 3;
        for (final BlockPos pos : towers)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                if (building.getBuildingLevel() < 4) 
                { 
                    barracksClaimRadius = 2;
                    break;
                }
            }
        }
        return barracksClaimRadius;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        towers.clear();
        towers.addAll(NBTUtils.streamCompound(compound.getList(TAG_TOWERS, Constants.NBT.TAG_COMPOUND))
                        .map(resultCompound -> BlockPosUtil.read(resultCompound, TAG_POS))
                        .collect(Collectors.toList()));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        final ListNBT towerTagList = towers.stream().map(pos -> BlockPosUtil.write(new CompoundNBT(), TAG_POS, pos)).collect(NBTUtils.toListNBT());
        compound.put(TAG_TOWERS, towerTagList);

        return compound;
    }

    public List<BlockPos> getTowers()
    {
        return towers;
    }

    /**
     * Barracks building View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the barracks view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowBarracksBuilding(this);
        }
    }
}
