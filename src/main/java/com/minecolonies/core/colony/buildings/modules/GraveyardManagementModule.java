package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenBuriedModEvent;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.tileentities.TileEntityGrave;
import com.minecolonies.core.tileentities.TileEntityNamedGrave;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.TAG_STRING;

/**
 * The graveyard list module.
 */
public class GraveyardManagementModule extends AbstractBuildingModule implements IBuildingModule, IPersistentModule, IBuildingEventsModule
{
    /**
     * The tag to store the list of resting citizen in this graveyard
     */
    private static final String TAG_RIP_CITIZEN_LIST = "ripCitizenList";

    /**
     * NBTTag to store grave data about the most recent grave.
     */
    private static final String TAG_GRAVE_DATA = "gravedata";

    /**
     * NBT tag for the list of visible graves.
     */
    private static final String TAG_VISUAL_GRAVES = "visualgraves";

    /**
     * NBT tag for the position of a given visible grave.
     */
    private static final String TAG_VISUAL_GRAVES_BLOCKPOS = "visualgravesblockpos";

    /**
     * NBT tag for the facing of a given visible grave.
     */
    private static final String TAG_VISUAL_GRAVES_FACING = "visualgravesfacing";

    /**
     * The list of resting citizen in this graveyard.
     */
    private final List<String> restingCitizen = new ArrayList<>();

    /**
     * Indicates whether the grave positions were loaded from the module data (as opposed to the legacy building data).
     */
    private boolean gravePositionsLoadedFromModuleNbt;

    /**
     * The set of registered grave plot positions and their facing.
     */
    private final Set<Tuple<BlockPos, Direction>> gravePositions = new HashSet<>();

    /**
     * The data of the last grave dug by the undertaker.
     */
    @Nullable
    private GraveData lastGraveData;

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        restingCitizen.clear();
        if (compound.contains(TAG_RIP_CITIZEN_LIST))
        {
            final ListTag ripCitizen = compound.getList(TAG_RIP_CITIZEN_LIST, TAG_STRING);
            for (int i = 0; i < ripCitizen.size(); i++)
            {
                final String citizenName = ripCitizen.getString(i);
                restingCitizen.add(citizenName);
            }
        }

        if (compound.contains(TAG_GRAVE_DATA))
        {
            lastGraveData = new GraveData();
            lastGraveData.read(compound.getCompound(TAG_GRAVE_DATA));
        }
        else lastGraveData = null;

