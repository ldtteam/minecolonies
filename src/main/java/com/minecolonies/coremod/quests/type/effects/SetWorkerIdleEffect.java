package com.minecolonies.coremod.quests.type.effects;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IQuestGiver;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.quests.IQuest;
import net.minecraft.util.ResourceLocation;

/**
 * Quest effect which sets the worker idle
 */
public class SetWorkerIdleEffect implements IQuestEffect, ICitizenQuestEffect
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "workeridle");

    /**
     * Citizen data we applied to
     */
    private ICitizenData citizenData;

    /**
     * Quest this effect is in
     */
    private IQuest quest;

    /**
     * The amount of idle days to set
     */
    private int idleDays = 1;

    public SetWorkerIdleEffect(final IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public void onStart()
    {
        final IQuestGiver giver = quest.getQuestGiver();
        if (giver instanceof ICitizenData)
        {
            applyToCitizen((ICitizenData) giver);
        }
    }

    @Override
    public void onFinish()
    {

    }

    @Override
    public void onCancel()
    {
        citizenData.setIdleDays(0);
    }

    @Override
    public ICitizenData getCitizenData()
    {
        return citizenData;
    }

    @Override
    public void applyToCitizen(final ICitizenData data)
    {
        citizenData = data;
        data.setIdleDays(idleDays);
    }
}
