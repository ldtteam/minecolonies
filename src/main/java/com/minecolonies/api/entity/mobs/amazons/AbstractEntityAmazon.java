package com.minecolonies.api.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import static com.minecolonies.core.colony.events.raid.RaiderConstants.*;
import static com.minecolonies.core.colony.events.raid.RaiderConstants.ATTACK_DAMAGE;

/**
 * Abstract for all amazon entities.
 */
public abstract class AbstractEntityAmazon extends AbstractEntityMinecoloniesMonster
{
    /**
     * Swim speed for amazons
     */
    private static final double AMAZON_SWIM_BONUS = 1.9;

    /**
     * Constructor method for Abstract amazon.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityAmazon(final EntityType<? extends AbstractEntityAmazon> type, final Level world)
    {
        super(type, world);
        initStatsFor(BARBARIAN_BASE_HEALTH * (world.getDifficulty().getId() + 0.1), world.getDifficulty().getId() + 0.1, ATTACK_DAMAGE * world.getDifficulty().getId());
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
    public boolean checkSpawnRules(final LevelAccessor worldIn, final MobSpawnType spawnReasonIn)
    {
        return true;
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.AMAZON;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return AMAZON_SWIM_BONUS;
    }
}
