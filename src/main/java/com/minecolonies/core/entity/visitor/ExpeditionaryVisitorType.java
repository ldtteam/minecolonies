package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import com.minecolonies.core.entity.ai.visitor.EntityAIExpeditionary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

/**
 * Visitor type for expeditionary visitors in the town hall.
 */
public class ExpeditionaryVisitorType implements IVisitorType
{
    /**
     * Extra data fields.
     */
    public static final ColonyExpeditionTypeData EXTRA_DATA_EXPEDITION_TYPE = new ColonyExpeditionTypeData();
    public static final ExpeditionData           EXTRA_DATA_EXPEDTIION      = new ExpeditionData();

    @Override
    public ResourceLocation getId()
    {
        return ModVisitorTypes.EXPEDITIONARY_VISITOR_TYPE_ID;
    }

    @Override
    public EntityType<? extends AbstractEntityVisitor> getEntityType()
    {
        return ModEntities.EXPEDITIONARY;
    }

    @Override
    public void createStateMachine(final AbstractEntityVisitor visitor)
    {
        new EntityAIExpeditionary(visitor);
    }

    @Override
    public List<IVisitorExtraData<?>> getExtraDataKeys()
    {
        return List.of(EXTRA_DATA_EXPEDITION_TYPE, EXTRA_DATA_EXPEDTIION);
    }

    /**
     * Extra data for storing the expedition type instance.
     */
    public static class ColonyExpeditionTypeData extends AbstractVisitorExtraData<Optional<ColonyExpeditionType>>
    {
        private static final String TAG_VALUE = "value";

        public ColonyExpeditionTypeData()
        {
            super("expedition-type", Optional.empty());
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            getValue().ifPresent(val -> compound.putString(TAG_VALUE, val.getId().toString()));
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            if (compoundTag.contains(TAG_VALUE))
            {
                setValue(Optional.ofNullable(ColonyExpeditionTypeManager.getInstance().getExpeditionType(new ResourceLocation(compoundTag.getString(TAG_VALUE)))));
            }
        }
    }

    /**
     * Extra data for storing the expedition instance.
     */
    public static class ExpeditionData extends AbstractVisitorExtraData<Optional<Expedition>>
    {
        private static final String TAG_VALUE = "value";

        public ExpeditionData()
        {
            super("expedition", Optional.empty());
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            getValue().ifPresent(val -> {
                final CompoundTag valueCompound = new CompoundTag();
                val.write(valueCompound);
                compound.put(TAG_VALUE, valueCompound);
            });
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            if (compoundTag.contains(TAG_VALUE))
            {
                setValue(Optional.of(Expedition.loadFromNBT(compoundTag.getCompound(TAG_VALUE))));
            }
        }
    }
}
