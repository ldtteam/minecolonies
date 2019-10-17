package com.minecolonies.coremod.network.messages;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.ColonyUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.wrapper.InvWrapper;
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
public class BuildToolPlaceMessage extends AbstractMessage<BuildToolPlaceMessage, IMessage>
{
    /**
     * Language key for missing hut message.
     */
    private static final String NO_HUT_IN_INVENTORY = "com.minecolonies.coremod.gui.buildtool.nohutininventory";

    /**
     * The state at the offset position.
     */
    private IBlockState state;

    private String   structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  isHut;
    private boolean  mirror;

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
     *  @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param isHut         true if hut, false if decoration
     * @param mirror        the mirror of the building or decoration.
     * @param state the state.
     */
    public BuildToolPlaceMessage(
      final String structureName,
      final String workOrderName,
      final BlockPos pos,
      final int rotation,
      final boolean isHut,
      final Mirror mirror,
      final IBlockState state)
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

        state = NBTUtil.readBlockState(ByteBufUtils.readTag(buf));

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

        ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new NBTTagCompound(), state));
    }

    @Override
    public void messageOnServerThread(final BuildToolPlaceMessage message, final EntityPlayerMP player)
    {
        final StructureName sn = new StructureName(message.structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponentString("Can not build " + message.workOrderName + ": schematic missing!"));
            return;
        }
        if (message.isHut)
        {
            handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, message.rotation, message.pos, message.mirror, message.state);
        }
        else
        {
            handleDecoration(CompatibilityUtils.getWorldFromEntity(player), player, sn, message.workOrderName, message.rotation, message.pos, message.mirror);
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
      @NotNull final EntityPlayer player,
      final StructureName sn,
      final int rotation,
      @NotNull final BlockPos buildPos,
      final boolean mirror,
      final IBlockState state)
    {
        final String hut = sn.getSection();
        final Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut);
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
                    AdvancementTriggers.PLACE_STRUCTURE.trigger((EntityPlayerMP) player, sn);
                }

                world.destroyBlock(buildPos, true);
                world.setBlockState(buildPos, state.withRotation(BlockPosUtil.getRotationFromRotations(rotation)));
                ((AbstractBlockHut) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());

                boolean complete = false;
                int level = 0;
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory), itemStack -> itemStack.isItemEqual(new ItemStack(Item.getItemFromBlock(block), 1)));
                if (slot != -1)
                {
                    final ItemStack stack = player.inventory.getStackInSlot(slot);
                    final NBTTagCompound compound = stack.getTagCompound();
                    if (compound != null)
                    {
                        if (compound.hasKey(TAG_OTHER_LEVEL))
                        {
                            level = compound.getInteger(TAG_OTHER_LEVEL);
                        }
                        if (compound.hasKey(TAG_PASTEABLE))
                        {
                            String schematic = sn.toString();
                            schematic = schematic.substring(0, schematic.length()-1);
                            schematic += level;
                            InstantStructurePlacer.loadAndPlaceStructureWithRotation(player.world, schematic,
                              buildPos, rotation,mirror ? Mirror.FRONT_BACK : Mirror.NONE, false);
                            complete = true;
                        }
                    }
                    player.inventory.clearMatchingItems(Item.getItemFromBlock(block), -1, 1, null);
                }
                setupBuilding(world, player, sn, rotation, buildPos, mirror, level, complete);
            }
        }
        else
        {
            LanguageHandler.sendPlayerMessage(player, BuildToolPlaceMessage.NO_HUT_IN_INVENTORY);
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
                                          @NotNull final World world, @NotNull final EntityPlayer player,
                                          final StructureName sn, final String workOrderName,
                                          final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
        @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            String schem = sn.toString();
            String woName = workOrderName;

            if (schem.matches("^.*[a-zA-Z_-]\\d$"))
            {

                schem = schem.replaceAll("\\d$", "");
                schem+='1';
            }

            if (woName.matches("^.*[a-zA-Z_-]\\d$"))
            {
                woName = woName.replaceAll("\\d$", "");
                woName+='1';
            }

            colony.getWorkManager().addWorkOrder(new WorkOrderBuildDecoration(schem, woName, rotation, buildPos, mirror), false);
        }
        else
        {
            Log.getLogger().error("handleDecoration: Could not build " + sn);
        }
    }

    /**
     * setup the building once it has been placed.
     * @param world         World the hut is being placed into.
     * @param player        Who placed the hut.
     * @param sn            The name of the structure.
     * @param rotation      The number of times the structure should be rotated.
     * @param buildPos      The location the hut is being placed.
     * @param mirror        Whether or not the structure is mirrored.
     * @param level         the future initial building level.
     * @param complete      if pasted.
     */
    private static void setupBuilding(
      @NotNull final World world, @NotNull final EntityPlayer player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final int level, final boolean complete)
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


            building.setStyle(sn.getStyle());
            building.setRotation(rotation);
            building.setBuildingLevel(level);

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

                building.setCorners(corners.getFirst().getFirst(), corners.getFirst().getSecond(), corners.getSecond().getFirst(), corners.getSecond().getSecond());
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
