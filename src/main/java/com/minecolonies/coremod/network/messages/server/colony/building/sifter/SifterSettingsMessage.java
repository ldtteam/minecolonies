package com.minecolonies.coremod.network.messages.server.colony.building.sifter;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the sifter mode from the GUI.
 */
public class SifterSettingsMessage extends AbstractBuildingServerMessage<BuildingSifter>
{
    /**
     * The quantity to produce.
     */
    private int quantity;

    /**
     * Empty constructor used when registering the
     */
    public SifterSettingsMessage()
    {
        super();
    }

    /**
     * Set the mode of the sifter.
     *
     * @param building      the building to set it for.
     * @param dailyQuantity the quantity to produce.
     * @param block         the mode to set.
     */
    public SifterSettingsMessage(@NotNull final BuildingSifter.View building, final int dailyQuantity)
    {
        super(building);
        this.quantity = dailyQuantity;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        quantity = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(quantity);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingSifter building)
    {
        int qty = quantity;
        if (qty > building.getMaxDailyQuantity())
        {
            qty = building.getMaxDailyQuantity();

            final PlayerEntity player = ctxIn.getSender();
            if (player == null)
            {
                return;
            }

            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.toomuch", qty), player.getUniqueID());
        }

        building.setup(qty);
    }
}
