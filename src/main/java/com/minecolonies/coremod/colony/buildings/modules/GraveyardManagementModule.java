package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
     * NBTTag to store grave data.
     */
    private static final String TAG_GRAVE_DATA = "gravedata";

    /**
     * The list of resting citizen in this graveyard.
     */
    private final List<String> restingCitizen = new ArrayList<>();

    /**
     * The data of the last grave dug by the undertaker.
     */
    @Nullable
    private GraveData lastGraveData;

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        restingCitizen.clear();
        if (compound.getAllKeys().contains(TAG_RIP_CITIZEN_LIST))
        {
            final ListNBT ripCitizen = compound.getList(TAG_RIP_CITIZEN_LIST, TAG_STRING);
            for (int i = 0; i < ripCitizen.size(); i++)
            {
                final String citizenName = ripCitizen.getString(i);
                restingCitizen.add(citizenName);
            }
        }

        if (compound.getAllKeys().contains(TAG_GRAVE_DATA))
        {
            lastGraveData = new GraveData();
            lastGraveData.read(compound.getCompound(TAG_GRAVE_DATA));
        }
        else lastGraveData = null;
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT ripCitizen = new ListNBT();
        for (@NotNull final String citizenName : restingCitizen)
        {
            ripCitizen.add(StringNBT.valueOf(citizenName));
        }
        compound.put(TAG_RIP_CITIZEN_LIST, ripCitizen);

        if(lastGraveData != null)
        {
            compound.put(TAG_GRAVE_DATA, lastGraveData.write());
        }
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        final IColony colony = building.getColony();
        final List<BlockPos> graves = new ArrayList<>(colony.getGraveManager().getGraves().keySet());
        final List<BlockPos> cleanList = new ArrayList<>();

        for (@NotNull final BlockPos grave : graves)
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), grave))
            {
                final TileEntity tileEntity = colony.getWorld().getBlockEntity(grave);
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
     * Add a citizen to the list of resting citizen in this graveyard
     */
    public void buryCitizenHere(final Tuple<BlockPos, Direction> positionAndDirection)
    {
        if(lastGraveData != null && !restingCitizen.contains(lastGraveData.getCitizenName()))
        {
            final IColony colony = building.getColony();
            Direction facing = positionAndDirection.getB();
            if(facing == Direction.UP || facing == Direction.DOWN)
            {
                facing = Direction.NORTH; //prevent setting an invalid HorizontalDirection
            }
            colony.getWorld().setBlockAndUpdate(positionAndDirection.getA(), ModBlocks.blockNamedGrave.defaultBlockState().setValue(AbstractBlockMinecoloniesNamedGrave.FACING, facing));

            TileEntity tileEntity = colony.getWorld().getBlockEntity(positionAndDirection.getA());
            if (tileEntity instanceof TileEntityNamedGrave)
            {
                final String firstName = StringUtils.split(lastGraveData.getCitizenName())[0];
                final String lastName = lastGraveData.getCitizenName().replaceFirst(firstName,"");

                final ArrayList<String> lines = new ArrayList<>();
                lines.add(firstName);
                lines.add(lastName);
                if (lastGraveData.getCitizenJobName() != null) { lines.add(lastGraveData.getCitizenJobName()); }
                ((TileEntityNamedGrave) tileEntity).setTextLines(lines);
            }

            restingCitizen.add(lastGraveData.getCitizenName());
            markDirty();
        }
    }
}
