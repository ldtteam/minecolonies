package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.coremod.entity.ai.citizen.guard.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 *
 * @param <J>
 */
public abstract class AbstractEntityAIGuard<J extends AbstractJobGuard> extends AbstractEntityAIInteract<J>
{

    /**
     * Tools and Items needed by the worker.
     */
    public final List<ToolType>  toolsNeeded = new ArrayList<>();
    /**
     * List of items that are required by the guard based on building level
     * and guard level.  This array holds a pointer to the building level
     * and then pointer to GuardItemsNeeded
     */
    public final Map<Integer,List<GuardItemsNeeded>> itemsNeeded = new HashMap<>();

    /**
     * Holds a list of required armor for this guard
     */
    private final Map<EntityEquipmentSlot, Item> requiredArmor = new LinkedHashMap<>();

    /**
     * How many more ticks we have until next attack.
     */
    protected int currentAttackDelay = 0;

    /**
     * The current target for our guard.
     */
    protected EntityLivingBase target = null;

    /**
     * The current blockPos we're patrolling at.
     */
    private BlockPos currentPatrolPoint = null;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepare),
          new AITarget(DECIDE, this::decide),
          new AITarget(GUARD_ATTACK_PROTECT, this::attackProtect),
          new AITarget(GUARD_ATTACK_PHYSICAL, this::attackPhysical),
          new AITarget(GUARD_ATTACK_RANGED, this::attackRanged)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getStrength() + worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);

        final List<GuardItemsNeeded> itemlvl1Needed = new ArrayList<>();
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.LEATHER_BOOTS, 1, 2, 4));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE, 1, 2, 4));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.LEATHER_HELMET, 1, 2, 4));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.LEATHER_LEGGINGS, 1, 2, 4));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.GOLDEN_BOOTS, 1, 5, 99));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, 1, 5, 99));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.GOLDEN_HELMET, 1, 5, 99));
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.GOLDEN_LEGGINGS, 1, 5, 99));
        itemsNeeded.put(Integer.valueOf(1), itemlvl1Needed);


        final List<GuardItemsNeeded> itemlvl2Needed = new ArrayList<>();
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.GOLDEN_BOOTS, 1, 1, 4));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, 1, 1, 4));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.GOLDEN_HELMET, 1, 1, 4));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.GOLDEN_LEGGINGS, 1, 1, 4));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.CHAINMAIL_BOOTS, 1, 5, 99));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE, 1, 5, 99));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.CHAINMAIL_HELMET, 1, 5, 99));
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.CHAINMAIL_LEGGINGS, 1, 5, 99));
        itemsNeeded.put(Integer.valueOf(2), itemlvl2Needed);

         
        final List<GuardItemsNeeded> itemlvl3Needed = new ArrayList<>();
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.CHAINMAIL_BOOTS, 1, 1, 8));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE, 1, 1, 8));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.CHAINMAIL_HELMET, 1, 1, 8));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.CHAINMAIL_LEGGINGS, 1, 1, 8));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.IRON_BOOTS, 1, 8, 99));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.IRON_CHESTPLATE, 1, 8, 99));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.IRON_HELMET, 1, 8, 99));
        itemlvl3Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.IRON_LEGGINGS, 1, 8, 99));
        itemsNeeded.put(Integer.valueOf(3), itemlvl3Needed);


        final List<GuardItemsNeeded> itemlvl4Needed = new ArrayList<>();
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.IRON_BOOTS, 1, 1, 16));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.IRON_CHESTPLATE, 1, 1, 16));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.IRON_HELMET, 1, 1, 16));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.IRON_LEGGINGS, 1, 1, 16));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.DIAMOND_BOOTS, 1, 17, 99));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, 1, 17, 99));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.DIAMOND_HELMET, 1, 17, 99));
        itemlvl4Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.DIAMOND_LEGGINGS, 1, 17, 99));
        itemsNeeded.put(Integer.valueOf(4), itemlvl4Needed);

        final List<GuardItemsNeeded> itemlvl5Needed = new ArrayList<>();
        itemlvl5Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.FEET,  Items.DIAMOND_BOOTS, 1, 1, 99));
        itemlvl5Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, 1, 1, 99));
        itemlvl5Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.HEAD,  Items.DIAMOND_HELMET, 1, 1, 99));
        itemlvl5Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.LEGS,  Items.DIAMOND_LEGGINGS, 1, 1, 99));
        itemsNeeded.put(Integer.valueOf(5), itemlvl5Needed);
}

    @Override
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingGuards.class;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the armour have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        updateArmor();
    }

    protected abstract int getAttackRange();

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link AIState}.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the herder for herding
     *
     * @return The next {@link AIState}.
     */
    private AIState prepare()
    {
        setDelay(Constants.TICKS_SECOND * PREPARE_DELAY_SECONDS);

        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
            else if (getOwnBuilding() != null)
            {
                InventoryFunctions.matchFirstInProviderWithSimpleAction(worker,
                  stack -> !ItemStackUtils.isEmpty(stack)
                             && ItemStackUtils.doesItemServeAsWeapon(stack)
                             && ItemStackUtils.hasToolLevel(stack, tool, 0, getOwnBuilding().getMaxToolLevel()),
                  itemStack -> worker.getCitizenItemHandler().setMainHeldItem(itemStack));
            }
        }

        
        @Nullable final AbstractBuildingWorker building = getOwnBuilding();
        if (building != null)
        {
            final List<GuardItemsNeeded> itemList = itemsNeeded.get(Integer.valueOf(building.getBuildingLevel()));
            if (itemList != null)
            {
                final int level = worker.getCitizenData().getLevel();
                for (final GuardItemsNeeded item : itemList)
                {
                	//Could have multiple armor request,  make sure the require armor falls into the guard
                	//level.
                	if (level >= item.getMinLevelRequired() && level <= item.getMaxLevelRequired())
                	{
                		//Save the requested armor item,  so when the guard goes to put it on
                		//they will put on the correct armor.
                		requiredArmor.put(item.getType(), item.getItemNeeded());
                		checkIfRequestForItemExistOrCreateAsynch(item.getItemStackNeeded());
                	}
                }
            }
        }

        if (getOwnBuilding() != null)
        {
            final TileEntityColonyBuilding chest = getOwnBuilding().getTileEntity();
            for (int i = 0; i < getOwnBuilding().getTileEntity().getSizeInventory(); i++)
            {
                final ItemStack stack = chest.getStackInSlot(i);

                if (InventoryUtils.findFirstSlotInProviderWith(chest,
                  itemStack -> itemStack.getItem() instanceof ItemArmor) != -1)
                {
                    InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                      getOwnBuilding(), itemStack -> itemStack.getItem() instanceof ItemArmor,
                      stack.getCount(),
                      new InvWrapper(worker.getInventoryCitizen()));
                }
            }
        }

        return DECIDE;
    }

    /**
     * Decide what we should do next!
     *
     * @return the next AIState.
     */
    protected AIState decide()
    {
        setDelay(Constants.TICKS_SECOND);
        for (final ToolType toolType : toolsNeeded)
        {
            if (getOwnBuilding() != null && !InventoryUtils.hasItemHandlerToolWithLevel(new InvWrapper(getInventory()),
              toolType,
              0,
              getOwnBuilding().getMaxToolLevel()))
            {
                return START_WORKING;
            }
        }

        if (getOwnBuilding(AbstractBuildingGuards.class) != null
              && target == null)
        {
            if (worker.getLastAttackedEntity() != null && !worker.getLastAttackedEntity().isDead)
            {
                if (worker.getDistance(worker.getLastAttackedEntity()) > getAttackRange() * 5 && !worker.canEntityBeSeen(worker.getLastAttackedEntity()))
                {
                    worker.setLastAttackedEntity(null);
                    return START_WORKING;
                }
                target = worker.getLastAttackedEntity();
                return START_WORKING;
            }

            final AbstractBuildingGuards guardBuilding = getOwnBuilding();

            switch (guardBuilding.getTask())
            {
                case PATROL:
                    if (currentPatrolPoint == null)
                    {
                        currentPatrolPoint = guardBuilding.getNextPatrolTarget(null);
                    }
                    if (currentPatrolPoint != null
                          && (worker.isWorkerAtSiteWithMove(currentPatrolPoint, 2) || worker.getCitizenStuckHandler().isStuck()))
                    {
                        currentPatrolPoint = guardBuilding.getNextPatrolTarget(currentPatrolPoint);
                    }
                    break;
                case GUARD:
                    worker.isWorkerAtSiteWithMove(guardBuilding.getGuardPos(), GUARD_POS_RANGE);
                    break;
                case FOLLOW:
                    worker.isWorkerAtSiteWithMove(guardBuilding.getPlayerToFollow(), GUARD_POS_RANGE);
                    break;
                default:
                    worker.isWorkerAtSiteWithMove(worker.getCitizenColonyHandler().getWorkBuilding().getLocation(), GUARD_POS_RANGE);
                    break;
            }
        }

        if (target == null)
        {
            target = getTarget();
        }

        if (target != null && target.isDead)
        {
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            target = null;
        }

        return DECIDE;
    }

    /**
     * Get a target for the guard.
     *
     * @return The next AIState to go to.
     */
    protected EntityLivingBase getTarget()
    {
        final AbstractBuildingGuards building = getOwnBuilding();

        if (building != null && target == null && worker.getCitizenColonyHandler().getColony() != null)
        {
            for (final CitizenData citizen : worker.getCitizenColonyHandler().getColony().getCitizenManager().getCitizens())
            {
                if (citizen.getCitizenEntity().isPresent())
                {
                    final EntityLivingBase entity = citizen.getCitizenEntity().get().getRevengeTarget();
                    if (entity instanceof AbstractEntityBarbarian
                          && worker.canEntityBeSeen(entity))
                    {
                        return entity;
                    }
                }
            }

            final List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, getSearchArea());

            for (final EntityLivingBase entity : targets)
            {
                if (entity instanceof EntityPlayer)
                {
                    final EntityPlayer player = (EntityPlayer) entity;

                    if (worker.getCitizenColonyHandler().getColony() != null
                          && worker.getCitizenColonyHandler().getColony().getPermissions().hasPermission(player, Action.GUARDS_ATTACK)
                          && worker.canEntityBeSeen(player))
                    {
                        return entity;
                    }
                }
            }

            float closest = -1;
            EntityLivingBase targetEntity = null;

            for (final MobEntryView mobEntry : building.getMobsToAttack())
            {

                if (mobEntry.hasAttack())
                {
                    for (final EntityLivingBase entity : targets)
                    {
                        if (mobEntry.getEntityEntry().getEntityClass().isInstance(entity)
                              && worker.canEntityBeSeen(entity)
                              && (worker.getDistance(entity) < closest
                                    || (int) closest == -1))
                        {
                            closest = worker.getDistance(entity);
                            targetEntity = entity;
                        }
                    }

                    if (targetEntity != null)
                    {
                        return targetEntity;
                    }
                }
            }
        }
        else if (target != null)
        {
            return target;
        }

        return null;
    }

    /**
     * Check if the guard can protect himself with a shield
     * And if so, do it.
     *
     * @return The next AIState.
     */
    protected AIState attackProtect()
    {
        setDelay(2);
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
          Items.SHIELD,
          -1);

        if (shieldSlot != -1
              && target != null
              && !target.isDead)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.OFF_HAND, shieldSlot);
            worker.setActiveHand(EnumHand.OFF_HAND);

            worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
            }
        }

        return GUARD_ATTACK_PHYSICAL;
    }

    protected AIState attackPhysical()
    {

        if (worker.getRevengeTarget() != null
              && !worker.getRevengeTarget().isDead
              && worker.getDistance(worker.getRevengeTarget()) < getAttackRange())
        {
            target = worker.getRevengeTarget();
        }

        if (target == null || target.isDead)
        {
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            return DECIDE;
        }

        if (currentAttackDelay != 0)
        {
            currentAttackDelay--;
            return GUARD_ATTACK_PROTECT;
        }
        else
        {
            currentAttackDelay = getAttackDelay();
        }

        if (getOwnBuilding() != null)
        {

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
                return GUARD_ATTACK_PHYSICAL;
            }

            final int swordSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()),
              ToolType.SWORD,
              0,
              getOwnBuilding().getMaxToolLevel());

            if (swordSlot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, swordSlot);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

                worker.swingArm(EnumHand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) getRandomPitch());

                double damageToBeDealt = BASE_PHYSICAL_DAMAGE;

                if (worker.getHealth() <= DOUBLE_DAMAGE_THRESHOLD)
                {
                    damageToBeDealt *= 2;
                }

                final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);

                if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
                {
                    if (heldItem.getItem() instanceof ItemSword)
                    {
                        damageToBeDealt += ((ItemSword) heldItem.getItem()).getAttackDamage();
                    }
                    else
                    {
                        damageToBeDealt += TinkersWeaponHelper.getDamage(heldItem);
                    }
                    damageToBeDealt += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute());
                }

                final DamageSource source = new DamageSource(worker.getName());
                if (Configurations.gameplay.pvp_mode && target instanceof EntityPlayer)
                {
                    source.setDamageBypassesArmor();
                }

                target.attackEntityFrom(source, (float) damageToBeDealt);
                target.setRevengeTarget(worker);

                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
        }
        return GUARD_ATTACK_PHYSICAL;
    }

    protected AIState attackRanged()
    {
        if (worker.getLastAttackedEntity() != null
              && !worker.getLastAttackedEntity().isDead
              && worker.getDistance(worker.getLastAttackedEntity()) < getAttackRange())
        {
            target = worker.getLastAttackedEntity();
        }

        if (target == null || target.isDead)
        {
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            return DECIDE;
        }

        if (currentAttackDelay != 0)
        {
            currentAttackDelay--;
            return GUARD_ATTACK_RANGED;
        }
        else
        {
            currentAttackDelay = getAttackDelay();
        }

        if (getOwnBuilding() != null && worker.getCitizenData() != null)
        {

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
                return GUARD_ATTACK_RANGED;
            }

            final int bowslot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()),
              ToolType.BOW,
              0,
              getOwnBuilding().getMaxToolLevel());

            if (bowslot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, bowslot);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

                worker.swingArm(EnumHand.MAIN_HAND);

                final EntityTippedArrow arrow = new GuardArrow(world, worker);
                final double xVector = target.posX - worker.posX;
                final double yVector = target.getEntityBoundingBox().minY + target.height / getAimHeight() - arrow.posY;
                final double zVector = target.posZ - worker.posZ;
                final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                double damage = getRangedAttackDamage();
                final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);

                arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

                if (Configurations.gameplay.pvp_mode && target instanceof EntityPlayer)
                {
                    damage *= 2;
                }

                if (worker.getHealth() <= DOUBLE_DAMAGE_THRESHOLD)
                {
                    damage *= 2;
                }

                arrow.setDamage(damage);
                final double xDiff = target.posX - worker.posX;
                final double zDiff = target.posZ - worker.posZ;
                final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
                final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

                worker.move(MoverType.SELF, goToX, 0, goToZ);
                worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) getRandomPitch());
                worker.world.spawnEntity(arrow);

                target.setRevengeTarget(worker);

                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
        }
        return GUARD_ATTACK_RANGED;
    }

    /**
     * Gets the reload time for a Range guard attack.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            return RANGED_ATTACK_DELAY_BASE / (worker.getCitizenData().getLevel() + 1);
        }
        return RANGED_ATTACK_DELAY_BASE;
    }

    /**
     * Gets the aim height for ranged guards.
     *
     * @return the aim height.
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    protected double getAimHeight()
    {
        return 3.0D;
    }

    /**
     * Damage per ranged attack.
     *
     * @return the attack damage
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    protected float getRangedAttackDamage()
    {
        return 2;
    }

    /**
     * Get the {@link AxisAlignedBB} we're searching for targets in.
     *
     * @return the {@link AxisAlignedBB}
     */
    protected AxisAlignedBB getSearchArea()
    {
        final AbstractBuildingGuards building = (AbstractBuildingGuards) getOwnBuilding();

        if (building != null)
        {
            final double x1 = worker.posX + (building.getBonusVision() + DEFAULT_VISION);
            final double x2 = worker.posX - (building.getBonusVision() + DEFAULT_VISION);
            final double y1 = worker.posY + Y_VISION;
            final double y2 = worker.posY - Y_VISION;
            final double z1 = worker.posZ + (building.getBonusVision() + DEFAULT_VISION);
            final double z2 = worker.posZ - (building.getBonusVision() + DEFAULT_VISION);

            return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        }

        return getOwnBuilding().getTargetableArea(world);
    }

    /**
     * Updates the equipment. Take the first item of the required type only.
     * Skip over the items not requires. Ex.  Required Iron, skip leather and
     * everything else.
     */
    protected void updateArmor()
    {
        worker.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStackUtils.EMPTY);

        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(i);

            if (ItemStackUtils.isEmpty(stack))
            {
                new InvWrapper(worker.getInventoryCitizen()).extractItem(i, Integer.MAX_VALUE, false);
                continue;
            }

            if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == ItemStackUtils.EMPTY &&
            		requiredArmor.get(((ItemArmor) stack.getItem()).armorType) == stack.getItem()
            	)
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, stack);
            }
        }
    }

    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (worker.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }



    /**
     * Class to hold information about required item for the guard.
     *
     */
    public class GuardItemsNeeded
    {
    	/**
    	 * Quantity required on the required
    	 */
    	private final int quantity;

    	/**
    	 * Min level the citizen has to be to required the item
    	 */
    	private final int minLevelRequired;

    	/**
    	 *Max level the citizen can be to required the item
    	 */
    	private final int maxLevelRequired;
    	/**
    	 * Item that is being required
    	 */
    	private final Item itemNeeded;
    	/**
    	 * Item type that is required
    	 */
    	private final EntityEquipmentSlot type;

    	/**
    	 * @param type		item type for the required item
    	 * @param item		item that is being required
    	 * @param quantity	quantity required for the item
    	 * @param min		min level required to demand item
    	 * @param max		max level that the item will be required
    	 */
    	public GuardItemsNeeded(final EntityEquipmentSlot type, final Item item, final int quantity, final int min, final int max)
    	{
    		this.type = type;
    		this.minLevelRequired = min;
    		this.maxLevelRequired = max;
    		this.itemNeeded = item;
    		this.quantity = quantity;
    	}

    	/**
    	 * @return  item that is required in stack format
    	 */
    	public ItemStack getItemStackNeeded()
    	{
    		return new ItemStack(itemNeeded, quantity);
    	}

    	/**
    	 * @return item that is required
    	 */
    	public Item getItemNeeded()
    	{
    		return itemNeeded;
    	}

    	/**
    	 * @return min level for this item to be required
    	 */
    	public int getMinLevelRequired()
    	{
    		return minLevelRequired;
    	}

    	/**
    	 * @return max level for this item to be require
    	 */
    	public int getMaxLevelRequired()
    	{
    		return maxLevelRequired;
    	}

    	/**
    	 * @return type of the item
    	 */
    	public EntityEquipmentSlot getType()
    	{
    		return type;
    	}

    	/**
    	 * @return number of items required
    	 */
    	public int getQuantity()
    	{
    		return quantity;
    	}

    }
}
