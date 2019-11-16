package com.minecolonies.coremod.entity.citizen.citizenhandlers.responsehandlers;

import com.minecolonies.api.entity.ai.util.AbstractInteractionResponseHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The server side interaction response handler.
 */
public class ServerCitizenInteractionResponseHandler extends AbstractInteractionResponseHandler
{
    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param responseTuples the tuples mapping player responses to further interactions.
     */
    @SafeVarargs
    public ServerCitizenInteractionResponseHandler(final ITextComponent inquiry, final boolean primary, final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        super(inquiry, primary, responseTuples);
    }

    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public ServerCitizenInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
    }

    @Override
    public void onResponseTriggered(final ITextComponent response)
    {
        //todo server side action! very specific!
    }
}
