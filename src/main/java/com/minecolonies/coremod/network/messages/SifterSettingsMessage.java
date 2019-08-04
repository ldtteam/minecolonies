package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the sifter mode from the GUI.
 */
public class SifterSettingsMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the sifter.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

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
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.quantity = dailyQuantity;
        this.block = block.getItemStack();
        this.mesh = mesh.getItemStack();
        this.dimension = building.getColony().getDimension();
        this.buy = buy;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
        quantity = buf.readInt();
        block = buf.readItemStack();
        mesh = buf.readItemStack();
        buy = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
        buf.writeInt(quantity);
        buf.writeItemStack(block);
        buf.writeItemStack(mesh);
        buf.writeBoolean(buy);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingSifter building = colony.getBuildingManager().getBuilding(buildingId, BuildingSifter.class);
            if (building != null)
            {
                int qty = quantity;
                if (qty > building.getMaxDailyQuantity())
                {
                    qty = building.getMaxDailyQuantity();
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.toomuch", qty));
                }
                building.setup(new ItemStorage(block), new ItemStorage(mesh), qty);

                if (buy)
                {
                    InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), mesh);
                }
            }
        }
    }
}
