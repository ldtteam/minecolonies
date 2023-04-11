package com.minecolonies.api.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

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
     * Have player attempt to accept a colony quest.
     * @param questID the unique id of the quest.
     * @param player the player trying to accept it.
     * @return true if successful.
     */
    boolean attemptAcceptQuest(ResourceLocation questID, Player player);

    /**
     * Conclude a given quest. This is called FROM the quest, to the colony.
     * @param questId the unique id of the quest.
     */
    void concludeQuest(ResourceLocation questId);

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
    @Nullable
    IColonyQuest getAvailableOrInProgressQuest(final ResourceLocation questId);

    /**
     * On world load handling.
     */
    void onWorldLoad();
}
