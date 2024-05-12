package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.core.entity.other.SittingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.sounds.EventType.GREETING;
import static com.minecolonies.api.util.SoundUtils.playSoundAtCitizenWith;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

public abstract class AbstractCivilianEntity extends AbstractFastMinecoloniesEntity implements Npc
{
    /**
     * Time after which the next player collision is possible
     */
    protected long nextPlayerCollisionTime = 0;

    /**
     * Create a new instance.
     * @param type from type.
     * @param worldIn the world.
     */
    protected AbstractCivilianEntity(final EntityType<? extends PathfinderMob> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    public abstract void setCivilianData(@Nullable ICivilianData data);

    /**
     * Setter for the citizen data.
     *
     * @return civilian data
     */
    public abstract ICivilianData getCivilianData();

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    public abstract void markDirty(final int time);

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    public abstract int getCivilianID();

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    public abstract void setCitizenId(int id);

    @Override
    public boolean checkBedExists()
    {
        if (tickCount % 5 == randomVariance % 5)
        {
            return true;
        }

        if (getSleepingPos().isPresent())
        {
            final BlockPos pos = getSleepingPos().get();
            final BlockState state = level.getBlockState(getSleepingPos().get());
            return state.getBlock().isBed(state,level,pos,this);
        }

        return false;
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        if (entityIn instanceof ServerPlayer)
        {
            onPlayerCollide((Player) entityIn);
        }
        super.push(entityIn);
    }

    /**
     * On player collision action
     *
     * @param player
     */
    public void onPlayerCollide(final Player player)
    {
        if (player.level().getGameTime() > nextPlayerCollisionTime)
        {
            nextPlayerCollisionTime = player.level().getGameTime() + TICKS_SECOND * 15;
            getNavigation().stop();
            getLookControl().setLookAt(player);

            playSoundAtCitizenWith(level, blockPosition(), GREETING, getCivilianData());
        }
    }

    /**
     * Queue a sound at the citizen.
     *
     * @param soundEvent  the sound event to play.
     * @param length      the length of the event.
     * @param repetitions the number of times to play it.
     */
    public abstract void queueSound(@NotNull final SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions);

    /**
     * Queue a sound at the citizen.
     *
     * @param soundEvent  the sound event to play.
     * @param length      the length of the event.
     * @param repetitions the number of times to play it.
     */
    public abstract void queueSound(@NotNull final SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions, final float volume, final float pitch);

    @Override
    public String toString()
    {
        final ICivilianData data = getCivilianData();
        final String id = data == null ? "none" : "" + data.getId();
        final String colony = data == null ? "none" : "" + data.getColony().getName();
        return "Enity: " + getDisplayName().getString() + " Type: [" + getClass().getSimpleName() + "] at pos: " + blockPosition() + " civilian id: " + id + " colony: " + colony;
    }

    /**
     * Prevent riding entities except ours.
     *
     * @param entity entity to ride on
     * @param force  force flag
     * @return true if successful.
     */
    @Override
    public boolean startRiding(final @NotNull Entity entity, final boolean force)
    {
        if (entity instanceof SittingEntity || entity instanceof MinecoloniesMinecart)
        {
            return super.startRiding(entity, force);
        }
        return false;
    }
}
