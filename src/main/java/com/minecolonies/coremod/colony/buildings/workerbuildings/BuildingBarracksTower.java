package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
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
    private static final String SCHEMATIC_NAME = "BarracksTower";
    private static final int    DEFENCE_BONUS  = 0;
    private static final int    OFFENCE_BONUS  = 5;

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
    public BuildingBarracksTower(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public int getDefenceBonus()
    {
        return DEFENCE_BONUS;
    }

    @Override
    public int getOffenceBonus()
    {
        return OFFENCE_BONUS;
    }

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
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final int buildingLevel = getBuildingLevel();
        final AbstractBuilding building = getColony().getBuildingManager().getBuilding(barracks);
        if (building != null && buildingLevel < getMaxBuildingLevel() && buildingLevel < building.getBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1, builder);
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.worker.needBarracks"));
        }
    }

    @Override
    public boolean assignCitizen(final CitizenData citizen)
    {
        final boolean assignalResult = super.assignCitizen(citizen);
        if (citizen != null && assignalResult)
        {
            final AbstractBuilding building = citizen.getHomeBuilding();
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
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        barracks = NBTUtil.getPosFromTag(compound.getCompound(TAG_POS));
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
        if (barracks != null)
        {
            compound.put(TAG_POS, NBTUtil.createPosTag(barracks));
        }
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
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingGuards.View
    {
        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
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
    }
}
