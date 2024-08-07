package com.minecolonies.core.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;

/**
 * Objective type entity killing mining.
 */
public class KillEntityObjectiveTemplateTemplate extends DialogueObjectiveTemplateTemplate implements IKillEntityObjectiveTemplate
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
    public KillEntityObjectiveTemplateTemplate(final int target, final int entitiesToKill, final EntityType<?> entityToKill, final int nextObjective, final List<Integer> rewards)
    {
        super(target, buildDialogueTree(entityToKill), rewards);
        this.entitiesToKill = entitiesToKill;
        this.nextObjective = nextObjective;
        this.entityToKill = entityToKill;
    }

    @NotNull
    private static DialogueElement buildDialogueTree(final EntityType<?> entityToKill)
    {
        final Component text = Component.translatableEscape("com.minecolonies.coremod.questobjectives.kill", entityToKill.getDescription());
        final AnswerElement answer1 = new AnswerElement(Component.translatableEscape("com.minecolonies.coremod.questobjectives.answer.later"),
                new IQuestDialogueAnswer.CloseUIDialogueAnswer());
        final AnswerElement answer2 = new AnswerElement(Component.translatableEscape("com.minecolonies.coremod.questobjectives.answer.cancel"),
                new IQuestDialogueAnswer.QuestCancellationDialogueAnswer());
        return new DialogueElement(text, List.of(answer1, answer2));
    }

    /**
     * Parse the mine block objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target = details.get(TARGET_KEY).getAsInt();
        final int quantity = details.get(QUANTITY_KEY).getAsInt();
        final EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(details.get(ENTITY_TYPE_KEY).getAsString()));
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new KillEntityObjectiveTemplateTemplate(target, quantity, entityType, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addKillQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return createObjectiveInstance();
    }

    @Override
    public Component getProgressText(final IQuestInstance quest, final Style style)
    {
        if (quest.getCurrentObjectiveInstance() instanceof EntityKillProgressInstance progress)
        {
            return Component.translatableEscape("com.minecolonies.coremod.questobjectives.kill.progress",
              progress.currentProgress,
              entitiesToKill,
              entityToKill.getDescription().plainCopy().setStyle(style));
        }
        return Component.empty();
    }

    @Override
    public @Nullable IObjectiveInstance createObjectiveInstance()
    {
        return new EntityKillProgressInstance(this);
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
            QuestObjectiveEventHandler.removeKillQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    @Override
    public void onEntityKill(final IObjectiveInstance killProgressData, final IQuestInstance colonyQuest, final Player player)
    {
        if (killProgressData.isFulfilled())
        {
            return;
        }

        ((EntityKillProgressInstance) killProgressData).currentProgress++;
        if (killProgressData.isFulfilled())
        {
            cleanupListener(colonyQuest);
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
            QuestObjectiveEventHandler.addKillQuestObjectiveListener(this.entityToKill, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    private static class EntityKillProgressInstance implements IObjectiveInstance
    {
        /**
         * The template belonging to this progress instance.
         */
        private final KillEntityObjectiveTemplateTemplate template;

        private int currentProgress = 0;

        public EntityKillProgressInstance(final KillEntityObjectiveTemplateTemplate template)
        {
            this.template = template;
        }

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= template.entitiesToKill;
        }

        @Override
        public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_QUANTITY, currentProgress);
            return compoundTag;
        }

        @Override
        public int getMissingQuantity()
        {
            return template.entitiesToKill > currentProgress ? template.entitiesToKill - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
