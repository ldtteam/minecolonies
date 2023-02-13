package com.minecolonies.coremod.colony.buildings.modules;

/**
 * The tavern living module for citizen to call their home.
 */
public class TavernLivingBuildingModule extends LivingBuildingModule
{
    @Override
    public int getModuleMax()
    {
        return building.getBuildingLevel() > 0 ? 4 : 0;
    }
}
