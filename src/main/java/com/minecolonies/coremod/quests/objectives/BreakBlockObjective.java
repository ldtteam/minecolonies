package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IAnswerResult;
import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IObjectiveData;
import com.minecolonies.api.quests.IQuestObjective;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;
import static com.minecolonies.api.util.constant.QuestParseConstant.*;

/**
 * Objective type tracking block mining.
 */
public class BreakBlockObjective extends DialogueObjective implements IBreakBlockObjective
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
    public BreakBlockObjective(final int target, final int blocksToMine, final Block blockToMine, final int nextObjective, final List<Integer> rewards)
    {
        super(target, new DialogueElement("I am still waiting for you to mine %d " + blockToMine.getName().getString() + " !",
          List.of(new AnswerElement("Sorry, be right back!", new IAnswerResult.ReturnResult()), new AnswerElement("I don't have time for this!", new IAnswerResult.CancelResult()))), rewards);
        this.blocksToMine = blocksToMine;
        this.nextObjective = nextObjective;
        this.blockToMine = blockToMine;
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
        final Block block = ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(details.get(BLOCK_KEY).getAsString())).get().get();
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : -1;

        return new BreakBlockObjective(target, quantity, block, nextObj, parseRewards(jsonObject));
    }

    @Override
    public IObjectiveData init(final IColonyQuest colonyQuest)
    {
        super.init(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            ((Colony) colonyQuest.getColony()).getEventHandler().addQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
        return new BlockMiningProgressData();
    }

    @Nullable
    @Override
    public IObjectiveData getObjectiveData()
    {
        return new BlockMiningProgressData();
    }

    @Override
    public void onAbort(final IColonyQuest colonyQuest)
    {
        cleanupEvent(colonyQuest);
    }

    private void cleanupEvent(final IColonyQuest colonyQuest)
    {
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            ((Colony) colonyQuest.getColony()).getEventHandler().removeQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    @Override
    public void onBlockBreak(final IObjectiveData blockMiningProgressData, final IColonyQuest colonyQuest, final Player player)
    {
        if (blockMiningProgressData.isFulfilled())
        {
            return;
        }

        ((BlockMiningProgressData) blockMiningProgressData).currentProgress++;
        if (blockMiningProgressData.isFulfilled())
        {
            colonyQuest.advanceObjective(player, nextObjective);
        }
    }

    @Override
    public void onWorldLoad(final IColonyQuest colonyQuest)
    {
        super.onWorldLoad(colonyQuest);
        if (colonyQuest.getColony() instanceof Colony)
        {
            // Only serverside cleanup.
            ((Colony) colonyQuest.getColony()).getEventHandler().addQuestObjectiveListener(this.blockToMine, colonyQuest.getAssignedPlayer(), colonyQuest);
        }
    }

    /**
     * Progress data of this objective.
     */
    public class BlockMiningProgressData implements IObjectiveData
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
