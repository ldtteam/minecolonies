package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public interface IFieldDataManager
{
    /**
     * Creates a new entry from a given {@link IColonyView}, the position as {@link BlockPos} and the data passed in as {@link ByteBuf}.
     *
     * @param colony        The {@link IColonyView} to which the new {@link IBuildingView} belongs.
     * @param position      The position of the new {@link IBuildingView}.
     * @param networkBuffer The data from which to load the new {@link IBuildingView} stored in the networks {@link ByteBuf}.
     * @return The {@link IBuildingView} with the data loaded from the {@link ByteBuf}.
     */
    IFieldView createViewFrom(final IColonyView colony, final BlockPos position, final FriendlyByteBuf networkBuffer);
}
