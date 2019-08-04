package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

/**
 * Entity action to visit a given building.
 */
public class EntityAICitizenCheckWorkerBuilding extends EntityAICitizenWander
{
    private final IBuildingWorker building;

    public EntityAICitizenCheckWorkerBuilding(final AbstractEntityCitizen citizen, final double speed, final IBuildingWorker building, final double randomModifier)
    {
        super(citizen, speed, randomModifier);
        this.building = building;
    }
    
    @Override
    public void startExecuting()
    {
        super.citizen.getNavigator().tryMoveToBlockPos(building.getPosition(), super.speed);
    }
}