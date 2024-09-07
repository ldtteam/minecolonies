package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.entity.ai.visitor.EntityAIExpeditionary;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DespawnTimeData.DespawnTime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_FINISHED_LEAVING_MESSAGE;
import static net.minecraft.world.level.Level.TICKS_PER_DAY;

/**
 * Visitor type for expeditionary visitors in the town hall.
 */
public class ExpeditionaryVisitorType implements IVisitorType
{
    /**
     * Extra data fields.
     */
    public static final DespawnTimeData EXTRA_DATA_DESPAWN_TIME = new DespawnTimeData();

    /**
     * Despawn time of 1 day.
     */
    public static final long DEFAULT_DESPAWN_TIME = TICKS_PER_DAY;

    @Override
    public ResourceLocation getId()
    {
        return ModVisitorTypes.EXPEDITIONARY_VISITOR_TYPE_ID;
    }

    @Override
    public Function<Level, AbstractEntityVisitor> getEntityCreator()
    {
        return ModEntities.EXPEDITIONARY::create;
    }

    @Override
    public void createStateMachine(final AbstractEntityVisitor visitor)
    {
        new EntityAIExpeditionary(visitor);
    }

    @Override
    public List<IVisitorExtraData<?>> getExtraDataKeys()
    {
        return List.of(EXTRA_DATA_DESPAWN_TIME);
    }

    @Override
    @NotNull
    public InteractionResult onPlayerInteraction(final AbstractEntityVisitor visitor, final Player player, final Level level, final InteractionHand hand)
    {
        visitor.getEntityStateController().setCurrentDelay(TICKS_SECOND * 10);
        visitor.getNavigation().stop();
        visitor.getLookControl().setLookAt(player);
        return InteractionResult.PASS;
    }

    @Override
    public void update(final IVisitorData visitor)
    {
        final Optional<AbstractEntityCitizen> entity = visitor.getEntity();
        if (entity.isEmpty())
        {
            return;
        }

        final ExpeditionStatus expeditionStatus = visitor.getColony().getExpeditionManager().getExpeditionStatus(visitor.getId());
        final DespawnTime despawnTime = visitor.getExtraDataValue(EXTRA_DATA_DESPAWN_TIME);
        if (expeditionStatus.mayRemoveVisitor() && despawnTime.isElapsed(visitor.getColony().getWorld()))
        {
            visitor.getColony().getVisitorManager().removeCivilian(visitor);

            if (expeditionStatus == ExpeditionStatus.CREATED || expeditionStatus == ExpeditionStatus.ACCEPTED)
            {
                visitor.getColony().getExpeditionManager().removeCreatedExpedition(visitor.getId());
            }
        }

        if (expeditionStatus == ExpeditionStatus.FINISHED && entity.get().getInventoryCitizen().isEmpty())
        {
            visitor.getColony().getVisitorManager().removeCivilian(visitor);
            MessageUtils.format(EXPEDITION_FINISHED_LEAVING_MESSAGE, visitor.getName()).sendTo(visitor.getColony()).forManagers();
        }
    }

    /**
     * Extra data for storing the despawn time.
     */
    public static class DespawnTimeData extends AbstractVisitorExtraData<DespawnTime>
    {
        /**
         * Holder for the despawn time data.
         *
         * @param start    when the data was last set.
         * @param duration how long the duration will be for.
         */
        public record DespawnTime(long start, long duration)
        {
            /**
             * Create a new instance for the despawn time.
             *
             * @param level    the level calling from.
             * @param duration the duration of the despawn timer.
             * @return the created instance.
             */
            public static DespawnTime fromNow(Level level, long duration)
            {
                return new DespawnTime(level.getGameTime(), duration);
            }

            /**
             * Check if the despawn timer has elapsed.
             *
             * @param level the level calling from.
             * @return true if so.
             */
            public boolean isElapsed(Level level)
            {
                return duration <= 0 || level.getGameTime() > start + duration;
            }
        }

        /**
         * Default constructor.
         */
        public DespawnTimeData()
        {
            super("despawn-time", new DespawnTime(0, 0));
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            compound.putLong("start", getValue().start);
            compound.putLong("time", getValue().duration);
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            setValue(new DespawnTime(compoundTag.getLong("start"), compoundTag.getLong("time")));
        }
    }
}
