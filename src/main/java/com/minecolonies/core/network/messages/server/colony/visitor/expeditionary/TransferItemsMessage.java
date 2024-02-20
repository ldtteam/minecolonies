package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Optional;

/**
 * Network message for transferring items to the expeditionary.
 */
public class TransferItemsMessage extends AbstractColonyServerMessage
{
    /**
     * The id of the visitor.
     */
    private int visitorId;

    /**
     * The id of the expedition type.
     */
    private ResourceLocation expeditionType;

    /**
     * The id of the requirement to fulfill.
     */
    private ResourceLocation requirementId;

    /**
     * Deserialization constructor.
     */
    public TransferItemsMessage()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param visitorViewData the visitor view data.
     * @param expeditionType  the expedition type.
     * @param requirementId   the requirement to fulfill.
     */
    public TransferItemsMessage(final IVisitorViewData visitorViewData, final ColonyExpeditionType expeditionType, final ResourceLocation requirementId)
    {
        super(visitorViewData.getColony());
        this.visitorId = visitorViewData.getId();
        this.expeditionType = expeditionType.getId();
        this.requirementId = requirementId;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer)
        {
            return;
        }

        final ColonyExpeditionType colonyExpeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionType);
        if (colonyExpeditionType == null)
        {
            Log.getLogger().warn("Transferring items for expedition failed, expedition type '{}' does not exist on the server side.", expeditionType);
            return;
        }

        final IVisitorData visitor = colony.getVisitorManager().getCivilian(visitorId);

        final Optional<ColonyExpeditionRequirement> requirement = colonyExpeditionType.getRequirements().stream()
                                                                    .filter(f -> f.getId().equals(requirementId))
                                                                    .findFirst();
        if (requirement.isPresent())
        {
            final RequirementHandler handler = requirement.get().createHandler(visitor::getInventory);
            final int needed = handler.getAmount() - handler.getAmountAvailable();

            if (needed > 0)
            {
                if (ctxIn.getSender().isCreative())
                {
                    InventoryUtils.addItemStackToItemHandler(visitor.getInventory(), handler.getDefaultItemStack().copyWithCount(needed));
                }
                else
                {
                    InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(new InvWrapper(ctxIn.getSender().getInventory()),
                      handler.getItemPredicate(),
                      needed,
                      visitor.getInventory());
                }
            }
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(visitorId);
        buf.writeResourceLocation(expeditionType);
        buf.writeResourceLocation(requirementId);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        visitorId = buf.readInt();
        expeditionType = buf.readResourceLocation();
        requirementId = buf.readResourceLocation();
    }
}
