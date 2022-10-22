package com.minecolonies.coremod.quests.type.rewards;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.quests.IQuest;
import com.minecolonies.coremod.quests.type.IQuestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
    public void applyReward(final IQuest quest, final PlayerEntity playerEntity)
    {
        for (final ItemStack stack : rewardStacks)
        {
            if (!InventoryUtils.addItemStackToItemHandler(new InvWrapper(playerEntity.inventory), stack) && !ItemStackUtils.isEmpty(stack))
            {
                InventoryUtils.spawnItemStack(playerEntity.world, playerEntity.getPosX(), playerEntity.getPosY(), playerEntity.getPosZ(), stack);
            }
        }
    }
}
