package com.minecolonies.api.entity.mobs.vikings;

import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all norsemen entities.
 */
public abstract class AbstractEntityNorsemen extends AbstractEntityRaiderMob
{
    /**
     * Swim speed for pirates
     */
    private static final double PIRATE_SWIM_BONUS = 2.3;

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
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityNorsemen(final EntityType<? extends AbstractEntityNorsemen> type, final Level world)
    {
        super(type, world);
        this.textureId = new Random().nextInt(NORSEMEN_TEXTURES);
    }

    @Override
    public void playAmbientSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && level.random.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public float getVoicePitch()
    {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.0F;
    }

    @Override
    public boolean checkSpawnRules(final LevelAccessor worldIn, final MobSpawnType spawnReasonIn)
    {
        return true;
    }

    /**
     * Get the unique texture id.
     *
     * @return the texture id.
     */
    public int getTextureId()
    {
        return this.textureId;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigation()
    {
        AbstractAdvancedPathNavigate navigator = super.getNavigation();
        navigator.getPathingOptions().withStartSwimCost(2.5D).withSwimCost(1.1D);
        return navigator;
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.NORSEMAN;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return PIRATE_SWIM_BONUS;
    }
}
