package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.entity.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.sounds.SoundManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
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
     * Whether this entity can be stuck for stuckhandling
     */
    private boolean canBeStuck = true;

    /**
     * Time after which the next player collision is possible
     */
    protected long nextPlayerCollisionTime = 0;

    /**
     * Sound manager of the civilian.
     */
    private SoundManager soundManager;

    /**
     * Create a new instance.
     * @param type from type.
     * @param worldIn the world.
     */
    protected AbstractCivilianEntity(final EntityType<? extends PathfinderMob> type, final Level worldIn)
    {
        super(type, worldIn);
        if (worldIn.isClientSide)
        {
            soundManager = new SoundManager((ClientLevel) worldIn);
        }
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
    public abstract void markDirty();

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
    public void tick()
    {
        super.tick();
        if (level.isClientSide)
        {
            soundManager.tick();
        }
    }

    @Override
    public boolean checkBedExists()
    {
        if (tickCount % 5 == randomVariance % 5)
        {
            return true;
        }

        // todo make university its own synch message.
        return this.getSleepingPos().map(pos-> {
            BlockState state = this.level.getBlockState(pos);
            return state.getBlock().isBed(state, this.level, pos, this);
        }).orElse(false);
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
        if (player.level.getGameTime() > nextPlayerCollisionTime)
        {
            nextPlayerCollisionTime = player.level.getGameTime() + TICKS_SECOND * 15;
            getNavigation().stop();
            getLookControl().setLookAt(player);

            playSoundAtCitizenWith(level, blockPosition(), GREETING, getCivilianData());
        }
    }

    /**
     * Get the sound manager.
     * @return the sound manager.
     */
    public SoundManager getSoundManager()
    {
        return soundManager;
    }
}
