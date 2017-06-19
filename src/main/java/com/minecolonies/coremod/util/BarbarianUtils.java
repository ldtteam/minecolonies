package com.minecolonies.coremod.util;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.*;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Utils for the Barbarians
 */
public class BarbarianUtils
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
     * Centralized Barbarian Attributes are set here.
     * @param barbarian the barbarian to set the Attributes on.
     */
    public static void setBarbarianAttributes(EntityMob barbarian)
    {
        barbarian.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        barbarian.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(ARMOR);
        barbarian.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BARBARIAN_BASE_HEALTH);
    }

    /**
     * Centralized Barbarian AITasks are set here.
     * @param barbarian the barbarian to set the AITasks on.
     */
    public static void setBarbarianAITasks(EntityMob barbarian)
    {
        barbarian.tasks.addTask(PRIORITY_ZERO, new EntityAISwimming(barbarian));
        barbarian.tasks.addTask(PRIORITY_TWO, new EntityAIWander(barbarian, AI_MOVE_SPEED));
        barbarian.targetTasks.addTask(PRIORITY_THREE, new EntityAINearestAttackableTarget<>(barbarian, EntityPlayer.class, true));
        barbarian.targetTasks.addTask(PRIORITY_FOUR, new EntityAINearestAttackableTarget<>(barbarian, EntityCitizen.class, true));
        barbarian.tasks.addTask(PRIORITY_FIVE, new EntityAIMoveTowardsRestriction(barbarian, MOVE_TOWARDS_RESTRICTION_SPEED));
        barbarian.tasks.addTask(PRIORITY_SIX, new EntityAIMoveThroughVillage(barbarian, MOVE_THROUGH_VILLAGE_SPEED, false));
        barbarian.tasks.addTask(PRIORITY_SEVEN, new EntityAIWatchClosest(barbarian, EntityPlayer.class, MAX_WATCH_DISTANCE));
        barbarian.tasks.addTask(PRIORITY_EIGHT, new EntityAILookIdle(barbarian));

        if(barbarian instanceof EntityBarbarian || barbarian instanceof EntityChiefBarbarian)
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIBarbarianAttackMelee(barbarian));
        }
        else if(barbarian instanceof EntityArcherBarbarian)
        {
            barbarian.tasks.addTask(PRIORITY_ONE, new EntityAIAttackArcher(barbarian));
        }

    }

    /**
     * Centralized Barbarian Equipment is set here.
     * @param barbarian the barbarian to give the Equipment to.
     */
    public static void setBarbarianEquipment(EntityMob barbarian)
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
     * @param barbarian The barbarian for which to return the loot table
     */
    public static ResourceLocation getBarbarianLootTables(EntityMob barbarian)
    {
        if(barbarian instanceof  EntityBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
        }
        else if(barbarian instanceof  EntityArcherBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
        }
        else if(barbarian instanceof EntityChiefBarbarian)
        {
            return new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");
        }
        else
        {
            return null;
        }
    }
}
