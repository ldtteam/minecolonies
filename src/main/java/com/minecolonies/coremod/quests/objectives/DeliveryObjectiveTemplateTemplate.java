package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.quests.IQuestDialogueAnswer;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Delivery type objective.
 */
public class DeliveryObjectiveTemplateTemplate extends DialogueObjectiveTemplateTemplate implements IQuestDeliveryObjective
{
    /**
     * The stack to be delivered.
     */
    private final ItemStack item;

    /**
     * The quantity to be delivered.
     */
    private final int quantity;

    /**
     * Next objective to go to, on fulfillment. -1 if final objective.
     */
    private final int nextObjective;

    /**
     * Nbt mode.
     */
    private final String nbtMode;

    /**
     * The two dialogue options.
     */
    private DialogueElement readyDialogueElement;
    private DialogueElement waitingDialogueElement;

    /**
     * Create a new delivery objective,
     *
     * @param target   the target to receive the delivery.
     * @param item     the item to be delivered.
     * @param quantity the quantity to be delivered.
     * @param rewards the rewards this unlocks.
     */
    public DeliveryObjectiveTemplateTemplate(final int target, final ItemStack item, final int quantity, final int nextObjective, final List<Integer> rewards, final String nbtMode)
    {
        super(target, null, rewards);
        this.item = item;
        this.quantity = quantity;
        this.nextObjective = nextObjective;
        this.nbtMode = nbtMode;
        this.buildDialogueTrees();
    }

    private void buildDialogueTrees()
    {
        this.readyDialogueElement = new DialogueElement("Oh hey, you brought " + item.getDisplayName().getString() + " can I have it?",
          List.of(new AnswerElement("Yes, here you are!", new IQuestDialogueAnswer.NextObjectiveDialogueAnswer(this.nextObjective)), new AnswerElement("No, wait!", new IQuestDialogueAnswer.CloseUIDialogueAnswer())));

        this.waitingDialogueElement = new DialogueElement("I am still waiting for " + item.getDisplayName().getString() + " !",
          List.of(new AnswerElement("Sorry, be right back!", new IQuestDialogueAnswer.CloseUIDialogueAnswer()), new AnswerElement("I don't have any of it!", new IQuestDialogueAnswer.QuestCancellationDialogueAnswer())));
    }

    /**
     * Parse the dialogue objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjectiveTemplate createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int target = details.get(TARGET_KEY).getAsInt();
        final int quantity = details.get(QUANTITY_KEY).getAsInt();
        final ItemStack item = new ItemStack(ForgeRegistries.ITEMS.getHolder(new ResourceLocation(details.get(ITEM_KEY).getAsString())).get().get());
        if (details.has(NBT_KEY))
        {
            try
            {
                item.setTag(TagParser.parseTag(GsonHelper.getAsString(details, NBT_KEY)));
            }
            catch (CommandSyntaxException e)
            {
                Log.getLogger().error("Unable to load itemstack nbt from json!");
                throw new RuntimeException(e);
            }
        }
        final int nextObj = details.has(NEXT_OBJ_KEY) ? details.get(NEXT_OBJ_KEY).getAsInt() : - 1;
        final String nbtMode = details.has(NBT_MODE_KEY) ? details.get(NBT_MODE_KEY).getAsString() : "";
        return new DeliveryObjectiveTemplateTemplate(target, item, quantity, nextObj, parseRewards(jsonObject), nbtMode);
    }

    @Override
    public boolean hasItem(final Player player, final IQuestInstance colonyQuest)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, item, !nbtMode.equals("any"), !nbtMode.equals("any"))) >= quantity;
    }

    @Override
    public boolean tryDiscountItem(final Player player, final IQuestInstance colonyQuest)
    {
        return InventoryUtils.attemptReduceStackInItemHandler(new InvWrapper(player.getInventory()), this.item, this.quantity, nbtMode.equals("any"), nbtMode.equals("any"));
    }

    @Override
    public DialogueElement getDialogueTree()
    {
        return waitingDialogueElement;
    }

    @Override
    public DialogueElement getReadyDialogueTree()
    {
        return readyDialogueElement;
    }
}
