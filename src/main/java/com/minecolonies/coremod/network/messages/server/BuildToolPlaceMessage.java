package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InstantStructurePlacer;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.BuildingUtils;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OTHER_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PASTEABLE;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildToolPlaceMessage implements IMessage
{
    /**
     * Language key for missing hut
     */
    private static final String NO_HUT_IN_INVENTORY = "com.minecolonies.coremod.gui.buildtool.nohutininventory";

    /**
     * The state at the offset position.
     */
    private BlockState state;

    private String   structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  isHut;
    private boolean  mirror;

    /**
     * Empty constructor used when registering the
     */
    public BuildToolPlaceMessage()
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
     * @param state         the state.
     */
    public BuildToolPlaceMessage(
      final String structureName,
      final String workOrderName,
      final BlockPos pos,
      final int rotation,
      final boolean isHut,
      final Mirror mirror,
      final BlockState state)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.state = state;
    }

    /**
     * Reads this packet from a {@link PacketBuffer}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        structureName = buf.readString(32767);
        workOrderName = buf.readString(32767);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        state = Block.getStateById(buf.readInt());
    }

    /**
     * Writes this packet to a {@link PacketBuffer}.
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
        final PlayerEntity player = ctxIn.getSender();
        final StructureName sn = new StructureName(structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new StringTextComponent("Can not build " + workOrderName + ": schematic missing!"));
            return;
        }
        if (isHut)
        {
            handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state);
        }
        else
        {
            handleDecoration(CompatibilityUtils.getWorldFromEntity(player), player, sn, workOrderName, rotation, pos, mirror);
        }
    }

    /**
     * Handles the placement of huts.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param sn       The name of the structure.
     * @param rotation The number of times the structure should be rotated.
     * @param buildPos The location the hut is being placed.
     * @param mirror   Whether or not the strcture is mirrored.
     */
    private static void handleHut(
      @NotNull final World world,
      @NotNull final PlayerEntity player,
      final StructureName sn,
      final int rotation,
      @NotNull final BlockPos buildPos,
      final boolean mirror,
      final BlockState state)
    {
        final String hut = sn.getSection();
        final ItemStack stack = BuildingUtils.getItemStackForHutFromInventory(player.inventory, hut);
        final Block block = stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock() : null;

        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (tempColony != null
              && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                    && !(block instanceof BlockHutTownHall
                           && !IColonyManager.getInstance().isTooCloseToColony(world, buildPos))))
        {
            return;
        }

        if (block != null && player.inventory.hasItemStack(new ItemStack(block)))
        {
            if (EventHandler.onBlockHutPlaced(world, player, block, buildPos))
            {
                if (tempColony != null)
                {
                    AdvancementUtils.TriggerAdvancementPlayersForColony(tempColony, playerMP -> AdvancementTriggers.PLACE_STRUCTURE.trigger(playerMP, sn));
                }
                else
                {
                    AdvancementTriggers.PLACE_STRUCTURE.trigger((ServerPlayerEntity) player, sn);
                }

                world.destroyBlock(buildPos, true);
                world.setBlockState(buildPos, state);
                ((AbstractBlockHut) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());

                boolean complete = false;
                int level = 0;

                final CompoundNBT compound = stack.getTag();
                if (compound != null)
                {
                    if (compound.keySet().contains(TAG_OTHER_LEVEL))
                    {
                        level = compound.getInt(TAG_OTHER_LEVEL);
                    }
                    if (compound.keySet().contains(TAG_PASTEABLE))
                    {
                        String schematic = sn.toString();
                        schematic = schematic.substring(0, schematic.length() - 1);
                        schematic += level;
                        InstantStructurePlacer.loadAndPlaceStructureWithRotation(player.world, schematic,
                          buildPos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, false);
                        complete = true;
                    }
                }
                player.inventory.clearMatchingItems(itemStack -> itemStack.isItemEqual(new ItemStack(block, 1)), 1);
                setupBuilding(world, player, sn, rotation, buildPos, mirror, level, complete);
            }
        }
        else
        {
            LanguageHandler.sendPlayerMessage(player, NO_HUT_IN_INVENTORY);
        }
    }

    /**
     * Creates the {@link WorkOrderBuildDecoration} to start building the decoration.
     *
     * @param world         The world the decoration is being built in.
     * @param player        The player who placed the decoration.
     * @param sn            The name of the structure.
     * @param workOrderName The style of the decoration.
     * @param rotation      The number of times the decoration is rotated.
     * @param buildPos      The location the decoration will be built.
     * @param mirror        Whether or not the strcture is mirrored.
     */
    private static void handleDecoration(
      @NotNull final World world, @NotNull final PlayerEntity player,
      final StructureName sn, final String workOrderName,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
        @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            String schem = sn.toString();
            String woName = workOrderName;

            if (!schem.contains("cache"))
            {
                if (schem.matches("^.*[a-zA-Z_-]\\d$"))
                {

                    schem = schem.replaceAll("\\d$", "");
                    schem += '1';
                }

                if (woName.matches("^.*[a-zA-Z_-]\\d$"))
                {
                    woName = woName.replaceAll("\\d$", "");
                    woName += '1';
                }
            }

            colony.getWorkManager().addWorkOrder(new WorkOrderBuildDecoration(schem, woName, rotation, buildPos, mirror), false);
        }
        else
        {
            Log.getLogger().error("handleDecoration: Could not build " + sn, new Exception());
        }
    }

    /**
     * setup the building once it has been placed.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param sn       The name of the structure.
     * @param rotation The number of times the structure should be rotated.
     * @param buildPos The location the hut is being placed.
     * @param mirror   Whether or not the structure is mirrored.
     * @param level    the future initial building level.
     * @param complete if pasted.
     */
    private static void setupBuilding(
      @NotNull final World world, @NotNull final PlayerEntity player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final int level, final boolean complete)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            Log.getLogger().error("BuildTool: building is null!", new Exception());
        }
        else
        {
            if (building.getTileEntity() != null)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
                if (colony == null)
                {
                    Log.getLogger().info("No colony for " + player.getName().getFormattedText());
                }
                else
                {
                    building.getTileEntity().setColony(colony);
                }
            }


            building.setStyle(sn.getStyle());
            building.setBuildingLevel(level);

            if (!(building instanceof IRSComponent))
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

                ConstructionTapeHelper.placeConstructionTape(building.getPosition(), corners, world);
            }

            if (mirror)
            {
                building.invertMirror();
            }
            if (complete)
            {
                building.onUpgradeComplete(building.getBuildingLevel());
            }
        }
    }
}
