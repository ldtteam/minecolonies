package com.minecolonies.coremod.network.messages;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakingProduct;
import com.minecolonies.coremod.entity.ai.citizen.baker.ProductState;
import com.minecolonies.coremod.event.EventHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Move a building to another location.
 */
public class BuildingMoveMessage implements IMessage
{
    /**
     * Position of building.
     */
    private BlockPos buildingId;

    /**
     * The state at the offset position.
     */
    private BlockState state;

    private String   structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  mirror;

    /**
     * Empty constructor used when registering the 
     */
    public BuildingMoveMessage()
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
     * @param mirror        the mirror of the building or decoration.
     * @param building      the building.
     * @param state         the state.
     */
    public BuildingMoveMessage(
      final String structureName,
      final String workOrderName,
      final BlockPos pos,
      final int rotation,
      final Mirror mirror,
      final IBuildingView building,
      final BlockState state)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.buildingId = building.getID();
        this.state = state;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        structureName = buf.readString();
        workOrderName = buf.readString();
        pos = buf.readBlockPos();
        rotation = buf.readInt();
        mirror = buf.readBoolean();
        buildingId = buf.readBlockPos();
        state = Block.getStateById(buf.readInt());
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeString(structureName);
        buf.writeString(workOrderName);
        buf.writeBlockPos(pos);
        buf.writeInt(rotation);
        buf.writeBoolean(mirror);
        buf.writeBlockPos(buildingId);
        buf.writeInt(Block.getStateId(state));
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
        final ServerPlayerEntity player = ctxIn.getSender();
        final StructureName sn = new StructureName(structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new StringTextComponent("Can not build " + workOrderName + ": schematic missing!"));
            return;
        }
        handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, buildingId, state);
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
     * @param state         the hut state.
     */
    private static void handleHut(
      @NotNull final World world, @NotNull final PlayerEntity player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockPos oldBuildingId, final BlockState state)
    {
        final String hut = sn.getSection();

        final Block block = ForgeRegistries.BLOCKS.get(new ResourceLocation(Constants.MOD_ID, "blockHut" + hut));
        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (tempColony != null
              && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                    && !(block instanceof BlockHutTownHall
                           && !IColonyManager.getInstance().isTooCloseToColony(world, buildPos))))
        {
            return;
        }

        @Nullable final IBuilding oldBuilding = IColonyManager.getInstance().getBuilding(world, oldBuildingId);
        if (oldBuilding instanceof BuildingTownHall)
        {
            if (tempColony != null)
            {
                tempColony.getBuildingManager().setTownHall(null);
            }
            world.destroyBlock(oldBuildingId, false);
        }
        else if (oldBuilding instanceof BuildingWareHouse && tempColony != null)
        {
            tempColony.getBuildingManager().setWareHouse(null);
        }

        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(oldBuildingId, false);
            world.destroyBlock(buildPos, true);

            world.setBlockState(buildPos, state.rotate(BlockPosUtil.getRotationFromRotations(rotation)));
            ((AbstractBlockHut) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());
            setupBuilding(world, player, sn, rotation, buildPos, mirror, oldBuilding);
        }
    }

    /**
     * setup the building once it has been placed.
     *
     * @param world       World the hut is being placed into.
     * @param player      Who placed the hut.
     * @param sn          The name of the structure.
     * @param rotation    The number of times the structure should be rotated.
     * @param buildPos    The location the hut is being placed.
     * @param mirror      Whether or not the strcture is mirrored.
     * @param oldBuilding The old building id.
     */
    private static void setupBuilding(
      @NotNull final World world, @NotNull final PlayerEntity player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, @Nullable final IBuilding oldBuilding)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            Log.getLogger().error("BuildTool: building is null!");
        }
        else
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
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
                final List<ICitizenData> workers = oldBuilding.getAssignedCitizen();
                for (final ICitizenData citizen : workers)
                {
                    citizen.setWorkBuilding(null);
                    citizen.setWorkBuilding((IBuildingWorker) building);
                    building.assignCitizen(citizen);
                }
            }

            if (oldBuilding instanceof BuildingBaker)
            {
                for (final Map.Entry<ProductState, List<BakingProduct>> task : ((BuildingBaker) oldBuilding).getTasks().entrySet())
                {
                    for (final BakingProduct product : task.get())
                    {
                        ((BuildingBaker) building).addToTasks(task.getKey(), product);
                    }
                }
            }
            else if (oldBuilding instanceof BuildingDeliveryman)
            {
                ((IBuildingDeliveryman) building).setBuildingToDeliver(((IBuildingDeliveryman) oldBuilding).getBuildingToDeliver());
            }

            if (oldBuilding instanceof BuildingHome)
            {
                final List<ICitizenData> residents = oldBuilding.getAssignedCitizen();
                for (final ICitizenData citizen : residents)
                {
                    citizen.setHomeBuilding(building);
                    building.assignCitizen(citizen);
                }
            }

            if (building instanceof BuildingTownHall)
            {
                colony.getBuildingManager().setTownHall((BuildingTownHall) building);
            }

            colony.getWorkManager().addWorkOrder(new WorkOrderBuildRemoval(oldBuilding, oldBuilding.getBuildingLevel()), false);
            colony.getWorkManager().addWorkOrder(new WorkOrderBuildBuilding(building, building.getBuildingLevel()), false);
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntitys(), "com.minecolonies.coremod.workOrderAdded");
        }
    }
}
