package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Implements the Archer Barbarian Entity
 */
public class EntityArcherBarbarian extends AbstractArcherBarbarian
{

    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");

    public EntityArcherBarbarian(final World worldIn)
    {
        super(worldIn);
    }

    protected EntityArrow getArrow(final float p_190726_1_)
    {
        final ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

        if (itemstack.getItem() == Items.SPECTRAL_ARROW)
        {
            final EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(this.world, this);
            entityspectralarrow.setEnchantmentEffectsFromEntity(this, p_190726_1_);
            return entityspectralarrow;
        }
        else
        {
            final EntityArrow entityarrow = super.getArrow(p_190726_1_);

            if (itemstack.getItem() == Items.TIPPED_ARROW && entityarrow instanceof EntityTippedArrow)
            {
                ((EntityTippedArrow) entityarrow).setPotionEffect(itemstack);
            }

            return entityarrow;
        }
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 5;
    }

    @Override
    protected boolean canDespawn()
    {
        return (world.isDaytime());
    }

    @Override
    protected SoundEvent getHurtSound()
    {
        return BarbarianSounds.barbarianHurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return BarbarianSounds.barbarianDeath;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return BarbarianSounds.barbarianSay;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LOOT;
    }
}
