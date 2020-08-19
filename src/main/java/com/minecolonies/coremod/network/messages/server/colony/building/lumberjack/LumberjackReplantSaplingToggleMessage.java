package com.minecolonies.coremod.network.messages.server.colony.building.lumberjack;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Class used for setting whether saplings should be planted after lj chops a tree.
 */
public class LumberjackReplantSaplingToggleMessage extends AbstractBuildingServerMessage<BuildingLumberjack>
{

    /**
     * Whether the lumberjack should replant a sapling or not.
     */
    private boolean shouldReplant;

    /**
     * Empty standard constructor.
     */
    public LumberjackReplantSaplingToggleMessage()
    {
        super();
    }

    /**
     * Creates a message which will be sent to set the replant setting in the lumberjack.
     *
     * @param building      the building view of the lumberjack
     * @param shouldReplant whether or not the sapling should be replanted.
     */
    public LumberjackReplantSaplingToggleMessage(final BuildingLumberjack.View building, final boolean shouldReplant)
    {
        super(building);
        this.shouldReplant = shouldReplant;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        shouldReplant = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(shouldReplant);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingLumberjack building)
    {
        building.setShouldReplant(shouldReplant);
    }
}
