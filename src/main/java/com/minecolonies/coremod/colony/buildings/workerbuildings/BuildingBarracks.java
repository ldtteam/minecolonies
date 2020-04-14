package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.BlockBarracksTowerSubstitution;
import com.minecolonies.coremod.client.gui.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
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
    private static final String SCHEMATIC_NAME = "Barracks";

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
                world.setBlockState(tower, Blocks.AIR.getDefaultState());
            }
        }
        super.onDestroyed();
    }

    @Override
    public void registerBlockPosition(@NotNull final IBlockState block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == ModBlocks.blockBarracksTowerSubstitution || block.getBlock() == ModBlocks.blockHutBarracksTower)
        {
            if (world.getBlockState(pos).getBlock() != ModBlocks.blockHutBarracksTower)
            {
                world.setBlockState(pos, ModBlocks.blockHutBarracksTower.getDefaultState().withProperty(BlockBarracksTowerSubstitution.FACING, block.getValue(AbstractBlockHut.FACING)));
                final TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileEntityColonyBuilding)
                {
                    ((TileEntityColonyBuilding) tile).setStyle(this.getStyle());
                }
                getColony().getBuildingManager().addNewBuilding((TileEntityColonyBuilding) world.getTileEntity(pos), world);
            }
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
        if (colony.getWorld().isRemote)
        {
            return;
        }

        if (colony.getRaiderManager().isRaided())
        {
            if (!colony.getRaiderManager().areSpiesEnabled())
            {
                final int amount = InventoryUtils.getItemCountInItemHandler(this.getTileEntity().getInventory(), Items.GOLD_INGOT, 0);
                if (amount >= SPIES_GOLD_COST)
                {
                    InventoryUtils.removeStackFromItemHandler(tileEntity.getInventory(), new ItemStack(Items.GOLD_INGOT, SPIES_GOLD_COST));
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
        int sum = newLevel;
        for (final BlockPos pos : towers)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                sum += building.getBuildingLevel();
            }
        }
        return Math.max(1, sum / getMaxBuildingLevel());
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.barracks;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        towers.clear();
        towers.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_TOWERS, Constants.NBT.TAG_COMPOUND))
                        .map(resultCompound -> BlockPosUtil.readFromNBT(resultCompound, TAG_POS))
                        .collect(Collectors.toList()));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList towerTagList = towers.stream().map(pos -> BlockPosUtil.writeToNBT(new NBTTagCompound(), TAG_POS, pos)).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_TOWERS, towerTagList);

        return compound;
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
