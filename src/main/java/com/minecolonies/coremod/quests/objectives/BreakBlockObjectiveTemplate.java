package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.event.QuestObjectiveEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;
import static com.minecolonies.api.quests.QuestParseConstant.*;

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
        super(target, new DialogueElement("I am still waiting for you to mine %d " + blockToMine.getName().getString() + " !",
          List.of(new AnswerElement("Sorry, be right back!", new IQuestDialogueAnswer.CloseUIDialogueAnswer()), new AnswerElement("I don't have time for this!", new IQuestDialogueAnswer.QuestCancellationDialogueAnswer()))), rewards);
        this.blocksToMine = blocksToMine;
        this.nextObjective = nextObjective;
        this.blockToMine = blockToMine;
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

        return new BreakBlockObjectiveTemplate(target, quantity, block, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveInstance startObjective(final IQuestInstance colonyQuest)
    {
        super.startObjective(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            QuestObjectiveEventHandler.addQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return new BlockMiningProgressInstance();
    }

    @Nullable
    @Override
    public IObjectiveInstance getObjectiveInstance()
    {
        return new BlockMiningProgressInstance();
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
            QuestObjectiveEventHandler.removeQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
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
            QuestObjectiveEventHandler.addQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    public class BlockMiningProgressInstance implements IObjectiveInstance
    {
        private int currentProgress = 0;

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= blocksToMine;
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
            return blocksToMine > currentProgress ? blocksToMine - currentProgress : 0;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentProgress = nbt.getInt(TAG_QUANTITY);
        }
    }
}
