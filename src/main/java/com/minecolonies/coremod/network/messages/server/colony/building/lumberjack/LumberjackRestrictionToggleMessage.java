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
public class LumberjackRestrictionToggleMessage extends AbstractBuildingServerMessage<BuildingLumberjack>
{

    /**
     * Whether the lumberjack shouldbe restricted.
     */
    private boolean shouldRestrict;

    /**
     * Empty standard constructor.
     */
    public LumberjackRestrictionToggleMessage()
    {
        super();
    }

    /**
     * Creates a message which will be sent to set the restrict setting in the lumberjack.
     *
     * @param building       the building view of the lumberjack
     * @param shouldRestrict whether or not the lumberjack should be restricted.
     */
    public LumberjackRestrictionToggleMessage(final BuildingLumberjack.View building, final boolean shouldRestrict)
    {
        super(building);
        this.shouldRestrict = shouldRestrict;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        shouldRestrict = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(shouldRestrict);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingLumberjack building)
    {
        building.setShouldRestrict(shouldRestrict);
    }
}
