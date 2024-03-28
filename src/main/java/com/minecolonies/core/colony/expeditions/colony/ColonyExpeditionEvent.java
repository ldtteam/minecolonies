package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatusType;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
        compound.put(TAG_REMAINING_ITEMS, remainingItems.stream().map(IForgeItemStack::serializeNBT).collect(NBTUtils.toListNBT()));
        compound.putLong(TAG_END_TIME, endTime);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        expeditionId = compoundTag.getInt(TAG_EXPEDITION_ID);
        inventory.deserializeNBT(compoundTag.getCompound(TAG_INVENTORY));
        remainingItems = NBTUtils.streamCompound(compoundTag.getList(TAG_REMAINING_ITEMS, Tag.TAG_COMPOUND)).map(ItemStack::of).collect(Collectors.toCollection(ArrayDeque::new));
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
            if (!mob.isAttackable() || mob.isInvulnerable() || mob.isInvulnerableTo(colony.getWorld().damageSources().source(DamageTypes.MOB_ATTACK)))
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
                final IExpeditionMember<?> attacker = getNextAttacker(expedition);
                if (attacker == null)
                {
                    // When no attackers are available, it means everyone is dead, so the fight cannot continue
                    remainingItems.clear();
                    getExpedition().setStatus(ExpeditionStatus.KILLED);
                    break;
                }

                final ItemStack weapon = attacker.getPrimaryWeapon();
                mob.hurt(colony.getWorld().damageSources().mobAttack(mob), getWeaponDamage(weapon, mob.getMobType()));
                weapon.hurtAndBreak(1, mob, (m) -> attacker.setPrimaryWeapon(ItemStack.EMPTY));

                if (!mob.isDeadOrDying())
                {
                    final float damageAmount = handleDamageReduction(attacker, colony.getWorld().damageSources().mobAttack(mob), (float) damage.getValue());
                    attacker.setHealth(Math.min(0, attacker.getHealth() - damageAmount));
                }
            }

            if (mob.isDeadOrDying())
            {
                expedition.mobKilled(entityType);
                processLootTable(mob.getLootTable(), expeditionType).forEach(expedition::rewardFound);
            }

            mob.remove(RemovalReason.DISCARDED);
        }
    }

    /**
     * Get the next attacker to engage in a mob fight.
     *
     * @param expedition the expedition instance.
     * @return the next member to attack, or null if no one is capable of fighting.
     */
    private IExpeditionMember<?> getNextAttacker(final ColonyExpedition expedition)
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

        return selected;
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

        if (endTime < colony.getWorld().getGameTime() && remainingItems.isEmpty())
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
        final Level world = colony.getWorld();
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
            remainingItems = new ArrayDeque<>(processLootTable(expeditionType.getLootTable(), expeditionType));
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

        MessageUtils.format(EXPEDITION_FINISH_MESSAGE, expedition.getLeader().getName()).withPriority(MessagePriority.IMPORTANT).sendTo(colony).forManagers();

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
                    member.setHealth(Mth.clamp(member.getHealth() + foodStack.getNutrition(), 0, member.getMaxHealth()));
                }
                else
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the damage the given weapon item stack will do.
     *
     * @param itemStack the item stack.
     * @param target    the kind of mob they will deal damage to.
     * @return the damage value.
     */
    private float getWeaponDamage(final ItemStack itemStack, final MobType target)
    {
        if (ItemStackUtils.isTool(itemStack, ToolType.SWORD))
        {
            if (Compatibility.isTinkersWeapon(itemStack))
            {
                return Math.min(1, (float) Compatibility.getAttackDamage(itemStack));
            }

            if (itemStack.getItem() instanceof SwordItem swordItem)
            {
                return swordItem.getDamage() + EnchantmentHelper.getDamageBonus(itemStack, target);
            }
        }

        if (ItemStackUtils.isTool(itemStack, ToolType.BOW))
        {
            return 6;
        }

        return 1;
    }

    /**
     * Handler methods for calculating damage reduction based on available armor.
     *
     * @param expeditionMember the expedition member.
     * @param damageSource     the damage source.
     * @param damage           the amount of incoming damage.
     * @return the calculated damage after absorption.
     */
    private float handleDamageReduction(final @NotNull IExpeditionMember<?> expeditionMember, final DamageSource damageSource, final float damage)
    {
        final ItemStack head = expeditionMember.getArmor(EquipmentSlot.HEAD);
        final ItemStack chest = expeditionMember.getArmor(EquipmentSlot.CHEST);
        final ItemStack legs = expeditionMember.getArmor(EquipmentSlot.LEGS);
        final ItemStack feet = expeditionMember.getArmor(EquipmentSlot.FEET);

        final int armorPieces = (head.isEmpty() ? 0 : 1) + (chest.isEmpty() ? 0 : 1) + (legs.isEmpty() ? 0 : 1) + (feet.isEmpty() ? 0 : 1);
        if (armorPieces > 0)
        {
            final float dividedDamage = damage / armorPieces;

            float finalDamage = damage;
            if (!head.isEmpty() && head.getItem() instanceof ArmorItem armorItem && damageSource.getEntity() != null)
            {
                head.hurtAndBreak(Math.round(dividedDamage), (LivingEntity) damageSource.getEntity(), e -> expeditionMember.setArmor(EquipmentSlot.HEAD, ItemStack.EMPTY));
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }
            if (!chest.isEmpty() && chest.getItem() instanceof ArmorItem armorItem && damageSource.getEntity() != null)
            {
                chest.hurtAndBreak(Math.round(dividedDamage), (LivingEntity) damageSource.getEntity(), e -> expeditionMember.setArmor(EquipmentSlot.CHEST, ItemStack.EMPTY));
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }
            if (!legs.isEmpty() && legs.getItem() instanceof ArmorItem armorItem && damageSource.getEntity() != null)
            {
                legs.hurtAndBreak(Math.round(dividedDamage), (LivingEntity) damageSource.getEntity(), e -> expeditionMember.setArmor(EquipmentSlot.LEGS, ItemStack.EMPTY));
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }
            if (!feet.isEmpty() && feet.getItem() instanceof ArmorItem armorItem && damageSource.getEntity() != null)
            {
                feet.hurtAndBreak(Math.round(dividedDamage), (LivingEntity) damageSource.getEntity(), e -> expeditionMember.setArmor(EquipmentSlot.FEET, ItemStack.EMPTY));
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }
            return finalDamage;
        }

        return damage;
    }

    /**
     * Generate loot table rewards.
     *
     * @param lootTableId    the input loot table id.
     * @param expeditionType the expedition type.
     * @return the list of items.
     */
    private List<ItemStack> processLootTable(final ResourceLocation lootTableId, final ColonyExpeditionType expeditionType)
    {
        final LootParams lootParams =
          new Builder((ServerLevel) colony.getWorld()).withLuck(expeditionType.getDifficulty().getLuckLevel()).create(LootContextParamSet.builder().build());

        final LootTable lootTable = colony.getWorld().getServer().getLootData().getLootTable(lootTableId);
        return lootTable.getRandomItems(lootParams);
    }
}
