package com.minecolonies.api.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface of the Quest manager of each colony.
 */
public interface IQuestManager extends INBTSerializable<CompoundTag>
{
    /**
     * All quests that exist.
     */
   Map<ResourceLocation, IQuestTemplate> GLOBAL_SERVER_QUESTS = new HashMap<>();

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
    void completeQuest(ResourceLocation questId);

    /**
     * On each colony tick.
     */
    void onColonyTick();

    /**
     * Deactivate a given quest.
     * @param questID the id of the quest.
     */
    void deleteQuest(ResourceLocation questID);

    /**
     * Get the currently available or in progress quest with a given id.
     * @param questId the id.
     * @return the quest.
     */
    @Nullable
    IQuestInstance getAvailableOrInProgressQuest(final ResourceLocation questId);

    /**
     * On world load handling.
     */
    void onWorldLoad();

    /**
     * Unlock a quest with a given id.
     * @param questId the quest to unlock.
     */
    void unlockQuest(ResourceLocation questId);

    /**
     * Check if a quest is unlocked.
     * @param questId the id of the quest.
     * @return true if so.
     */
    boolean isUnlocked(ResourceLocation questId);

    /**
     * Alter the quest reputation.
     * @param difference the alteration.
     */
    void alterReputation(double difference);

    /**
     * Get the reputation of the colony.
     * @return the reputation.
     */
    double getReputation();

    /**
     * Get the list of available quests in the colony.
     *
     * @return the list of quest instances.
     */
    List<IQuestInstance> getAvailableQuests();

    /**
     * Get the list of in progress quests in the colony.
     *
     * @return the list of quest instances.
     */
    List<IQuestInstance> getInProgressQuests();

    /**
     * Get the list of completed quests in the colony.
     *
     * @return the list of quest templates, and how often they've been completed.
     */
    List<FinishedQuest> getFinishedQuests();

    /**
     * Inject an available quest manually.
     * @param questInstance the quest instance to inject.
     */
    void injectAvailableQuest(IQuestInstance questInstance);
}
