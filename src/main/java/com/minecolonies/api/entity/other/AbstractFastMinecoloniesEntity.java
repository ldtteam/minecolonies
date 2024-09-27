package com.minecolonies.api.entity.other;

import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.LookHandler;
import com.minecolonies.api.util.constant.ColonyConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Special abstract minecolonies mob that overrides laggy vanilla behaviour.
 */
public abstract class AbstractFastMinecoloniesEntity extends PathfinderMob implements IStuckHandlerEntity
{
    /**
     * Whether this entity can be stuck for stuckhandling
     */
    private boolean canBeStuck = true;

    /**
     * Random update variance for this entity, used to spread out updates equalls
     */
    public final int randomVariance = ColonyConstants.rand.nextInt(20);

    /**
     * Cache fluid state
     */
    private boolean isInFluid = false;

    /**
     * Cache fire state
     */
    private boolean onFire = false;

    /**
     * Entity push cache.
     */
    private List<Entity> entityPushCache = new ArrayList<>();

    /**
     * The timepoint at which the entity last collided
     */
    private long lastHorizontalCollision = 0;

    /**
     * Create a new instance.
     *
     * @param type    from type.
     * @param worldIn the world.
     */
    protected AbstractFastMinecoloniesEntity(final EntityType<? extends PathfinderMob> type, final Level worldIn)
    {
        super(type, worldIn);
        lookControl = new LookHandler(this);
    }

    @Override
    public boolean canBeLeashed(Player player)
    {
        return false;
    }

    @Override
    public boolean canBeStuck()
    {
        return canBeStuck;
    }

    /**
     * Sets whether the entity currently can be stuck
     *
     * @param canBeStuck
     */
    public void setCanBeStuck(final boolean canBeStuck)
    {
        this.canBeStuck = canBeStuck;
    }

    @Override
    protected boolean isHorizontalCollisionMinor(Vec3 vec3)
    {
        lastHorizontalCollision = level.getGameTime();
        return super.isHorizontalCollisionMinor(vec3);
    }

    /**
     * Whether the citizen collided in the last 10 ticks
     *
     * @return
     */
    public boolean hadHorizontalCollission()
    {
        return level.getGameTime() - lastHorizontalCollision < 10;
    }

    @Override
    public boolean checkBedExists()
    {
        return false;
    }

    @Override
    protected void removeFrost()
    {

    }

    @Override
    protected void tryAddFrost()
    {

    }

    @Override
    public void onInsideBubbleColumn(boolean down)
    {

    }

    @Override
    protected int decreaseAirSupply(final int supply)
    {
        return supply - 1;
    }

    @Override
    protected int increaseAirSupply(final int supply)
    {
        return supply + 1;
    }

    @Override
    protected void onChangedBlock(final BlockPos pos)
    {
        // This just tries to apply soulspeed or frostwalker
    }

    /**
     * Ignores cramming
     */
    @Override
    public void pushEntities()
    {
        if (this.level().isClientSide())
        {
            this.level.getEntities(EntityTypeTest.forClass(Player.class), this.getBoundingBox(), EntityUtils.pushableBy()).forEach(this::doPush);
        }
        else
        {
            if (this.tickCount % 10 == randomVariance % 10)
            {
                entityPushCache = this.level.getEntities(this, this.getBoundingBox(), EntityUtils.pushableBy());
            }

            if (!entityPushCache.isEmpty())
            {
                for (int i = 0, entityPushCacheSize = entityPushCache.size(); i < entityPushCacheSize; i++)
                {
                    final Entity entity = entityPushCache.get(i);
                    if (getBoundingBox().contains(entity.position()))
                    {
                        this.doPush(entity);
                    }
                }
            }
        }
    }

