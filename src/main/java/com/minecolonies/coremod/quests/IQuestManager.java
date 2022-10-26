package com.minecolonies.coremod.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.UUID;

/**
 * Manager of quest types, quest trigger and running instances
 */
public interface IQuestManager extends INBTSerializable<CompoundTag>
{
    boolean acceptQuest(final int questID, final Player player);

    List<IQuest> getQuestsForUUID(UUID userID);

    void onColonyTick();
}
