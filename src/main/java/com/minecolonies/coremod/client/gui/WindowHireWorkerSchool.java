package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowHireWorkerSchool extends WindowHireWorker
{
    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowHireWorkerSchool(final IColonyView c, final BlockPos buildingId)
    {
        super(c, buildingId);
    }

    /**
     * Clears and resets/updates all citizens.
     */
    @Override
    protected void updateCitizens()
    {
        citizens.clear();

        //Removes all citizens which already have a job.
        citizens = colony.getCitizens().values().stream()
                     .filter(citizen -> building.getPosition().equals(citizen.getWorkBuilding()) || building.canAssign(citizen))
                     .filter(citizen -> (citizen.getWorkBuilding() == null && building.getWorkerId().size() < building.getMaxInhabitants())
                                          || building.getPosition().equals(citizen.getWorkBuilding())).sorted(Comparator.comparing(ICitizenDataView::getName))
                     .collect(Collectors.toList());
    }

    @Override
    protected Style createColor(final Skill primary, final Skill secondary, final Skill current)
    {
        if (current == Skill.Intelligence)
        {
            return Style.EMPTY.applyFormatting(TextFormatting.GREEN).applyFormatting(TextFormatting.BOLD);
        }
        return super.createColor(primary, secondary, current);
    }
}
