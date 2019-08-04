package com.minecolonies.coremod.network.messages;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage implements IMessage
{
    /**
     * Height of the chest in the supplyship to be placed.
     */
    private static final int SUPPLY_SHIP_CHEST_HEIGHT = 6;

    /**
     * The state at the offset position.
     */
    private BlockState state;

    private boolean                  complete;
    private String                   structureName;
    private String                   workOrderName;
    private int                      rotation;
    private BlockPos                 pos;
    private boolean                  isHut;
    private boolean                  mirror;
    private WindowBuildTool.FreeMode freeMode;

    /**
     * Empty constructor used when registering the 
     */
    public BuildToolPasteMessage()
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
     * @param isHut         true if hut, false if decoration
     * @param mirror        the mirror of the building or decoration.
     * @param complete      paste it complete (with structure blocks) or without.
     * @param state the state.
     */
    public BuildToolPasteMessage(
      final String structureName,
      final String workOrderName, final BlockPos pos,
      final int rotation, final boolean isHut,
      final Mirror mirror, final boolean complete,
      final WindowBuildTool.FreeMode freeMode, final BlockState state)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
        this.freeMode = freeMode;
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

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        complete = buf.readBoolean();

        final int modeId = buf.readInt();
        if(modeId >= 0)
        {
            freeMode = WindowBuildTool.FreeMode.values()[modeId];
        }

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

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeBoolean(complete);

        if(freeMode == null)
        {
            buf.writeInt(-1);
        }
        else
        {
            buf.writeInt(freeMode.ordinal());
        }

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
        final StructureName sn = new StructureName(structureName);
        final ServerPlayerEntity player = ctxIn.getSender();
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new StringTextComponent("Can not build " + workOrderName + ": schematic missing!"));
            return;
        }

        if (player.isCreative())
        {
            if (isHut)
            {
                handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state);
            }

            InstantStructurePlacer.loadAndPlaceStructureWithRotation(player.world, structureName,
              pos, rotation, mirror ? Mirror.FRONT_BACK : Mirror.NONE, complete);

            if (isHut)
            {
                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(CompatibilityUtils.getWorldFromEntity(player), pos);
                if (building != null)
                {
                    building.onUpgradeComplete(building.getBuildingLevel());
                    final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
                    ConstructionTapeHelper.removeConstructionTape(workOrder, CompatibilityUtils.getWorldFromEntity(player));
                }
            }
        }
        else if(freeMode !=  null )
        {
            if(player.getStats().get(Stats.ITEM_USED.get(ModItems.supplyChest)) > 0 && !MineColonies.getConfig().getCommon().allowInfiniteSupplyChests.get())
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.error.supplyChestAlreadyPlaced");
                return;
            }
            final List<ItemStack> stacks = new ArrayList<>();
            final int chestHeight;
            if(freeMode == WindowBuildTool.FreeMode.SUPPLYSHIP)
            {
                stacks.add(new ItemStack(ModItems.supplyChest));
                chestHeight = SUPPLY_SHIP_CHEST_HEIGHT;
            }
            else if(freeMode == WindowBuildTool.FreeMode.SUPPLYCAMP)
            {
                stacks.add(new ItemStack(ModItems.supplyCamp));
                chestHeight = 1;
            }
            else
            {
                chestHeight = 0;
            }

            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.progress.supplies_placed");
            player.addStat(Stats.ITEM_USED.get(ModItems.supplyChest), 1);
            if(InventoryUtils.removeStacksFromItemHandler(new InvWrapper(player.inventory), stacks))
            {
                InstantStructurePlacer.loadAndPlaceStructureWithRotation(player.world, structureName,
                  pos, rotation, mirror ? Mirror.FRONT_BACK : Mirror.NONE, complete);
                player.getEntityWorld().setBlockState(pos.up(chestHeight), Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, player.getHorizontalFacing()));
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "item.supplyChestDeployer.missing");
            }
        }
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
     * @param state         The state of the hut.
     */
    private static void handleHut(
                                   @NotNull final World world, @NotNull final PlayerEntity player,
                                   final StructureName sn,
                                   final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockState state)
    {
        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (tempColony != null
              && !tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
              && !IColonyManager.getInstance().isTooCloseToColony(world, buildPos))
        {
            return;
        }

        final String hut = sn.getSection();

        final Block block = ForgeRegistries.BLOCKS.get(new ResourceLocation(Constants.MOD_ID, "blockHut" + hut));
        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(buildPos, true);
            world.setBlockState(buildPos, state.rotate(BlockPosUtil.getRotationFromRotations(rotation)));
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
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the strcture is mirrored.
     */
    private static void setupBuilding(
                                       @NotNull final World world, @NotNull final PlayerEntity player,
                                       final StructureName sn,
                                       final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            Log.getLogger().error("BuildTool: building is null!");
        }
        else
        {
            if (building.getTileEntity() != null)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
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
            name = name.substring(name.length() - 1);

            try
            {
                final int level = Integer.parseInt(name);
                building.setBuildingLevel(level);
            }
            catch (final NumberFormatException e)
            {
                Log.getLogger().warn("Couldn't parse the level.", e);
            }

            building.setStyle(sn.getStyle());
            building.setRotation(rotation);

            if (!(building instanceof PostBox))
            {
                ConstructionTapeHelper.removeConstructionTape(building.getCorners(), world);
                final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
                final Structure wrapper = new Structure(world, workOrder.getStructureName(), new PlacementSettings());
                final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
                  = ColonyUtils.calculateCorners(building.getPosition(),
                  world,
                  wrapper,
                  workOrder.getRotation(world),
                  workOrder.isMirrored());

                building.setCorners(corners.getA().getA(), corners.getA().getB(), corners.getB().getA(), corners.getB().getB());
                building.setHeight(wrapper.getHeight());
            }

            if (mirror)
            {
                building.invertMirror();
            }
        }
    }
}
