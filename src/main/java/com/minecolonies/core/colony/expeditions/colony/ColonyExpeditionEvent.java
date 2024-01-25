package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.AbstractExpeditionEvent;
import com.minecolonies.core.items.ItemAdventureToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_INVENTORY;

public class ColonyExpeditionEvent extends AbstractExpeditionEvent
{
    /**
     * The event ID.
     */
    public static final ResourceLocation COLONY_EXPEDITION_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "colony_expedition");

    /**
     * NBT tags.
     */
    private static final String TAG_EXPEDITION_TYPE  = "expeditionType";
    private static final String TAG_DAYS_IN_PROGRESS = "daysInProgress";
    private static final String TAG_REMAINING_ITEMS  = "remainingItems";

    /**
     * The size of the expedition inventory.
     */
    private static final int EXPEDITION_INVENTORY_SIZE = 27;

    /**
     * The maximum amount of days an expedition can take.
     * This is actually 3 days, but we take into consideration the potential partial first day of travelling.
     */
    private static final int MAX_EXPEDITION_DAYS_LENGTH = 4;

    /**
     * The inventory for the expedition.
     */
    private final ItemStackHandler inventory = new ItemStackHandler(EXPEDITION_INVENTORY_SIZE);

    /**
     * Random instance for calculating random values.
     */
    private final Random random = new Random();

    /**
     * The expedition type for this colony expedition.
     */
    private ColonyExpeditionType expeditionType;

    /**
     * The amount of days that the expedition is currently in progress.
     */
    private int daysInProgress = 0;

    /**
     * Contains a set of items that still have yet to be found.
     */
    @Nullable
    private Deque<ItemStack> remainingItems;

    /**
     * Create a new colony expedition event.
     *
     * @param colony     the colony instance.
     * @param expedition the expedition instance.
     */
    public ColonyExpeditionEvent(final IColony colony, final IExpedition expedition)
    {
        super(colony.getEventManager().getAndTakeNextEventID(), colony, expedition);
    }

    /**
     * Create a new colony expedition event.
     *
     * @param id         the event ID.
     * @param colony     the colony instance.
     * @param expedition the expedition instance.
     */
    private ColonyExpeditionEvent(final int id, final IColony colony, final IExpedition expedition)
    {
        super(id, colony, expedition);
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBT compound with saved values
     * @return the raid event.
     */
    public static ColonyExpeditionEvent loadFromNBT(final IColony colony, final CompoundTag compound)
    {
        return AbstractExpeditionEvent.loadFromNBT(colony, compound, ColonyExpeditionEvent::new);
    }

    private void processAdventureToken(final ItemStack itemStack)
    {
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return COLONY_EXPEDITION_EVENT_TYPE_ID;
    }

    @Override
    public void onUpdate()
    {
        if (!getExpedition().getStatus().equals(ExpeditionStatus.EMBARKED) || remainingItems == null)
        {
            return;
        }

        final ItemStack nextItem = remainingItems.getFirst();
        if (nextItem.getItem() instanceof ItemAdventureToken)
        {
            if (nextItem.hasTag())
            {
                processAdventureToken(nextItem);
            }
        }
        else
        {
            getExpedition().rewardFound(nextItem);
        }
    }

    @Override
    public void onStart()
    {
        final Level world = getColony().getWorld();
        if (!world.isClientSide)
        {
            getExpedition().getEquipment().forEach(f -> InventoryUtils.addItemStackToItemHandler(inventory, f));

            final LootParams lootParams = new Builder((ServerLevel) world)
                                            .withLuck(expeditionType.getDifficulty().getLuckLevel())
                                            .create(LootContextParamSet.builder().build());

            final LootTable lootTable = getColony().getWorld().getServer().getLootData().getLootTable(expeditionType.getLootTable());

            // Copy the items, natively a Stack implementation, to a deque, so we can pop the first item off each colony tick.
            remainingItems = new ArrayDeque<>(lootTable.getRandomItems(lootParams));
        }
    }

    @Override
    public void onFinish()
    {
        getColony().getExpeditionManager().addExpedition(getExpedition(), ColonyExpeditionEvent.class);
    }

    @Override
    public void onNightFall()
    {
        if (getExpedition().getStatus().equals(ExpeditionStatus.EMBARKED))
        {
            daysInProgress++;
            if (daysInProgress >= MAX_EXPEDITION_DAYS_LENGTH)
            {
                final int chance = random.nextInt(100);
                if (chance <= 2)
                {
                    getExpedition().setStatus(ExpeditionStatus.LOST);
                }
                else
                {
                    getExpedition().setStatus(ExpeditionStatus.RETURNED);
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_EXPEDITION_TYPE, expeditionType.getId().toString());
        compound.putInt(TAG_DAYS_IN_PROGRESS, daysInProgress);
        compound.put(TAG_INVENTORY, inventory.serializeNBT());

        if (remainingItems != null)
        {
            compound.put(TAG_REMAINING_ITEMS, remainingItems.stream()
                                                .map(IForgeItemStack::serializeNBT)
                                                .collect(NBTUtils.toListNBT()));
        }
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(new ResourceLocation(compoundTag.getString(TAG_EXPEDITION_TYPE)));
        daysInProgress = compoundTag.getInt(TAG_DAYS_IN_PROGRESS);
        inventory.deserializeNBT(compoundTag.getCompound(TAG_INVENTORY));

        if (compoundTag.contains(TAG_REMAINING_ITEMS))
        {
            remainingItems = NBTUtils.streamCompound(compoundTag.getList(TAG_REMAINING_ITEMS, Tag.TAG_COMPOUND))
                               .map(ItemStack::of)
                               .collect(Collectors.toCollection(ArrayDeque::new));
        }
    }
}
