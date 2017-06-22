package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Handles the AI of the guard entities.
 */
public class EntityAIMeleeGuard extends AbstractEntityAIGuard
{
    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 30;

    /**
     * The pitch will be divided by this to calculate it for the arrow sound.
     */
    private static final double PITCH_DIVIDER = 1.0D;

    /**
     * The base pitch, add more to this to change the sound.
     */
    private static final double BASE_PITCH = 0.8D;

    /**
     * Random is multiplied by this to get a random arrow sound.
     */
    private static final double PITCH_MULTIPLIER = 0.4D;

    /**
     * Quantity to be moved to rotate the entity without actually moving.
     */
    private static final double MOVE_MINIMAL = 0.01D;

    /**
     * Quantity the worker should turn around all at once.
     */
    private static final double TURN_AROUND = 180D;

    /**
     * Normal volume at which sounds are played at.
     */
    private static final double BASIC_VOLUME = 1.0D;

    /**
     * Base speed of the guard he follows his target.
     */
    private static final int BASE_FOLLOW_SPEED = 1;

    /**
     * Base multiplier increasing the attack speed each level.
     */
    private static final double BASE_FOLLOW_SPEED_MULTIPLIER = 0.25D;

    /**
     * The Min distance of the guard to attack entities.
     */
    private static final double MIN_ATTACK_DISTANCE = 2.0D;

    /**
     * Damage per range attack.
     */
    private static final double DAMAGE_PER_ATTACK = 0.5;

    /**
     * Chance that a mob is lit on fire when a weapon has the fire aspect enchantment.
     */
    private static final int FIRE_CHANCE_MULTIPLIER = 4;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    public EntityAIMeleeGuard(@NotNull final JobGuard job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_SEARCH_TARGET, this::searchTarget),
          new AITarget(GUARD_GET_TARGET, this::getTarget),
          new AITarget(GUARD_HUNT_DOWN_TARGET, this::huntDown),
          new AITarget(GUARD_PATROL, this::patrol),
          new AITarget(GUARD_RESTOCK, this::goToBuilding)
        );

        if (worker.getCitizenData() != null)
        {
            worker.setSkillModifier(2 * worker.getCitizenData().getStrength() + worker.getCitizenData().getEndurance());
            worker.setCanPickUpLoot(true);
        }
    }

    @Override
    protected AIState searchTarget()
    {
        if (checkForToolOrWeapon(ToolType.SWORD))
        {
            return AIState.GUARD_SEARCH_TARGET;
        }
        InventoryFunctions.matchFirstInProviderWithSimpleAction(worker, stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.doesItemServeAsWeapon(stack), worker::setHeldItem);
        return super.searchTarget();
    }

    /**
     * Follow the target and kill it.
     *
     * @return the next AIState.
     */
    protected AIState huntDown()
    {
        if(huntDownlastAttacker())
        {
            targetEntity = this.worker.getLastAttacker();
        }

        if (!targetEntity.isEntityAlive() || checkForToolOrWeapon(ToolType.SWORD))
        {
            targetEntity = null;
            worker.setAIMoveSpeed((float) 1.0D);
            return AIState.GUARD_GATHERING;
        }

        if (worker.canEntityBeSeen(targetEntity) && worker.getDistanceToEntity(targetEntity) <= MIN_ATTACK_DISTANCE)
        {
            worker.resetActiveHand();
            boolean killedEnemy = attackEntity(targetEntity, (float) DAMAGE_PER_ATTACK);
            setDelay(getReloadTime());
            attacksExecuted += 1;
            currentSearchDistance = START_SEARCH_DISTANCE;

            if(killedEnemy)
            {
                return AIState.GUARD_GATHERING;
            }

            if (attacksExecuted >= getMaxAttacksUntilRestock())
            {
                return AIState.GUARD_RESTOCK;
            }

            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

        if (shouldReturnToTarget(targetEntity.getPosition(), FOLLOW_RANGE))
        {
            return AIState.GUARD_PATROL;
        }

        worker.setAIMoveSpeed((float) (BASE_FOLLOW_SPEED + BASE_FOLLOW_SPEED_MULTIPLIER * worker.getExperienceLevel()));
        worker.isWorkerAtSiteWithMove(targetEntity.getPosition(), (int) MIN_ATTACK_DISTANCE);

        return AIState.GUARD_SEARCH_TARGET;
    }

    private boolean attackEntity(@NotNull final EntityLivingBase entityToAttack, final float baseDamage)
    {
        double damgeToBeDealt = baseDamage;

        if (worker.getHealth() <= 2)
        {
            damgeToBeDealt *= 2;
        }

        final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem != null)
        {
            if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
            {
                if(heldItem.getItem() instanceof ItemSword)
                {
                    damgeToBeDealt += ((ItemSword) heldItem.getItem()).getDamageVsEntity();
                }
                else
                {
                    damgeToBeDealt += Compatibility.getAttackDamage(heldItem);
                }
            }
            damgeToBeDealt += EnchantmentHelper.getModifierForCreature(heldItem, targetEntity.getCreatureAttribute());
        }

        targetEntity.attackEntityFrom(new DamageSource(worker.getName()), (float) damgeToBeDealt);
        targetEntity.setRevengeTarget(worker);

        final int fireAspectModifier = EnchantmentHelper.getFireAspectModifier(worker);
        if (fireAspectModifier > 0)
        {
            targetEntity.setFire(fireAspectModifier * FIRE_CHANCE_MULTIPLIER);
        }

        boolean killedEnemy = false;
        if (targetEntity.getHealth() <= 0.0F)
        {
            this.onKilledEntity(targetEntity);
            killedEnemy = true;
        }

        worker.faceEntity(entityToAttack, (float) TURN_AROUND, (float) TURN_AROUND);
        worker.getLookHelper().setLookPositionWithEntity(entityToAttack, (float) TURN_AROUND, (float) TURN_AROUND);

        final double xDiff = targetEntity.posX - worker.posX;
        final double zDiff = targetEntity.posZ - worker.posZ;

        final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        worker.moveEntity(goToX, 0, goToZ);

        worker.swingArm(EnumHand.MAIN_HAND);
        worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) getRandomPitch());

        worker.damageItemInHand(1);
        return killedEnemy;
    }

    private int getReloadTime()
    {
        return BASE_RELOAD_TIME / (worker.getExperienceLevel() + 1);
    }

    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (worker.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
