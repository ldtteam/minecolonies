package com.minecolonies.coremod.quests;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.UUID;

/**
 * Manager of quest types, quest trigger and running instances
 */
public interface IQuestManager extends INBTSerializable<CompoundNBT>
{
    boolean acceptQuest(final int questID, final PlayerEntity player);

    List<IQuest> getQuestsForUUID(UUID userID);

    void onColonyTick();
}
