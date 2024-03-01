package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_INVENTORY;

/**
 * Event class for simulating colony expeditions.
 */
public class ColonyExpeditionEvent extends AbstractExpeditionEvent
{
    /**
     * The event ID.
     */
    public static final ResourceLocation COLONY_EXPEDITION_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "colony_expedition");

    /**
     * NBT tags.
     */
    private static final String TAG_EXPEDITION_ID              = "expeditionId";
    private static final String TAG_EXPEDITION_TYPE            = "expeditionType";
    private static final String TAG_REMAINING_ITEMS            = "remainingItems";
    private static final String TAG_END_TIME                   = "endTime";
    private static final String TAG_FLAG_MINIMUM_TIME_ELAPSED  = "flagMinTimeElapsed";
    private static final String TAG_FLAG_REMAINING_ITEMS_EMPTY = "flagRemainingItemsEmpty";

    /**
     * The size of the expedition inventory.
     */
    private static final int EXPEDITION_INVENTORY_SIZE = 27;

    /**
     * Random instance.
     */
    private static final Random random = new Random();

    /**
     * The inventory for the expedition.
     */
    private final ItemStackHandler inventory = new ItemStackHandler(EXPEDITION_INVENTORY_SIZE);

    /**
     * The id of this expedition.
     */
    private int expeditionId;

    /**
     * The expedition type for this colony expedition.
     */
    private ResourceLocation expeditionTypeId;

    /**
     * Contains a set of items that still have yet to be found.
     */
    private Deque<ItemStack> remainingItems = new ArrayDeque<>();

    /**
     * The minimum time that the expedition is able to end at.
     */
    private long endTime = -1;

    /**
     * Whether the timeout has passed.
     */
    private boolean isMinimumTimeElapsed = false;

    /**
     * Whether the remaining items list was emptied out.
     */
    private boolean isRemainingItemsEmpty = false;

    /**
     * The cached expedition instance.
     */
    private ColonyExpedition cachedExpedition;

    /**
     * Create a new colony expedition event.
     *
     * @param colony         the colony instance.
     * @param expeditionType the expedition type.
     * @param expedition     the expedition instance.
     */
    public ColonyExpeditionEvent(final IColony colony, final ColonyExpeditionType expeditionType, final ColonyExpedition expedition)
    {
        super(colony);
        id = colony.getEventManager().getAndTakeNextEventID();
        expeditionId = expedition.getId();
        expeditionTypeId = expeditionType.getId();

        // Move the equipment into the temporary event storage for inventory simulation.
        expedition.getEquipment().forEach(f -> InventoryUtils.addItemStackToItemHandler(inventory, f));

        final LootParams lootParams = new Builder((ServerLevel) colony.getWorld())
                                        .withLuck(expeditionType.getDifficulty().getLuckLevel())
                                        .create(LootContextParamSet.builder().build());

        final LootTable lootTable = getColony().getWorld().getServer().getLootData().getLootTable(expeditionType.getLootTable());

        // Copy the items, natively a Stack implementation, to a deque, so we can pop the first item off each colony tick.
        remainingItems = new ArrayDeque<>(lootTable.getRandomItems(lootParams));
    }

    /**
     * Create a new colony expedition event.
     *
     * @param colony the colony instance.
     */
    private ColonyExpeditionEvent(final IColony colony)
    {
        super(colony);
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

    @Override
    public IExpedition getExpedition()
    {
        if (cachedExpedition == null)
        {
            cachedExpedition = colony.getExpeditionManager().getExpedition(expeditionId);
        }
        return cachedExpedition;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        final Level world = getColony().getWorld();
        if (!world.isClientSide)
        {
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionTypeId);
            if (expeditionType == null)
            {
                Log.getLogger().warn("Expedition cannot start because of a missing expedition type: '{}'", expeditionTypeId);
                return;
            }

            final int randomTimeOffset = expeditionType.getDifficulty().getRandomTime();
            final int randomTime = random.nextInt(-randomTimeOffset, randomTimeOffset);

            endTime = world.getGameTime() + 1; //expeditionType.getDifficulty().getBaseTime() + randomTime;
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putInt(TAG_EXPEDITION_ID, expeditionId);
        compound.putString(TAG_EXPEDITION_TYPE, expeditionTypeId.toString());
        compound.put(TAG_INVENTORY, inventory.serializeNBT());
        compound.put(TAG_REMAINING_ITEMS, remainingItems.stream()
                                            .map(IForgeItemStack::serializeNBT)
                                            .collect(NBTUtils.toListNBT()));
        compound.putLong(TAG_END_TIME, endTime);
        compound.putBoolean(TAG_FLAG_MINIMUM_TIME_ELAPSED, isMinimumTimeElapsed);
        compound.putBoolean(TAG_FLAG_REMAINING_ITEMS_EMPTY, isRemainingItemsEmpty);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        expeditionId = compoundTag.getInt(TAG_EXPEDITION_ID);
        expeditionTypeId = new ResourceLocation(compoundTag.getString(TAG_EXPEDITION_TYPE));
        inventory.deserializeNBT(compoundTag.getCompound(TAG_INVENTORY));
        remainingItems = NBTUtils.streamCompound(compoundTag.getList(TAG_REMAINING_ITEMS, Tag.TAG_COMPOUND))
                           .map(ItemStack::of)
                           .collect(Collectors.toCollection(ArrayDeque::new));
        endTime = compoundTag.getLong(TAG_END_TIME);
        isMinimumTimeElapsed = compoundTag.getBoolean(TAG_FLAG_MINIMUM_TIME_ELAPSED);
        isRemainingItemsEmpty = compoundTag.getBoolean(TAG_FLAG_REMAINING_ITEMS_EMPTY);
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
        // We have to continuously check if the minimum end time of the expedition has already passed
        if (!isMinimumTimeElapsed && endTime < getColony().getWorld().getGameTime())
        {
            isMinimumTimeElapsed = true;
        }

        // If the minimum time has passed and the loot table is empty, we can finish the expedition
        if (isMinimumTimeElapsed && isRemainingItemsEmpty)
        {
            if (getExpedition().getActiveMembers().isEmpty())
            {
                getExpedition().setStatus(ExpeditionStatus.KILLED);
                return;
            }

            final int chance = random.nextInt(100);
            if (chance <= 2)
            {
                getExpedition().setStatus(ExpeditionStatus.LOST);
            }
            else
            {
                getExpedition().setStatus(ExpeditionStatus.RETURNED);
            }
            return;
        }

        // If the deque is empty, we can set the flag for loot table empty to be done.
        if (remainingItems == null || remainingItems.isEmpty())
        {
            isRemainingItemsEmpty = true;
            return;
        }

        // Process the next item in the loot table deque.
        final ItemStack nextItem = remainingItems.getFirst();
        if (nextItem.equals(ItemStack.EMPTY))
        {
            return;
        }

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
    public void onFinish()
    {
        colony.getExpeditionManager().finishExpedition(expeditionId);

        // Remove all members to the travelling manager and re-spawn them.
        for (final IExpeditionMember member : getExpedition().getActiveMembers())
        {
            colony.getTravelingManager().finishTravellingFor(member.getId());
        }
    }
}
