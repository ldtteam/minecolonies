package com.minecolonies.core.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;

/**
 * Objective type tracking block placing.
 */
public class PlaceBlockObjectiveTemplate extends DialogueObjectiveTemplateTemplate implements IPlaceBlockObjectiveTemplate
{
    /**
     * Amount of blocks to place.
     */
    private final int qty;

    /**
     * The block to place.
     */
    private final Block blockToPlace;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Create a new objective of this type.
     *
     * @param target       the target citizen.
     * @param qty the number of blocks to place.
     * @param blockToPlace  the block to place.
     * @param rewards the rewards this unlocks.
     */
    public PlaceBlockObjectiveTemplate(final int target, final int qty, final Block blockToPlace, final int nextObjective, final List<Integer> rewards)
    {
        super(target, buildDialogueTree(blockToPlace), rewards);
        this.qty = qty;
        this.nextObjective = nextObjective;
        this.blockToPlace = blockToPlace;
    }

    @NotNull
    private static DialogueElement buildDialogueTree(final Block blockToMine)
    {
        final Component text = Component.translatable("com.minecolonies.coremod.questobjectives.placeblock", blockToMine.getName());
        final AnswerElement answer1 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.later"),
                new IQuestDialogueAnswer.CloseUIDialogueAnswer());
        final AnswerElement answer2 = new AnswerElement(Component.translatable("com.minecolonies.coremod.questobjectives.answer.cancel"),
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
        final Block block = ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(details.get(BLOCK_KEY).getAsString())).get().get();
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new PlaceBlockObjectiveTemplate(target, quantity, block, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addQuestPlaceObjectiveListener(this.blockToPlace, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return createObjectiveInstance();
    }

    @Override
    public Component getProgressText(final IQuestInstance quest, final Style style)
    {
        if (quest.getCurrentObjectiveInstance() instanceof BlockPlacementProgressInstance progress)
        {
            return Component.translatable("com.minecolonies.coremod.questobjectives.placeblock.progress",
              progress.currentProgress,
              blockToPlace,
              blockToPlace.getName().setStyle(style));
        }
        return Component.empty();
    }

    @Override
    public @Nullable IObjectiveInstance createObjectiveInstance()
    {
        return new BlockPlacementProgressInstance(this);
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
            QuestObjectiveEventHandler.removeQuestPlaceBlockObjectiveListener(this.blockToPlace, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    @Override
    public void onBlockPlace(final IObjectiveInstance blockPlacementProgressData, final IQuestInstance colonyQuest, final Player player)
    {
        if (blockPlacementProgressData.isFulfilled())
        {
            return;
        }

        ((BlockPlacementProgressInstance) blockPlacementProgressData).currentProgress++;
        if (blockPlacementProgressData.isFulfilled())
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
            QuestObjectiveEventHandler.addQuestPlaceObjectiveListener(this.blockToPlace, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    private static class BlockPlacementProgressInstance implements IObjectiveInstance
    {
        /**
         * The template belonging to this progress instance.
         */
        private final PlaceBlockObjectiveTemplate template;

        private int currentProgress = 0;

        public BlockPlacementProgressInstance(final PlaceBlockObjectiveTemplate template)
        {
            this.template = template;
        }

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= template.qty;
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
            return template.qty > currentProgress ? template.qty - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
