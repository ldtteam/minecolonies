package com.minecolonies.coremod.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TEXTURE_UUID;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;

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
    private List<IVisitorExtraData<?>> extraData;

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
        this.extraData = visitorType.getExtraDataKeys();
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
        final ResourceLocation visitorTypeKey = new ResourceLocation(nbt.contains(TAG_VISITOR_TYPE) ? nbt.getString(TAG_VISITOR_TYPE) : "");
        final IVisitorType visitorType = IMinecoloniesAPI.getInstance().getVisitorTypeRegistry().getValue(visitorTypeKey);
        final IVisitorData data = new VisitorData(nbt.getInt(TAG_ID), colony, visitorType);
        data.deserializeNBT(nbt);
        return data;
    }

    @Override
    public EntityType<? extends AbstractEntityVisitor> getEntityType()
    {
        return visitorType.getEntityType();
    }

    @Override
    public <T> T getExtraDataValue(final IVisitorExtraData<T> extraData)
    {
        return this.extraData.stream().filter(f -> f.getKey().equals(extraData.getKey())).findFirst().orElse(extraData.getDefaultValue());
    }

    @Override
    public void updateEntityIfNecessary()
    {
        if (getEntity().isPresent())
        {
            final Entity entity = getEntity().get();
            if (entity.isAlive() && WorldUtil.isEntityBlockLoaded(entity.level, entity.blockPosition()))
            {
                return;
            }
        }

        if (getLastPosition() != BlockPos.ZERO && (getLastPosition().getX() != 0 && getLastPosition().getZ() != 0) && WorldUtil.isEntityBlockLoaded(getColony().getWorld(),
          getLastPosition()))
        {
            getColony().getVisitorManager().spawnOrCreateCivilian(this, getColony().getWorld(), getLastPosition(), true);
        }
        else if (getHomeBuilding() != null)
        {
            if (WorldUtil.isEntityBlockLoaded(getColony().getWorld(), getHomeBuilding().getID()))
            {
                final BlockPos spawnPos = BlockPosUtil.findSpawnPosAround(getColony().getWorld(), getHomeBuilding().getID());
                if (spawnPos != null)
                {
                    getColony().getVisitorManager().spawnOrCreateCivilian(this, getColony().getWorld(), spawnPos, true);
                }
            }
        }
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
        for (final IVisitorExtraData<?> extraDataKey : extraData)
        {
            if (nbtTagCompound.contains(extraDataKey.getKey()))
            {
                extraDataKey.deserializeNBT(nbtTagCompound.getCompound(extraDataKey.getKey()));
            }
        }

        // TODO: 1.20.2 Remove backwards compat for old visitor data
        if (nbtTagCompound.contains(TAG_SITTING))
        {
            regularVisitorData.setSittingPosition(BlockPosUtil.read(nbtTagCompound, TAG_SITTING));
            final ItemStack itemStack = ItemStack.of(nbtTagCompound.getCompound(TAG_RECRUIT_COST));
            itemStack.setCount(nbtTagCompound.getInt(TAG_RECRUIT_COST_QTY));
            regularVisitorData.setRecruitCost(itemStack);
            if (nbtTagCompound.contains(TAG_TEXTURE_UUID))
            {
                regularVisitorData.setTextureUUID(nbtTagCompound.getUUID(TAG_TEXTURE_UUID));
            }
        }
    }

    @Override
    public void applyResearchEffects()
    {
        // no research effects for now
    }
}
