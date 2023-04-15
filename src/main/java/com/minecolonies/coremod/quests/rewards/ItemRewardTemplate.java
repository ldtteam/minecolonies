package com.minecolonies.coremod.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import com.minecolonies.api.util.Log;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import static com.minecolonies.api.quests.QuestParseConstant.*;

/**
 * Item based quest reward.
 */
public class ItemRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The stack to give to the player.
     */
    private final ItemStack item;

    /**
     * Setup the item reward.
     * @param item the item.
     */
    public ItemRewardTemplate(final ItemStack item)
    {
        this.item = item;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
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
        item.setCount(quantity);
        return new ItemRewardTemplate(item);
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        player.getInventory().add(item);
    }
}
