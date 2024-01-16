package com.minecolonies.core.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.moduleviews.LivingBuildingModuleView;
import com.minecolonies.core.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Living building view.
 */
public abstract class LivingBuildingView extends AbstractBuildingView
{
    /**
     * Creates an instance of the citizen hut window.
     *
     * @param c the colonyView.
     * @param l the position the hut is at.
     */
    public LivingBuildingView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter for the list of residents.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<Integer> getResidents()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getAssignedCitizens();
    }

    /**
     * Removes a resident from the building.
     *
     * @param index the index to remove it from.
     */
    public void removeResident(final int id)
    {
        getModuleViewByType(LivingBuildingModuleView.class).remove(id);
    }

    /**
     * Add a resident from the building.
     *
     * @param id the id of the citizen.
     */
    public void addResident(final int id)
    {
        getModuleViewByType(LivingBuildingModuleView.class).add(id);
    }

    /**
     * Get the max citizens.
     * @return the max.
     */
    public int getMax()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getMax();
    }

    /**
     * Get the  hiring mode.
     * @return the mode.
     */
    public HiringMode getHiringMode()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getHiringMode();
    }

    /**
     * Adjust the hiring mode.
     * @param value the value to set it to.
     */
    public void setHiringMode(final HiringMode value)
    {
        getModuleViewByType(LivingBuildingModuleView.class).setHiringMode(value);
        Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, value, getModuleViewByType(LivingBuildingModuleView.class).getProducer().getRuntimeID()));
    }
}
