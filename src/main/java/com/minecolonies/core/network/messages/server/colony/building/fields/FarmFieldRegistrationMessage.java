package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

/**
 * TODO: Remove in 1.20.2
 */
public class FarmFieldRegistrationMessage extends AbstractColonyServerMessage
{
    /**
     * The field position.
     */
    private BlockPos position;

    /**
     * Forge default constructor
     */
    public FarmFieldRegistrationMessage()
    {
        super();
    }

    /**
     * @param position the field position.
     */
    public FarmFieldRegistrationMessage(IColony colony, BlockPos position)
    {
        super(colony);
        this.position = position;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        final Optional<IBuildingExtension> field = colony.getBuildingManager()
                                         .getBuildingExtension(f -> f.getBuildingExtensionType().equals(BuildingExtensionRegistries.farmField.get()) && f.getPosition().equals(position))
                                         .stream()
                                         .findFirst();

        if (field.isEmpty())
        {
            colony.getBuildingManager().addBuildingExtension(FarmField.create(position));
        }
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        position = buf.readBlockPos();
    }
}
