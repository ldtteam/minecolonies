package com.minecolonies.api.entity.mobs.vikings;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.sounds.BarbarianSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all norsemen entities.
 */
public abstract class AbstractEntityNorsemen extends AbstractEntityMinecoloniesMob
{
    /**
     * Amount of unique norsemen textures.
     */
    private static final int NORSEMEN_TEXTURES = 3;

    /**
     * Texture id of the norsemen.
     */
    private int textureId;

    /**
     * Constructor method for Abstract norsemen..
     * @param type the type.
     * @param world the world.
     */
    public AbstractEntityNorsemen(final EntityType<? extends AbstractEntityNorsemen> type, final World world)
    {
        super(type, world);
        this.textureId = new Random().nextInt(NORSEMEN_TEXTURES);
    }

    @Override
    public void playAmbientSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && world.rand.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return BarbarianSounds.barbarianSay;
    }

    @Override
    protected float getSoundPitch()
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F + 1.0F;
    }


    @Override
    public boolean canSpawn(final IWorld worldIn, final SpawnReason spawnReasonIn)
    {
        return true;
    }

    /**
     * Get the unique texture id.
     * @return the texture id.
     */
    public int getTextureId()
    {
        return this.textureId;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigator()
    {
        AbstractAdvancedPathNavigate navigator = super.getNavigator();
        navigator.getPathingOptions().withStartSwimCost(2.5D).withSwimCost(1.1D);
        return navigator;
    }
}
