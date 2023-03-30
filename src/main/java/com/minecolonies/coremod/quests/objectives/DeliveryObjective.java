package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.quests.IAnswerResult;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Delivery type objective.
 */
public class DeliveryObjective extends DialogueObjective implements IQuestActionObjective
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
     * The two dialogue options.
     */
    private DialogueElement readyDialogueElement;
    private DialogueElement waitingDialogueElement;

    /**
     * Create a new delivery objective,
     * @param target the target to receive the delivery.
     * @param item the item to be delivered.
     * @param quantity the quantity to be delivered.
     */
    public DeliveryObjective(final int target, final ItemStack item, final int quantity)
    {
        super(target, null);
        this.item = item;
        this.quantity = quantity;
        this.buildDialogueTrees();
    }

    private void buildDialogueTrees()
    {
        this.readyDialogueElement = new DialogueElement("Oh hey, you brought " + item.getDisplayName().getString() + " can I have it?",
          List.of(new AnswerElement("Yes, here you are!", new IAnswerResult.AdvanceResult()), new AnswerElement("No, wait!", new IAnswerResult.ReturnResult())));

        this.waitingDialogueElement = new DialogueElement("I am still waiting for " + item.getDisplayName().getString() + " !",
          List.of(new AnswerElement("Sorry, be right back!", new IAnswerResult.ReturnResult()), new AnswerElement("I don't have any of it!", new IAnswerResult.CancelResult())));
    }

    /**
     * Parse the dialogue objective from json.
     * @param jsonObject the json to parse it from.
     * @return a new objective object.
     */
    public static IQuestObjective createObjective(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject("details");
        final int target = details.get("target").getAsInt();
        final int quantity = details.get("qty").getAsInt();
        final ItemStack item = new ItemStack(ForgeRegistries.ITEMS.getHolder(new ResourceLocation(details.get("item").getAsString())).get().get());

        return new DeliveryObjective(target, item, quantity);
    }

    @Override
    public boolean isReady(final Player player, final IColonyQuest colonyQuest)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), item.getItem()) >= quantity;
    }

    @Override
    public boolean tryResolve(final Player player, final IColonyQuest colonyQuest)
    {
        return InventoryUtils.attemptReduceStackInItemHandler(new InvWrapper(player.getInventory()), this.item, this.quantity);
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
