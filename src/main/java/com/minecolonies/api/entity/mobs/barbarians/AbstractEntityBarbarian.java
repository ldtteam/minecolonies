package com.minecolonies.api.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.minecolonies.core.colony.events.raid.RaiderConstants.*;
import static com.minecolonies.core.colony.events.raid.RaiderConstants.ATTACK_DAMAGE;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityBarbarian extends AbstractEntityMinecoloniesMonster
{
    /**
     * Swim speed for barbarians
     */
    private static final double BARBARIAN_SWIM_BONUS = 2.0;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityBarbarian(final EntityType<? extends AbstractEntityBarbarian> type, final Level world)
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
    public RaiderType getRaiderType()
    {
        return RaiderType.BARBARIAN;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return BARBARIAN_SWIM_BONUS;
    }
}
