package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.blocks.BlockHutTownHall;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildToolPlaceMessage extends AbstractMessage<BuildToolPlaceMessage, IMessage>
{
    /**
     * Language key for missing hut message.
     */
    private static final String NO_HUT_IN_INVENTORY = "com.minecolonies.coremod.gui.buildtool.nohutininventory";
    private String   hutDec;
    private String   style;
    private int      rotation;
    private BlockPos pos;
    private boolean  isHut;

    /**
     * Empty constructor used when registering the message.
     */
    public BuildToolPlaceMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param hutDec   String representation of sort of hutDec that made the request
     * @param style    String representation of style that was requested
     * @param pos      BlockPos
     * @param rotation int representation of the rotation
     * @param isHut    true if hut, false if decoration
     */
    public BuildToolPlaceMessage(final String hutDec, final String style, final BlockPos pos, final int rotation, final boolean isHut)
    {
        super();
        this.hutDec = hutDec;
        this.style = style;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        hutDec = ByteBufUtils.readUTF8String(buf);
        style = ByteBufUtils.readUTF8String(buf);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, hutDec);
        ByteBufUtils.writeUTF8String(buf, style);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);
    }

    @Override
    public void messageOnServerThread(final BuildToolPlaceMessage message, final EntityPlayerMP player)
    {
        final World world = player.world;
        if (message.isHut)
        {
            handleHut(world, player, message.hutDec, message.style, message.rotation, message.pos);
        }
        else
        {
            handleDecoration(world, player, message.hutDec, message.style, message.rotation, message.pos);
        }
    }

    /**
     * Handles the placement of huts.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param hut      The hut we are placing.
     * @param style    The style of the hut.
     * @param rotation The number of times the structure should be rotated.
     * @param buildPos The location the hut is being placed.
     */
    private static void handleHut(
                                   @NotNull final World world, @NotNull final EntityPlayer player,
                                   final String hut, final String style, final int rotation, @NotNull final BlockPos buildPos)
    {
        if (Structures.getStylesForHut(hut) == null)
        {
            Log.getLogger().error("No record of hut: " + hut);
            return;
        }

        final Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut);
        final Colony tempColony = ColonyManager.getClosestColony(world, buildPos);
        if (tempColony != null
                && (!tempColony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS)
                && !(block instanceof BlockHutTownHall
                && BlockPosUtil.getDistance2D(tempColony.getCenter(), buildPos) >= Configurations.workingRangeTownHall * 2 + Configurations.townHallPadding)))
        {
            return;
        }

        if (block != null && player.inventory.hasItemStack(new ItemStack(block)))
        {
            if (EventHandler.onBlockHutPlaced(world, player, block, buildPos))
            {

                world.destroyBlock(buildPos, true);
                world.setBlockState(buildPos, block.getDefaultState().withRotation(BlockUtils.getRotation(rotation)));
                block.onBlockPlacedBy(world, buildPos, world.getBlockState(buildPos), player, null);

                player.inventory.clearMatchingItems(Item.getItemFromBlock(block), -1, 1, null);

                @Nullable final AbstractBuilding building = ColonyManager.getBuilding(world, buildPos);

                if (building == null)
                {
                    Log.getLogger().error("BuildTool: building is null!");
                }
                else
                {
                    if (building.getTileEntity() != null)
                    {
                        final Colony colony = ColonyManager.getColony(world, buildPos);
                        if (colony == null)
                        {
                            Log.getLogger().info("No colony for " + player.getName());
                        }
                        else
                        {
                            building.getTileEntity().setColony(colony);
                        }
                    }
                    building.setStyle(style);
                    building.setRotation(rotation);
                }
            }
        }
        else
        {
            LanguageHandler.sendPlayerLocalizedMessage(player, BuildToolPlaceMessage.NO_HUT_IN_INVENTORY);
        }
    }

    /**
     * Creates the {@link WorkOrderBuildDecoration} to start building the decoration.
     *
     * @param world      The world the decoration is being built in.
     * @param player     The player who placed the decoration.
     * @param decoration The name of the decoration.
     * @param style      The style of the decoration.
     * @param rotation   The number of times the decoration is rotated.
     * @param buildPos   The location the decoration will be built.
     */
    private static void handleDecoration(
                                          @NotNull final World world, @NotNull final EntityPlayer player,
                                          final String decoration, final String style, final int rotation, @NotNull final BlockPos buildPos)
    {
        if (Structures.getStylesForDecoration(decoration) == null)
        {
            Log.getLogger().error("No record of decoration: " + decoration);
            return;
        }

        @Nullable final Colony colony = ColonyManager.getColony(world, buildPos);
        if (colony != null && colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
        {
            colony.getWorkManager().addWorkOrder(new WorkOrderBuildDecoration(style, decoration, rotation, buildPos));
        }
    }
}
