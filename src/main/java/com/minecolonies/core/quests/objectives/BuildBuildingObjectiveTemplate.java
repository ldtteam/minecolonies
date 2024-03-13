package com.minecolonies.core.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.core.colony.Colony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;

/**
 * Objective type tracking build building.
 */
public class BuildBuildingObjectiveTemplate extends DialogueObjectiveTemplateTemplate implements IBuildingUpgradeObjectiveTemplate
{
    /**
     * The building to level.
     */
    private final BuildingEntry buildingEntry;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Whether to consider existing buildings.
     */
    private final boolean countExisting;

    /**
     * Whether to consider existing buildings. The building level to reach.
     */
    private final int lvl;

    /**
     * The number of buildings to reach a level for. If 0, count cumulative.
     */
    private final int qty;

    /**
     * Create a new objective of this type.
     *
     * @param target        the target citizen.
     * @param qty           the number of levels.
     * @param buildingEntry the building to level.
     * @param rewards       the rewards this unlocks.
     */
    public BuildBuildingObjectiveTemplate(
      final int target,
      final BuildingEntry buildingEntry,
      final int lvl,
      final int qty,
      final boolean countExisting,
      final int nextObjective,
      final List<Integer> rewards)
    {
        super(target, buildDialogueTree(buildingEntry, qty, lvl, countExisting), rewards);
        this.lvl = lvl;
        this.qty = qty;
        this.countExisting = countExisting;
        this.nextObjective = nextObjective;
        this.buildingEntry = buildingEntry;
    }

    @NotNull
    private static DialogueElement buildDialogueTree(final BuildingEntry buildingEntry, final int qty, final int lvl, final boolean countExisting)
    {
        final Component text;
        if (countExisting)
        {
            if (qty > 0)
            {
                text =
                  Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding.existing", lvl, qty, Component.translatableEscape(buildingEntry.getTranslationKey()));
            }
            else
            {
                text = Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding.cumulative.existing",
                  lvl,
                  Component.translatableEscape(buildingEntry.getTranslationKey()));
            }
        }
        else
        {
            if (qty > 0)
            {
                text = Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding", qty, lvl, Component.translatableEscape(buildingEntry.getTranslationKey()));
            }
            else
            {
                text = Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding.cumulative", lvl, Component.translatableEscape(buildingEntry.getTranslationKey()));
            }
        }

