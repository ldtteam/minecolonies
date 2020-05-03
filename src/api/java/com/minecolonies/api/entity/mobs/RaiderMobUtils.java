package com.minecolonies.api.entity.mobs;

import com.google.common.collect.Multimap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.entity.mobs.pirates.IPirateEntity;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_BARBARIAN_DIFFICULTY;
import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Util class for raider mobs/spawning
 */
public final class RaiderMobUtils
{

    /**
     * Distances in which spawns are spread
     */
    public static double MOB_SPAWN_DEVIATION_STEPS = 0.3;

    /**
     * Mob attribute, used for custom attack damage
     */
    public final static IAttribute MOB_ATTACK_DAMAGE = new RangedAttribute(null, "mc_mob_damage", 2.0, 1.0, 20);

    /**
     * Damage increased by 1 for every 200 raid level difficulty
     */
    public static int DAMAGE_PER_X_RAID_LEVEL = 200;

    /**
     * Max damage from raidlevels
     */
    public static int MAX_RAID_LEVEL_DAMAGE = 3;

    private RaiderMobUtils()
    {
        throw new IllegalStateException("Tried to initialize: MobSpawnUtils but this is a Utility class.");
    }

    /**
     * Sets up the mob ai for a minecolonies mob. Calls into the api to get the required ai tasks from the registry and loads the tasks.
     *
     * @param mob The mob to set the AI Tasks on.
     */
    public static void setupMobAi(final AbstractEntityMinecoloniesMob mob)
    {
        final Multimap<Integer, Goal> aiTasks = IMinecoloniesAPI.getInstance().getMobAIRegistry().getEntityAiTasksForMobs(mob);
        aiTasks.keySet().forEach(priority -> aiTasks.get(priority).forEach(task -> mob.goalSelector.addGoal(priority, task)));

        final Multimap<Integer, Goal> aiTargetTasks = IMinecoloniesAPI.getInstance().getMobAIRegistry().getEntityAiTargetTasksForMobs(mob);
        aiTargetTasks.keySet().forEach(priority -> aiTargetTasks.get(priority).forEach(task -> mob.targetSelector.addGoal(priority, task)));
    }

    /**
     * Set mob attributes.
     *
     * @param mob    The mob to set the attributes on.
     * @param colony The colony that the mob is attacking.
     */
    public static void setMobAttributes(final AbstractEntityMinecoloniesMob mob, final IColony colony)
    {
        final double difficultyModifier = (mob.world.getDifficulty().getId() / 2d) * (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().barbarianHordeDifficulty.get()
                                                                                        / (double) DEFAULT_BARBARIAN_DIFFICULTY);
        mob.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        final int raidLevel = colony.getRaiderManager().getColonyRaidLevel();

        final double attackDamage =
          difficultyModifier * (ATTACK_DAMAGE + Math.min(
            raidLevel / DAMAGE_PER_X_RAID_LEVEL,
            MAX_RAID_LEVEL_DAMAGE));

        mob.getAttribute(MOB_ATTACK_DAMAGE).setBaseValue(attackDamage);

        if (mob instanceof IChiefMobEntity)
        {
            final double chiefArmor = difficultyModifier * CHIEF_BONUS_ARMOR;
            mob.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(chiefArmor);
            mob.getAttribute(MOB_ATTACK_DAMAGE).setBaseValue(attackDamage + 2.0);
            mob.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * 2 * difficultyModifier));
        }
        else
        {
            final double armor = difficultyModifier * ARMOR;
            mob.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armor);
            mob.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * difficultyModifier));
        }

        if (difficultyModifier >= 1.4d)
        {
            mob.setEnvDamageImmunity(true);
        }

        mob.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getHealthBasedOnRaidLevel(raidLevel) * difficultyModifier);
        mob.setHealth(mob.getMaxHealth());
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @param raidLevel
     * @return returns the health in the form of a double
     */
    private static double getHealthBasedOnRaidLevel(final int raidLevel)
    {
        return Math.max(BARBARIAN_BASE_HEALTH, (BARBARIAN_BASE_HEALTH + raidLevel * BARBARIAN_HEALTH_MULTIPLIER));
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *
     * @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     * @param colony         the colony to spawn them close to.
     * @param eventID        the event id.
     */
    public static void spawn(
      final EntityType entityToSpawn,
      final int numberOfSpawns,
      final BlockPos spawnLocation,
      final World world,
      final IColony colony,
      final int eventID)
    {
        if (spawnLocation != null && entityToSpawn != null && world != null && numberOfSpawns > 0)
        {
            final int x = spawnLocation.getX();
            final int y = BlockPosUtil.getFloor(spawnLocation, world).getY();
            final int z = spawnLocation.getZ();
            double spawnDeviationX = 0;
            double spawnDeviationZ = 0;

            for (int i = 0; i < numberOfSpawns; i++)
            {
                final AbstractEntityMinecoloniesMob entity = (AbstractEntityMinecoloniesMob) entityToSpawn.create(world);

                if (entity != null)
                {
                    entity.setPositionAndRotation(x + spawnDeviationX, y + 1.0, z + spawnDeviationZ, (float) MathHelper.wrapDegrees(world.rand.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    CompatibilityUtils.addEntity(world, entity);
                    entity.setColony(colony);
                    entity.setEventID(eventID);
                    entity.registerWithColony();
                    spawnDeviationZ += MOB_SPAWN_DEVIATION_STEPS;

                    if (spawnDeviationZ > 2)
                    {
                        spawnDeviationZ = 0;
                        spawnDeviationX += MOB_SPAWN_DEVIATION_STEPS;
                    }
                }
            }
        }
    }

    /**
     * Set the equipment of a certain mob.
     *
     * @param mob the equipment to set up.
     */
    public static void setEquipment(final AbstractEntityMinecoloniesMob mob)
    {
        if (mob instanceof IMeleeBarbarianEntity)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof IArcherMobEntity)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof IChiefBarbarianEntity)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof IPirateEntity)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof ICaptainPirateEntity)
            {
                if (new Random().nextBoolean())
                {
                    mob.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.pirateHelmet_1));
                    mob.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(ModItems.pirateChest_1));
                    mob.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(ModItems.pirateLegs_1));
                    mob.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(ModItems.pirateBoots_1));
                }
                else
                {
                    mob.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.pirateHelmet_2));
                    mob.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(ModItems.pirateChest_2));
                    mob.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(ModItems.pirateLegs_2));
                    mob.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(ModItems.pirateBoots_2));
                }
            }
        }
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<AbstractEntityMinecoloniesMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorldFromEntity(entity).getEntitiesWithinAABB(
          AbstractEntityMinecoloniesMob.class,
          entity.getBoundingBox().expand(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isAlive);
    }
}
