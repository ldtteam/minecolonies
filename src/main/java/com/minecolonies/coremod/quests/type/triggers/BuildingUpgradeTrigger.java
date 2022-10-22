package com.minecolonies.coremod.quests.type.triggers;

import com.google.common.eventbus.Subscribe;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.busevents.BuildingUpgradeEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.type.IQuestType;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class BuildingUpgradeTrigger implements IQuestTrigger
{
    /**
     * ID
     */
    public final static ResourceLocation ID = new ResourceLocation(MOD_ID, "buildingupgrade");

    private final IQuestType questType;

    public BuildingUpgradeTrigger(final IQuestType questType) {this.questType = questType;}

    @Override
    public ResourceLocation getID()
    {
        return null;
    }

    @Override
    public boolean shouldTrigger(final IColony colony)
    {
        return false;
    }

    @Override
    public void registerWith(final IColony colony)
    {
        colony.getColonyBus().register(this);
    }

    @Subscribe
    public void onBuildingUpgrade(final BuildingUpgradeEvent event)
    {
        Log.getLogger().warn("Building upgraded!: colony id:" + event.getColony().getID());
    }

    @Override
    public void unregister(final IColony colony)
    {
        colony.getColonyBus().unregister(this);
    }
}