    /**
     * Prevent citizens and visitors from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerLevel serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
    }

    @Override
    public boolean canSpawnSprintParticle()
    {
        return false;
    }

    @Override
    public void updateFluidOnEyes()
    {
        if (tickCount % 20 == randomVariance)
        {
            super.updateFluidOnEyes();
        }
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing()
    {
        if (tickCount % 10 == randomVariance % 10)
        {
            isInFluid = super.updateInWaterStateAndDoFluidPushing();
        }

        return isInFluid;
    }

    @Override
    public void setSharedFlagOnFire(boolean newState)
    {
        if (newState != onFire)
        {
            super.setSharedFlagOnFire(newState);
            onFire = newState;
        }
    }

    @Override
    protected void handleNetherPortal()
    {
        // Noop our entities dont use portals
    }

    @Override
    public void updateSwimming()
    {
        // Noop our entities dont swim
    }

    @Override
    public boolean isInWall()
    {
        if (tickCount % 10 == randomVariance % 10)
        {
            return super.isInWall();
        }

        return false;
    }

    @Override
    public boolean isInWaterRainOrBubble()
    {
        // Used to extinguish fire, only check if on fire
        if (getRemainingFireTicks() > 0 || level.isClientSide)
        {
            return super.isInWaterRainOrBubble();
        }

        return false;
    }

    @Override
    protected void tryAddSoulSpeed()
    {

    }

    @Override
    protected void removeSoulSpeed()
    {

    }

    @Override
    public boolean canSpawnSoulSpeedParticle()
    {
        return false;
    }

    @Override
    public void updateFallFlying()
    {
        // Simplified updateFallflying to only set flags when they did change
        if (!this.level().isClientSide && tickCount % 5 == randomVariance % 5)
        {
            boolean flag = this.getSharedFlag(7);
            if (!flag || this.onGround() || this.isPassenger() || this.hasEffect(MobEffects.LEVITATION))
            {
                flag = false;
                this.setSharedFlag(7, flag);
            }
        }
    }

    @Override
    protected void sendDebugPackets()
    {
    }

    @Override
    public void setTicksFrozen(int p_146918_)
    {

    }

    @Override
    public void updateSwimAmount()
    {

    }

    /**
     * Get the team this entity is assigned to.
     *
     * @return the team instance.
     */
    @Nullable
    protected abstract PlayerTeam getAssignedTeam();

    @Override
    @Nullable
    public final Team getTeam()
    {
        final PlayerTeam assignedTeam = getAssignedTeam();
        registerToTeamInternal(assignedTeam);
        return assignedTeam;
    }

    /**
     * Register this entity to its own assigned team.
     */
    public void registerToTeam()
    {
        registerToTeamInternal(getAssignedTeam());
    }

    /**
     * Internal method for team registration.
     *
     * @param team the team to register to.
     */
    private void registerToTeamInternal(@Nullable final PlayerTeam team)
    {
        if (team != null && !isInTeam(team))
        {
            level.getScoreboard().addPlayerToTeam(getScoreboardName(), team);
        }
    }

    /**
     * Remove the entity from its own assigned team.
     */
    public void removeFromTeam()
    {
        final PlayerTeam team = getAssignedTeam();
        if (team != null && isInTeam(team))
        {
            level.getScoreboard().removePlayerFromTeam(getScoreboardName(), team);
        }
    }

    /**
     * Check if the current entity is assigned to the provided team.
     *
     * @param team the input team.
     * @return true if so.
     */
    private boolean isInTeam(@NotNull final PlayerTeam team)
    {
        return Objects.equals(level.getScoreboard().getPlayersTeam(getScoreboardName()), team);
    }

    @Override
    public void remove(@NotNull final RemovalReason reason)
    {
        super.remove(reason);
        removeFromTeam();
    }

    /**
     * Static Byte values to avoid frequent autoboxing
     */
    final Byte ENABLE  = 2;
    final Byte DISABLE = 0;

    @Override
    public void setShiftKeyDown(boolean enable)
    {
        if (enable)
        {
            this.entityData.set(DATA_SHARED_FLAGS_ID, ENABLE);
        }
        else
        {
            this.entityData.set(DATA_SHARED_FLAGS_ID, DISABLE);
        }
    }

    @Override
    public boolean isShiftKeyDown()
    {
        return (this.entityData.get(DATA_SHARED_FLAGS_ID)).byteValue() == ENABLE.byteValue();
    }
}
