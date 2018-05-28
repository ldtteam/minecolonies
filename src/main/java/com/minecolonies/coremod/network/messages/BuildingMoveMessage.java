package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.BlockHutTownHall;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakingProduct;
import com.minecolonies.coremod.entity.ai.citizen.baker.ProductState;
import com.minecolonies.coremod.event.EventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildingMoveMessage extends AbstractMessage<BuildingMoveMessage, IMessage>
{
    /**
     * Language key for missing hut message.
     */
    private BlockPos buildingId;
    private String   structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  mirror;

    /**
     * Empty constructor used when registering the message.
     */
    public BuildingMoveMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *  @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param mirror        the mirror of the building or decoration.
     * @param building
     */
    public BuildingMoveMessage(
            final String structureName,
            final String workOrderName,
            final BlockPos pos,
            final int rotation,
            final Mirror mirror,
            final AbstractBuildingView building)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.buildingId = building.getID();
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
        pos = BlockPosUtil.readFromByteBuf(buf);
        rotation = buf.readInt();
        mirror = buf.readBoolean();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
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
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(rotation);
        buf.writeBoolean(mirror);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void messageOnServerThread(final BuildingMoveMessage message, final EntityPlayerMP player)
    {
        final StructureName sn = new StructureName(message.structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponentString("Can not build " + message.workOrderName + ": schematic missing!"));
            return;
        }
        handleHut(CompatibilityUtils.getWorld(player), player, sn, message.rotation, message.pos, message.mirror, message.buildingId);
    }

    /**
     * Handles the placement of huts.
     *
     * @param world         World the hut is being placed into.
     * @param player        Who placed the hut.
     * @param sn            The name of the structure.
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the strcture is mirrored.
     * @param oldBuildingId The old building id.
     */
    private static void handleHut(
            @NotNull final World world, @NotNull final EntityPlayer player,
            final StructureName sn,
            final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockPos oldBuildingId)
    {
        final String hut = sn.getSection();
        final Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut);
        final Colony tempColony = ColonyManager.getClosestColony(world, buildPos);
        if (tempColony != null
                && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                && !(block instanceof BlockHutTownHall
                && !ColonyManager.isTooCloseToColony(world, buildPos))))
        {
            return;
        }

        @Nullable AbstractBuilding oldBuilding = ColonyManager.getBuilding(world, oldBuildingId);
        if (oldBuilding instanceof BuildingTownHall)
        {
            oldBuilding = null;
            if (tempColony != null)
            {
                tempColony.getBuildingManager().setTownHall(null);
            }
            world.destroyBlock(oldBuildingId, false);
        }
        else if(oldBuilding instanceof BuildingWareHouse && tempColony != null)
        {
            tempColony.getBuildingManager().setWareHouse(null);
        }

        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(oldBuildingId, false);
            world.destroyBlock(buildPos, true);

            world.setBlockState(buildPos, block.getDefaultState().withRotation(BlockUtils.getRotation(rotation)));
            ((AbstractBlockHut) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());
            setupBuilding(world, player, sn, rotation, buildPos, mirror, oldBuilding);
        }
    }

    /**
     * setup the building once it has been placed.
     *  @param world         World the hut is being placed into.
     * @param player        Who placed the hut.
     * @param sn            The name of the structure.
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the strcture is mirrored.
     * @param oldBuilding The old building id.
     */
    private static void setupBuilding(
            @NotNull final World world, @NotNull final EntityPlayer player,
            final StructureName sn,
            final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, @Nullable final AbstractBuilding oldBuilding)
    {
        @Nullable final AbstractBuilding building = ColonyManager.getBuilding(world, buildPos);

        if (building == null)
        {
            Log.getLogger().error("BuildTool: building is null!");
        }
        else
        {
            final Colony colony = ColonyManager.getColony(world, buildPos);
            if (colony == null)
            {
                Log.getLogger().info("No colony for " + player.getName());
                return;
            }

            if (building.getTileEntity() != null)
            {
                building.getTileEntity().setColony(colony);
            }
            building.setStyle(sn.getStyle());
            building.setRotation(rotation);
            if (mirror)
            {
                building.invertMirror();
            }

            building.setBuildingLevel(oldBuilding.getBuildingLevel());

            if (oldBuilding instanceof AbstractBuildingWorker)
            {
                final List<CitizenData> workers = ((AbstractBuildingWorker) oldBuilding).getAssignedCitizen();
                for(final CitizenData citizen : workers)
                {
                    citizen.setWorkBuilding(null);
                    citizen.setWorkBuilding((AbstractBuildingWorker) building);
                    ((AbstractBuildingWorker) building).assignCitizen(citizen);
                }
            }

            if (oldBuilding instanceof BuildingBaker)
            {
                for (final Map.Entry<ProductState, List<BakingProduct>> task :((BuildingBaker) oldBuilding).getTasks().entrySet())
                {
                    for(final BakingProduct product : task.getValue())
                    {
                        ((BuildingBaker) building).addToTasks(task.getKey(), product);
                    }
                }
            }
            else if(oldBuilding instanceof BuildingDeliveryman)
            {
                ((BuildingDeliveryman) building).setBuildingToDeliver(((BuildingDeliveryman) oldBuilding).getBuildingToDeliver());
            }

            if (oldBuilding instanceof BuildingHome)
            {
                final List<CitizenData> residents = ((BuildingHome) oldBuilding).getAssignedCitizen();
                for(final CitizenData citizen : residents)
                {
                    citizen.setHomeBuilding(building);
                    ((BuildingHome) building).assignCitizen(citizen);
                }
            }

            colony.getWorkManager().addWorkOrder(new WorkOrderBuildRemoval(oldBuilding, oldBuilding.getBuildingLevel()), false);
            colony.getWorkManager().addWorkOrder(new WorkOrderBuildBuilding(building, building.getBuildingLevel()), false);
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        }
    }
}
