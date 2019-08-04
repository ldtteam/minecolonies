package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the sifter mode from the GUI.
 */
public class SifterSettingsMessage extends AbstractMessage<SifterSettingsMessage, IMessage>
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
     * Empty constructor used when registering the message.
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimension = buf.readInt();
        quantity = buf.readInt();
        block = ByteBufUtils.readItemStack(buf);
        mesh = ByteBufUtils.readItemStack(buf);
        buy = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimension);
        buf.writeInt(quantity);
        ByteBufUtils.writeItemStack(buf, block);
        ByteBufUtils.writeItemStack(buf, mesh);
        buf.writeBoolean(buy);
    }

    @Override
    public void messageOnServerThread(final SifterSettingsMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingSifter building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingSifter.class);
            if (building != null)
            {
                int qty = message.quantity;
                if (qty > building.getMaxDailyQuantity())
                {
                    qty = building.getMaxDailyQuantity();
                    player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.sifter.toomuch", qty));
                }
                building.setup(new ItemStorage(message.block), new ItemStorage(message.mesh), qty);

                if (message.buy)
                {
                    InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), message.mesh);
                }
            }
        }
    }
}
