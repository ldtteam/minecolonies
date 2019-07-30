    package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.blockout.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.tileentities.ITileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.MAX_PRIO;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class containing the container action of the buildings.
 */
public abstract class AbstractBuildingContainer extends AbstractCitizenAssignable implements IBuildingContainer
{
    /**
     * A list which contains the position of all containers which belong to the
     * worker building.
     */
    protected final List<BlockPos> containerList = new ArrayList<>();

    /**
     * List of items the worker should keep. With the quantity and if he should keep it in the inventory as well.
     */
    protected final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> keepX = new HashMap<>();

    /**
     * The tileEntity of the building.
     */
    private ITileEntityColonyBuilding tileEntity;

    /**
     * Priority of the building in the pickUpList.
     */
    private int pickUpPriority = 1;

    /**
     * Priority state of the building in the pickUpList.
     */
    private boolean priorityStatic = false;

    /**
     * The constructor for the building container.
     * @param pos the position of it.
     */
    public AbstractBuildingContainer(final BlockPos pos, final Colony colony)
    {
        super(pos, colony);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        final ListNBT containerTagList = compound.getList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.size(); ++i)
        {
            final CompoundNBT containerCompound = containerTagList.getCompound(i);
            containerList.add(NBTUtil.getPosFromTag(containerCompound));
        }
        if (compound.keySet().contains(TAG_PRIO))
        {
            this.pickUpPriority = compound.getInt(TAG_PRIO);
        }
        if (compound.keySet().contains(TAG_PRIO_MODE))
        {
            this.priorityStatic = compound.getBoolean(TAG_PRIO_MODE);
        }
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
        @NotNull final ListNBT containerTagList = new ListNBT();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.add(NBTUtil.createPosTag(pos));
        }
        compound.put(TAG_CONTAINERS, containerTagList);
        compound.putInt(TAG_PRIO, this.pickUpPriority);
        compound.putBoolean(TAG_PRIO_MODE, this.priorityStatic);
    }

    /**
     * Get the pick up priority of the building.
     *
     * @return the priority, an integer.
     */
    @Override
    public int getPickUpPriority()
    {
        return this.pickUpPriority;
    }

    /**
     * Increase or decrease the current pickup priority.
     *
     * @param value the new prio to add to.
     */
    @Override
    public void alterPickUpPriority(final int value)
    {
        if (this.pickUpPriority + value < 1)
        {
            this.pickUpPriority = 1;
        }
        else if (this.pickUpPriority + value > MAX_PRIO)
        {
            this.pickUpPriority = MAX_PRIO;
        }
        else
        {
            this.pickUpPriority += value;
        }
    }

    /**
     * Check if the priority is static and it shouldn't change.
     *
     * @return the priority state, a boolean.
     */
    @Override
    public boolean isPriorityStatic()
    {
        return this.priorityStatic;
    }

    /**
     * Change the current priority state.
     */
    @Override
    public void alterPriorityState()
    {
        this.priorityStatic = !this.priorityStatic;
    }

    /**
     * Add a new container to the building.
     *
     * @param pos position to add.
     */
    @Override
    public void addContainerPosition(@NotNull final BlockPos pos)
    {
        if (!containerList.contains(pos))
        {
            containerList.add(pos);
        }
    }

    /**
     * Remove a container from the building.
     *
     * @param pos position to remove.
     */
    @Override
    public void removeContainerPosition(final BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     *
     * @return a copy of the list to avoid currentModification exception.
     */
    @Override
    public List<BlockPos> getAdditionalCountainers()
    {
        return new ArrayList<>(containerList);
    }

    /**
     * Register a blockState and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param blockState to be registered
     * @param pos   of the blockState
     */
    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        registerBlockPosition(blockState.getBlock(), pos, world);
    }

    /**
     * Register a block and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param block to be registered
     * @param pos   of the block
     */
    @Override
    @SuppressWarnings("squid:S1172")
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block instanceof BlockContainer || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Try to transfer a stack to one of the inventories of the building.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link ItemStackUtils#EMPTY} if the stack was completely accepted
     */
    @Override
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (tileEntity == null || InventoryUtils.isProviderFull(tileEntity))
        {
            final Iterator<BlockPos> posIterator = containerList.iterator();
            @NotNull ItemStack resultStack = stack.copy();

            while (posIterator.hasNext() && !ItemStackUtils.isEmpty(resultStack))
            {
                final BlockPos pos = posIterator.next();
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    resultStack = InventoryUtils.addItemStackToProviderWithResult(tempTileEntity, stack);
                }
            }

            return resultStack;
        }
        else
        {
            return InventoryUtils.addItemStackToProviderWithResult(tileEntity, stack);
        }
    }

    /**
     * Sets the tile entity for the building.
     *
     * @param te {@link ITileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    @Override
    public void setTileEntity(final ITileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    @Override
    public ITileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid())
                && colony != null
                && colony.getWorld() != null
                && getPosition() != null
                && colony.getWorld().getBlockState(getPosition())
                != Blocks.AIR && colony.getWorld().getBlockState(this.getPosition()).getBlock() instanceof AbstractBlockHut)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(getPosition());
            if (te instanceof TileEntityColonyBuilding)
            {
                tileEntity = (TileEntityColonyBuilding) te;
                if (tileEntity.getBuilding() == null)
                {
                    tileEntity.setColony(colony);
                    tileEntity.setBuilding(this);
                }
            }
            else
            {
                Log.getLogger().error("Somehow the wrong TileEntity is at the location where the building should be!");
                Log.getLogger().error("Trying to restore order!");
                colony.getWorld().setTileEntity(getPosition(), new TileEntityColonyBuilding());
            }
        }

        return tileEntity;
    }

    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Override
    public boolean hasCapability(
            @Nonnull final Capability<?> capability, @Nullable final Direction facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final Direction facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null && getTileEntity() != null)
        {
           return tileEntity.getCapability(capability, facing);
        }
        return null;
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
