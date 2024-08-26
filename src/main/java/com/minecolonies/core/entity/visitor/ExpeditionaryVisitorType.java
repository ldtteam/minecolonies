package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.entity.ai.visitor.EntityAIExpeditionary;
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
     * Despawn time of 20 minutes.
     */
    public static final int DEFAULT_DESPAWN_TIME = 24000;

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
        final Integer despawnTime = visitor.getExtraDataValue(EXTRA_DATA_DESPAWN_TIME);
        if (entity.get().level.getGameTime() >= despawnTime && expeditionStatus.equals(ExpeditionStatus.UNKNOWN))
        {
            visitor.getColony().getVisitorManager().removeCivilian(visitor);
        }

        if (expeditionStatus == ExpeditionStatus.FINISHED && entity.get().getInventoryCitizen().isEmpty())
        {
            visitor.getColony().getVisitorManager().removeCivilian(visitor);
            MessageUtils.format(EXPEDITION_FINISHED_LEAVING_MESSAGE, visitor.getName()).sendTo(visitor.getColony()).forManagers();
        }
    }

    /**
     * Extra data for storing the de-spawn time.
     */
    public static class DespawnTimeData extends AbstractVisitorExtraData<Integer>
    {
        public DespawnTimeData()
        {
            super("despawn-time", 0);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            compound.putInt("time", getValue());
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            setValue(compoundTag.getInt("time"));
        }
    }
}
