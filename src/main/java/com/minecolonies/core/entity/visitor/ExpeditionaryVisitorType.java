package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.visitor.*;
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
        return List.of(EXTRA_DATA_EXPEDITION_TYPE);
    }

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
}
