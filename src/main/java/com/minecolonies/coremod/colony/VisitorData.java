package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MAX_HEALTH;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;

public class VisitorData extends CitizenData implements IVisitorData
{
    private static final String TAG_RECRUIT_COST = "rcost";

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
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compoundNBT = super.serializeNBT();
        CompoundNBT item = new CompoundNBT();
        recruitCost.write(item);
        compoundNBT.put(TAG_RECRUIT_COST, item);
        BlockPosUtil.write(compoundNBT, TAG_SITTING, sittingPosition);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbtTagCompound)
    {
        super.deserializeNBT(nbtTagCompound);
        sittingPosition = BlockPosUtil.read(nbtTagCompound, TAG_SITTING);
        recruitCost = ItemStack.read(nbtTagCompound.getCompound(TAG_RECRUIT_COST));
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
    public static IVisitorData loadVisitorFromNBT(final IColony colony, final CompoundNBT nbt)
    {
        final IVisitorData data = new VisitorData(nbt.getInt(TAG_ID), colony);
        data.deserializeNBT(nbt);
        return data;
    }

    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        super.serializeViewNetworkData(buf);
        buf.writeCompoundTag(recruitCost.write(new CompoundNBT()));
    }

    /**
     * Initializes the entities values from citizen data.
     */
    @Override
    public void initEntityValues()
    {
        if (!getCitizenEntity().isPresent())
        {
            return;
        }

        final AbstractEntityCitizen citizen = getCitizenEntity().get();

        citizen.setCitizenId(getId());
        citizen.getCitizenColonyHandler().setColonyId(getColony().getID());

        citizen.setIsChild(isChild());
        citizen.setCustomName(new StringTextComponent(getName()));

        citizen.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);

        citizen.setFemale(isFemale());
        citizen.setTextureId(getTextureId());

        citizen.getDataManager().set(DATA_COLONY_ID, getColony().getID());
        citizen.getDataManager().set(DATA_CITIZEN_ID, getId());
        citizen.getDataManager().set(DATA_IS_FEMALE, citizen.isFemale() ? 1 : 0);
        citizen.getDataManager().set(DATA_TEXTURE, citizen.getTextureId());
        citizen.getDataManager().set(DATA_IS_ASLEEP, isAsleep());
        citizen.getDataManager().set(DATA_IS_CHILD, isChild());
        citizen.getDataManager().set(DATA_BED_POS, getBedPos());

        citizen.getCitizenExperienceHandler().updateLevel();

        setLastPosition(citizen.getPosition());

        citizen.getCitizenJobHandler().onJobChanged(citizen.getCitizenJobHandler().getColonyJob());

        markDirty();
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
}