        final AnswerElement answer1 = new AnswerElement(Component.translatableEscape("com.minecolonies.coremod.questobjectives.answer.later"),
          new IQuestDialogueAnswer.CloseUIDialogueAnswer());
        final AnswerElement answer2 = new AnswerElement(Component.translatableEscape("com.minecolonies.coremod.questobjectives.answer.cancel"),
          new IQuestDialogueAnswer.QuestCancellationDialogueAnswer());
        return new DialogueElement(text, List.of(answer1, answer2));
    }

    /**
     * Parse the build building objective from json.
     *
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);

        final int target = details.get(TARGET_KEY).getAsInt();
        final BuildingEntry buildingEntry = IMinecoloniesAPI.getInstance().getBuildingRegistry().get(new ResourceLocation(details.get(BUILDING_KEY).getAsString()));

        final int level = details.get(LEVEL_KEY).getAsInt();
        final int quantity = details.get(QUANTITY_KEY).getAsInt();
        final boolean countExisting = details.get(COUNT_EXIST_KEY).getAsBoolean();

        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new BuildBuildingObjectiveTemplate(target, buildingEntry, level, quantity, countExisting, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);

        final IObjectiveInstance instance = createObjectiveInstance();
        if (countExisting)
        {
            checkInitialObjectiveProgress(colonyQuest, instance);
            if (instance.isFulfilled())
            {
                return colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
            }
        }

        if (colonyQuest.getColony() instanceof Colony)
        {
            colonyQuest.getColony().getBuildingManager().trackBuildingLevelUp(this.buildingEntry, colonyQuest);
        }
        return instance;
    }

    /**
     * Upon start, we want to check if the necessary buildings already exist.
     *
     * @param colonyQuest the quest instance.
     * @param localInstance the local instance to adjust if necessary.
     */
    private void checkInitialObjectiveProgress(final IQuestInstance colonyQuest, final IObjectiveInstance localInstance)
    {
        if (localInstance instanceof BuildingProgressInstance progressInstance)
        {
            if (qty > 0)
            {
                for (final IBuilding building : colonyQuest.getColony().getBuildingManager().getBuildings().values())
                {
                    if (building.getBuildingType() == buildingEntry && building.getBuildingLevel() >= lvl)
                    {
                        progressInstance.currentProgress++;
                    }
                }
            }
            else
            {
                for (final IBuilding building : colonyQuest.getColony().getBuildingManager().getBuildings().values())
                {
                    if (building.getBuildingType() == buildingEntry)
                    {
                        progressInstance.currentProgress += building.getBuildingLevel();
                    }
                }
            }
        }
    }

    /**
     * Advances the quest if finished, does the necessary cleanup as well.
     *
     * @param colonyQuest the quest instance.
     * @return the next quest or null if not finished.
     */
    private boolean advanceIfFinished(final IQuestInstance colonyQuest)
    {
        final IObjectiveInstance objective = colonyQuest.getCurrentObjectiveInstance();
        if (objective instanceof BuildingProgressInstance progressInstance && progressInstance.isFulfilled())
        {
            cleanupListener(colonyQuest);
            colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
            return true;
        }
        return false;
    }

    /**
     * Cleanup the listener of this objective,
     *
     * @param colonyQuest the listener.
     */
    private void cleanupListener(final IQuestInstance colonyQuest)
    {
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            colonyQuest.getColony().getBuildingManager().stopTrackingBuildingLevelUp(this.buildingEntry, colonyQuest);
        }
    }

    @Override
    public Component getProgressText(final IQuestInstance quest, final Style style)
    {
        if (quest.getCurrentObjectiveInstance() instanceof BuildingProgressInstance progress)
        {
            if (qty > 0)
            {
                return Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding.progress",
                  Math.min(progress.currentProgress, qty),
                  qty,
                  Component.translatableEscape(buildingEntry.getTranslationKey()).setStyle(style));
            }
            else
            {
                return Component.translatableEscape("com.minecolonies.coremod.questobjectives.buildbuilding.progress.cumulative",
                  Math.min(progress.currentProgress, lvl),
                  lvl,
                  Component.translatableEscape(buildingEntry.getTranslationKey()).setStyle(style));
            }
        }
        return Component.empty();
    }

    @Override
    public void onCancellation(final IQuestInstance colonyQuest)
    {
        cleanupListener(colonyQuest);
    }

    @Override
    public void onWorldLoad(final IQuestInstance colonyQuest)
    {
        super.onWorldLoad(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony colony && !advanceIfFinished(colonyQuest))
        {
            colony.getBuildingManager().trackBuildingLevelUp(this.buildingEntry, colonyQuest);
        }
    }

    @Override
    public @NotNull IObjectiveInstance createObjectiveInstance()
    {
        return new BuildingProgressInstance(this, 0);
    }

    @Override
    public void onBuildingUpgrade(final IObjectiveInstance progressData, final IQuestInstance colonyQuest, final int level)
    {
        if (progressData.isFulfilled())
        {
            return;
        }

        if (!(progressData instanceof BuildingProgressInstance buildingProgressInstance))
        {
            return;
        }

        if (qty > 0)
        {
            if (level >= lvl)
            {
                buildingProgressInstance.currentProgress++;
            }
        }
        else
        {
            buildingProgressInstance.currentProgress++;
        }

        advanceIfFinished(colonyQuest);
    }

    /**
     * Progress data of this objective.
     */
    private static class BuildingProgressInstance implements IObjectiveInstance
    {
        /**
         * The template belonging to this progress instance.
         */
        private final BuildBuildingObjectiveTemplate template;

        /**
         * The current progress towards the objective.
         */
        private int currentProgress;

        public BuildingProgressInstance(final BuildBuildingObjectiveTemplate template, final int currentProgress)
        {
            this.template = template;
            this.currentProgress = currentProgress;
        }

        @Override
        public boolean isFulfilled()
        {
            return template.qty > 0 ? currentProgress >= template.qty : currentProgress >= template.lvl;
        }

        @Override
        public int getMissingQuantity()
        {
            return 0;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_QUANTITY, currentProgress);
            return compoundTag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
