package com.minecolonies.coremod.quests;

import com.google.common.eventbus.EventBus;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IQuestGiver;
import com.minecolonies.api.colony.busevents.IColonyStateEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.quests.type.IQuestType;
import com.minecolonies.coremod.quests.type.QuestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class QuestManager implements IQuestManager
{
    /**
     * All accepted quests
     */
    private static final Map<UUID, List<IQuest>> globalRunningQuests = new HashMap<>();

    /**
     * All available quest types
     */
    private static final Map<ResourceLocation, IQuestType> questTypes = new HashMap<>();

    /**
     * Quests of this colony
     */
    private final List<IQuest> colonyQuests = new ArrayList<>();

    /**
     * This manager's colony
     */
    private final IColony colony;

    /**
     * Quest id giver
     */
    private static int nextQuestID = 1;

    public QuestManager(final IColony colony, final EventBus colonybus)
    {
        colonybus.register(this);
        this.colony = colony;
    }

    @SubscribeEvent
    public void onColonyStateChange(final IColonyStateEvent event)
    {
        if (event.isColonyActive())
        {

        }
        else
        {

        }
    }

    /**
     * Registers a new quest type
     *
     * @param type type to add
     */
    public static void addAvailableQuestType(final IQuestType type)
    {
        if (questTypes.containsKey(type.getID()))
        {
            Log.getLogger().error("Duplicated quest type id for " + type.getID());
        }

        questTypes.put(type.getID(), type);
    }

    /**
     * Registers a new quest type
     *
     * @param type type to add
     */
    public static void removeAvailableQuestType(final IQuestType type)
    {
        if (questTypes.containsKey(type.getID()))
        {
            Log.getLogger().error("No quest type id for " + type.getID());
            return;
        }

        questTypes.remove(type.getID());
    }

    /**
     * Get a quest type by id
     *
     * @param id id to check
     * @return quest type
     */
    public static IQuestType getTypeByID(final ResourceLocation id)
    {
        return questTypes.get(id);
    }

    @Override
    public boolean acceptQuest(final int questID, final Player player)
    {
        final IQuest quest = colonyQuests.get(questID);
        if (quest == null)
        {
            return false;
        }

        quest.onStart(player);

        return true;
    }

    @Override
    public List<IQuest> getQuestsForUUID(final UUID userID)
    {
        return globalRunningQuests.get(userID);
    }

    @Override
    public void onColonyTick()
    {
        if (colonyQuests.isEmpty())
        {
            for (final ICitizenData citizenData : colony.getCitizenManager().getCitizens())
            {
                if (citizenData.getJob() instanceof JobLumberjack)
                {
                    createNewQuest(null, citizenData);
                    break;
                }
            }
        }
    }

    /**
     * Creates a new quest for the given type
     *
     * @param typeID
     * @param questGiver
     * @return
     */
    public IQuest createNewQuest(final ResourceLocation typeID, final IQuestGiver questGiver)
    {
        final IQuest quest = new Quest(nextQuestID++, new QuestType(null), colony, questGiver);
        colonyQuests.add(quest);
        return quest;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {

    }
}
