package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    private final IColonyView                       colony;
    private       int                               entityId;
    private       double                            happiness;
    private       String                            job = "";
    private       int                               colonyId;
    private       List<IInteractionResponseHandler> primaryInteractions;
    private       boolean                           hasAnyPrimaryInteraction;
    private       boolean                           hasPrimaryBlockingInteractions;
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
        recruitmentCosts = ItemStack.read(buf.readCompoundTag());
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
