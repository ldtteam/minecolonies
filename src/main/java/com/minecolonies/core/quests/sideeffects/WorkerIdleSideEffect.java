package com.minecolonies.core.quests.sideeffects;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Quest effect which sets the worker idle
 */
public class WorkerIdleSideEffect implements IQuestSideEffect, ICitizenQuestSideEffect
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "workeridle");

    /**
     * Citizen data we applied to
     */
    private ICitizenData citizenData;

    /**
     * Quest this effect is in
     */
    private final IQuestInstance quest;

    public WorkerIdleSideEffect(final IQuestInstance quest)
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
        //applyToCitizen(quest.getQuestGiver());
    }

    @Override
    public void onFinish()
    {
        citizenData.setIdleDays(0);
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
        /**
         * The amount of idle days to set
         */
        int idleDays = 1;
        data.setIdleDays(idleDays);
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider)
    {
        return null;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag nbt)
    {

    }
}
