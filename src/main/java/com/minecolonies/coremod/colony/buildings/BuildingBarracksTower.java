package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

/**
 * Building class of the BarracksTower.
 */
public class BuildingBarracksTower extends AbstractBuildingGuards
{
    /**
     * Name description of the guard hat.
     */
    private static final String GUARD_TOWER = "BarracksTower";

    /**
     * Offence bonus related to this building.
     */
    private static final int OFFENCE_BONUS = 5;

    /**
     * Tag to store the barracks pos to NBT.
     */
    private static final String TAG_POS = "pos";

    /**
     * Position of the barracks.
     */
    private BlockPos barracks = null;

    /**
     * Constructor for the BarracksTower building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBarracksTower(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Guard schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return GUARD_TOWER;
    }

    @Override
    public void requestUpgrade(final EntityPlayer player)
    {
        final int buildingLevel = getBuildingLevel();
        final AbstractBuilding building = getColony().getBuildingManager().getBuilding(barracks);
        if (building != null && buildingLevel < getMaxBuildingLevel() && buildingLevel < building.getBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1);
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.worker.needBarracks"));
        }
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public void setWorker(final CitizenData citizen)
    {
        super.setWorker(citizen);
        if (citizen != null)
        {
            final AbstractBuilding building = citizen.getHomeBuilding();
            if (building instanceof BuildingHome)
            {
                building.removeCitizen(citizen);
            }
            citizen.setHomeBuilding(this);
        }
    }

    @Override
    public int getOffenceBonus()
    {
        return OFFENCE_BONUS;
    }

    @Override
    public int getDefenceBonus()
    {
        return 0;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        barracks = NBTUtil.getPosFromTag(compound.getCompoundTag(TAG_POS));
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (barracks != null)
        {
            compound.setTag(TAG_POS, NBTUtil.createPosTag(barracks));
        }
    }

    @Override
    public boolean hasEnoughWorkers()
    {
        return getWorker().size() >= getBuildingLevel();
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
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final ColonyView c, final BlockPos l)
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


