package com.minecolonies.core.colony.events;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.expeditions.ExpeditionFinishedStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionFinishedStatusType;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounter;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounterManager;
import com.minecolonies.core.items.ItemAdventureToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_INVENTORY;
import static com.minecolonies.core.generation.ExpeditionResourceManager.getStructureId;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionStructureLootProvider.RUINED_PORTAL_ID;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionStructureLootProvider.STRONGHOLD_ID;

/**
 * Event class for simulating colony expeditions.
 */
public class ColonyExpeditionEvent implements IColonyEvent
{
    /**
     * Tags for the adventure tokens.
     */
    public static final String TOKEN_TAG_EXPEDITION_TYPE                 = "type";
    public static final String TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START = "structure_start";
    public static final String TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END   = "structure_end";
    public static final String TOKEN_TAG_EXPEDITION_STRUCTURE            = "structure";
    public static final String TOKEN_TAG_EXPEDITION_TYPE_ENCOUNTER       = "encounter";
    public static final String TOKEN_TAG_EXPEDITION_ENCOUNTER            = "encounter";
    public static final String TOKEN_TAG_EXPEDITION_ENCOUNTER_AMOUNT     = "amount";
    public static final String TOKEN_TAG_EXPEDITION_ENCOUNTER_SCALE      = "scale";

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
     * The size of the expedition inventory.
     */
    private static final int EXPEDITION_INVENTORY_SIZE = 27;

    /**
     * Usage damage percentages.
     */
    private static final float MIN_PERCENTAGE_USAGE_DAMAGE = 0.05f;
    private static final float MAX_PERCENTAGE_USAGE_DAMAGE = 0.15f;

    /**
     * The id of this event.
     */
    private final int id;

    /**
     * The id of the expedition.
     */
    private final int expeditionId;

    /**
     * The colony this event is for.
     */
    private final IColony colony;

    /**
     * Random instance.
     */
    private final RandomSource random;

