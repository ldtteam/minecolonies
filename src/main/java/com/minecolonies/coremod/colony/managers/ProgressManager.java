package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ColonyProgressType;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IProgressManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.modules.BuildingModules;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.ColonyProgressType.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_PROGRESSION_NAME;
import static com.minecolonies.coremod.colony.managers.RaidManager.MIN_REQUIRED_RAIDLEVEL;

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
     *
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
        else if (total == 10)
        {
            trigger(TEN_CITIZENS_SPAWNED);
        }
        else if (total == colony.getCitizenManager().maxCitizensFromResearch())
        {
            trigger(CITIZEN_CAP_MET);
        }
        else if (total >= 11 && employed >= 4)
        {
            trigger(NOT_ENOUGH_JOBS);
        }
    }

    @Override
    public void progressWorkOrderPlacement(final IWorkOrder workOrder)
    {
        if (workOrder.getWorkOrderType() == WorkOrderType.BUILD && workOrder.getStructurePath().contains("builder"))
        {
            trigger(BUILT_ENQUEUED);
        }
    }

    @Override
    public void progressBuildBuilding(final IBuilding building)
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
        else if (building instanceof BuildingWareHouse)
        {
            trigger(WAREHOUSE_BUILT);
        }
        else if (building.hasModule(BuildingModules.TAVERN_VISITOR))
        {
            trigger(TAVERN_BUILT);
        }

        if (building.hasModule(LivingBuildingModule.class))
        {
            int housingSpots = building.getFirstModuleOccurance(LivingBuildingModule.class).getModuleMax();
            for(final IBuilding colonyBuilding:colony.getBuildingManager().getBuildings().values())
            {
                if (housingSpots >= 4)
                {
                    break;
                }

                if (colonyBuilding.hasModule(LivingBuildingModule.class))
                {
                    housingSpots += colonyBuilding.getFirstModuleOccurance(LivingBuildingModule.class).getModuleMax();
                }
            }
            trigger(ALL_CITIZENS_HOMED);
        }

        if (building.getColony().getRaiderManager().getColonyRaidLevel() >= MIN_REQUIRED_RAIDLEVEL)
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
            MessageUtils.format(PARTIAL_PROGRESSION_NAME + type.name().toLowerCase(Locale.US))
              .withPriority(MessagePriority.IMPORTANT)
              .sendTo(colony)
              .forAllPlayers();
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
    public void read(@NotNull final CompoundTag compound)
    {
        notifiedProgress.clear();
        final CompoundTag progressCompound = compound.getCompound(TAG_PROGRESS_MANAGER);
        final ListTag progressTags = progressCompound.getList(TAG_PROGRESS_LIST, Tag.TAG_COMPOUND);
        notifiedProgress.addAll(NBTUtils.streamCompound(progressTags)
                                  .map(progressTypeCompound -> values()[progressTypeCompound.getInt(TAG_PROGRESS_TYPE)])
                                  .collect(Collectors.toList()));
        printProgress = progressCompound.getBoolean(TAG_PRINT_PROGRESS);
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        final CompoundTag progressCompound = new CompoundTag();
        @NotNull final ListTag progressTagList = notifiedProgress.stream()
                                                   .map(this::writeProgressTypeToNBT)
                                                   .collect(NBTUtils.toListNBT());

        progressCompound.put(TAG_PROGRESS_LIST, progressTagList);
        progressCompound.putBoolean(TAG_PRINT_PROGRESS, printProgress);
        compound.put(TAG_PROGRESS_MANAGER, progressCompound);
    }

    /**
     * Writes a single colony progress type to NBT.
     *
     * @param type the type.
     * @return the NBT representation.
     */
    private CompoundTag writeProgressTypeToNBT(final ColonyProgressType type)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_PROGRESS_TYPE, type.ordinal());
        return compound;
    }
}

