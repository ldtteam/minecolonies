package com.minecolonies.core.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;

/**
 * Data for visitors
 */
public class VisitorData extends CitizenData implements IVisitorData
{
    /**
     * Recruit nbt tag
     */
    private static final String TAG_RECRUIT_COST = "rcost";
    private static final String TAG_RECRUIT_COST_QTY = "rcostqty";

    /**
     * The position the citizen is sitting at
     */
    private BlockPos sittingPosition = BlockPos.ZERO;

    /**
     * The recruitment level, used for stats/equipment and costs
     */
    private ItemStack recruitCost = ItemStack.EMPTY;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorData(final int id, final IColony colony)
    {
        super(id, colony);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundNBT = super.serializeNBT();
        CompoundTag item = new CompoundTag();
        recruitCost.save(item);
        compoundNBT.put(TAG_RECRUIT_COST, item);
        compoundNBT.putInt(TAG_RECRUIT_COST_QTY, recruitCost.getCount());
        BlockPosUtil.write(compoundNBT, TAG_SITTING, sittingPosition);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbtTagCompound)
    {
        super.deserializeNBT(nbtTagCompound);
        sittingPosition = BlockPosUtil.read(nbtTagCompound, TAG_SITTING);
        recruitCost = ItemStack.of(nbtTagCompound.getCompound(TAG_RECRUIT_COST));
        recruitCost.setCount(nbtTagCompound.getInt(TAG_RECRUIT_COST_QTY));
    }

    @Override
    public void setRecruitCosts(final ItemStack item)
    {
        this.recruitCost = item;
    }

    @Override
    public ItemStack getRecruitCost()
    {
        return recruitCost;
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
        final IVisitorData data = new VisitorData(nbt.getInt(TAG_ID), colony);
        data.deserializeNBT(nbt);
        return data;
    }

    @Override
    public void serializeViewNetworkData(@NotNull final FriendlyByteBuf buf)
    {
        super.serializeViewNetworkData(buf);
        buf.writeItem(recruitCost);
        buf.writeInt(recruitCost.getCount());
    }

    @Override
    public BlockPos getSittingPosition()
    {
        return sittingPosition;
    }

    @Override
    public void setSittingPosition(final BlockPos pos)
    {
        this.sittingPosition = pos;
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
    public void applyResearchEffects()
    {
        // no research effects for now
    }
}