    /**
     * The inventory for the expedition.
     */
    private final ItemStackHandler inventory = new ItemStackHandler(EXPEDITION_INVENTORY_SIZE);

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
        this.colony = colony;
        this.random = RandomSource.create();
        this.id = colony.getEventManager().getAndTakeNextEventID();
        this.expeditionId = expedition.getId();
    }

    /**
     * Create a new colony expedition event.
     *
     * @param colony the colony instance.
     */
    private ColonyExpeditionEvent(final int id, final int expeditionId, final IColony colony)
    {
        this.id = id;
        this.expeditionId = expeditionId;
        this.colony = colony;
        this.random = RandomSource.create();
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
        final int id = compound.getInt(TAG_ID);
        final int expeditionId = compound.getInt(TAG_EXPEDITION_ID);
        return new ColonyExpeditionEvent(id, expeditionId, colony);
    }

    /**
     * Get the expedition instance for this event.
     *
     * @return the expedition instance.
     */
    public ColonyExpedition getExpedition()
    {
        if (cachedExpedition == null)
        {
            cachedExpedition = colony.getExpeditionManager().getActiveExpedition(expeditionId);
        }
        return cachedExpedition;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_ID, id);
        compound.putInt(TAG_EXPEDITION_ID, expeditionId);
        compound.put(TAG_INVENTORY, inventory.serializeNBT());
        compound.put(TAG_REMAINING_ITEMS, remainingItems.stream().map(IForgeItemStack::serializeNBT).collect(NBTUtils.toListNBT()));
        compound.putLong(TAG_END_TIME, endTime);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        inventory.deserializeNBT(compoundTag.getCompound(TAG_INVENTORY));
        remainingItems = NBTUtils.streamCompound(compoundTag.getList(TAG_REMAINING_ITEMS, Tag.TAG_COMPOUND)).map(ItemStack::of).collect(Collectors.toCollection(ArrayDeque::new));
        endTime = compoundTag.getLong(TAG_END_TIME);
    }

    /**
     * Simulates an entire mob fighting process.
     * This is essentially a turn based combat system where each guard rotates attacks.
     *
     * @param encounter          the encounter to fight.
     * @param encounterAmount    a number for the mob encounter amount.
     * @param scaleEncounterSize whether the difficulty should scale the size of the amount of mobs.
     */
    private void processMobFight(final @NotNull ExpeditionEncounter encounter, final int encounterAmount, final boolean scaleEncounterSize)
    {
        final ColonyExpedition expedition = getExpedition();
        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
        if (expeditionType == null)
        {
            return;
        }

        // Determine the amount of mobs we're going to fight
        int amount = encounterAmount;
        if (scaleEncounterSize)
        {
            amount *= expeditionType.difficulty().getMobEncounterMultiplier();
        }

        // Determine the mob type
        MobType mobType = MobType.UNDEFINED;
        try
        {
            final Entity entity = encounter.getEntityType().create(colony.getWorld());
            if (entity instanceof Mob mob)
            {
                mobType = mob.getMobType();
            }
            entity.remove(RemovalReason.DISCARDED);
        }
        catch (Exception ex)
        {
            Log.getLogger().warn("Failure attempting to spawn", ex);
        }

        for (int i = 0; i < amount; i++)
        {
            double encounterHealth = encounter.getHealth();

            // Keep the fight going as long as the mob is not dead.
            while (encounterHealth > 0)
            {
                final IExpeditionMember<?> attacker = getNextAttacker(expedition);
                if (attacker == null)
                {
                    // When no attackers are available, it means everyone is dead, so the fight cannot continue
                    remainingItems.clear();
                    break;
                }

                final ItemStack weapon = attacker.getPrimaryWeapon();
                encounterHealth -= CombatRules.getDamageAfterAbsorb(getWeaponDamage(weapon, mobType), encounter.getArmor(), 0);
                if (weapon.hurt(1, random, null))
                {
                    attacker.setPrimaryWeapon(ItemStack.EMPTY);
                }

                if (encounterHealth > 0)
                {
                    final float damageAmount = handleDamageReduction(attacker, encounter.getDamage());
                    attacker.setHealth(Math.max(0, attacker.getHealth() - damageAmount));
                    encounterHealth -= encounter.getReflectingDamage();

                    if (attacker.isDead())
                    {
                        getExpedition().memberLost(attacker);
                    }
                }
            }

            if (encounterHealth <= 0)
            {
                expedition.mobKilled(encounter.getId());
                final List<ItemStack> loot = processLootTable(encounter.getLootTable(), expeditionType);
                loot.forEach(expedition::rewardFound);
            }
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

        final String type = compound.getString(TOKEN_TAG_EXPEDITION_TYPE);
        switch (type)
        {
            case TOKEN_TAG_EXPEDITION_TYPE_ENCOUNTER:
            {
                final String encounterId = compound.getString(TOKEN_TAG_EXPEDITION_ENCOUNTER);
                final ExpeditionEncounter encounter = ExpeditionEncounterManager.getInstance().getEncounter(new ResourceLocation(encounterId));
                if (encounter == null)
                {
                    Log.getLogger().warn("Expedition loot table referred to encounter '{}' which does not exist.", encounterId);
                    break;
                }

                final int amount = compound.contains(TOKEN_TAG_EXPEDITION_ENCOUNTER_AMOUNT) ? compound.getInt(TOKEN_TAG_EXPEDITION_ENCOUNTER_AMOUNT) : 1;
                final boolean scaleAmount = !compound.contains(TOKEN_TAG_EXPEDITION_ENCOUNTER_SCALE) || compound.getBoolean(TOKEN_TAG_EXPEDITION_ENCOUNTER_SCALE);

                processMobFight(encounter, amount, scaleAmount);
                break;
            }
            case TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START:
            {
                final ResourceLocation structureId = new ResourceLocation(compound.getString(TOKEN_TAG_EXPEDITION_STRUCTURE));
                final Optional<StructureType<?>> structureType = BuiltInRegistries.STRUCTURE_TYPE.getOptional(structureId);
                if (structureType.isEmpty())
                {
                    Log.getLogger().warn("Expedition loot table referred to structure type '{}' which does not exist.", structureType);
                    break;
                }

                getExpedition().advanceStage(Component.translatable(EXPEDITION_STAGE_STRUCTURE + structureId));

                if (structureId.equals(getStructureId(RUINED_PORTAL_ID)))
                {
                    colony.getExpeditionManager().unlockNether();
                }
                else if (structureId.equals(getStructureId(STRONGHOLD_ID)))
                {
                    colony.getExpeditionManager().unlockEnd();
                }
                break;
            }
            case TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END:
            {
                getExpedition().advanceStage(Component.translatable(EXPEDITION_STAGE_WILDERNESS));
                break;
            }
            default:
                Log.getLogger().warn("Adventure token with type '{}' found. This adventure token is not implemented to do anything, skipping.", type);
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
    public void setStatus(final EventStatus status)
    {
        // No-op, expedition status uses a different enumeration to control active status, which can only be modified directly within this event.
    }

    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return COLONY_EXPEDITION_EVENT_TYPE_ID;
    }

    @Override
    public void setColony(final @NotNull IColony colony)
    {
        // No-op
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
        final Level world = colony.getWorld();
        if (!world.isClientSide)
        {
            final ColonyExpedition expedition = getExpedition();
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
            if (expeditionType == null)
            {
                Log.getLogger().warn("Expedition cannot start because of a missing expedition type: '{}'", expedition.getExpeditionTypeId());
                return;
            }

            // Calculate the end time
            final int randomTimeOffset = expeditionType.difficulty().getRandomTime();
            final int randomTime = random.nextInt(-randomTimeOffset, randomTimeOffset);

            endTime = world.getGameTime() + 1; // TODO: expeditionType.getDifficulty().getBaseTime() + randomTime;

            // Move the equipment into the temporary event storage for inventory simulation.
            expedition.getEquipment().forEach(f -> InventoryUtils.addItemStackToItemHandler(inventory, f));

            // Generate the loot table
            remainingItems = new ArrayDeque<>(processLootTable(expeditionType.lootTable(), expeditionType));
        }
    }

    @Override
    public void onFinish()
    {
        final ColonyExpedition expedition = getExpedition();
        expedition.cleanStages();

        ExpeditionFinishedStatus finishedStatus = ExpeditionFinishedStatus.RETURNED;
        if (expedition.getActiveMembers().isEmpty())
        {
            finishedStatus = ExpeditionFinishedStatus.KILLED;
        }
        else
        {
            final int chance = random.nextInt(100);
            if (chance <= 2)
            {
                finishedStatus = ExpeditionFinishedStatus.LOST;
            }
        }

        if (!colony.getExpeditionManager().finishExpedition(expeditionId, finishedStatus))
        {
            Log.getLogger().warn(String.format("Expedition with id %d could not be found after finishing.", expeditionId));
        }

        if (finishedStatus.getStatusType().equals(ExpeditionFinishedStatusType.SUCCESSFUL))
        {
            MessageUtils.format(EXPEDITION_FINISH_MESSAGE, expedition.getLeader().getName()).withPriority(MessagePriority.IMPORTANT).sendTo(colony).forManagers();
        }
        else
        {
            MessageUtils.format(EXPEDITION_FAILURE_MESSAGE, expedition.getLeader().getName()).withPriority(MessagePriority.DANGER).sendTo(colony).forManagers();
        }

        // Remove all members to the travelling manager and respawn them.
        for (final IExpeditionMember<?> member : expedition.getMembers())
        {
            colony.getTravelingManager().finishTravellingFor(member.getId());

            if (member.isDead())
            {
                member.removeFromColony(colony);
            }

            // Apply usage damage to all armor of all members.
            final ArmorList armor = getArmor(member);
            damageArmor(armor,
              armor.getTotalArmor() * Mth.randomBetween(random, MIN_PERCENTAGE_USAGE_DAMAGE, MAX_PERCENTAGE_USAGE_DAMAGE),
              slot -> member.setArmor(slot, ItemStack.EMPTY));
        }

        // Add all the loot to the leader inventory
        final IVisitorData leaderData = expedition.getLeader().resolveCivilianData(colony);
        if (leaderData != null)
        {
            InventoryUtils.clearItemHandler(leaderData.getInventory());
            InventoryUtils.transferAllItemHandler(inventory, leaderData.getInventory());
        }
    }

    /**
     * Processes the next item from the loot table list.
     */
    private void processLootTableEntry()
    {
        // Process the next item in the loot table deque.
        final ItemStack nextItem = remainingItems.pop();
        if (nextItem.isEmpty())
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
     * @param damage           the amount of incoming damage.
     * @return the calculated damage after absorption.
     */
    private float handleDamageReduction(final @NotNull IExpeditionMember<?> expeditionMember, final float damage)
    {
        final Map<EquipmentSlot, Tuple<ItemStack, ArmorItem>> armor = new HashMap<>();
        armor.computeIfAbsent(EquipmentSlot.HEAD, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.CHEST, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.LEGS, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.FEET, getArmorPiece(expeditionMember));

        final int armorPieces = armor.size();
        if (armorPieces > 0)
        {
            float finalDamage = damage;
            for (final Map.Entry<EquipmentSlot, Tuple<ItemStack, ArmorItem>> entry : armor.entrySet())
            {
                final ArmorItem armorItem = entry.getValue().getB();
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }

            damageArmor(getArmor(expeditionMember), finalDamage, slot -> expeditionMember.setArmor(slot, ItemStack.EMPTY));
            return finalDamage;
        }

        return damage;
    }

    /**
     * Get the list of armor a member is wearing.
     *
     * @param expeditionMember the member.
     * @return the armor list.
     */
    private ArmorList getArmor(final @NotNull IExpeditionMember<?> expeditionMember)
    {
        final ArmorList armor = new ArmorList();
        armor.computeIfAbsent(EquipmentSlot.HEAD, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.CHEST, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.LEGS, getArmorPiece(expeditionMember));
        armor.computeIfAbsent(EquipmentSlot.FEET, getArmorPiece(expeditionMember));
        return armor;
    }

    /**
     * Damage all armor in an armor list.
     *
     * @param list    the armor list.
     * @param damage  the amount of damage dealt.
     * @param onBreak function called when an armor slot breaks.
     */
    private void damageArmor(final @NotNull ArmorList list, final float damage, final Consumer<EquipmentSlot> onBreak)
    {
        final int armorPieces = list.size();
        final float dividedDamage = damage / armorPieces;
        for (final Map.Entry<EquipmentSlot, Tuple<ItemStack, ArmorItem>> entry : list.entrySet())
        {
            if (entry.getValue().getA().hurt(Math.round(damage / dividedDamage), random, null))
            {
                onBreak.accept(entry.getKey());
            }
        }
    }

    /**
     * Extract armor piece from a member.
     *
     * @param member the member instance.
     * @return the lambda function to provide to the armor map.
     */
    private Function<EquipmentSlot, Tuple<ItemStack, ArmorItem>> getArmorPiece(final IExpeditionMember<?> member)
    {
        return (slot) -> {
            final ItemStack armor = member.getArmor(slot);
            if (armor.getItem() instanceof ArmorItem armorItem)
            {
                return new Tuple<>(armor, armorItem);
            }
            return null;
        };
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
        final LootParams lootParams = new Builder((ServerLevel) colony.getWorld())
                                        .withParameter(ModLootConditions.EXPEDITION_DIFFICULTY_PARAM, expeditionType.difficulty())
                                        .create(ModLootConditions.EXPEDITION_PARAMS);

        return colony.getWorld().getServer().getLootData().getLootTable(lootTableId).getRandomItems(lootParams);
    }

    /**
     * Wrapper for a combination of all armor.
     */
    private static class ArmorList extends HashMap<EquipmentSlot, Tuple<ItemStack, ArmorItem>>
    {
        /**
         * Get the total armor level.
         *
         * @return the armor level.
         */
        public int getTotalArmor()
        {
            int armor = 0;
            for (Tuple<ItemStack, ArmorItem> entry : this.values())
            {
                armor += entry.getB().getDefense();
            }
            return armor;
        }
    }
}