        gravePositions.clear();
        gravePositionsLoadedFromModuleNbt = compound.contains(TAG_VISUAL_GRAVES, Tag.TAG_LIST);
        if (gravePositionsLoadedFromModuleNbt)
        {
            readGravePositions(compound);
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag ripCitizen = new ListTag();
        for (@NotNull final String citizenName : restingCitizen)
        {
            ripCitizen.add(StringTag.valueOf(citizenName));
        }
        compound.put(TAG_RIP_CITIZEN_LIST, ripCitizen);

        if(lastGraveData != null)
        {
            compound.put(TAG_GRAVE_DATA, lastGraveData.write());
        }

        writeGravePositions(compound);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        final IColony colony = building.getColony();
        final List<BlockPos> graves = new ArrayList<>(colony.getGraveManager().getGraves().keySet());
        final List<BlockPos> cleanList = new ArrayList<>();

        for (@NotNull final BlockPos grave : graves)
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), grave))
            {
                final BlockEntity tileEntity = colony.getWorld().getBlockEntity(grave);
                if (tileEntity instanceof TileEntityGrave)
                {
                    cleanList.add(grave);
                }
            }
        }

        // grave list
        buf.writeInt(cleanList.size());
        for (@NotNull final BlockPos grave : cleanList)
        {
            buf.writeBlockPos(grave);
        }

        //resting citizen list
        buf.writeInt(restingCitizen.size());
        for (@NotNull final String citizenName : restingCitizen)
        {
            buf.writeUtf(citizenName);
        }

        buf.writeInt(gravePositions.size());
        for (final Tuple<BlockPos, Direction> gravePlotEntry : gravePositions)
        {
            buf.writeBlockPos(gravePlotEntry.getA());
        }
    }

    /**
     * Migrates burial plot positions stored on the graveyard building before ownership moved to this module.
     * Module data always takes precedence when both formats are present.
     *
     * @param compound the legacy building data.
     */
    public void migrateLegacyGravePositions(final CompoundTag compound)
    {
        if (!gravePositionsLoadedFromModuleNbt && compound.contains(TAG_VISUAL_GRAVES, Tag.TAG_LIST))
        {
            readGravePositions(compound);
            markDirty();
        }
    }

    /**
     * Read the burial plot positions and their directions from NBT.
     *
     * @param compound the NBT data containing the burial plot positions.
     */
    private void readGravePositions(final CompoundTag compound)
    {
        gravePositions.clear();
        final ListTag visualGraveTagList = compound.getList(TAG_VISUAL_GRAVES, Tag.TAG_COMPOUND);
        for (int i = 0; i < visualGraveTagList.size(); ++i)
        {
            final CompoundTag graveCompound = visualGraveTagList.getCompound(i);
            final Direction facing = Direction.byName(graveCompound.getString(TAG_VISUAL_GRAVES_FACING));
            if (facing != null)
            {
                gravePositions.add(new Tuple<>(BlockPosUtil.read(graveCompound, TAG_VISUAL_GRAVES_BLOCKPOS), facing));
            }
        }
    }

    /**
     * Write the burial plot positions and their directions to NBT.
     *
     * @param compound the NBT data to write the burial plot positions to.
     */
    private void writeGravePositions(final CompoundTag compound)
    {
        final ListTag visualGraveTagList = new ListTag();
        for (final Tuple<BlockPos, Direction> gravePosition : gravePositions)
        {
            final CompoundTag graveCompound = new CompoundTag();
            BlockPosUtil.write(graveCompound, TAG_VISUAL_GRAVES_BLOCKPOS, gravePosition.getA());
            graveCompound.putString(TAG_VISUAL_GRAVES_FACING, gravePosition.getB().getName());
            visualGraveTagList.add(graveCompound);
        }
        compound.put(TAG_VISUAL_GRAVES, visualGraveTagList);
    }

    /**
     * Get the burial plot positions and their directions.
     *
     * @return the burial plot positions.
     */
    public Set<Tuple<BlockPos, Direction>> getGravePositions()
    {
        return gravePositions;
    }

    /**
     * Register a named grave block as a burial plot.
     *
     * @param state the block state.
     * @param pos the block position.
     */
    public void registerGravePosition(final BlockState state, final BlockPos pos)
    {
        if (state.getBlock() == ModBlocks.blockNamedGrave
              && gravePositions.add(new Tuple<>(pos, state.getValue(AbstractBlockMinecoloniesNamedGrave.FACING))))
        {
            markDirty();
        }
    }

    /**
     * Return a random free visual grave position.
     *
     * @return a free burial plot, or {@code null} when none are available.
     */
    @Nullable
    public Tuple<BlockPos, Direction> getRandomFreeVisualGravePos()
    {
        final List<Tuple<BlockPos, Direction>> availablePositions = new ArrayList<>();
        for (final Tuple<BlockPos, Direction> gravePosition : gravePositions)
        {
            if (building.getColony().getWorld().getBlockState(gravePosition.getA()).canBeReplaced())
            {
                availablePositions.add(gravePosition);
            }
        }

        if (availablePositions.isEmpty())
        {
            return null;
        }

        Collections.shuffle(availablePositions);
        return availablePositions.get(0);
    }

    /**
     * Setter for the last grave data.
     * @param graveData the last grave the worker has dug.
     */
    public void setLastGraveData(final GraveData graveData)
    {
        this.lastGraveData = graveData;
        markDirty();
    }

    /**
     * Get for the last grave.
     * @return the last grave the worker has dug.
     */
    public GraveData getLastGraveData()
    {
        return this.lastGraveData;
    }

    /**
     * Check if one of the citizens in the list is resting.
     * @param citizens the citizens to check.
     * @return true if so.
     */
    public boolean hasRestingCitizen(final Set<String> citizens)
    {
        for (final String citizen : citizens)
        {
            if (restingCitizen.contains(citizen))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a citizen to the list of resting citizens in this graveyard.
     *
     * @param positionAndDirection the burial plot position and grave direction.
     * @param worker the undertaker performing the burial.
     * @return {@code true} if the named grave was placed and the burial was recorded; otherwise {@code false}.
     */
    public boolean buryCitizenHere(final Tuple<BlockPos, Direction> positionAndDirection, final AbstractEntityCitizen worker)
    {
        if(lastGraveData != null && !restingCitizen.contains(lastGraveData.getCitizenName()))
        {
            final IColony colony = building.getColony();
            Direction facing = positionAndDirection.getB();
            if(facing == Direction.UP || facing == Direction.DOWN)
            {
                facing = Direction.NORTH; //prevent setting an invalid HorizontalDirection
            }

            colony.getWorld().destroyBlock(positionAndDirection.getA(), true, worker);
            if (!colony.getWorld().setBlockAndUpdate(positionAndDirection.getA(),
              ModBlocks.blockNamedGrave.defaultBlockState().setValue(AbstractBlockMinecoloniesNamedGrave.FACING, facing)))
            {
                return false;
            }

            BlockEntity tileEntity = colony.getWorld().getBlockEntity(positionAndDirection.getA());
            if (tileEntity instanceof TileEntityNamedGrave)
            {
                final String firstName = StringUtils.split(lastGraveData.getCitizenName())[0];
                final String lastName = lastGraveData.getCitizenName().replaceFirst(firstName,"");

                final ArrayList<String> lines = new ArrayList<>();
                lines.add(firstName);
                lines.add(lastName);
                if (lastGraveData.getCitizenJobName() != null)
                {
                    lines.add(lastGraveData.getCitizenJobName());
                }
                ((TileEntityNamedGrave) tileEntity).setTextLines(lines);
            }

            restingCitizen.add(lastGraveData.getCitizenName());
            markDirty();

            final CompoundTag savedCitizenNbt = lastGraveData.getCitizenDataNBT();
            if (savedCitizenNbt != null)
            {
                IMinecoloniesAPI.getInstance().getEventBus().post(new CitizenBuriedModEvent(
                  colony,
                  positionAndDirection.getA(),
                  savedCitizenNbt,
                  lastGraveData.getCitizenName(),
                  lastGraveData.getCitizenJobName(),
                  worker.getCitizenData()));
            }
            return true;
        }
        return false;
    }
}
