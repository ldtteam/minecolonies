package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.Window;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Building class for the Barracks Tower.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class BuildingBarracksTower extends AbstractBuildingGuards
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String TAG_POS = "pos";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Our constants. The Schematic names, Defence bonus, and Offence bonus.
     */
    private static final String SCHEMATIC_NAME = "barrackstower";

    /**
     * Position of the barracks for this tower.
     */
    private BlockPos barracks = null;


    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingBarracksTower(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }

    @Override
    public void requestUpgrade(final Player player, final BlockPos builder)
    {
        final int buildingLevel = getBuildingLevel();
        final IBuilding building = getColony().getBuildingManager().getBuilding(barracks);
        if (building != null && buildingLevel < getMaxBuildingLevel() && buildingLevel < building.getBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1, builder, false);
        }
        else
        {
            player.sendMessage(new TranslatableComponent("com.minecolonies.coremod.worker.needbarracks"), player.getUUID());
        }
    }

    @Override
    public boolean canDeconstruct()
    {
        return false;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.barracksTower;
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        final boolean assignalResult = super.assignCitizen(citizen);
        if (citizen != null && assignalResult)
        {
            final IBuilding building = citizen.getHomeBuilding();
            if (building != null && !(building instanceof AbstractBuildingGuards))
            {
                building.removeCitizen(citizen);
            }
            citizen.setHomeBuilding(this);
            citizen.setWorkBuilding(this);
        }
        return assignalResult;
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        return 0;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        final IBuilding barrack = colony.getBuildingManager().getBuilding(barracks);
        if (barrack == null)
        {
            return;
        }
        ChunkDataHelper.claimColonyChunks(colony, true, barracks, barrack.getClaimRadius(newLevel));

        if (newLevel == barrack.getMaxBuildingLevel())
        {
            boolean allUpgraded = true;
            for (BlockPos tower : ((BuildingBarracks) barrack).getTowers())
            {
                Log.getLogger().info("Upgraded: " + allUpgraded);
                if (colony.getBuildingManager().getBuilding(tower).getBuildingLevel() != barrack.getMaxBuildingLevel())
                    allUpgraded = false;
            }

            if (allUpgraded)
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, AdvancementTriggers.ALL_TOWERS::trigger);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        barracks = NbtUtils.readBlockPos(compound.getCompound(TAG_POS));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        if (barracks != null)
        {
            compound.put(TAG_POS, NbtUtils.writeBlockPos(barracks));
        }

        return compound;
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Adds the position of the main barracks.
     *
     * @param pos the BlockPos.
     */
    public void addBarracks(final BlockPos pos)
    {
        barracks = pos;
    }

    /**
     * The client view for the bakery building.
     */
    public static class View extends AbstractBuildingGuards.View
    {
        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Check if it has enough workers.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return getWorkerId().size() >= getBuildingLevel();
        }

        /**
         * Creates a new window for the building
         * @return a blockui window
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, getSchematicName());
        }
    }
}
