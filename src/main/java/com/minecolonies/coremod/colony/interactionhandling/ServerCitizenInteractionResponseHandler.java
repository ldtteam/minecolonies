package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.AbstractInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The server side interaction response handler.
 */
public abstract class ServerCitizenInteractionResponseHandler extends AbstractInteractionResponseHandler
{
    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param primary if primary interaction.
     * @param priority the interaction priority.
     * @param responseTuples the tuples mapping player responses to further interactions.
     */
    @SafeVarargs
    public ServerCitizenInteractionResponseHandler(final ITextComponent inquiry, final boolean primary, final ChatPriority priority, final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        super(inquiry, primary, priority, responseTuples);
    }

    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public ServerCitizenInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
    }
}
