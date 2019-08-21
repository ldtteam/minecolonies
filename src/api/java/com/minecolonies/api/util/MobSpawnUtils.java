package com.minecolonies.api.util;

import com.google.common.collect.Multimap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.IArcherMobEntity;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.entity.mobs.pirates.IPirateEntity;
import com.minecolonies.api.entity.mobs.util.MobEventsUtils;
import com.minecolonies.api.items.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.IntStream;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

public final class MobSpawnUtils
{

    private MobSpawnUtils()
    {
        throw new IllegalStateException("Tried to initialize: MobSpawnUtils but this is a Utility class.");
    }

    /**
     * Sets up the mob ai for a minecolonies mob.
     * Calls into the api to get the required ai tasks from the registry and loads the tasks.
     *
     * @param mob The mob to set the AI Tasks on.
     */
    public static void setupMobAi(final AbstractEntityMinecoloniesMob mob)
    {
        final Multimap<Integer, EntityAIBase> aiTasks = IMinecoloniesAPI.getInstance().getMobAIRegistry().getEntityAiTasksForMobs(mob);
        aiTasks.keySet().forEach(priority -> aiTasks.get(priority).forEach(task -> mob.tasks.addTask(priority, task)));

        final Multimap<Integer, EntityAIBase> aiTargetTasks = IMinecoloniesAPI.getInstance().getMobAIRegistry().getEntityAiTargetTasksForMobs(mob);
        aiTargetTasks.keySet().forEach(priority -> aiTasks.get(priority).forEach(task -> mob.tasks.addTask(priority, task)));
    }

    /**
     * Set mob attributes.
     *
     * @param mob The mob to set the attributes on.
     * @param colony    The colony that the mob is attacking.
     */
    public static void setMobAttributes(final EntityLiving mob, final IColony colony)
    {
        mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        final double attackDamage = Configurations.gameplay.barbarianHordeDifficulty >= 10 ? ATTACK_DAMAGE * 2 : ATTACK_DAMAGE;
        mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
        if (mob instanceof IChiefBarbarianEntity)
        {
            final double chiefArmor = Configurations.gameplay.barbarianHordeDifficulty > 5 ? CHIEF_ARMOR * 2 : CHIEF_ARMOR;
            mob.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(chiefArmor);
        }
        else
        {
            final double armor = Configurations.gameplay.barbarianHordeDifficulty * ARMOR;
            mob.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armor);
        }
        mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getHealthBasedOnRaidLevel(colony));
        mob.setHealth(mob.getMaxHealth());
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @return returns the health in the form of a double
     */
    private static double getHealthBasedOnRaidLevel(final IColony colony)
    {
        if (colony != null)
        {
            final int raidLevel = (int) (MobEventsUtils.getColonyRaidLevel(colony) * BARBARIAN_HEALTH_MULTIPLIER);
            return Math.max(BARBARIAN_BASE_HEALTH, (BARBARIAN_BASE_HEALTH + raidLevel) * ((double) Configurations.gameplay.barbarianHordeDifficulty * 0.1));
        }
        return BARBARIAN_BASE_HEALTH;
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *  @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     * @param colony the colony to spawn them close to.
     */
    public static void spawn(
      final ResourceLocation entityToSpawn,
      final int numberOfSpawns,
      final BlockPos spawnLocation,
      final World world,
      final IColony colony)
    {
        if (spawnLocation != null && entityToSpawn != null && world != null)
        {

            final int x = spawnLocation.getX();
            final int y = BlockPosUtil.getFloor(spawnLocation, world).getY();
            final int z = spawnLocation.getZ();

            IntStream.range(0, numberOfSpawns).forEach(theInteger ->
            {
                final AbstractEntityBarbarian entity = (AbstractEntityBarbarian) EntityList.createEntityByIDFromName(entityToSpawn, world);

                if (entity != null)
                {
                    setEquipment(entity);
                    entity.setPositionAndRotation(x, y + 1.0, z, (float) MathHelper.wrapDegrees(world.rand.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    CompatibilityUtils.spawnEntity(world, entity);
                    entity.setColony(colony);
                }
            });
        }
    }

    /**
     * Set the equipment of a certain mob.
     * @param mob the equipment to set up.
     */
    public static void setEquipment(final AbstractEntityMinecoloniesMob mob)
    {
        if (mob instanceof IMeleeBarbarianEntity)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof IArcherMobEntity)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof IChiefBarbarianEntity)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof IPirateEntity)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof ICaptainPirateEntity)
            {
                if (new Random().nextBoolean())
                {
                    mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.pirateHelmet_1));
                    mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.pirateChest_1));
                    mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.pirateLegs_1));
                    mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.pirateBoots_1));
                }
                else
                {
                    mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.pirateHelmet_2));
                    mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.pirateChest_2));
                    mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.pirateLegs_2));
                    mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.pirateBoots_2));
                }
            }
        }
    }
}
