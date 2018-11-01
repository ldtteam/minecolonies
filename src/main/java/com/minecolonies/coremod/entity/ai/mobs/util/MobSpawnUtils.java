package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIAttackArcher;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIRaiderAttackMelee;
import com.minecolonies.coremod.entity.ai.mobs.aitasks.EntityAIWalkToRandomHuts;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.*;
import com.minecolonies.coremod.entity.ai.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityChiefPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Utils used for Barbarian Spawning
 */
public final class MobSpawnUtils
{
    /**
     * Loot tables for Barbarians.
     */
    public static final ResourceLocation BarbarianLootTable = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
    public static final ResourceLocation ArcherLootTable    = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
    public static final ResourceLocation ChiefLootTable     = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");

    /**
     * Loot tables for Pirates.
     */
    public static final ResourceLocation PirateLootTable = new ResourceLocation(Constants.MOD_ID, "entitypiratedrops");
    public static final ResourceLocation PirateArcherLootTable = new ResourceLocation(Constants.MOD_ID, "entityarcherpiratedrops");
    public static final ResourceLocation PirateChiefLootTable = new ResourceLocation(Constants.MOD_ID, "entitychiefpiratedrops");

    /**
     * Barbarian Attack Damage.
     */
    public static final double ATTACK_DAMAGE = 2.0D;

    /**
     * Values used in Spawn() method
     */
    private static final double WHOLE_CIRCLE = 360.0;

    /**
     * Values used for AI Task's Priorities.
     */
    private static final int    PRIORITY_ZERO               = 0;
    private static final int    PRIORITY_ONE                = 1;
    private static final int    PRIORITY_TWO                = 2;
    private static final int    PRIORITY_THREE              = 3;
    private static final int    PRIORITY_FOUR               = 4;
    private static final int    PRIORITY_FIVE               = 5;

    /**
     * Other various values used for AI Tasks.
     */
    private static final double AI_MOVE_SPEED               = 2.0D;
    private static final float  MAX_WATCH_DISTANCE          = 8.0F;

    /**
     * Values used for mob attributes.
     */
    private static final double FOLLOW_RANGE                = 35.0D;
    private static final double MOVEMENT_SPEED              = 0.25D;
    private static final double ARMOR                       = 1.5D;
    private static final double CHIEF_ARMOR                 = 8D;
    private static final double BARBARIAN_BASE_HEALTH       = 5;
    private static final double BARBARIAN_HEALTH_MULTIPLIER = 0.2;

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
    public static void setMobAttributes(final EntityLiving mob, final Colony colony)
    {
        mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        if(mob instanceof EntityChiefBarbarian)
        {
            mob.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(CHIEF_ARMOR);
        }
        else
        {
            mob.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        }
        mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getHealthBasedOnRaidLevel(colony));
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
        mob.targetTasks.addTask(PRIORITY_TWO, new EntityAINearestAttackableTarget<>(mob, EntityPlayer.class, true));
        mob.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(mob, EntityCitizen.class, true));
        mob.tasks.addTask(PRIORITY_FIVE, new EntityAIWatchClosest(mob, EntityPlayer.class, MAX_WATCH_DISTANCE));

        if (mob instanceof EntityArcherBarbarian)
        {
            mob.tasks.addTask(PRIORITY_ONE, new EntityAIAttackArcher(mob));
        }
        else
        {
            mob.tasks.addTask(PRIORITY_ONE, new EntityAIRaiderAttackMelee(mob));
        }
    }

    /**
     * Get the mob Loot Table.
     *
     * @param mob The mob for which to get the Loot Table.
     * @return The loot table.
     */
    public static ResourceLocation getBarbarianLootTable(final EntityLiving mob)
    {
        if (mob instanceof EntityBarbarian)
        {
            return BarbarianLootTable;
        }
        else if (mob instanceof EntityArcherBarbarian)
        {
            return ArcherLootTable;
        }
        else if (mob instanceof EntityChiefBarbarian)
        {
            return ChiefLootTable;
        }
        else if (mob instanceof EntityPirate)
        {
            return PirateLootTable;
        }
        else if (mob instanceof EntityArcherPirate)
        {
            return PirateArcherLootTable;
        }
        else if (mob instanceof EntityChiefPirate)
        {
            return PirateChiefLootTable;
        }

        return BarbarianLootTable;
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *
     * @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     */
    public static void spawn(final ResourceLocation entityToSpawn, final int numberOfSpawns, final BlockPos spawnLocation, final World world)
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
                }
            });
        }
    }

    public static void setEquipment(final AbstractEntityMinecoloniesMob mob)
    {
        if (mob instanceof EntityBarbarian)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof EntityArcherBarbarian || mob instanceof EntityArcherPirate)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof EntityChiefBarbarian)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof AbstractEntityPirate)
        {
            mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof EntityChiefPirate)
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
