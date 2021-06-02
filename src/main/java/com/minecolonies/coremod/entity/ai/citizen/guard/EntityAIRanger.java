package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobCanSee;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobWalkRandomEdge;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.DECIDE;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_RANGED;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger, AbstractBuildingGuards>
{
    private static final int    TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 4;
    private static final double SWITCH_STRAFING_DIRECTION                 = 0.3d;
    private static final double STRAFING_SPEED                            = 0.7f;
    private static final double ARROW_EXTRA_DAMAGE                        = 2.0f;

    /**
     * Visible combat icon
     */
    private final static VisibleCitizenStatus ARCHER_COMBAT     =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/archer_combat.png"), "com.minecolonies.gui.visiblestatus.archer_combat");
    private static final int                  GUARD_BONUS_RANGE = 10;

    /**
     * Whether the guard is moving towards his target
     */
    private boolean movingToTarget = false;

    /**
     * Indicates if strafing should be clockwise or not.
     */
    private int strafingClockwise = 1;

    /**
     * Amount of time strafing is able to run.
     */
    private int strafingTime = -1;

    /**
     * Amount of time the guard has been in one spot.
     */
    private int timeAtSameSpot = 0;

    /**
     * Number of ticks the guard has been way too close to target.
     */
    private int tooCloseNumTicks = 0;

    /**
     * Boolean for fleeing pathfinding
     */
    private boolean fleeing = false;

    /**
     * Physical Attack delay in ticks.
     */
    public static final int RANGED_ATTACK_DELAY_BASE = 30;

    /**
     * The path for fleeing
     */
    private PathResult fleePath;

    /**
     * Amount of time the guard has been able to see their target.
     */
    private int timeCanSee = 0;

    /**
     * Last distance to determine if the guard is stuck.
     */
    private double lastDistance = 0.0f;

    /**
     * Creates the abstract part of the AI.inte Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIRanger(@NotNull final JobRanger job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_ATTACK_RANGED, this::attackRanged, 10)
        );
        toolsNeeded.add(ToolType.BOW);
        worker.getNavigator().getPathingOptions().withJumpDropCost(0.95D);
    }

    @Override
    public IAIState getAttackState()
    {
        strafingTime = 0;
        tooCloseNumTicks = 0;
        timeAtSameSpot = 0;
        timeCanSee = 0;
        fleeing = false;
        movingToTarget = false;
        worker.getCitizenData().setVisibleStatus(ARCHER_COMBAT);

        return GUARD_ATTACK_RANGED;
    }

    /**
     * Getter for the attackrange
     */
    @Override
    protected int getAttackRange()
    {
        return getRealAttackRange();
    }

    /**
     * Calculates the actual attack range
     *
     * @return The attack range
     */
    private int getRealAttackRange()
    {
        int attackDist = BASE_DISTANCE_FOR_RANGED_ATTACK;
        // + 1 Blockrange per building level for a total of +5 from building level
        if (buildingGuards != null)
        {
            attackDist += buildingGuards.getBuildingLevel();
        }
        // ~ +1 each three levels for a total of +10 from guard level
        if (worker.getCitizenData() != null)
        {
            attackDist += (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) / 50.0f) * 15;
        }

        attackDist = Math.min(attackDist, MAX_DISTANCE_FOR_RANGED_ATTACK);

        if (target != null)
        {
            attackDist += worker.getPosY() - target.getPosY();
        }

        if (buildingGuards.getTask().equals(GuardTaskSetting.GUARD))
        {
            attackDist += GUARD_BONUS_RANGE;
        }

        return attackDist;
    }

    @Override
    public boolean hasMainWeapon()
    {
        return !checkForToolOrWeapon(ToolType.BOW);
    }

    @Override
    public void wearWeapon()
    {
        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.BOW, 0, buildingGuards.getMaxToolLevel());
        if (bowSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, bowSlot);
        }
    }

    /**
     * The ranged attack modus. Ticked every 10 Ticks.
     *
     * @return the next state to go to.
     */
    protected IAIState attackRanged()
    {
        final IAIState state = preAttackChecks();
        if (state != getState())
        {
            worker.getNavigator().clearPath();
            worker.getMoveHelper().strafe(0, 0);
            setDelay(STANDARD_DELAY);
            worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
            return state;
        }

        fighttimer = COMBAT_TIME;
        final double sqDistanceToEntity = BlockPosUtil.getDistanceSquared2D(worker.getPosition(), new BlockPos(target.getPositionVec()));
        final boolean canSee = worker.getEntitySenses().canSee(target);
        final double sqAttackRange = getRealAttackRange() * getRealAttackRange();

        if (canSee)
        {
            timeCanSee++;
        }
        else
        {
            timeCanSee--;
        }

        if (lastDistance == sqDistanceToEntity)
        {
            timeAtSameSpot++;
        }
        else
        {
            timeAtSameSpot = 0;
        }

        // Stuck
        if (sqDistanceToEntity > sqAttackRange && timeAtSameSpot > 8 || (!canSee && timeAtSameSpot > 8))
        {
            worker.getNavigator().clearPath();
            return DECIDE;
        }
        // Move inside attackrange
        else if (sqDistanceToEntity > sqAttackRange || !canSee)
        {
            if (worker.getNavigator().noPath())
            {
                moveInAttackPosition();
            }
            worker.getMoveHelper().strafe(0, 0);
            movingToTarget = true;
            strafingTime = -1;
        }
        // Clear chasing when in range
        else if (movingToTarget && sqDistanceToEntity < sqAttackRange)
        {
            worker.getNavigator().clearPath();
            movingToTarget = false;
            strafingTime = -1;
        }

        // Reset Fleeing status
        if (fleeing && !fleePath.isComputing())
        {
            fleeing = false;
            tooCloseNumTicks = 0;
        }

        // Check if the target is too close
        if (sqDistanceToEntity < RANGED_FLEE_SQDIST && !buildingGuards.getTask().equals(GuardTaskSetting.GUARD))
        {
            tooCloseNumTicks++;
            strafingTime = -1;

            // Fleeing
            if (!fleeing && !movingToTarget && sqDistanceToEntity < RANGED_FLEE_SQDIST && tooCloseNumTicks > 3)
            {
                fleePath = worker.getNavigator().moveAwayFromLivingEntity(target, getRealAttackRange() / 2.0, getCombatMovementSpeed());
                fleeing = true;
                worker.getMoveHelper().strafe(0, (float) strafingClockwise * 0.2f);
                strafingClockwise *= -1;
            }
        }
        else
        {
            tooCloseNumTicks = 0;
        }

        // Combat movement for guards not on guarding block task
        if (!shouldStayCloseToPos())
        {
            // Toggle strafing direction randomly if strafing
            if (strafingTime >= TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS)
            {
                if ((double) worker.getRNG().nextFloat() < SWITCH_STRAFING_DIRECTION)
                {
                    strafingClockwise *= -1;
                }
                this.strafingTime = 0;
            }

            // Strafe when we're close enough
            if (sqDistanceToEntity < (sqAttackRange / 2.0) && tooCloseNumTicks < 1)
            {
                strafingTime++;
            }
            else if (sqDistanceToEntity > (sqAttackRange / 2.0) + 5)
            {
                strafingTime = -1;
            }

            // Strafe or flee, when not already fleeing or moving in
            if ((strafingTime > -1) && !fleeing && !movingToTarget)
            {
                worker.getMoveHelper().strafe(0, (float) (getCombatMovementSpeed() * strafingClockwise * STRAFING_SPEED));
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
            else
            {
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
        }

        if (worker.isHandActive())
        {
            if (!canSee && timeCanSee < -6)
            {
                worker.resetActiveHand();
            }
            else if (canSee && sqDistanceToEntity <= sqAttackRange)
            {
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.swingArm(Hand.MAIN_HAND);

                int amountOfArrows = 1;
                if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS) > 0)
                {
                    if (worker.getRandom().nextDouble() < worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS))
                    {
                        amountOfArrows++;
                    }
                }

                for (int i = 0; i < amountOfArrows; i++)
                {
                    final ArrowEntity arrow = ModEntities.MC_NORMAL_ARROW.create(world);
                    arrow.setShooter(worker);

                    if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARROW_PIERCE) > 0)
                    {
                        arrow.setPierceLevel((byte) 2);
                    }

                    arrow.setPosition(worker.getPosX(), worker.getPosY() + 1, worker.getPosZ());
                    final double xVector = target.getPosX() - worker.getPosX();
                    final double yVector = target.getBoundingBox().minY + target.getHeight() / getAimHeight() - arrow.getPosY();
                    final double zVector = target.getPosZ() - worker.getPosZ();

                    final double distance = MathHelper.sqrt(xVector * xVector + zVector * zVector);
                    double damage = getRangedAttackDamage() + worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_DAMAGE);

                    if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0)
                    {
                        // Research allows archers to consume arrows from inventory for extra damage.
                        int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem);
                        if (slot != -1)
                        {
                            if (!ItemStackUtils.isEmpty(worker.getInventoryCitizen().extractItem(slot, 1, false)))
                            {
                                damage += ARROW_EXTRA_DAMAGE;
                            }
                        }
                    }


                    // Add bow enchant effects: Knocback and fire
                    final ItemStack bow = worker.getHeldItem(Hand.MAIN_HAND);

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, bow) > 0)
                    {
                        arrow.setFire(100);
                    }
                    final int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, bow);
                    if (k > 0)
                    {
                        arrow.setKnockbackStrength(k);
                    }

                    final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) + 1);

                    arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

                    if (worker.getHealth() <= worker.getMaxHealth() * 0.2D)
                    {
                        damage *= 2;
                    }

                    arrow.setDamage(damage);
                    worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    worker.world.addEntity(arrow);
                }

                timeCanSee = 0;
                target.setRevengeTarget(worker);
                currentAttackDelay = getAttackDelay();
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
                worker.resetActiveHand();
                worker.decreaseSaturationForContinuousAction();
            }
            else
            {
                /*
                 * It is possible the object is higher than guard and guard can't get there.
                 * Guard will try to back up to get some distance to be able to shoot target.
                 */
                if (target.getPosY() > worker.getPosY() + Y_VISION + Y_VISION)
                {
                    fleePath = worker.getNavigator().moveAwayFromLivingEntity(target, 10, getCombatMovementSpeed());
                    fleeing = true;
                    worker.getMoveHelper().strafe(0, 0);
                }
            }
        }
        else
        {
            reduceAttackDelay(10);
            if (currentAttackDelay <= 0)
            {
                worker.setActiveHand(Hand.MAIN_HAND);
            }
        }
        lastDistance = sqDistanceToEntity;

        return GUARD_ATTACK_RANGED;
    }

    @Override
    protected void atBuildingActions()
    {
        super.atBuildingActions();


        if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0)
        {
            // Pickup arrows and request arrows
            InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(getOwnBuilding(),
              item -> item.getItem() instanceof ArrowItem,
              64,
              worker.getInventoryCitizen());

            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem) < 16)
            {
                checkIfRequestForItemExistOrCreateAsynch(new ItemStack(Items.ARROW), 64, 16);
            }
        }
    }

    /**
     * Gets the reload time for a physical guard attack.
     *
     * @return the reload time, min PHYSICAL_ATTACK_DELAY_MIN Ticks
     */
    @Override
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            final int attackDelay = RANGED_ATTACK_DELAY_BASE - (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) / 2);
            return attackDelay < PHYSICAL_ATTACK_DELAY_MIN * 2 ? PHYSICAL_ATTACK_DELAY_MIN * 2 : attackDelay;
        }
        return RANGED_ATTACK_DELAY_BASE;
    }

    /**
     * Calculates the ranged attack damage
     *
     * @return the attack damage
     */
    private double getRangedAttackDamage()
    {
        if (worker.getCitizenData() != null)
        {
            int enchantDmg = 0;
            if (MineColonies.getConfig().getServer().rangerEnchants.get())
            {
                final ItemStack heldItem = worker.getHeldItem(Hand.MAIN_HAND);
                // Normalize to +1 dmg
                enchantDmg += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute()) / 2.5;
                enchantDmg += EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, heldItem);
            }

            return (RANGER_BASE_DMG + getLevelDamage() + enchantDmg) * MineColonies.getConfig().getServer().rangerDamageMult.get();
        }
        return RANGER_BASE_DMG * MineColonies.getConfig().getServer().rangerDamageMult.get();
    }

    /**
     * Calculates the dmg increase per level
     *
     * @return the level damage.
     */
    public int getLevelDamage()
    {
        if (worker.getCitizenData() == null)
        {
            return 0;
        }
        // Level scaling damage, +1 every 5 levels
        return (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Agility) / 50) * 10;
    }

    @Override
    protected double getCombatSpeedBonus()
    {
        return worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Agility) * SPEED_LEVEL_BONUS;
    }

    /**
     * Gets the aim height for ranged guards.
     *
     * @return the aim height. Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    private double getAimHeight()
    {
        return 3.0D;
    }

    @Override
    public void moveInAttackPosition()
    {
        ((MinecoloniesAdvancedPathNavigate) worker.getNavigator()).setPathJob(new PathJobCanSee(worker,
            target,
            world,
            buildingGuards.getGuardPos(), shouldStayCloseToPos()
             ? 20 : 40),
          null,
          getCombatMovementSpeed());
    }

    /**
     * If the guard should stick close to the guard/patroll pos.
     * @return true if so.
     */
    private boolean shouldStayCloseToPos()
    {
        return buildingGuards.getTask().equals(GuardTaskSetting.GUARD) || (buildingGuards.getTask().equals(GuardTaskSetting.PATROL) && buildingGuards.shallPatrolManually());
    }

    @Override
    public void guardMovement()
    {
        if (worker.getRandom().nextInt(3) < 1)
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 3);
            return;
        }

        if (worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 10) && Math.abs(buildingGuards.getGuardPos().getY() - worker.getPosition().getY()) < 3)
        {
            // Moves the ranger randomly to close edges, for better vision to mobs
            ((MinecoloniesAdvancedPathNavigate) worker.getNavigator()).setPathJob(new PathJobWalkRandomEdge(world, buildingGuards.getGuardPos(), 20, worker),
              null,
              getCombatMovementSpeed());
        }
    }

    @Override
    public Class<AbstractBuildingGuards> getExpectedBuildingClass()
    {
        return AbstractBuildingGuards.class;
    }
}
