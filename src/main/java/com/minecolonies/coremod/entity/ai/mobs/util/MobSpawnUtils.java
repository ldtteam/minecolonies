package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIAttackArcher;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIRaiderAttackMelee;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIWalkToRandomHuts;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.*;
import com.minecolonies.coremod.entity.ai.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.IntStream;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Utils used for Barbarian Spawning
 */
public final class MobSpawnUtils
{
    /**
     * Private constructor to hide the implicit public one.
     */
    private MobSpawnUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Set mob attributes.
     *
     * @param mob The mob to set the attributes on.
     * @param colony    The colony that the mob is attacking.
     */
    public static void setMobAttributes(final LivingEntity mob, final Colony colony)
    {
        mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        final double attackDamage = Configurations.gameplay.barbarianHordeDifficulty >= 10 ? ATTACK_DAMAGE * 2 : ATTACK_DAMAGE;
        mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
        if(mob instanceof EntityChiefBarbarian)
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
    private static double getHealthBasedOnRaidLevel(final Colony colony)
    {
        if (colony != null)
        {
            final int raidLevel = (int) (MobEventsUtils.getColonyRaidLevel(colony) * BARBARIAN_HEALTH_MULTIPLIER);
            return Math.max(BARBARIAN_BASE_HEALTH, (BARBARIAN_BASE_HEALTH + raidLevel) * ((double) Configurations.gameplay.barbarianHordeDifficulty * 0.1));
        }
        return BARBARIAN_BASE_HEALTH;
    }

    /**
     * Set mob AI Tasks.
     *
     * @param mob The mob to set the AI Tasks on.
     */
    public static void setMobAI(final AbstractEntityMinecoloniesMob mob)
    {
        mob.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(mob));
        mob.tasks.addTask(PRIORITY_FOUR, new EntityAIWalkToRandomHuts(mob, AI_MOVE_SPEED));
        mob.targetTasks.addTask(PRIORITY_TWO, new EntityAINearestAttackableTarget<>(mob, PlayerEntity.class, true));
        mob.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(mob, EntityCitizen.class, true));
        mob.tasks.addTask(PRIORITY_FIVE, new EntityAIWatchClosest(mob, PlayerEntity.class, MAX_WATCH_DISTANCE));

        if (mob instanceof EntityArcherBarbarian || mob instanceof EntityArcherPirate)
        {
            mob.tasks.addTask(PRIORITY_ONE, new EntityAIAttackArcher(mob));
        }
        else
        {
            mob.tasks.addTask(PRIORITY_ONE, new EntityAIRaiderAttackMelee(mob));
        }
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
      final Colony colony)
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
                    CompatibilityUtils.addEntity(world, entity);
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
        if (mob instanceof EntityBarbarian)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof EntityArcherBarbarian || mob instanceof EntityArcherPirate)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof EntityChiefBarbarian)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof AbstractEntityPirate)
        {
            mob.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof EntityCaptainPirate)
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
}
