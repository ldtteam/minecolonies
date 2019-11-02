package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ColonyProgressType;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IProgressManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.ColonyProgressType.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The Progress manager which tracks the colony progress to send help messages to the player.
 */
public class ProgressManager implements IProgressManager
{
    /**
     * The progress events which have been broadcasted to the colony already.
     */
    private final List<ColonyProgressType> notifiedProgress = new ArrayList<>();

    /**
     * The connected colony.
     */
    private final Colony colony;

    /**
     * If progress should be printed.
     */
    private boolean printProgress = true;

    /**
     * Creates the progress for a colony.
     * @param colony the colony.
     */
    public ProgressManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void progressBuildingPlacement(final Block block)
    {
        if (block == ModBlocks.blockHutTownHall)
        {
            trigger(COLONY_FOUNDED);
        }
        else if (block == ModBlocks.blockHutBuilder)
        {
            trigger(BUILDER_PLACED);
        }
    }

    @Override
    public void progressCitizenSpawn(final int total, final int employed)
    {
        if (total == 1)
        {
            trigger(FIRST_CITIZEN_SPAWNED);
        }
        else if (total == 4)
        {
            trigger(FOUR_CITIZENS_SPAWNED);
        }
        else if (total == 5)
        {
            trigger(FIVE_CITIZENS_SPAWNED);
        }
        else if (total == 6)
        {
            trigger(SIX_CITIZENS_SPAWNED);
        }
        else if (total == 7)
        {
            trigger(SEVEN_CITIZENS_SPAWNED);
        }
        else if (total == 8)
        {
            trigger(EIGHT_CITIZENS_SPAWNED);
        }
        else if (total == 9)
        {
            trigger(NINE_CITIZENS_SPAWNED);
        }
        else if (total >= 10 && employed >= 4)
        {
            trigger(NOT_ENOUGH_JOBS);
        }
    }

    @Override
    public void progressWorkOrderPlacement(final IWorkOrder workOrder)
    {
        if (workOrder instanceof WorkOrderBuildBuilding && ((WorkOrderBuildBuilding) workOrder).getStructureName().contains("Builder"))
        {
            trigger(BUILT_ENQUEUED);
        }
    }

    @Override
    public void progressBuildBuilding(final IBuilding building, final int totalLevels, final int totalHousing)
    {
        if (building instanceof BuildingBuilder)
        {
            trigger(BUILDER_BUILT);
        }
        else if (building instanceof BuildingMiner || building instanceof BuildingLumberjack)
        {
            trigger(RESOURCE_PROD_BUILT);
        }
        else if (building instanceof BuildingFisherman || building instanceof BuildingFarmer)
        {
            trigger(FOOD_PROD_BUILT);
        }
        else if (totalHousing == 4 && (building instanceof BuildingTownHall || building instanceof BuildingHome))
        {
            trigger(ALL_CITIZENS_HOMED);
        }
        else if (building instanceof BuildingWareHouse)
        {
            trigger(WAREHOUSE_BUILT);
        }
        else if (totalLevels == 20)
        {
            trigger(TWENTY_BUILDING_LEVELS);
        }
    }

    @Override
    public void progressEmploy(final int employed)
    {
        if (employed == 4)
        {
            trigger(FOUR_CITIZEN_EMPLOYED);
        }
    }

    @Override
    public void progressEmploymentModeChange()
    {
        trigger(MANUAL_EMPLOYMENT_ON);
    }

    @Override
    public void trigger(final ColonyProgressType type)
    {
        if (!printProgress)
        {
            return;
        }

        if (!notifiedProgress.contains(type))
        {
            notifiedProgress.add(type);
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.progress." + type.name().toLowerCase(Locale.US));
            colony.markDirty();
        }
    }

    @Override
    public void togglePrintProgress()
    {
        printProgress = !printProgress;
        colony.markDirty();
    }

    @Override
    public boolean isPrintingProgress()
    {
        return printProgress;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        notifiedProgress.clear();
        final NBTTagCompound progressCompound = compound.getCompoundTag(TAG_PROGRESS_MANAGER);
        final NBTTagList progressTags = progressCompound.getTagList(TAG_PROGRESS_LIST, Constants.NBT.TAG_COMPOUND);
        notifiedProgress.addAll(NBTUtils.streamCompound(progressTags)
                         .map(progressTypeCompound -> values()[progressTypeCompound.getInteger(TAG_PROGRESS_TYPE)])
                         .collect(Collectors.toList()));
        printProgress = progressCompound.getBoolean(TAG_PRINT_PROGRESS);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagCompound progressCompound = new NBTTagCompound();
        @NotNull final NBTTagList progressTagList = notifiedProgress.stream()
                                                     .map(this::writeProgressTypeToNBT)
                                                     .collect(NBTUtils.toNBTTagList());

        progressCompound.setTag(TAG_PROGRESS_LIST, progressTagList);
        progressCompound.setBoolean(TAG_PRINT_PROGRESS, printProgress);
        compound.setTag(TAG_PROGRESS_MANAGER, progressCompound);
    }

    /**
     * Writes a single colony progress type to NBT.
     * @param type the type.
     * @return the NBT representation.
     */
    private NBTTagCompound writeProgressTypeToNBT(final ColonyProgressType type)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_PROGRESS_TYPE, type.ordinal());
        return compound;
    }
}

