package com.minecolonies.core.colony.buildings;

import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.core.blocks.BlockMinecoloniesRack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
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
public abstract class AbstractBuildingContainer extends AbstractSchematicProvider implements IBuildingContainer
{
    /**
     * A list which contains the position of all containers which belong to the worker building.
     */
    protected final Set<BlockPos> containerList = new HashSet<>();

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

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        final ListTag containerTagList = compound.getList(TAG_CONTAINERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.size(); ++i)
        {
            final CompoundTag containerCompound = containerTagList.getCompound(i);
            containerList.add(NbtUtils.readBlockPos(containerCompound));
        }
        if (compound.contains(TAG_PRIO))
        {
            this.unscaledPickUpPriority = compound.getInt(TAG_PRIO);
        }
        if (compound.contains(TAG_PRIO_STATE))
        {
            // This was the old int representation of Pickup:Never
            if (compound.getInt(TAG_PRIO_STATE) == 0)
            {
                this.unscaledPickUpPriority = 0;
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        @NotNull final ListTag containerTagList = new ListTag();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.add(NbtUtils.writeBlockPos(pos));
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
        this.unscaledPickUpPriority = Mth.clamp(this.unscaledPickUpPriority + value, 0, getMaxBuildingPriority(false));
    }

    @Override
    public void addContainerPosition(@NotNull final BlockPos pos)
    {
        containerList.add(pos);
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
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        registerBlockPosition(blockState.getBlock(), pos, world);
    }

    @Override
    @SuppressWarnings("squid:S1172")
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        if (block instanceof AbstractBlockHut)
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof TileEntityColonyBuilding buildingEntity)
            {
                buildingEntity.setStructurePack(StructurePacks.getStructurePack(getStructurePack()));
                buildingEntity.setMirror(isMirrored());
                final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                if (building != null)
                {
                    building.setStructurePack(getStructurePack());
                    building.setParent(getID());
                }
            }
        }
        else if (block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof TileEntityRack rackEntity)
            {
                rackEntity.setBuildingPos(this.getID());
            }
        }
    }

    /**
     * Gets the list of tags, and finds the first location registered there. 
     * @param tagName the name of the tag to query
     * @return the BlockPos, or null if not found
     */
    @Nullable
    protected BlockPos getFirstLocationFromTag(@NotNull final String tagName)
    {
        final List<BlockPos> locations = getLocationsFromTag(tagName);
        return locations.isEmpty() ? null : locations.get(0);
    }

    /**
     * Gets the list of tags, and finds all locations registered there.
     * @param tagName the name of the tag to query
     * @return all the matching BlockPos, or an empty list if not found
     */
    @NotNull
    protected List<BlockPos> getLocationsFromTag(@NotNull final String tagName)
    {
        if (tileEntity != null)
        {
            return new ArrayList<>(tileEntity.getWorldTagNamePosMap().getOrDefault(tagName, Collections.emptySet()));
        }
        return Collections.emptyList();
    }

    @Override
    public void setTileEntity(final AbstractTileEntityColonyBuilding te)
    {
        tileEntity = te;
        if (te != null && te.isOutdated())
        {
            safeUpdateTEDataFromSchematic();
        }
    }

    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap == ForgeCapabilities.ITEM_HANDLER && getTileEntity() != null)
        {
            return tileEntity.getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
