package com.minecolonies.core.quests.rewards;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestRewardTemplate;
import com.minecolonies.api.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public static IQuestRewardTemplate createReward(@NotNull final HolderLookup.Provider provider, final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final int quantity = details.get(QUANTITY_KEY).getAsInt();
        final ItemStack item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(details.get(ITEM_KEY).getAsString())).getDefaultInstance();
        if (details.has(COMPONENTS_KEY))
        {
            item.applyComponents(Utils.deserializeCodecMessFromJson(DataComponentPatch.CODEC, provider, details.getAsJsonObject(COMPONENTS_KEY)));
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
