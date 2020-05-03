package com.minecolonies.coremod.network.messages.server.colony.building.crusher;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the crusher mode from the GUI.
 */
public class CrusherSetModeMessage extends AbstractBuildingServerMessage<BuildingCrusher>
{
    /**
     * The quantity to produce.
     */
    private int quantity;

    /**
     * The crusher mode.
     */
    private ItemStack crusherMode;

    /**
     * Empty constructor used when registering the 
     */
    public CrusherSetModeMessage()
    {
        super();
    }

    /**
     * Set the mode of the crusher.
     *
     * @param building      the building to set it for.
     * @param dailyQuantity the quantity to produce.
     * @param crusherMode   the mode to set.
     */
    public CrusherSetModeMessage(@NotNull final BuildingCrusher.View building, final ItemStorage crusherMode, final int dailyQuantity)
    {
        super(building);
        this.quantity = dailyQuantity;
        this.crusherMode = crusherMode.getItemStack();
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        quantity = buf.readInt();
        crusherMode = buf.readItemStack();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(quantity);
        buf.writeItemStack(crusherMode);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingCrusher building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        int qty = quantity;
        if (qty > building.getMaxDailyQuantity())
        {
            qty = building.getMaxDailyQuantity();
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.crusher.toomuch", qty));
        }
        building.setCrusherMode(new ItemStorage(crusherMode), qty);
    }
}
