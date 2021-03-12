package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
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
import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getMaxBuildingPriority;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class containing the container action of the buildings.
 */
public abstract class AbstractBuildingContainer extends AbstractCitizenAssignable implements IBuildingContainer
{
    /**
     * A list which contains the position of all containers which belong to the worker building.
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
     * Priority of the building in the pickUpList. This is the unscaled value (mainly for a more intuitive GUI).
     */
    private int unscaledPickUpPriority = 1;

    /**
     * The constructor for the building container.
     *
     * @param pos    the position of it.
     * @param colony the colony.
     */
    public AbstractBuildingContainer(final BlockPos pos, final IColony colony)
    {
        super(pos, colony);
    }

    /**
     * Get a module matching a certain predicate.
     * @param clazz the class of the module.
     * @param modulePredicate the predicate to match.
     * @param <T> the optional type.
     * @return optional of the matching predicate (could be empty).
     */
    @NotNull
    public abstract <T extends IBuildingModule> Optional<T> getModuleMatching(Class<T> clazz, Predicate<? super T> modulePredicate);

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
            this.unscaledPickUpPriority = compound.getInt(TAG_PRIO);
        }
        if (compound.keySet().contains(TAG_PRIO_STATE))
        {
            // This was the old int representation of Pickup:Never
            if (compound.getInt(TAG_PRIO_STATE) == 0)
            {
                this.unscaledPickUpPriority = 0;
            }
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
        compound.putInt(TAG_PRIO, this.unscaledPickUpPriority);

        return compound;
    }

    @Override
    public int getPickUpPriority()
    {
        return this.unscaledPickUpPriority;
    }

    @Override
    public void alterPickUpPriority(final int value)
    {
        this.unscaledPickUpPriority = MathHelper.clamp(this.unscaledPickUpPriority + value, 0, getMaxBuildingPriority(false));
    }

    @Override
    public void addContainerPosition(@NotNull final BlockPos pos)
    {
        if (!containerList.contains(pos))
        {
            containerList.add(pos);
        }
    }

    @Override
    public void removeContainerPosition(final BlockPos pos)
    {
        containerList.remove(pos);
    }

    @Override
    public List<BlockPos> getContainers()
    {
        final List<BlockPos> list = new ArrayList<>(containerList);;
        list.add(this.getPosition());
        return list;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        registerBlockPosition(blockState.getBlock(), pos, world);
    }

    @Override
    @SuppressWarnings("squid:S1172")
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block instanceof AbstractBlockHut)
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityColonyBuilding)
            {
                ((TileEntityColonyBuilding) entity).setStyle(this.getStyle());
                final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                if (building != null)
                {
                    building.setStyle(this.getStyle());
                }
            }
        }
        else if (block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
            if (block instanceof BlockMinecoloniesRack)
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    ((TileEntityRack) entity).setBuildingPos(this.getID());
                }
            }
        }
    }

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
