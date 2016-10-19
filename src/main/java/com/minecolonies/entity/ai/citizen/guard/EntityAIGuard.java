package com.minecolonies.entity.ai.citizen.guard;

import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.colony.jobs.JobGuard;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.Log;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Handles the AI of the guard entities.
 */
public class EntityAIGuard extends AbstractEntityAISkill<JobGuard> implements IRangedAttackMob
{
    /**
     * The start search distance of the guard to track/attack entities may get more depending on the level.
     */
    private static final double MAX_ATTACK_DISTANCE = 20.0D;

    /**
     * Quantity to be moved to rotate the entity without actually moving.
     */
    private static final double MOVE_MINIMAL = 0.01D;

    /**
     * Quantity the worker should turn around all at once.
     */
    private static final double TURN_AROUND = 180D;

    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 100;

    /**
     * Base speed of the guard he follows his target.
     */
    private static final int BASE_FOLLOW_SPEED = 1;

    /**
     * Base multiplier increasing the attack speed each level.
     */
    private static final double BASE_FOLLOW_SPEED_MULTIPLIER = 0.25D;

    /**
     * Distance the guard starts searching.
     */
    private static final int START_SEARCH_DISTANCE = 5;

    /**
     * Basic delay after operations.
     */
    private static final int BASE_DELAY = 10;

    /**
     * Max amount the guard can shoot arrows before restocking.
     */
    private static final int MAX_ARROWS_SHOT = 50;

    /**
     * Normal volume at which sounds are played at.
     */
    private static final double BASIC_VOLUME           = 1.0D;

    /**
     * Y range in which the guard detects other entities.
     */
    private static final double HEIGHT_DETECTION_RANGE = 10D;

    /**
     * Guard has to aim x higher to hit his target.
     */
    private static final double AIM_HEIGHT = 3.0D;

    /**
     * Experience the guard receives each shot arrow.
     */
    private static final double XP_EACH_ARROW = 0.2;

    /**
     * The distance the guard is searching entities in currently.
     */
    private int currentSearchDistance = START_SEARCH_DISTANCE;

    /**
     * The current target.
     */
    private EntityLivingBase targetEntity;

    /**
     * Current goTo task.
     */
    private BlockPos currentPatrolTarget;

    /**
     * Containing all close entities.
     */
    private List<Entity> entityList;

