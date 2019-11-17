package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.AbstractInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.TriggerServerResponseHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;

/**
 * The client side interaction response handler for citizens.
 */
public class ClientCitizenInteractionResponseHandler extends AbstractInteractionResponseHandler
{
    /**
     * The colony id of the citizen.
     */
    private final int colonyId;

    /**
     * The id of the specific citizen.
     */
    private final int citizenId;

    /**
     * The dimension id of the colony.
     */
    private final int dimension;

    /**
     * Create an instance of the client side citizen response handler.
     * @param colonyId the colony id of the citizen.
     * @param citizenId the citizens id itself.
     * @param dimension the dimension the colony is in.
     * @param primary if primary interaction.
     * @param priority the priority of this interaction.
     * @param inquiry the inquiry of the citizen.
     * @param responseTuples all possible responses of the player mapped to the next inquiry.
     */
    @SafeVarargs
    public ClientCitizenInteractionResponseHandler(final int colonyId, final int citizenId, final int dimension, final boolean primary, final ChatPriority priority, final ITextComponent inquiry, final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        super(inquiry, primary, priority, responseTuples);
        this.colonyId = colonyId;
        this.citizenId = citizenId;
        this.dimension = dimension;
    }

    /**
     * Create an instance of the citizen interaction response handler for the client side from nbt..
     * @param colonyId the colony id.
     * @param citizenId the citizen id.
     * @param dimension the dimension id.
     * @param compoundNBT the compound to deserialize the actual data from.
     */
    public ClientCitizenInteractionResponseHandler(final int colonyId, final int citizenId, final int dimension, final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
        this.colonyId = colonyId;
        this.citizenId = citizenId;
        this.dimension = dimension;
    }

    @Override
    public void onResponseTriggered(final ITextComponent response)
    {
        Network.getNetwork().sendToServer(new TriggerServerResponseHandler(this.colonyId, this.citizenId, this.dimension, this.getInquiry(), response));
    }
}
