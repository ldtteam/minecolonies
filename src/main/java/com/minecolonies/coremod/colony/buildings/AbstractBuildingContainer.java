package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractCitizenAssignable;
import com.minecolonies.coremod.inventory.api.CombinedItemHandler;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.MAX_PRIO;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CONTAINERS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PRIO;

/**
 * Class containing the container action of the buildings.
 */
public abstract class AbstractBuildingContainer extends AbstractCitizenAssignable implements ICapabilityProvider
{
    /**
     * A list which contains the position of all containers which belong to the
     * worker building.
     */
    protected final List<BlockPos> containerList = new ArrayList<>();

    /**
     * List of items the worker should keep.
     */
    protected final Map<Predicate<ItemStack>, Integer> keepX = new HashMap<>();

    /**
     * The tileEntity of the building.
     */
    private TileEntityColonyBuilding tileEntity;

    /**
     * Priority of the building in the pickUpList.
     */
    private int pickUpPriority = 1;

    /**
     * The constructor for the building container.
     * @param pos the position of it.
     */
    public AbstractBuildingContainer(final BlockPos pos, final Colony colony)
    {
        super(pos, colony);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        final NBTTagList containerTagList = compound.getTagList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.tagCount(); ++i)
        {
            final NBTTagCompound containerCompound = containerTagList.getCompoundTagAt(i);
            containerList.add(NBTUtil.getPosFromTag(containerCompound));
        }
        if (compound.hasKey(TAG_PRIO))
        {
            this.pickUpPriority = compound.getInteger(TAG_PRIO);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList containerTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag(TAG_CONTAINERS, containerTagList);
        compound.setInteger(TAG_PRIO, this.pickUpPriority);
    }

    /**
     * Get the pick up priority of the building.
     *
     * @return the priority, an integer.
     */
    public int getPickUpPriority()
    {
        return this.pickUpPriority;
    }

    /**
     * Increase or decrease the current pickup priority.
     *
     * @param value the new prio to add to.
     */
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
     * Add a new container to the building.
     *
     * @param pos position to add.
     */
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
    public void removeContainerPosition(final BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     *
     * @return a copy of the list to avoid currentModification exception.
     */
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
    public void registerBlockPosition(@NotNull final IBlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
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
     * @param te {@link TileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    public void setTileEntity(final TileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    public TileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid())
                && colony != null
                && colony.getWorld() != null
                && getLocation() != null
                && colony.getWorld().getBlockState(getLocation())
                != null && colony.getWorld().getBlockState(this.getLocation()).getBlock() instanceof AbstractBlockHut)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(getLocation());
            if (te instanceof TileEntityColonyBuilding)
            {
                tileEntity = (TileEntityColonyBuilding) te;
                if (tileEntity.getBuilding() == null)
                {
                    tileEntity.setColony(colony);
                    tileEntity.setBuilding(this);
                }
            }
        }

        return tileEntity;
    }

    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Override
    public boolean hasCapability(
            @Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null)
        {
            final Set<ICapabilityProvider> providers = new HashSet<>();

            //Add myself
            providers.add(getTileEntity());

            //Add additional containers
            providers.addAll(getAdditionalCountainers().stream()
                    .map(getTileEntity().getWorld()::getTileEntity)
                    .filter(entity -> (entity instanceof TileEntityChest) || (entity instanceof TileEntityRack))
                    .collect(Collectors.toSet()));
            providers.removeIf(Objects::isNull);

            //Map all providers to IItemHandlers.
            final Set<IItemHandlerModifiable> modifiables = providers
                    .stream()
                    .flatMap(provider -> InventoryUtils.getItemHandlersFromProvider(provider).stream())
                    .filter(handler -> handler instanceof IItemHandlerModifiable)
                    .map(handler -> (IItemHandlerModifiable) handler)
                    .collect(Collectors.toSet());

            return (T) new CombinedItemHandler(getSchematicName(), modifiables.toArray(new IItemHandlerModifiable[modifiables.size()]));
        }

        return null;
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
