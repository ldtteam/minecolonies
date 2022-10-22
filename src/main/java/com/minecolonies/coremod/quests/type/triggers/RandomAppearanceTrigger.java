package com.minecolonies.coremod.quests.type.triggers;

import com.google.common.eventbus.Subscribe;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.busevents.ColonyTickEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.quests.type.IQuestType;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class RandomAppearanceTrigger implements IQuestTrigger
{
    /**
     * ID
     */
    public final static ResourceLocation ID = new ResourceLocation(MOD_ID, "random");

    private final IQuestType questType;

    public RandomAppearanceTrigger(final IQuestType questType)
    {
        this.questType = questType;
    }

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public boolean shouldTrigger(final IColony colony)
    {
        return new Random().nextInt(100) == 0;
    }

    @Subscribe
    public void onColonyTick(final ColonyTickEvent event)
    {
        Log.getLogger().warn("Radnom appeareance trigger tick! " + questType);
    }

    @Override
    public void registerWith(final IColony colony)
    {

    }

    @Override
    public void unregister(final IColony colony)
    {

    }
}
