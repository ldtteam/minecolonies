package com.minecolonies.core.colony.interactionhandling.registry;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager creating and loading an instance of the interactionResponseHandler from NBT.
 */
public final class InteractionResponseHandlerManager implements IInteractionResponseHandlerDataManager
{
    @Nullable
    @Override
    public IInteractionResponseHandler createFrom(@NotNull final HolderLookup.Provider provider, @NotNull final ICitizen citizen, @NotNull final CompoundTag compound)
    {
        final ResourceLocation handlerType =
          compound.contains(NbtTagConstants.TAG_HANDLER_TYPE)
            ? new ResourceLocation(Constants.MOD_ID, compound.getString(NbtTagConstants.TAG_HANDLER_TYPE))
            : ModInteractionResponseHandlers.STANDARD;
        final IInteractionResponseHandler handler = IInteractionResponseHandlerRegistry.getInstance().get(handlerType).getProducer().apply(citizen);
        if (handler != null)
        {
            try
            {
                handler.deserializeNBT(provider, compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("An Interaction %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  handlerType), ex);
                return null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Interaction type '%s' or missing constructor of proper format.", handlerType));
        }

        return handler;
    }
}
