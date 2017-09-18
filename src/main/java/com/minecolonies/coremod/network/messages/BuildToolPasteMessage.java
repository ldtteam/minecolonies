package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage extends AbstractMessage<BuildToolPasteMessage, IMessage>
{
    private boolean complete;
    private String structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  isHut;
    private boolean  mirror;

    /**
     * Empty constructor used when registering the message.
     */
    public BuildToolPasteMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param isHut         true if hut, false if decoration
     * @param mirror        the mirror of the building or decoration.
     * @param complete      paste it complete (with structure blocks) or without.
     */
    public BuildToolPasteMessage(final String structureName,
            final String workOrderName, final BlockPos pos,
            final int rotation, final boolean isHut,
            final Mirror mirror, final boolean complete)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        structureName = ByteBufUtils.readUTF8String(buf);
        workOrderName = ByteBufUtils.readUTF8String(buf);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        complete = buf.readBoolean();
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureName);
        ByteBufUtils.writeUTF8String(buf, workOrderName);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeBoolean(complete);
    }

    @Override
    public void messageOnServerThread(final BuildToolPasteMessage message, final EntityPlayerMP player)
    {
        final Structures.StructureName sn = new Structures.StructureName(message.structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponentString("Can not build " + message.workOrderName + ": schematic missing!"));
            return;
        }

        if (player.capabilities.isCreativeMode)
        {
            if (message.isHut)
            {
                handleHut(CompatibilityUtils.getWorld(player), player, sn, message.rotation, message.pos, message.mirror);
            }
            StructureWrapper.loadAndPlaceStructureWithRotation(player.world, message.structureName,
                    message.pos, message.rotation, message.mirror ? Mirror.FRONT_BACK : Mirror.NONE, message.complete);
        }
    }

    /**
     * Handles the placement of huts.
     *
     * @param world         World the hut is being placed into.
     * @param player        Who placed the hut.
     * @param sn            The name of the structure.
     * @param workOrderName The name of the work order.
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the strcture is mirrored.
     */
    private static void handleHut(
            @NotNull final World world, @NotNull final EntityPlayer player,
            final Structures.StructureName sn,
            final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
        final Colony tempColony = ColonyManager.getClosestColony(world, buildPos);
        if (tempColony != null
                && !tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                && BlockPosUtil.getDistance2D(tempColony.getCenter(), buildPos) >= Configurations.gameplay.workingRangeTownHall * 2 + Configurations.gameplay.townHallPadding)
        {
            return;
        }

        final String hut = sn.getSection();
        final Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut);
        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(buildPos, true);
            world.setBlockState(buildPos, block.getDefaultState().withRotation(BlockUtils.getRotation(rotation)));
            ((AbstractBlockHut) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());
            setupBuilding(world, player, sn, rotation, buildPos, mirror);
        }
    }

    /**
     * setup the building once it has been placed.
     *
     * @param world         World the hut is being placed into.
     * @param player        Who placed the hut.
     * @param sn            The name of the structure.
     * @param workOrderName The name of the work order.
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the strcture is mirrored.
     */
    private static void setupBuilding(
            @NotNull final World world, @NotNull final EntityPlayer player,
            final Structures.StructureName sn,
            final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
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
            String name = sn.toString();
            name = name.substring(name.length()-1);

            try
            {
                final int level = Integer.parseInt(name);
                building.setBuildingLevel(level);
            }
            catch(final NumberFormatException e)
            {
                Log.getLogger().warn("Couldn't parse the level.", e);
            }

            building.setStyle(sn.getStyle());
            building.setRotation(rotation);
            if (mirror)
            {
                building.setMirror();
            }
        }
    }
}
