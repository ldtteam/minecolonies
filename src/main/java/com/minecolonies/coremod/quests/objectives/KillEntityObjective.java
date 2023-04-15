package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IObjectiveData;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.event.QuestObjectiveEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;
import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Objective type entity killing mining.
 */
public class KillEntityObjective extends DialogueObjective implements IKillEntityObjective
{
    /**
     * Amount of entities to kill.
     */
    private final int entitiesToKill;

    /**
     * The block to mine.
     */
    private final EntityType<?> entityToKill;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Create a new objective of this type.
     * @param target the target citizen.
     * @param entitiesToKill the number of entities to kill.
     * @param entityToKill the entity to kill.
     */
    public KillEntityObjective(final int target, final int entitiesToKill, final EntityType<?> entityToKill, final int nextObjective, final List<Integer> rewards)
    {
        super(target, new DialogueElement("I am still waiting for you to kill %d " + entityToKill.getDescription().getString() + " !",
          List.of(new AnswerElement("Sorry, be right back!", new IQuestDialogueAnswer.CloseUIDialogueAnswer()), new AnswerElement("I don't have time for this!", new IQuestDialogueAnswer.QuestCancellationDialogueAnswer()))), rewards);
        this.entitiesToKill = entitiesToKill;
        this.nextObjective = nextObjective;
        this.entityToKill = entityToKill;
    }

    /**
     * Parse the mine block objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjective createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target = details.get(TARGET_KEY).getAsInt();
        final int quantity = details.get(QUANTITY_KEY).getAsInt();
        final EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getHolder(new ResourceLocation(details.get(ENTITY_TYPE_KEY).getAsString())).get().get();
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new KillEntityObjective(target, quantity, entityType, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveData startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return new EntityKillProgressData();
    }

    @Nullable
    @Override
    public IObjectiveData getObjectiveData()
    {
        return new EntityKillProgressData();
    }

    @Override
    public void onCancellation(final IQuestInstance colonyQuest)
    {
        cleanupListener(colonyQuest);
    }

    /**
     * Cleanup the listener of this event.
     * @param colonyQuest the quest instance it belongs to.
     */
    private void cleanupListener(final IQuestInstance colonyQuest)
    {
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.removeQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    @Override
    public void onEntityKill(final IObjectiveData killProgressData, final IQuestInstance colonyQuest, final Player player)
    {
        if (killProgressData.isFulfilled())
        {
            return;
        }

        ((EntityKillProgressData) killProgressData).currentProgress++;
        if (killProgressData.isFulfilled())
        {
            colonyQuest.advanceObjective(player, nextObjective);
        }
    }

    @Override
    public void onWorldLoad(final IQuestInstance colonyQuest)
    {
        super.onWorldLoad(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    public class EntityKillProgressData implements IObjectiveData
    {
        private int currentProgress = 0;

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= entitiesToKill;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_QUANTITY, currentProgress);
            return compoundTag;
        }

        @Override
        public int getMissingQuantity()
        {
            return entitiesToKill > currentProgress ? entitiesToKill - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
