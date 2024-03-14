package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.entity.ai.visitor.EntityAIExpeditionary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Visitor type for expeditionary visitors in the town hall.
 */
public class ExpeditionaryVisitorType implements IVisitorType
{
    /**
     * Extra data fields.
     */
    public static final ColonyExpeditionData EXTRA_DATA_EXPEDITION = new ColonyExpeditionData();

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
        return List.of(EXTRA_DATA_EXPEDITION);
    }

    @Override
    public @NotNull InteractionResult onPlayerInteraction(final AbstractEntityVisitor visitor, final Player player, final Level level, final InteractionHand hand)
    {
        visitor.getEntityStateController().setCurrentDelay(TICKS_SECOND * 10);
        visitor.getNavigation().stop();
        visitor.getLookControl().setLookAt(player);
        return InteractionResult.PASS;
    }

    /**
     * Extra data for storing the expedition builder instance.
     */
    public static class ColonyExpeditionData extends AbstractVisitorExtraData<ColonyExpedition>
    {
        public ColonyExpeditionData()
        {
            super("expedition", new ColonyExpedition(new ArrayList<>(), new ArrayList<>(), ExpeditionStatus.CREATED, -1, Level.OVERWORLD, new ResourceLocation("")));
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            getValue().write(compound);
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            setValue(ColonyExpedition.loadFromNBT(compoundTag));
        }
    }
}
