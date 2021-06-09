package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * View data for visitors
 */
public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    /**
     * The related colony view
     */
    private final IColonyView colony;

    /**
     * The recruitment costs
     */
    private       ItemStack                         recruitmentCosts;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorDataView(final int id, final IColonyView colony)
    {
        super(id);
        this.colony = colony;
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        super.deserialize(buf);
        recruitmentCosts = buf.readItem();
    }

    @Override
    public IColonyView getColonyView()
    {
        return colony;
    }

    @Override
    public ItemStack getRecruitCost()
    {
        return recruitmentCosts;
    }
}
