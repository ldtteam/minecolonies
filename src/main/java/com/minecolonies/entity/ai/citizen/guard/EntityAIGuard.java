package com.minecolonies.entity.ai.citizen.guard;

import com.minecolonies.colony.jobs.JobGuard;
import com.minecolonies.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.entity.ai.minimal.EntityAICitizenAvoidEntity;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.entity.ai.util.AIState.*;


/**
 * Handles the AI of the guard entities.
 */
public class EntityAIGuard extends AbstractEntityAISkill<JobGuard> implements IRangedAttackMob
{
    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    public EntityAIGuard(@NotNull final JobGuard job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::searchTarget)
        );
        worker.targetTasks.removeTask(new EntityAICitizenAvoidEntity(worker, EntityMob.class, 8.0F, 0.6D, 1.6D));
        worker.setSkillModifier(2 * worker.getCitizenData().getIntelligence() + worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    public AIState searchTarget()
    {
        List entityList = this.worker.worldObj.getEntitiesWithinAABB(EntityMob.class, this.getTargetableArea(20));
        entityList.addAll(this.worker.worldObj.getEntitiesWithinAABB(EntitySlime.class, this.getTargetableArea(20)));
        if(entityList.isEmpty())
        {
            return AIState.START_WORKING;
        }
        else
        {

            this.attackEntityWithRangedAttack((EntityLivingBase) entityList.get(0), 1);

            setDelay(100);
            return AIState.START_WORKING;
        }
    }

    protected AxisAlignedBB getTargetableArea(double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, 4.0D, range);
    }

    @Override
    public void attackEntityWithRangedAttack(@NotNull EntityLivingBase entityToAttack, float baseDamage)
    {
        EntityTippedArrow arrowEntity = new EntityTippedArrow(this.worker.worldObj, worker);
        double xVector = entityToAttack.posX - worker.posX;
        double yVector = entityToAttack.getEntityBoundingBox().minY + (double)(entityToAttack.height / 3.0F) - arrowEntity.posY;
        double zVector = entityToAttack.posZ - worker.posZ;
        double distance = (double) MathHelper.sqrt_double(xVector * xVector + zVector * zVector);
        arrowEntity.setThrowableHeading(xVector, yVector + distance * 0.20000000298023224D, zVector, 1.6F, (float)(14 - this.worker.worldObj.getDifficulty().getDifficultyId() * 4));
        int powerEntchanment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, worker);
        int punchEntchanment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, worker);
        DifficultyInstance difficulty = this.worker.worldObj.getDifficultyForLocation(new BlockPos(worker));
        arrowEntity.setDamage((baseDamage * 2.0D)
                + worker.getRandom().nextGaussian() * 0.25D
                + this.worker.worldObj.getDifficulty().getDifficultyId() * 0.11D);
        if(powerEntchanment > 0)
        {
            arrowEntity.setDamage(arrowEntity.getDamage() + (double)powerEntchanment * 0.5D + 0.5D);
        }

        if(punchEntchanment > 0)
        {
            arrowEntity.setKnockbackStrength(punchEntchanment);
        }

        boolean onFire = worker.isBurning() && difficulty.func_190083_c() && worker.getRandom().nextBoolean();
        onFire = onFire || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, worker) > 0;

        if(onFire)
        {
            arrowEntity.setFire(100);
        }

        ItemStack holdItem = worker.getHeldItem(EnumHand.OFF_HAND);
        if(holdItem != null && holdItem.getItem() == Items.TIPPED_ARROW)
        {
            arrowEntity.setPotionEffect(holdItem);
        }
        worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (worker.getRNG().nextFloat() * 0.4F + 0.8F));
        worker.worldObj.spawnEntityInWorld(arrowEntity);
    }





}
