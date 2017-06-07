package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Asher on 5/6/17.
 */
public class EntityArcherBarbarian extends AbstractArcherBarbarian {

    public EntityArcherBarbarian(World worldIn)
    {
        super(worldIn);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        this.setEquipment();
        return super.onInitialSpawn(difficulty, livingdata);
    }

    protected void setEquipment()
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    SoundEvent getStepSound()
    {
        return SoundEvents.ENTITY_ZOMBIE_STEP;
    }

    protected EntityArrow getArrow(float p_190726_1_)
    {
        ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

        if (itemstack.getItem() == Items.SPECTRAL_ARROW)
        {
            EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(this.world, this);
            entityspectralarrow.setEnchantmentEffectsFromEntity(this, p_190726_1_);
            return entityspectralarrow;
        }
        else
        {
            EntityArrow entityarrow = super.getArrow(p_190726_1_);

            if (itemstack.getItem() == Items.TIPPED_ARROW && entityarrow instanceof EntityTippedArrow)
            {
                ((EntityTippedArrow)entityarrow).setPotionEffect(itemstack);
            }

            return entityarrow;
        }
    }


    @Override
    public boolean getCanSpawnHere()
    {
        final Colony colony = ColonyManager.getClosestColony(world, this.getPosition());
        BlockPos location = colony.getCenter();
        final double distance = this.getDistance(location.getX(), location.getY(), location.getZ());
        final boolean innerBounds = (!(distance < 120) && !(distance > 160));
        if (innerBounds && !world.isDaytime()) //Not Inside Colony //Within range of Colony (directionX <= 160 && directionZ <= 160 ,, !(directionX < 120 && directionZ < 120))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected boolean isValidLightLevel()
    {
        return true;
        //return super.isValidLightLevel();
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
}
