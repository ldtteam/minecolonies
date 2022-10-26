package com.minecolonies.coremod.quests.type.rewards;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.quests.IQuest;
import com.minecolonies.coremod.quests.type.IQuestType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Quest reward with a certain itemstack
 */
public class ItemStackQuestReward implements IQuestReward
{
    /**
     * Quest reward ID
     */
    public static final ResourceLocation ID = new ResourceLocation(MOD_ID, "itemreward");

    private final IQuestType questType;

    /**
     * Itemstack rewards
     */
    private final List<ItemStack> rewardStacks = new ArrayList<>();

    public ItemStackQuestReward(final IQuestType questType) {this.questType = questType;}

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public void applyReward(final IQuest quest, final Player playerEntity)
    {
        for (final ItemStack stack : rewardStacks)
        {
            if (!InventoryUtils.addItemStackToItemHandler(new InvWrapper(playerEntity.getInventory()), stack) && !ItemStackUtils.isEmpty(stack))
            {
                InventoryUtils.spawnItemStack(playerEntity.level, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), stack);
            }
        }
    }
}
