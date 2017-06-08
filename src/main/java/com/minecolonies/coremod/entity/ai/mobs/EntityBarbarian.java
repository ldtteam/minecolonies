package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.security.Key;

/**
 * Created by Asher on 5/6/17.
 */
public class EntityBarbarian extends EntityMob
{

    final Colony colony = ColonyManager.getClosestColony(world, this.getPosition());

    public EntityBarbarian(World worldIn)
    {
        super(worldIn);
        this.getAlwaysRenderNameTag();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        this.setEquipment();

        this.spawn();

        return super.onInitialSpawn(difficulty, livingdata);
    }

    protected void spawn()
    {
        Entity entity = null;

        ResourceLocation en = EntityList.getKey(EntityBarbarian.class);
        if (en != null)
        {
            entity = EntityList.createEntityByIDFromName(en, world);
            if (entity != null)
            {
                entity.setCustomNameTag("Barbarian Copy");
                entity.setAlwaysRenderNameTag(true);
                entity.setLocationAndAngles(this.posX + 1, this.posY, this.posZ + 1, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
                world.spawnEntity(entity);
            }
        }
    }

    protected void setEquipment()
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Here we set various attributes for our mob. Like maximum health, armor, speed, ...
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 2.3D, true));
        this.tasks.addTask(3, new EntityAIWalkToRandomHuts(this, 2.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    private void applyEntityAI()
    {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCitizen.class, true));
    }

    @Override
    public boolean getCanSpawnHere()
    {
        if (colony != null)
        {
            BlockPos location = colony.getCenter();
            final double distance = this.getDistance(location.getX(), location.getY(), location.getZ());
            final boolean innerBounds = (!(distance < 120) && !(distance > 160));
            return (innerBounds && !world.isDaytime()); //Not Inside Colony but within 160 blocks of colony
        }
        else
        {
            return false;
        }
    }

    @Override
    public World getEntityWorld()
    {
        return world;
    }

    @Override
    protected boolean isValidLightLevel()
    {
        return true;
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 5;
    }

    @Override
    protected boolean canDespawn()
    {
        if (world.isDaytime())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int getSpawnProbability()
    {
        return colony.getCitizens().size() * 2;
    }

}
