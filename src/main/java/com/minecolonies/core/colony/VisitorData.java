package com.minecolonies.core.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.visitor.ModVisitorTypes.VISITOR_TYPE_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.core.entity.visitor.RegularVisitorType.EXTRA_DATA_RECRUIT_COST;
import static com.minecolonies.core.entity.visitor.RegularVisitorType.EXTRA_DATA_SITTING_POSITION;

/**
 * Data for visitors
 */
public class VisitorData extends CitizenData implements IVisitorData
{
    /**
     * NBT tags.
     */
    public static final  String TAG_VISITOR_TYPE     = "visitorType";
    public static final  String TAG_EXTRA_DATA       = "extra";
    private static final String TAG_RECRUIT_COST     = "rcost";
    private static final String TAG_RECRUIT_COST_QTY = "rcostqty";

    /**
     * The type of the visitor.
     */
    private final IVisitorType visitorType;

    /**
     * The extra data instances.
     */
    private final List<IVisitorExtraData<?>> extraData;

    /**
     * Create a VisitorData given an ID. Used as a super-constructor or during loading.
     *
     * @param id          ID of the visitor.
     * @param colony      colony the visitor belongs to.
     * @param visitorType the type of the visitor.
     */
    public VisitorData(final int id, final IColony colony, final IVisitorType visitorType)
    {
        super(id, colony);
        this.visitorType = visitorType;
        this.extraData = List.copyOf(visitorType.getExtraDataKeys());
    }

    /**
     * Loads this citizen data from nbt
     *
     * @param colony colony to load for
     * @param nbt    nbt compound to read from
     * @return new CitizenData
     */
    public static IVisitorData loadVisitorFromNBT(final IColony colony, final CompoundTag nbt)
    {
        final ResourceLocation visitorTypeKey = nbt.contains(TAG_VISITOR_TYPE) ? new ResourceLocation(nbt.getString(TAG_VISITOR_TYPE)) : VISITOR_TYPE_ID;
        final IVisitorType visitorType = IMinecoloniesAPI.getInstance().getVisitorTypeRegistry().getValue(visitorTypeKey);
        final IVisitorData data = new VisitorData(nbt.getInt(TAG_ID), colony, visitorType);
        data.deserializeNBT(nbt);
        return data;
    }

    @Override
    @NotNull
    public IVisitorType getVisitorType()
    {
        return visitorType;
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> void setExtraDataValue(final IVisitorExtraData<T> extraData, final T value)
    {
        final IVisitorExtraData<T> foundExtraData = this.extraData.stream()
                                                      .filter(f -> f.equals(extraData))
                                                      .map(m -> (IVisitorExtraData<T>) m)
                                                      .findFirst()
                                                      .orElseThrow();
        foundExtraData.setValue(value);
    }

    @Override
    protected void respawnAfterUpdate(final BlockPos position)
    {
        getColony().getVisitorManager().spawnOrCreateVisitor(visitorType, this, getColony().getWorld(), position);
    }

    @Override
    public void serializeViewNetworkData(@NotNull final FriendlyByteBuf buf)
    {
        super.serializeViewNetworkData(buf);
        buf.writeNbt(serializeNBT());
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putString(TAG_VISITOR_TYPE, visitorType.getId().toString());

        final CompoundTag extraDataCompound = new CompoundTag();
        for (final IVisitorExtraData<?> extraDataKey : extraData)
        {
            extraDataCompound.put(extraDataKey.getKey(), extraDataKey.serializeNBT());
        }
        compound.put(TAG_EXTRA_DATA, extraDataCompound);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbtTagCompound)
    {
        super.deserializeNBT(nbtTagCompound);
        final CompoundTag extraDataCompound = nbtTagCompound.getCompound(TAG_EXTRA_DATA);
        for (final IVisitorExtraData<?> extraDataKey : extraData)
        {
            if (extraDataCompound.contains(extraDataKey.getKey()))
            {
                extraDataKey.deserializeNBT(extraDataCompound.getCompound(extraDataKey.getKey()));
            }
        }

        // TODO: Next major release: Remove backwards compat for old visitor data
        if (nbtTagCompound.contains(TAG_SITTING))
        {
            setExtraDataValue(EXTRA_DATA_SITTING_POSITION, BlockPosUtil.read(nbtTagCompound, TAG_SITTING));
            final ItemStack itemStack = ItemStack.of(nbtTagCompound.getCompound(TAG_RECRUIT_COST));
            itemStack.setCount(nbtTagCompound.getInt(TAG_RECRUIT_COST_QTY));
            setExtraDataValue(EXTRA_DATA_RECRUIT_COST, itemStack);
        }
    }

    @Override
    public void update()
    {
        super.update();
        visitorType.update(this);
    }

    @Override
    public void applyResearchEffects()
    {
        // no research effects for now
    }
}
