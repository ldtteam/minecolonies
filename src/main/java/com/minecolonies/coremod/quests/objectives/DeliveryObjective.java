package com.minecolonies.coremod.quests.objectives;

import com.google.gson.JsonObject;
import com.minecolonies.api.quests.IQuestObjective;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Delivery type objective.
 */
public class DeliveryObjective implements IQuestObjective
{
    /**
     * The quest participant target of this delivery (0 if questgiver).
     */
    private final int target;

    /**
     * The stack to be delivered.
     */
    private final ItemStack item;

    /**
     * The quantity to be delivered.
     */
    private final int quantity;

    /**
     * Create a new delivery objective,
     * @param target the target to receive the delivery.
     * @param item the item to be delivered.
     * @param quantity the quantity to be delivered.
     */
    public DeliveryObjective(final int target, final ItemStack item, final int quantity)
    {
        this.target = target;
        this.item = item;
        this.quantity = quantity;
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
}
