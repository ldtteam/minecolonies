package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.google.common.collect.EvictingQueue;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.TAG_STRING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LAST_FOODS;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The food handler for the citizen.
 */
public class CitizenFoodHandler implements ICitizenFoodHandler
{
    /**
     * Food queue size.
     */
    private static final int FOOD_QUEUE_SIZE = 10;

    /**
     * Assigned citizen data.
     */
    private final ICitizenData  citizenData;

    /**
     * Collection of last food items a citizen has eaten.
     */
    private final EvictingQueue<Item> lastEatenFoods = EvictingQueue.create(FOOD_QUEUE_SIZE);

    /**
     * Food stat cache to avoid recalculating constantly.
     */
    private CitizenFoodStats foodStatCache = null;

    /**
     * Dirty tracking if food stat cache has to be recalculated.
     */
    private boolean dirty = false;

    /**
     * Create the food handler.
     * @param citizenData of it.
     */
    public CitizenFoodHandler(final ICitizenData citizenData)
    {
        super();
        this.citizenData = citizenData;
    }

    @Override
    public void addLastEaten(final Item item)
    {
        lastEatenFoods.add(item);
        citizenData.markDirty(TICKS_SECOND);
        dirty = true;
        if (lastEatenFoods.size() >= 10)
        {
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO + FOOD_DIVERSITY), ChatPriority.IMPORTANT));
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO + FOOD_DIVERSITY), ChatPriority.IMPORTANT));
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO + FOOD_QUALITY + URGENT), ChatPriority.BLOCKING));
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO + FOOD_QUALITY + URGENT), ChatPriority.BLOCKING));
        }
    }

    @Override
    public Item getLastEaten()
    {
        return lastEatenFoods.peek();
    }

    @Override
    public int checkLastEaten(final Item item)
    {
        int index = -1;
        for (final Item foodItem : lastEatenFoods)
        {
            if (foodItem == item)
            {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public CitizenFoodStats getFoodHappinessStats()
    {
        if (foodStatCache == null || dirty)
        {
            int qualityFoodCounter = 0;
            Set<Item> uniqueFoods = new HashSet<>();
            for (final Item foodItem : lastEatenFoods)
            {
                if (foodItem instanceof IMinecoloniesFoodItem)
                {
                    qualityFoodCounter++;
                }
                uniqueFoods.add(foodItem);
            }
            foodStatCache = new CitizenFoodStats(qualityFoodCounter, Math.max(1, uniqueFoods.size()));
        }
        return foodStatCache;
    }

    @Override
    public boolean hasFullFoodHistory()
    {
        return lastEatenFoods.size() >= FOOD_QUEUE_SIZE;
    }

    @Override
    public void read(final CompoundTag compound)
    {
        @NotNull final ListTag lastFoodNbt = compound.getList(TAG_LAST_FOODS, TAG_STRING);
        for (int i = 0; i < lastFoodNbt.size(); i++)
        {
            final Item lastFood = BuiltInRegistries.ITEM.get(new ResourceLocation(lastFoodNbt.getString(i)));
            if (lastFood != Items.AIR)
            {
                lastEatenFoods.add(lastFood);
            }
        }
    }

    @Override
    public void write(final CompoundTag compound)
    {
        @NotNull final ListTag lastEatenFoodsNBT = new ListTag();
        for (final Item foodItem : lastEatenFoods)
        {
            lastEatenFoodsNBT.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(foodItem).toString()));
        }
        compound.put(TAG_LAST_FOODS, lastEatenFoodsNBT);
    }

    @Override
    public double getDiseaseModifier(final double baseModifier)
    {
        if (lastEatenFoods.size() < 10 || baseModifier == 0)
        {
            return baseModifier;
        }
        return baseModifier * 0.5 * Math.min(2.5, 5.0/getFoodHappinessStats().diversity());
    }
}
