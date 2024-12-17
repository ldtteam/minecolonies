package com.minecolonies.api.entity.mobs.vikings;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all norsemen entities.
 */
public abstract class AbstractEntityNorsemenRaider extends AbstractEntityMinecoloniesRaider
{
    /**
     * Swim speed for norsemen
     */
    private static final double PIRATE_SWIM_BONUS = 2.3;

    /**
     * Amount of unique norsemen textures.
     */
    private static final int NORSEMEN_TEXTURES = 3;

    /**
     * Constructor method for Abstract norsemen.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityNorsemenRaider(final EntityType<? extends AbstractEntityNorsemenRaider> type, final Level world)
    {
        super(type, world, NORSEMEN_TEXTURES);
    }

    @Override
    public void playAmbientSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && level().random.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
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
