package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open an inventory.
 */
public class OpenCraftingGUIMessage extends AbstractMessage<OpenCraftingGUIMessage, IMessage>
{
    /**
     * The position of the inventory block/entity.
     */
    private BlockPos buildingId;

    /**
     * The colony id the field or building etc is in.
     */
    private int colonyId;

    /**
     * Size of the crafting grid.
     */
    private int gridSize;

    /**
     * Empty public constructor.
     */
    public OpenCraftingGUIMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building {@link AbstractBuildingView}
     */
    public OpenCraftingGUIMessage(@NotNull final AbstractBuildingView building, final int gridSize)
    {
        super();
        this.buildingId = building.getLocation();
        this.gridSize = gridSize;
        this.colonyId = building.getColony().getID();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.gridSize = buf.readInt();
        this.colonyId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.gridSize);
        buf.writeInt(this.colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void messageOnServerThread(final OpenCraftingGUIMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null && checkPermissions(colony, player))
        {
            final BlockPos pos = message.buildingId;
            player.openGui(MineColonies.instance, 0, player.world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static boolean checkPermissions(final Colony colony, final EntityPlayerMP player)
    {
        //Verify player has permission to change this huts settings
        return colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS);
    }
}
