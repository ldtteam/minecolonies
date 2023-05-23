package com.minecolonies.coremod.quests.objectives;

import com.google.common.collect.ImmutableCollection;
import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param target       the target citizen.
     * @param qty the number of levels.
     * @param buildingEntry  the building to level.
     * @param rewards the rewards this unlocks.
     */
    public BuildBuildingObjectiveTemplate(final int target, final BuildingEntry buildingEntry, final int lvl, final int qty, final boolean countExisting, final int nextObjective, final List<Integer> rewards)
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
                text = Component.translatable("com.minecolonies.coremod.questobjectives.buildbuilding.existing", lvl, qty, Component.translatable(buildingEntry.getTranslationKey()));
            }
            else
            {
                text = Component.translatable("com.minecolonies.coremod.questobjectives.buildbuilding.cumulative.existing", lvl, Component.translatable(buildingEntry.getTranslationKey()));
            }
        }
        else
        {
            if (qty > 0)
            {
                text = Component.translatable("com.minecolonies.coremod.questobjectives.buildbuilding", qty, lvl, Component.translatable(buildingEntry.getTranslationKey()));
            }
            else
            {
                text = Component.translatable("com.minecolonies.coremod.questobjectives.buildbuilding.cumulative", lvl, Component.translatable(buildingEntry.getTranslationKey()));
            }
        }

        final AnswerElement answer1 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.later"),
                new IQuestDialogueAnswer.CloseUIDialogueAnswer());
        final AnswerElement answer2 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.cancel"),
                new IQuestDialogueAnswer.QuestCancellationDialogueAnswer());
        return new DialogueElement(text, List.of(answer1, answer2));
    }

    /**
     * Parse the build building objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);

        final int target = details.get(TARGET_KEY).getAsInt();
        final BuildingEntry buildingEntry = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(new ResourceLocation(details.get(BUILDING_KEY).getAsString()));

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

        if (checkForFulfillment(colonyQuest))
        {
            // Already detect as finished!
            return new BuildingBuildingProgressInstance(true);
        }

        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            colonyQuest.getColony().getBuildingManager().trackBuildingLevelUp(this.buildingEntry, colonyQuest);
        }

        /*
         *   "com.minecolonies.coremod.questobjectives.buildbuilding": "I am still waiting for you to build %d level %d %s!",
         *   "com.minecolonies.coremod.questobjectives.buildbuilding.existing": "I am still waiting for you to reach level %d for %d %s!",
         *   "com.minecolonies.coremod.questobjectives.buildbuilding.cumulative": "I am still waiting for you to build or upgrade %d levels of %s!",
         *   "com.minecolonies.coremod.questobjectives.buildbuilding.cumulative.existing": "I am still waiting for you to reach %s totaling %d levels!"
         */

        //todo on start here, we want to check if its fulfilled already, then we dont even have to start it.
        return new BuildingBuildingProgressInstance();
    }

    private boolean checkForFulfillment(final IQuestInstance colonyQuest)
    {

    }

    @Nullable
    @Override
    public IObjectiveInstance createObjectiveInstance()
    {
        return new BuildingBuildingProgressInstance();
    }

    @Override
    public void onCancellation(final IQuestInstance colonyQuest)
    {
        cleanupListener(colonyQuest);
    }

    /**
     * Cleanup the listener of this objective,
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
    public void onBuildingUpgrade(final IObjectiveInstance progressData, final IQuestInstance colonyQuest, final ImmutableCollection<IBuilding> buildingList)
    {
        if (progressData.isFulfilled())
        {
            return;
        }

        ((BuildingBuildingProgressInstance) progressData).currentProgress++;
        if (progressData.isFulfilled())
        {
            cleanupListener(colonyQuest);
            colonyQuest.advanceObjective(colonyQuest.getColony().getWorld().getPlayerByUUID(colonyQuest.getAssignedPlayer()), nextObjective);
        }
    }

    @Override
    public void onWorldLoad(final IQuestInstance colonyQuest)
    {
        super.onWorldLoad(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            colonyQuest.getColony().getBuildingManager().trackBuildingLevelUp(this.buildingEntry, colonyQuest);
            //todo on start here, we want to check if its fulfilled already, then we dont even have to start it.
        }
    }

    /**
     * Progress data of this objective.
     */
    public class BuildingBuildingProgressInstance implements IObjectiveInstance
    {
        private int currentProgress = 0;

        @Override
        public boolean isFulfilled()
        {
            //todo adjust!

            return currentProgress >= qty;
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
            return qty > currentProgress ? qty - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
