package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected AbstractTileEntityColonyBuilding tileEntity;

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
     * @param colony the colony.
     */
    public AbstractBuildingContainer(final BlockPos pos, final IColony colony)
    {
        super(pos, colony);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        final ListNBT containerTagList = compound.getList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.size(); ++i)
        {
            final CompoundNBT containerCompound = containerTagList.getCompound(i);
            containerList.add(NBTUtil.readBlockPos(containerCompound));
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
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT containerTagList = new ListNBT();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.add(NBTUtil.writeBlockPos(pos));
        }
        compound.put(TAG_CONTAINERS, containerTagList);
        compound.putInt(TAG_PRIO, this.pickUpPriority);
        compound.putBoolean(TAG_PRIO_MODE, this.priorityStatic);

        return compound;
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
        this.pickUpPriority = MathHelper.clamp(this.pickUpPriority + value, 1, MAX_PRIO);
    }

    /**
     * Sets the pickup priority to the given value.
     *
     * @param pickUpPriority The new pickup priority.
     */
    @Override
    public void setPickUpPriority(final int pickUpPriority)
    {
        this.pickUpPriority = MathHelper.clamp(pickUpPriority, 1, MAX_PRIO);
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
            tileEntity.markInvDirty();
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
        tileEntity.markInvDirty();
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
        if (block instanceof ContainerBlock || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    @Override
    public AbstractTileEntityColonyBuilding getTileEntity()
    {
        return tileEntity;
    }

    /**
     * Sets the tile entity for the building.
     *
     * @param te {@link AbstractTileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    @Override
    public void setTileEntity(final AbstractTileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getTileEntity() != null)
        {
            return tileEntity.getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
