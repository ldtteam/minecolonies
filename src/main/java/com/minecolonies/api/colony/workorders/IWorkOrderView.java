package com.minecolonies.api.colony.workorders;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public interface IWorkOrderView extends IWorkOrder
{
    /**
     * Whether this work order should be shown in a specific building.
     *
     * @param view the building view.
     * @return a boolean
     */
    boolean shouldShowIn(IBuildingView view);

    /**
     * Deserialize the attributes and variables from the given buffer
     *
     * @param buf Byte buffer to deserialize.
     */
    void deserialize(@NotNull FriendlyByteBuf buf);

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     *
     * @param builderLocation position of the builders own hut.
     * @param builderLevel    level of the builders hut.
     * @return true if so.
     */
    boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel);
}
