package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;

/**
 * Entity action to visit a given building.
 */
public class EntityAICitizenCheckWorkerBuilding extends EntityAICitizenWander
{
    private final AbstractBuildingWorker building;

    public EntityAICitizenCheckWorkerBuilding(final EntityCitizen citizen, final double speed, final AbstractBuildingWorker building, final double randomModifier)
    {
        super(citizen, speed, randomModifier);
        this.building = building;
    }
    
    @Override
    public void startExecuting()
    {
        super.citizen.getNavigator().tryMoveToBlockPos(building.getLocation(), super.speed);
    }
}