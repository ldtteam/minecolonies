package com.minecolonies.api.entity.mobs.drownedpirate;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.PathingStuckHandler;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all drowned pirate entities.
 */
public abstract class AbstractDrownedEntityPirate extends AbstractEntityRaiderMob
{
    /**
     * Swim speed for pirates
     */
    private static final double PIRATE_SWIM_BONUS = 2.3;

    /**
     * Amount of unique pirate textures.
     */
    private static final int PIRATE_TEXTURES = 4;

    /**
     * Texture id of the pirates.
     */
    private int textureId;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractDrownedEntityPirate(final EntityType<? extends AbstractDrownedEntityPirate> type, final Level world)
    {
        super(type, world);
        this.textureId = new Random().nextInt(PIRATE_TEXTURES);
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
    public boolean checkSpawnObstruction(final LevelReader level)
    {
        return level.isUnobstructed(this);
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
        if (this.newNavigator == null)
        {
            this.newNavigator = IPathNavigateRegistry.getInstance().getNavigateFor(this);
            this.navigation = newNavigator;
            newNavigator.setSwimSpeedFactor(getSwimSpeedFactor());
            newNavigator.getPathingOptions().withStartSwimCost(2.5D).withSwimCost(1.0D).withCanEnterDoors(true).withDropCost(1D).withJumpCost(1D).withWalkUnderWater(true).withNonLadderClimbableCost(1D).setPassDanger(true);
            PathingStuckHandler stuckHandler = PathingStuckHandler.createStuckHandler()
                                                 .withTakeDamageOnStuck(0.4f)
                                                 .withBuildLeafBridges()
                                                 .withChanceToByPassMovingAway(0.20)
                                                 .withPlaceLadders();

            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().raidersbreakblocks.get())
            {
                stuckHandler.withBlockBreaks();
                stuckHandler.withCompleteStuckBlockBreak(6);
            }

            newNavigator.setStuckHandler(stuckHandler);
            this.newNavigator.setCanFloat(true);
        }
        return newNavigator;
    }

    @Override
    protected int decreaseAirSupply(final int supply)
    {
        return supply;
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.PIRATE;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return PIRATE_SWIM_BONUS;
    }
}
