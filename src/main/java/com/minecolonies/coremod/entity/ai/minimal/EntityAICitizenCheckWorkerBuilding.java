package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.IEntityCitizen;

/**
 * Entity action to visit a given building.
 */
public class EntityAICitizenCheckWorkerBuilding extends EntityAICitizenWander
{
    private final IBuildingWorker building;

    public EntityAICitizenCheckWorkerBuilding(final IEntityCitizen citizen, final double speed, final IBuildingWorker building, final double randomModifier)
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