package com.minecolonies.coremod.network.messages.server.colony.building.sifter;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
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
     * The sifter mode.
     */
    private ItemStack block;

    /**
     * The sifter mode.
     */
    private ItemStack mesh;

    /**
     * If this includes the buy action.
     */
    private boolean buy;

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
     * @param mesh          the mesh.
     * @param buy           if its a buy action.
     */
    public SifterSettingsMessage(@NotNull final BuildingSifter.View building, final ItemStorage block, final ItemStorage mesh, final int dailyQuantity, final boolean buy)
    {
        super(building);
        this.quantity = dailyQuantity;
        this.block = block.getItemStack();
        this.mesh = mesh.getItemStack();
        this.buy = buy;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        quantity = buf.readInt();
        block = buf.readItemStack();
        mesh = buf.readItemStack();
        buy = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(quantity);
        buf.writeItemStack(block);
        buf.writeItemStack(mesh);
        buf.writeBoolean(buy);
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

            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.toomuch", qty));
        }

        if (buy)
        {
            final PlayerEntity player = ctxIn.getSender();
            if (player == null)
            {
                return;
            }

            if(!player.isCreative()) {
                final int slot = InventoryUtils.
                        findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory),
                                itemStack -> itemStack.isItemEqual(mesh));

                //If the player doesn't have the item in the inventory, do not change anything
                if(slot < 0) {
                    return;
                }

                player.inventory.decrStackSize(slot, 1);
            }
        }

        building.setup(new ItemStorage(block), new ItemStorage(mesh), qty);
    }
}
