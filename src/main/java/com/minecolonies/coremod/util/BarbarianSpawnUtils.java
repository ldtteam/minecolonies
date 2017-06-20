package com.minecolonies.coremod.util;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.*;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.stream.IntStream;

/**
 * Utils for the Barbarians Spawning
 */
public final class BarbarianSpawnUtils
{
    /**
     * Values used for AI Task's Priorities
     */
    private static final int PRIORITY_ZERO  = 0;
    private static final int PRIORITY_ONE   = 1;
    private static final int PRIORITY_TWO   = 2;
    private static final int PRIORITY_THREE = 3;
    private static final int PRIORITY_FOUR  = 4;
    private static final int PRIORITY_FIVE  = 5;
    private static final int PRIORITY_SIX   = 6;
    private static final int PRIORITY_SEVEN = 7;
    private static final int PRIORITY_EIGHT = 8;

    /**
     * Other various values used for AI Tasks
     */
    private static final double MOVE_TOWARDS_RESTRICTION_SPEED = 1.0D;
    private static final double MOVE_THROUGH_VILLAGE_SPEED     = 1.0D;
    private static final double AI_MOVE_SPEED                  = 2.0D;
    private static final float  MAX_WATCH_DISTANCE             = 8.0F;

    /**
     * Values used for mob attributes
     */
    private static final double FOLLOW_RANGE          = 35.0D;
    private static final double MOVEMENT_SPEED        = 0.2D;
    private static final double ATTACK_DAMAGE         = 2.0D;
    private static final double ARMOR                 = 2.0D;
    private static final double BARBARIAN_BASE_HEALTH = 20;

    /**
     * Values used in Spawn() method
     */
    private static final float WHOLE_CIRCLE = 360.0F;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianSpawnUtils()
    {
    }

    /**
     * Centralized Barbarian Attributes are set here.
     *
     * @param barbarian the barbarian to set the Attributes on.
     */
    public static void setBarbarianAttributes(final EntityMob barbarian)
    {
        barbarian.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getHealthBasedOnRaidLevel(barbarian));
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @return returns the health in the form of a double
     */
    private static double getHealthBasedOnRaidLevel(final EntityMob barbarian)
    {
        final Colony colony = ColonyManager.getClosestColony(barbarian.getEntityWorld(), new BlockPos(barbarian));
        if (colony != null)
        {
            final int raidLevel = (int) (BarbarianUtils.getColonyRaidLevel(colony) * 1.5);
            return BARBARIAN_BASE_HEALTH + raidLevel;
        }
        return BARBARIAN_BASE_HEALTH;
    }

    /**
     * Centralized Barbarian AITasks are set here.
     *
     * @param barbarian the barbarian to set the AITasks on.
     */
    public static void setBarbarianAITasks(final EntityMob barbarian)
    {
        barbarian.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(barbarian));
        barbarian.tasks.addTask(PRIORITY_TWO, new EntityAIWalkToRandomHuts(barbarian, AI_MOVE_SPEED));
        barbarian.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(barbarian, EntityPlayer.class, true));
        barbarian.targetTasks.addTask(PRIORITY_FOUR, new EntityAINearestAttackableTarget<>(barbarian, EntityCitizen.class, true));
        barbarian.tasks.addTask(PRIORITY_FIVE, new EntityAIMoveTowardsRestriction(barbarian, MOVE_TOWARDS_RESTRICTION_SPEED));
        barbarian.tasks.addTask(PRIORITY_SIX, new EntityAIMoveThroughVillage(barbarian, MOVE_THROUGH_VILLAGE_SPEED, false));
        barbarian.tasks.addTask(PRIORITY_SEVEN, new EntityAIWatchClosest(barbarian, EntityPlayer.class, MAX_WATCH_DISTANCE));
        barbarian.tasks.addTask(PRIORITY_EIGHT, new EntityAILookIdle(barbarian));

        if (barbarian instanceof EntityBarbarian || barbarian instanceof EntityChiefBarbarian)
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIBarbarianAttackMelee(barbarian));
        }
        else if (barbarian instanceof EntityArcherBarbarian)
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIAttackArcher(barbarian));
        }
    }

    /**
     * Centralized Barbarian Equipment is set here.
     *
     * @param barbarian the barbarian to give the Equipment to.
     */
    public static void setBarbarianEquipment(final EntityMob barbarian)
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

    /**
     * Returns a barbarians loot table
     *
     * @param barbarian The barbarian for which to return the loot table
     */
    public static ResourceLocation getBarbarianLootTables(final EntityMob barbarian)
    {
        if (barbarian instanceof EntityBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
        }
        else if (barbarian instanceof EntityArcherBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
        }
        else if (barbarian instanceof EntityChiefBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");
        }
        else
        {
            return null;
        }
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
            if (!world.isBlockLoaded(spawnLocation))
            {
                final ForgeChunkManager.Ticket chunkTicket = ForgeChunkManager.requestTicket(MineColonies.instance, world, ForgeChunkManager.Type.NORMAL);
                if (chunkTicket != null)
                {
                    chunkTicket.getModData().setInteger("spawnX", spawnLocation.getX());
                    chunkTicket.getModData().setInteger("spawnY", spawnLocation.getY());
                    chunkTicket.getModData().setInteger("spawnZ", spawnLocation.getZ());
                    ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(spawnLocation.getX(), spawnLocation.getZ()));
                }
            }

            final int x = spawnLocation.getX();
            final int y = spawnLocation.getY();
            final int z = spawnLocation.getZ();

            IntStream.range(0, numberOfSpawns).forEach(theInteger ->
            {
                final EntityMob entity = (EntityMob) EntityList.createEntityByIDFromName(entityToSpawn, world);

                if (entity != null)
                {
                    setBarbarianEquipment(entity);
                    entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * WHOLE_CIRCLE), 0.0F);
                    world.spawnEntity(entity);
                }
            });
        }
    }
}
