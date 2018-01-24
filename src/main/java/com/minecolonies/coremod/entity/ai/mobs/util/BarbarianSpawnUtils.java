package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.*;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.entity.EntityList;
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

import java.util.stream.IntStream;

/**
 * Utils used for Barbarian Spawning
 */
public final class BarbarianSpawnUtils
{
    /**
     * Loot tables for Barbarians.
     */
    public static final ResourceLocation BarbarianLootTable = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
    public static final ResourceLocation ArcherLootTable    = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
    public static final ResourceLocation ChiefLootTable     = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");

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
    private static final double MOVEMENT_SPEED              = 0.2D;
    private static final double ARMOR                       = 2.0D;
    private static final double BARBARIAN_BASE_HEALTH       = 10;
    private static final double BARBARIAN_HEALTH_MULTIPLIER = 0.25;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianSpawnUtils()
    {
    }

    /**
     * Set barbarian attributes.
     *
     * @param barbarian The barbarian to set the attributes on.
     * @param colony    The colony that the barbarian is attacking.
     */
    public static void setBarbarianAttributes(final AbstractEntityBarbarian barbarian, final Colony colony)
    {
        barbarian.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getHealthBasedOnRaidLevel(colony));
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
            return BARBARIAN_BASE_HEALTH + raidLevel;
        }
        return BARBARIAN_BASE_HEALTH;
    }

    /**
     * Set barbarian AI Tasks.
     *
     * @param barbarian The barbarian to set the AI Tasks on.
     */
    public static void setBarbarianAI(final AbstractEntityBarbarian barbarian)
    {
        barbarian.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(barbarian));
        barbarian.tasks.addTask(PRIORITY_FOUR, new EntityAIWalkToRandomHuts(barbarian, AI_MOVE_SPEED));
        barbarian.targetTasks.addTask(PRIORITY_TWO, new EntityAINearestAttackableTarget<>(barbarian, EntityPlayer.class, true));
        barbarian.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(barbarian, EntityCitizen.class, true));
        barbarian.tasks.addTask(PRIORITY_FIVE, new EntityAIWatchClosest(barbarian, EntityPlayer.class, MAX_WATCH_DISTANCE));

        if (barbarian instanceof EntityArcherBarbarian)
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIAttackArcher(barbarian));
        }
        else
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIBarbarianAttackMelee(barbarian));
        }
    }

    /**
     * Get the barbarians Loot Table.
     *
     * @param barbarian The barbarian for which to get the Loot Table.
     * @return The loot table.
     */
    public static ResourceLocation getBarbarianLootTable(final AbstractEntityBarbarian barbarian)
    {
        if (barbarian instanceof EntityBarbarian)
        {
            return BarbarianLootTable;
        }
        else if (barbarian instanceof EntityArcherBarbarian)
        {
            return ArcherLootTable;
        }
        else if (barbarian instanceof EntityChiefBarbarian)
        {
            return ChiefLootTable;
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
                    setBarbarianEquipment(entity);
                    entity.setPositionAndRotation(x, y + 1.0, z, (float) MathHelper.wrapDegrees(world.rand.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    entity.applyInternalEntityAttributes();
                    CompatibilityUtils.spawnEntity(world, entity);
                }
            });
        }
    }

    public static void setBarbarianEquipment(final AbstractEntityBarbarian barbarian)
    {
        if (barbarian instanceof EntityBarbarian)
        {
            barbarian.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (barbarian instanceof EntityArcherBarbarian)
        {
            barbarian.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (barbarian instanceof EntityChiefBarbarian)
        {
            barbarian.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chiefSword));
            barbarian.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            barbarian.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            barbarian.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            barbarian.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
    }
}
