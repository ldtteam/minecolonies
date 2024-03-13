package com.minecolonies.core.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;

/**
 * Objective type tracking block mining.
 */
public class BreakBlockObjectiveTemplate extends DialogueObjectiveTemplateTemplate implements IBreakBlockObjectiveTemplate
{
    /**
     * Amount of blocks to mine.
     */
    private final int blocksToMine;

    /**
     * The block to mine.
     */
    private final Block blockToMine;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Create a new objective of this type.
     *
     * @param target       the target citizen.
     * @param blocksToMine the number of blocks to mine.
     * @param blockToMine  the block to mine.
     * @param rewards the rewards this unlocks.
     */
    public BreakBlockObjectiveTemplate(final int target, final int blocksToMine, final Block blockToMine, final int nextObjective, final List<Integer> rewards)
    {
        super(target, buildDialogueTree(blockToMine), rewards);
        this.blocksToMine = blocksToMine;
        this.nextObjective = nextObjective;
        this.blockToMine = blockToMine;
    }

    @NotNull
    private static DialogueElement buildDialogueTree(final Block blockToMine)
    {
        final Component text = Component.translatableEscape("com.minecolonies.coremod.questobjectives.breakblock", blockToMine.getName());
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
        final Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(details.get(BLOCK_KEY).getAsString()));
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new BreakBlockObjectiveTemplate(target, quantity, block, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addQuestMineObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return createObjectiveInstance();
    }

    @Override
    public Component getProgressText(final IQuestInstance quest, final Style style)
    {
        if (quest.getCurrentObjectiveInstance() instanceof BlockMiningProgressInstance progress)
        {
            return Component.translatableEscape("com.minecolonies.coremod.questobjectives.breakblock.progress",
              progress.currentProgress,
              blocksToMine,
              blockToMine.getName().setStyle(style));
        }
        return Component.empty();
    }

    @Override
    public @NotNull IObjectiveInstance createObjectiveInstance()
    {
        return new BlockMiningProgressInstance(this);
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
            QuestObjectiveEventHandler.removeQuestMineObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    @Override
    public void onBlockBreak(final IObjectiveInstance blockMiningProgressData, final IQuestInstance colonyQuest, final Player player)
    {
        if (blockMiningProgressData.isFulfilled())
        {
            return;
        }

        ((BlockMiningProgressInstance) blockMiningProgressData).currentProgress++;
        if (blockMiningProgressData.isFulfilled())
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
            QuestObjectiveEventHandler.addQuestMineObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    private static class BlockMiningProgressInstance implements IObjectiveInstance
    {
        private int currentProgress = 0;

        /**
         * The template belonging to this progress instance.
         */
        private final BreakBlockObjectiveTemplate template;

        /**
         * Default constructor.
         */
        public BlockMiningProgressInstance(final IQuestObjectiveTemplate template)
        {
            this.template = (BreakBlockObjectiveTemplate) template;
        }

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= template.blocksToMine;
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
            return template.blocksToMine > currentProgress ? template.blocksToMine - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
