package com.minecolonies.api.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface of the Quest manager of each colony.
 */
public interface IQuestManager extends INBTSerializable<CompoundTag>
{
    /**
     * All quests that exist.
     */
   Map<ResourceLocation, IQuestData> GLOBAL_SERVER_QUESTS = new HashMap<>();

    /**
     * On each colony tick.
     */
    void onColonyTick();

    /**
     * Deactivate a given quest.
     * @param questID the id of the quest.
     */
    void deactivateQuest(ResourceLocation questID);

    /**
     * Get the currently available or in progress quest with a given id.
     * @param questId the id.
     * @return the quest.
     */
    IColonyQuest getAvailableOrInProgressQuest(final ResourceLocation questId);
}
