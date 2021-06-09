package com.minecolonies.api.entity.mobs.vikings;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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
     * Random object.
     */
    private final Random random = new Random();

    /**
     * Constructor method for Abstract norsemen..
     *
     * @param type  the type.
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

        if (soundevent != null && level.random.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    protected float getVoicePitch()
    {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.0F;
    }

    @Override
    public boolean checkSpawnRules(final IWorld worldIn, final SpawnReason spawnReasonIn)
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
}
