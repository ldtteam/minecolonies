package com.minecolonies.core.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.core.colony.VisitorData.TAG_EXTRA_DATA;
import static com.minecolonies.core.colony.VisitorData.TAG_VISITOR_TYPE;

/**
 * View data for visitors
 */
public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    /**
     * The type of the visitor.
     */
    private IVisitorType visitorType;

    /**
     * The extra data instances.
     */
    private List<IVisitorExtraData<?>> extraData;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorDataView(final int id, final IColonyView colony)
    {
        super(id, colony);
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        final CompoundTag compoundTag = buf.readNbt();
        if (compoundTag != null)
        {
            final ResourceLocation visitorTypeKey = new ResourceLocation(compoundTag.getString(TAG_VISITOR_TYPE));
            visitorType = IMinecoloniesAPI.getInstance().getVisitorTypeRegistry().getValue(visitorTypeKey);
            if (visitorType != null)
            {
                extraData = visitorType.getExtraDataKeys();

                final CompoundTag compound = compoundTag.getCompound(TAG_EXTRA_DATA);
                for (final IVisitorExtraData<?> extraDataKey : extraData)
                {
                    extraDataKey.deserializeNBT(compound.getCompound(extraDataKey.getKey()));
                }
            }
        }
    }

    @Override
    public EntityType<? extends AbstractEntityVisitor> getEntityType()
    {
        return visitorType.getEntityType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtraDataValue(final IVisitorExtraData<T> extraData)
    {
        return this.extraData.stream()
                 .filter(f -> f.equals(extraData))
                 .map(m -> (T) m.getValue())
                 .findFirst()
                 .orElseThrow();
    }
}
