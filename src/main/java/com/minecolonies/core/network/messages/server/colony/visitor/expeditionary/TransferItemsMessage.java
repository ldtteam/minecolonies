package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Optional;

/**
 * Network message for transferring items to the expeditionary.
 */
public class TransferItemsMessage extends AbstractColonyServerMessage
{
    /**
     * The id of the expedition type.
     */
    private ResourceLocation expeditionTypeId;

    /**
     * The id of the requirement to fulfill.
     */
    private ResourceLocation requirementId;

    /**
     * In which hand the expedition sheet is.
     */
    private InteractionHand hand;

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
     * @param colonyView    the colony view.
     * @param requirementId the requirement to fulfill.
     */
    public TransferItemsMessage(final IColonyView colonyView, final ResourceLocation expeditionTypeId, final ResourceLocation requirementId, final InteractionHand hand)
    {
        super(colonyView);
        this.expeditionTypeId = expeditionTypeId;
        this.requirementId = requirementId;
        this.hand = hand;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer)
        {
            return;
        }

        final ItemStack itemInHand = ctxIn.getSender().getItemInHand(hand);
        final ExpeditionSheetContainerManager expeditionSheetContainerManager = new ExpeditionSheetContainerManager(itemInHand);

        final ColonyExpeditionType colonyExpeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionTypeId);
        if (colonyExpeditionType == null)
        {
            Log.getLogger().warn("Transferring items for expedition failed, expedition type '{}' does not exist on the server side.", expeditionTypeId);
            return;
        }

        final Optional<ColonyExpeditionRequirement> requirement = colonyExpeditionType.requirements().stream()
                                                                    .filter(f -> f.getId().equals(requirementId))
                                                                    .findFirst();
        if (requirement.isPresent())
        {
            final IItemHandler inventoryHandler = new InvWrapper(expeditionSheetContainerManager);

            final RequirementHandler handler = requirement.get().createHandler(inventoryHandler);
            final int needed = handler.getAmount() - handler.getAmountAvailable();

            if (needed > 0)
            {
                if (ctxIn.getSender().isCreative())
                {
                    InventoryUtils.addItemStackToItemHandler(inventoryHandler, handler.getDefaultItemStack().copyWithCount(needed));
                }
                else
                {
                    InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(new InvWrapper(ctxIn.getSender().getInventory()),
                      handler.getItemPredicate(),
                      needed,
                      inventoryHandler);
                }
            }
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(expeditionTypeId);
        buf.writeResourceLocation(requirementId);
        buf.writeEnum(hand);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        expeditionTypeId = buf.readResourceLocation();
        requirementId = buf.readResourceLocation();
        hand = buf.readEnum(InteractionHand.class);
    }
}
