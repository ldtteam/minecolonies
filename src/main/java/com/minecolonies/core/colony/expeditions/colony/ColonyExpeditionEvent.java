package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatusType;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.AbstractExpeditionEvent;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.interactionhandling.ExpeditionFinishedInteraction;
import com.minecolonies.core.items.ItemAdventureToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_FINISH_MESSAGE;
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_STAGE_WILDERNESS;
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
    private static final String TAG_EXPEDITION_ID   = "expeditionId";
    private static final String TAG_REMAINING_ITEMS = "remainingItems";
    private static final String TAG_END_TIME        = "endTime";

    /**
     * Attribute modifier for mob damage.
     */
    private static final String MODIFIER_MOB_DAMAGE_DIFFICULTY = "ExpeditionDamageMultiplier";

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
     * Map that retains the entities in combat.
     */
    private final Map<Integer, AbstractEntityCitizen> combatEntities = new HashMap<>();

    /**
     * The id of this expedition.
     */
    private int expeditionId;

    /**
     * Contains a set of items that still have yet to be found.
     */
    @NotNull
    private Deque<ItemStack> remainingItems = new ArrayDeque<>();

    /**
     * The minimum time that the expedition is able to end at.
     */
    private long endTime = -1;

    /**
     * The cached expedition instance.
     */
    private ColonyExpedition cachedExpedition;

    /**
     * Create a new colony expedition event.
     *
     * @param colony     the colony instance.
     * @param expedition the expedition instance.
     */
    public ColonyExpeditionEvent(final IColony colony, final ColonyExpedition expedition)
    {
        super(colony);
        id = colony.getEventManager().getAndTakeNextEventID();
        expeditionId = expedition.getId();
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
    public ColonyExpedition getExpedition()
    {
        if (cachedExpedition == null)
        {
            cachedExpedition = colony.getExpeditionManager().getExpedition(expeditionId);
        }
        return cachedExpedition;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putInt(TAG_EXPEDITION_ID, expeditionId);
        compound.put(TAG_INVENTORY, inventory.serializeNBT());
        compound.put(TAG_REMAINING_ITEMS, remainingItems.stream()
                                            .map(IForgeItemStack::serializeNBT)
                                            .collect(NBTUtils.toListNBT()));
        compound.putLong(TAG_END_TIME, endTime);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        expeditionId = compoundTag.getInt(TAG_EXPEDITION_ID);
        inventory.deserializeNBT(compoundTag.getCompound(TAG_INVENTORY));
        remainingItems = NBTUtils.streamCompound(compoundTag.getList(TAG_REMAINING_ITEMS, Tag.TAG_COMPOUND))
                           .map(ItemStack::of)
                           .collect(Collectors.toCollection(ArrayDeque::new));
        endTime = compoundTag.getLong(TAG_END_TIME);
    }

    /**
     * Simulates an entire mob fighting process.
     * This is essentially a turn based combat system where each guard rotates attacks.
     *
     * @param entityType the entity type to fight.
     */
    private void processMobFight(final @NotNull EntityType<?> entityType)
    {
        //livingEntity.hurt(colony.getWorld().damageSources().mobAttack());
        //
        //if (livingEntity.isDeadOrDying())
        //{
        //    livingEntity.getExperienceReward();
        //}

        final Entity rawEntity = entityType.create(colony.getWorld());
        if (rawEntity instanceof Mob mob)
        {
            final ColonyExpedition expedition = getExpedition();
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
            if (expeditionType == null)
            {
                return;
            }

            // We don't bother handling mobs that cannot be attacked/are invulnerable, because colonists will always lose in that case (because target mob won't take damage).
            if (!mob.isAttackable()
                  || mob.isInvulnerable()
                  || mob.isInvulnerableTo(colony.getWorld().damageSources().source(DamageTypes.MOB_ATTACK)))
            {
                return;
            }

            // Check if the mob can deal damage, before we attempt to damage it (prevent unfair one-way attack).
            final AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage == null)
            {
                return;
            }

            damage.addTransientModifier(new AttributeModifier(MODIFIER_MOB_DAMAGE_DIFFICULTY, expeditionType.getDifficulty().getMobDamageMultiplier(), Operation.MULTIPLY_TOTAL));

            // Keep the fight going as long as the mob is not dead.
            while (!mob.isDeadOrDying())
            {
                final AbstractEntityCitizen attacker = getNextAttacker(expedition);
                if (attacker == null)
                {
                    break;
                }

                attacker.doHurtTarget(mob);
                mob.doHurtTarget(attacker);
            }

            if (mob.isDeadOrDying())
            {
                expedition.mobKilled(entityType);
            }

            // When everyone is dead, the expedition is over.
            // Expedition results are however not cleared, in case we decide to add recovery missions, they're just not shown in the GUI.
            if (expedition.getActiveMembers().isEmpty())
            {
                remainingItems.clear();
                getExpedition().setStatus(ExpeditionStatus.KILLED);
            }
        }
    }

    /**
     * Get the next attacker to engage in a mob fight.
     *
     * @param expedition the expedition instance.
     * @return the next member to attack, or null if no one is capable of fighting.
     */
    private AbstractEntityCitizen getNextAttacker(final ColonyExpedition expedition)
    {
        final List<IExpeditionMember<?>> activeMembers = expedition.getActiveMembers();
        if (activeMembers.isEmpty())
        {
            return null;
        }

        IExpeditionMember<?> selected = null;
        for (final IExpeditionMember<?> member : activeMembers)
        {
            if (member instanceof ExpeditionCitizenMember && (selected == null || member.getHealth() > selected.getHealth()))
            {
                selected = member;
            }
        }

        final ExpeditionVisitorMember leader = expedition.getLeader();
        if (selected == null && !leader.isDead())
        {
            selected = leader;
        }

        if (selected != null)
        {
            final ICivilianData civilianData = selected.resolveCivilianData(colony);

            if (civilianData instanceof IVisitorData visitorData)
            {
                return visitorData.getEntity().orElseGet(() -> {
                    final AbstractEntityVisitor visitor = ModEntities.VISITOR.create(colony.getWorld());
                    visitor.setCivilianData(visitorData);
                    visitorData.setEntity(visitor);
                    return visitor;
                });
            }
            else if (civilianData instanceof ICitizenData citizenData)
            {
                return citizenData.getEntity().orElseGet(() -> {
                    final AbstractEntityCitizen citizen = ModEntities.CITIZEN.create(colony.getWorld());
                    citizen.setCivilianData(citizenData);
                    citizenData.setEntity(citizen);
                    return citizen;
                });
            }
        }

        return null;
    }

    /**
     * Processes all the special adventure tokens part of the loot table for special actions.
     *
     * @param itemStack the item stack.
     */
    private void processAdventureToken(final ItemStack itemStack)
    {
        final CompoundTag compound = itemStack.getTag();
        if (compound == null)
        {
            return;
        }

        final String type = compound.getString("type");
        switch (type)
        {
            case "mob":
            {
                final Optional<EntityType<?>> entityType = EntityType.byString(compound.getString("entity-type"));
                if (entityType.isEmpty())
                {
                    Log.getLogger().warn("Expedition loot table referred to entity type {} which does not exist.", () -> compound.getString("entity-type"));
                    break;
                }

                processMobFight(entityType.get());

                break;
            }
            case "structure_start":
            {
                final String structure = compound.getString("structure");
                getExpedition().advanceStage(Component.translatable(""));

                if (structure.equals("stronghold"))
                {
                    colony.getExpeditionManager().unlockEnd();
                }
                else if (structure.equals("ruined_portal"))
                {
                    colony.getExpeditionManager().unlockNether();
                }
                break;
            }
            case "structure_end":
            {
                getExpedition().advanceStage(Component.translatable(EXPEDITION_STAGE_WILDERNESS));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public EventStatus getStatus()
    {
        if (endTime == -1)
        {
            return EventStatus.STARTING;
        }

        if (endTime < getColony().getWorld().getGameTime() && remainingItems.isEmpty())
        {
            return EventStatus.DONE;
        }

        return EventStatus.PROGRESSING;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return COLONY_EXPEDITION_EVENT_TYPE_ID;
    }

    @Override
    public void onUpdate()
    {
        // If the deque is empty, we can set the flag for loot table empty to be done.
        if (!remainingItems.isEmpty())
        {
            processLootTableEntry();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        final Level world = getColony().getWorld();
        if (!world.isClientSide)
        {
            final ColonyExpedition expedition = getExpedition();
            expedition.setStatus(ExpeditionStatus.ONGOING);

            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
            if (expeditionType == null)
            {
                Log.getLogger().warn("Expedition cannot start because of a missing expedition type: '{}'", expedition.getExpeditionTypeId());
                return;
            }

            // Calculate the end time
            final int randomTimeOffset = expeditionType.getDifficulty().getRandomTime();
            final int randomTime = random.nextInt(-randomTimeOffset, randomTimeOffset);

            endTime = world.getGameTime() + 1; // TODO: expeditionType.getDifficulty().getBaseTime() + randomTime;

            // Move the equipment into the temporary event storage for inventory simulation.
            expedition.getEquipment().forEach(f -> InventoryUtils.addItemStackToItemHandler(inventory, f));

            // Generate the loot table
            final LootParams lootParams = new Builder((ServerLevel) colony.getWorld())
                                            .withLuck(expeditionType.getDifficulty().getLuckLevel())
                                            .create(LootContextParamSet.builder().build());

            final LootTable lootTable = getColony().getWorld().getServer().getLootData().getLootTable(expeditionType.getLootTable());
            remainingItems = new ArrayDeque<>(lootTable.getRandomItems(lootParams));
        }
    }

    @Override
    public void onFinish()
    {
        final ColonyExpedition expedition = getExpedition();

        // If no explicit status was selected yet, determine the status.
        if (expedition.getStatus().getStatusType().equals(ExpeditionStatusType.ONGOING))
        {
            if (expedition.getActiveMembers().isEmpty())
            {
                expedition.setStatus(ExpeditionStatus.KILLED);
                return;
            }

            final int chance = random.nextInt(100);
            if (chance <= 2)
            {
                expedition.setStatus(ExpeditionStatus.LOST);
            }
            else
            {
                expedition.setStatus(ExpeditionStatus.RETURNED);
            }
            return;
        }

        colony.getExpeditionManager().finishExpedition(expeditionId);

        MessageUtils.format(EXPEDITION_FINISH_MESSAGE, expedition.getLeader().getName())
          .withPriority(MessagePriority.IMPORTANT)
          .sendTo(colony)
          .forManagers();

        // Remove all members to the travelling manager and re-spawn them.
        for (final IExpeditionMember<?> member : expedition.getMembers())
        {
            colony.getTravelingManager().finishTravellingFor(member.getId());

            if (member.isDead())
            {
                member.removeFromColony(colony);
            }
            else
            {
                if (member instanceof ExpeditionVisitorMember visitorMember)
                {
                    final IVisitorData leaderData = visitorMember.resolveCivilianData(colony);
                    leaderData.triggerInteraction(new ExpeditionFinishedInteraction(expedition));
                }
            }
        }
    }

    /**
     * Processes the next item from the loot table list.
     */
    private void processLootTableEntry()
    {
        // Process the next item in the loot table deque.
        final ItemStack nextItem = remainingItems.getFirst();
        if (nextItem.equals(ItemStack.EMPTY))
        {
            return;
        }

        // Check if there's food left in an attempt to heal members.
        if (!attemptHealMembers())
        {
            remainingItems.clear();
            return;
        }

        if (nextItem.getItem() instanceof ItemAdventureToken)
        {
            processAdventureToken(nextItem);
        }
        else
        {
            getExpedition().rewardFound(nextItem);
        }
    }

    /**
     * Attempt a single heal attempt on all members.
     *
     * @return whether there is food left and the expedition may continue.
     */
    private boolean attemptHealMembers()
    {
        for (final IExpeditionMember<?> member : getExpedition().getActiveMembers())
        {
            if (member.getHealth() < member.getMaxHealth())
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(inventory, ItemStackUtils.ISFOOD);
                if (slot >= 0)
                {
                    final FoodProperties foodStack = inventory.getStackInSlot(slot).getFoodProperties(null);
                    member.heal(colony, foodStack.getNutrition());
                }
                else
                {
                    return false;
                }
            }
        }

        return true;
    }
}
