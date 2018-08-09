package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.guards.GuardItems;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;
import static com.minecolonies.coremod.entity.ai.citizen.guard.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Class taking of the abstract guard methods for both archer and knights.
 * @param <J> the generic job.
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
     * and then pointer to GuardItems
     */
    public final List<List<GuardItems>> itemsNeeded = new ArrayList<>();

    /**
     * Holds a list of required armor for this guard
     */
    private final Map<IToolType, List<GuardItems>> requiredArmor = new LinkedHashMap<IToolType, List<GuardItems>>();

    /**
     * Holds a list of required armor for this guard
     */
    private final Map<EntityEquipmentSlot, ItemStack> armorToWear = new HashMap<>();

    /**
     * Entities to kill before dumping into chest.
     */
    private static final int ACTIONS_UNTIL_DUMPING = 10;

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
     * The value of the speed which the guard will move.
     */
    private static final double ATTACK_SPEED = 0.8;

    /**
     * Indicates if strafing should be moving backwards or not.
     */
    private boolean strafingBackwards = false;

    /**
     * Indicates if strafing should be clockwise or not.
     */
    private boolean strafingClockwise = false;

    /**
     * Amount of time strafing is able to run.
     */
    private int strafingTime = -1;
 
    /**
     * Amount of time the guard has been in one spot.
     */
    private int timeAtSameSpot = 0;

    /**
     * Amount of time left until guard can attack again.
     */
    private int attackTime = 0;

    /**
     * Number of ticks the guard has been way to close to target.
     */
    private int toCloseNumTicks = 0;

    /**
     * Amount of time the guard has been able to see their target. 
     */
    private int timeCanSee = 0;

    /**
     * Last distance to determine if the guard is stuck.
     */
    private double lastDistance = 0.0f;

    private static final int TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 15;
    private static final double SWITCH_STRAFING_DIRECTION = 0.3d;
    private static final float STRAFING_SPEED = 0.6f;
    
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
          new AITarget(DECIDE, this::decide)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getStrength() + worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);

        final List<GuardItems> leatherLevelArmor = new ArrayList<>();
        leatherLevelArmor.add(new GuardItems(ToolType.BOOTS, EntityEquipmentSlot.FEET,  ARMOR_LEVEL_LEATHER, 1, 0, 20, 1, 3));
        leatherLevelArmor.add(new GuardItems(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, ARMOR_LEVEL_LEATHER, 1, 0, 20, 1, 3));
        leatherLevelArmor.add(new GuardItems(ToolType.HELMET, EntityEquipmentSlot.HEAD,  ARMOR_LEVEL_LEATHER, 1, 0, 20, 1, 3));
        leatherLevelArmor.add(new GuardItems(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS,  ARMOR_LEVEL_LEATHER, 1, 0, 20, 1, 3));
        itemsNeeded.add(leatherLevelArmor);

        final List<GuardItems> goldLevelArmor = new ArrayList<>();
        goldLevelArmor.add(new GuardItems(ToolType.BOOTS, EntityEquipmentSlot.FEET,  ARMOR_LEVEL_GOLD, 1, 0, 20, 1, 4));
        goldLevelArmor.add(new GuardItems(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, ARMOR_LEVEL_GOLD, 1, 0, 20, 1, 4));
        goldLevelArmor.add(new GuardItems(ToolType.HELMET, EntityEquipmentSlot.HEAD,  ARMOR_LEVEL_GOLD, 1, 0, 20, 1, 4));
        goldLevelArmor.add(new GuardItems(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS,  ARMOR_LEVEL_GOLD, 1, 0, 20, 1, 4));
        itemsNeeded.add(goldLevelArmor);

        final List<GuardItems> chainLevelArmor = new ArrayList<>();
        chainLevelArmor.add(new GuardItems(ToolType.BOOTS, EntityEquipmentSlot.FEET, ARMOR_LEVEL_CHAIN, 1, 0, 20, 2, 5));
        chainLevelArmor.add(new GuardItems(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, ARMOR_LEVEL_CHAIN, 1, 0, 20, 2, 5));
        chainLevelArmor.add(new GuardItems(ToolType.HELMET, EntityEquipmentSlot.HEAD, ARMOR_LEVEL_CHAIN, 1, 0, 20, 2, 5));
        chainLevelArmor.add(new GuardItems(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS, ARMOR_LEVEL_CHAIN, 1, 0, 20, 2, 5));
        itemsNeeded.add(chainLevelArmor);

        final List<GuardItems> ironLevelArmor = new ArrayList<>();
        ironLevelArmor.add(new GuardItems(ToolType.BOOTS, EntityEquipmentSlot.FEET, ARMOR_LEVEL_IRON, 1, 5, 30, 3, 5));
        ironLevelArmor.add(new GuardItems(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, ARMOR_LEVEL_IRON, 0, 5, 30, 3, 5));
        ironLevelArmor.add(new GuardItems(ToolType.HELMET, EntityEquipmentSlot.HEAD, ARMOR_LEVEL_IRON, 1, 5, 30, 3, 5));
        ironLevelArmor.add(new GuardItems(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS, ARMOR_LEVEL_IRON, 1, 5, 30, 3, 5));
        itemsNeeded.add(ironLevelArmor);

        final List<GuardItems> diamondLevelArmor = new ArrayList<>();
        diamondLevelArmor.add(new GuardItems(ToolType.BOOTS, EntityEquipmentSlot.FEET, ARMOR_LEVEL_DIAMOND, 1, 15, 99, 4, 5));
        diamondLevelArmor.add(new GuardItems(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, ARMOR_LEVEL_DIAMOND, 1, 15, 99, 4, 5));
        diamondLevelArmor.add(new GuardItems(ToolType.HELMET, EntityEquipmentSlot.HEAD, ARMOR_LEVEL_DIAMOND, 1, 15, 99, 4, 5));
        diamondLevelArmor.add(new GuardItems(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS, ARMOR_LEVEL_DIAMOND, 1, 15, 99, 4, 5));
        itemsNeeded.add(diamondLevelArmor);
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

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return getOwnBuilding(AbstractBuildingGuards.class).getTask() == GuardTask.FOLLOW ?  Integer.MAX_VALUE  : ACTIONS_UNTIL_DUMPING * getOwnBuilding().getBuildingLevel();
    }

    //todo we shouldn't do this too often.
    /**
     * Prepares the guard.
     * Fills his required armor and tool lists and transfer from building chest if required.
     *
     * @return The next {@link AIState}.
     */
    private AIState prepare()
    {
        setDelay(Constants.TICKS_SECOND * PREPARE_DELAY_SECONDS);

        @Nullable
        final AbstractBuildingGuards building = getOwnBuilding();
        if (building == null || worker.getCitizenData() == null)
        {
            return PREPARING;
        }

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

        requiredArmor.clear();
        armorToWear.clear();
        final Map<IToolType, List<GuardItems>> correctArmor = new LinkedHashMap<>();
        for (final List<GuardItems> itemList : itemsNeeded)
        {
            final int level = worker.getCitizenData().getLevel();
            for (final GuardItems item : itemList)
            {
                /*
                 * Make sure that we only take the item for the right building and guard level.
                 */
                if (level >= item.getMinLevelRequired() && level <= item.getMaxLevelRequired()
                      && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                {
                    final List<GuardItems> listOfItems = new ArrayList<>();
                    listOfItems.add(item);

                    if (correctArmor.containsKey(item.getItemNeeded()))
                    {
                        listOfItems.addAll(correctArmor.get(item.getItemNeeded()));
                    }
                    correctArmor.put(item.getItemNeeded(), listOfItems);
                }
            }
        }

        for (final Map.Entry<IToolType, List<GuardItems>> entry : correctArmor.entrySet())
        {
            final List<Integer> slotsWorker = InventoryUtils.findAllSlotsInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()),
              itemStack -> entry.getValue().stream().anyMatch(guardItems -> guardItems.doesMatchItemStack(itemStack)));
            int bestLevel = -1;
            final List<Integer> nonOptimalSlots = new ArrayList<>();
            int bestSlot = -1;
            for (final int slot : slotsWorker)
            {
                final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    final int level = ItemStackUtils.getMiningLevel(stack, entry.getKey());
                    if (level > bestLevel)
                    {
                        if (bestSlot != -1)
                        {
                            nonOptimalSlots.add(bestLevel);
                        }
                        bestLevel = level;
                        bestSlot = slot;
                    }
                }
            }

            int bestLevelChest = -1;
            int bestSlotChest = -1;
            IItemHandler bestHandler = null;
            final Map<IItemHandler, List<Integer>> slotsChest =
              InventoryUtils.findAllSlotsInProviderWith(building, itemStack -> entry.getValue().stream().anyMatch(guardItems -> guardItems.doesMatchItemStack(itemStack)));
            for (final Map.Entry<IItemHandler, List<Integer>> handlers : slotsChest.entrySet())
            {
                for (final int slot : handlers.getValue())
                {
                    final ItemStack stack = handlers.getKey().getStackInSlot(slot);
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        final int level = ItemStackUtils.getMiningLevel(stack, entry.getKey());
                        if (level > bestLevel && level > bestLevelChest)
                        {
                            bestLevelChest = level;
                            bestSlotChest = slot;
                            bestHandler = handlers.getKey();
                        }
                    }
                }
            }

            if (!entry.getValue().isEmpty())
            {
                if (bestLevelChest > bestLevel)
                {
                    final ItemStack armorStack = bestHandler.getStackInSlot(bestSlotChest).copy();
                    if (armorStack.getItem() instanceof ItemArmor)
                    {
                        armorToWear.put(((ItemArmor) armorStack.getItem()).armorType, armorStack);
                    }

                    InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(bestHandler, bestSlotChest, new InvWrapper(worker.getInventoryCitizen()));
                    if (bestSlot != -1)
                    {
                        nonOptimalSlots.add(bestSlot);
                    }
                }
                else if (bestSlot == -1)
                {
                    requiredArmor.put(entry.getKey(), entry.getValue());
                }
                else
                {
                    final ItemStack armorStack = new InvWrapper(worker.getInventoryCitizen()).getStackInSlot(bestSlot).copy();
                    if (armorStack.getItem() instanceof ItemArmor)
                    {
                        armorToWear.put(((ItemArmor) armorStack.getItem()).armorType, armorStack);
                    }
                }

                for (final int slot : nonOptimalSlots)
                {
                    InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(new InvWrapper(worker.getInventoryCitizen()), slot, building);
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

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.2f) && ((AbstractBuildingGuards) getOwnBuilding()).shallRetrieveOnLowHealth())
        {
            target = null;
            return START_WORKING;
        }

        if (!(worker.getLastAttackedEntity() != null
              && !worker.getLastAttackedEntity().isDead)
              && getOwnBuilding(AbstractBuildingGuards.class) != null
              && target == null)
        {
            final AbstractBuildingGuards guardBuilding = getOwnBuilding();
            if (worker.getLastAttackedEntity() != null && !worker.getLastAttackedEntity().isDead)
            {
                if ((worker.getDistance(worker.getLastAttackedEntity()) > getAttackRange() * 5 && !worker.canEntityBeSeen(worker.getLastAttackedEntity())))
                {
                    worker.setLastAttackedEntity(null);
                    return DECIDE;
                }
                target = worker.getLastAttackedEntity();
                return DECIDE;
            }


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
                    worker.addPotionEffect(new PotionEffect(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER));
                    this.world.getScoreboard().addPlayerToTeam(worker.getName(), TEAM_COLONY_NAME + worker.getCitizenColonyHandler().getColonyId());
                    final double distance = worker.getDistanceSq(guardBuilding.getPlayerToFollow());
                    if (guardBuilding.isTightGrouping())
                    {
                        worker.isWorkerAtSiteWithMove(guardBuilding.getPlayerToFollow(), GUARD_FOLLOW_TIGHT_RANGE);
                    }
                    else
                    {
                        if (distance < getAttackDistance())
                        {
                            worker.getNavigator().clearPath();
                        }
                        else
                        {
                            worker.isWorkerAtSiteWithMove(guardBuilding.getPlayerToFollow(), GUARD_FOLLOW_LOSE_RANGE);
                        }
                    }
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
            incrementActionsDone();
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            target = null;
        }
        else if (target != null && (worker.getDistance(target) > getAttackRange() * 5 && !worker.canEntityBeSeen(target)))
        {
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
        strafingTime =  0;
        toCloseNumTicks = 0;
        timeAtSameSpot = 0;
        timeCanSee = 0;
        
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
                          && worker.getCitizenColonyHandler().getColony().getPermissions().hasPermission(player, Action.GUARDS_ATTACK) || worker.getCitizenColonyHandler().getColony().isValidAttackingPlayer(player)
                          && worker.canEntityBeSeen(player))
                    {
                        return entity;
                    }
                }
                else if (entity instanceof EntityCitizen && worker.getCitizenColonyHandler().getColony().isValidAttackingGuard((EntityCitizen) entity) && worker.canEntityBeSeen(entity))
                {
                    return entity;
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
     * Get the {@link AxisAlignedBB} we're searching for targets in.
     *
     * @return the {@link AxisAlignedBB}
     */
    protected AxisAlignedBB getSearchArea()
    {
        final AbstractBuildingGuards building = getOwnBuilding();

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

        for (final ItemStack armorStack : armorToWear.values())
        {
            if (ItemStackUtils.isEmpty(armorStack))
            {
                continue;
            }
            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), itemStack -> itemStack.isItemEqual(armorStack));
            if (slot == -1)
            {
                continue;
            }
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            if (ItemStackUtils.isEmpty(stack))
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, ItemStackUtils.EMPTY);
                continue;
            }

            if (stack.getItem() instanceof ItemArmor)
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, stack);
            }
        }

        for (Map.Entry<IToolType, List<GuardItems>> entry : requiredArmor.entrySet())
        {
            int minlevel = Integer.MAX_VALUE;
            for (final GuardItems item : entry.getValue())
            {
                if (item.getArmorLevel() < minlevel)
                {
                    minlevel = item.getArmorLevel();
                }
            }
            checkForToolorWeaponASync(entry.getKey(), minlevel);
        }
    }


}
