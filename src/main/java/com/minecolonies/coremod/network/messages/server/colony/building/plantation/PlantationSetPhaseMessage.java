package com.minecolonies.coremod.network.messages.server.colony.building.plantation;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the plantation phase from the GUI.
 */
public class PlantationSetPhaseMessage extends AbstractBuildingServerMessage<BuildingPlantation>
{
    /**
     * The phase.
     */
    private ItemStack phase;

    /**
     * Empty constructor used when registering the
     */
    public PlantationSetPhaseMessage()
    {
        super();
    }

    /**
     * Set the phase of the planter.
     *
     * @param building      the building to set it for.
     * @param phase         the phase to set.
     */
    public PlantationSetPhaseMessage(@NotNull final BuildingPlantation.View building, final Item phase)
    {
        super(building);
        this.phase = new ItemStack(phase);
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        phase = buf.readItemStack();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeItemStack(phase);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingPlantation building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        building.setPhase(phase.getItem());
    }
}