    /**
     * Amount of arrows already shot.
     */
    private int arrowsShot = 0;

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
                new AITarget(START_WORKING, () -> GUARD_SEARCH_TARGET),
                new AITarget(GUARD_SEARCH_TARGET, this::searchTarget),
                new AITarget(GUARD_GET_TARGET, this::getTarget),
                new AITarget(GUARD_HUNT_DOWN_TARGET, this::huntDown),
                new AITarget(GUARD_PATROL, this::patrol),
                new AITarget(GUARD_RESTOCK, this::goToBuilding)
                );

        if(worker.getCitizenData() != null)
        {
            worker.setSkillModifier(2 * worker.getCitizenData().getIntelligence() + worker.getCitizenData().getStrength());
            worker.setCanPickUpLoot(true);
        }
    }

    //todo take armour out of chest? if he hasn't
    //todo wear shield when given.

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the fishes or rods have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        updateArmor();
    }

    private int getReloadTime()
    {
        return BASE_RELOAD_TIME / (worker.getExperienceLevel() + 1);
    }

    private AIState goToBuilding()
    {
        if(!walkToBuilding())
        {
            arrowsShot = 0;
            return AIState.START_WORKING;
        }
        return AIState.GUARD_RESTOCK;
    }

    /**
     * Updates the equipment. Always take the first item of each type and set it.
     */
    private void updateArmor()
    {
        worker.setItemStackToSlot(EntityEquipmentSlot.CHEST, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.FEET, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.HEAD, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.LEGS, null);

        for(int i = 0; i < worker.getInventoryCitizen().getSizeInventory(); i++)
        {
            ItemStack stack = worker.getInventoryCitizen().getStackInSlot(i);

            if(stack == null)
            {
                continue;
            }

            if(stack.stackSize == 0)
            {
                worker.getInventoryCitizen().setInventorySlotContents(i, null);
                continue;
            }

            if(stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType)== null)
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, stack);
            }
        }
    }

    /**
     * Chooses a target from the list.
     * @return the next state.
     */
    private AIState getTarget()
    {
        if(entityList.isEmpty())
        {
            return AIState.GUARD_PATROL;
        }

        if(entityList.get(0) instanceof EntityPlayer)
        {
            if(worker.getColony() != null && worker.getColony().getPermissions().getRank((EntityPlayer) entityList.get(0)) == Permissions.Rank.HOSTILE)
            {
                targetEntity = (EntityLivingBase) entityList.get(0);
                worker.getNavigator().clearPathEntity();
                return AIState.GUARD_HUNT_DOWN_TARGET;
            }
            entityList.remove(0);
            setDelay(BASE_DELAY);
            return AIState.GUARD_GET_TARGET;
        }
        else if (!worker.getEntitySenses().canSee(entityList.get(0)) || !(entityList.get(0)).isEntityAlive())
        {
            entityList.remove(0);
            setDelay(BASE_DELAY);
            return AIState.GUARD_GET_TARGET;
        }
        else
        {
            worker.getNavigator().clearPathEntity();
            targetEntity = (EntityLivingBase) entityList.get(0);
            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

    }

    /**
     * Searches for the next taget.
     * @return the next AIState.
     */
    private AIState searchTarget()
    {
        entityList = this.worker.worldObj.getEntitiesWithinAABB(EntityMob.class, this.getTargetableArea(currentSearchDistance));
        entityList.addAll(this.worker.worldObj.getEntitiesWithinAABB(EntitySlime.class, this.getTargetableArea(currentSearchDistance)));
        entityList.addAll(this.worker.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(currentSearchDistance)));

        if(checkOrRequestItems(new ItemStack(Items.BOW)))
        {
            return AIState.GUARD_SEARCH_TARGET;
        }
        worker.setHeldItem(worker.findFirstSlotInInventoryWith(Items.BOW));

        setDelay(BASE_DELAY);
        if(entityList.isEmpty())
        {
            if(currentSearchDistance < getMaxVision())
            {
                currentSearchDistance += START_SEARCH_DISTANCE;
            }
            else
            {
                currentSearchDistance = START_SEARCH_DISTANCE;
                Log.getLogger().info("Patroll! in searchTarget");
                return AIState.GUARD_PATROL;
            }

            return AIState.GUARD_SEARCH_TARGET;
        }
        return AIState.GUARD_GET_TARGET;
    }

    /**
     * Getter for the vision or attack distance.
     * @return the max vision.
     */
    private double getMaxVision()
    {
        BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ATTACK_DISTANCE + guardTower.getBonusVision());
    }

    /**
     * Getter calculating how many arrows the guard may shoot until restock.
     * @return the amount.
     */
    private int getMaxArrowsShot()
    {
        BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ARROWS_SHOT + guardTower.getBuildingLevel());
    }

    /**
     * Follow the target and kill it.
     * @return the next AIState.
     */
    private AIState huntDown()
    {
        if(!targetEntity.isEntityAlive())
        {
            targetEntity = null;
        }

        if (targetEntity != null)
        {
            if(worker.getEntitySenses().canSee(targetEntity) && worker.getDistanceToEntity(targetEntity) <= MAX_ATTACK_DISTANCE)
            {
                worker.resetActiveHand();
                attackEntityWithRangedAttack(targetEntity, 1);
                setDelay(getReloadTime());
                arrowsShot += 1;

                if(arrowsShot >= getMaxArrowsShot())
                {
                    return AIState.GUARD_RESTOCK;
                }

                return AIState.GUARD_HUNT_DOWN_TARGET;
            }
            worker.setAIMoveSpeed((float) (BASE_FOLLOW_SPEED + BASE_FOLLOW_SPEED_MULTIPLIER * worker.getExperienceLevel()));
            worker.isWorkerAtSiteWithMove(targetEntity.getPosition(), 3);

            return AIState.GUARD_SEARCH_TARGET;
        }

        worker.setAIMoveSpeed(1);
        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Lets the guard patrol inside the colony area searching for mobs.
     * @return the next state to go.
     */
    private AIState patrol()
    {
        worker.setAIMoveSpeed(1);

        if(currentPatrolTarget == null)
        {
            currentPatrolTarget = getRandomBuilding();
        }

        if(worker.isWorkerAtSiteWithMove(currentPatrolTarget, 3))
        {
            currentPatrolTarget = null;
        }

        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Gets a random building from his colony.
     * @return a random blockPos.
     */
    private BlockPos getRandomBuilding()
    {
        if(worker.getColony() == null)
        {
            return worker.getPosition();
        }

        Map<BlockPos, AbstractBuilding> buildingMap = worker.getColony().getBuildings();
        int random = worker.getRandom().nextInt(buildingMap.size());
        return (BlockPos) worker.getColony().getBuildings().keySet().toArray()[random];
    }

    @Override
    public void attackEntityWithRangedAttack(@NotNull EntityLivingBase entityToAttack, float baseDamage)
    {
        EntityTippedArrow arrowEntity = new EntityTippedArrow(this.worker.worldObj, worker);
        double xVector = entityToAttack.posX - worker.posX;
        double yVector = entityToAttack.getEntityBoundingBox().minY + entityToAttack.height / AIM_HEIGHT - arrowEntity.posY;
        double zVector = entityToAttack.posZ - worker.posZ;
        double distance = (double) MathHelper.sqrt_double(xVector * xVector + zVector * zVector);

        //Lower the variable higher the chance that the arrows hits the target.
        double chance = 10.0 / (worker.getExperienceLevel()+1); //standard is 6, hard is 2

        arrowEntity.setThrowableHeading(xVector, yVector + distance * 0.20000000298023224D, zVector, 1.6F, (float) chance);
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

        worker.addExperience(XP_EACH_ARROW);
        worker.faceEntity(entityToAttack, (float)TURN_AROUND, (float)TURN_AROUND);
        worker.getLookHelper().setLookPositionWithEntity(entityToAttack, (float)TURN_AROUND, (float)TURN_AROUND);

        double xDiff = targetEntity.posX - worker.posX;
        double zDiff = targetEntity.posZ - worker.posZ;

        double goToX = xDiff > 0? MOVE_MINIMAL : -MOVE_MINIMAL;
        double goToZ = zDiff > 0? MOVE_MINIMAL : -MOVE_MINIMAL;

        worker.moveEntity(goToX, 0, goToZ);

        worker.swingArm(EnumHand.MAIN_HAND);
        worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) getRandomPitch());
        worker.worldObj.spawnEntityInWorld(arrowEntity);

        worker.damageItemInHand(1);
    }

    private double getRandomPitch()
    {
        return 1.0D / (worker.getRNG().nextDouble() * 0.4D + 0.8D);
    }

    private AxisAlignedBB getTargetableArea(double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, HEIGHT_DETECTION_RANGE, range);
    }

}
