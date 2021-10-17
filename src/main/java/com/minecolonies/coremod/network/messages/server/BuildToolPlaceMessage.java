package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building. Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildToolPlaceMessage implements IMessage
{
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
    public BlockPos builder = BlockPos.ZERO;

    /**
     * Empty constructor used when registering the
     */
    public BuildToolPlaceMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool. Item in inventory required
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
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        structureName = buf.readUtf(32767);
        workOrderName = buf.readUtf(32767);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        state = Block.stateById(buf.readInt());

        builder = buf.readBlockPos();
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(structureName);
        buf.writeUtf(workOrderName);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeInt(Block.getId(state));

        buf.writeBlockPos(builder);
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
        final Player player = ctxIn.getSender();
        final StructureName sn = new StructureName(structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponent("Can not build " + workOrderName + ": schematic missing!"), player.getUUID());
            return;
        }
        if (isHut)
        {
            handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state);
        }
        else
        {
            handleDecoration(CompatibilityUtils.getWorldFromEntity(player), player, sn, workOrderName, rotation, pos, mirror, builder);
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
     * @param state    the state.
     */
    private static void handleHut(
      @NotNull final Level world,
      @NotNull final Player player,
      final StructureName sn,
      final int rotation,
      @NotNull final BlockPos buildPos,
      final boolean mirror,
      final BlockState state)
    {
        final Block blockAtPos = world.getBlockState(buildPos).getBlock();
        if (blockAtPos instanceof IBuilderUndestroyable || ModTags.indestructible.contains(blockAtPos))
        {
            LanguageHandler.sendPlayerMessage(player, INDESTRUCTIBLE_BLOCK_AT_POS);
            SoundUtils.playErrorSound(player, buildPos);
            return;
        }

        final String hut = sn.getSection();
        final ItemStack stack = BuildingUtils.getItemStackForHutFromInventory(player.getInventory(), hut);
        final Block block = stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock() : null;

        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (tempColony != null
              && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                    && !(block instanceof BlockHutTownHall
                           && IColonyManager.getInstance().isFarEnoughFromColonies(world, buildPos))))
        {
            return;
        }

        final CompoundTag compound = stack.getTag();
        if (tempColony != null && compound != null && compound.contains(TAG_COLONY_ID) && tempColony.getID() != compound.getInt(TAG_COLONY_ID))
        {
            LanguageHandler.sendPlayerMessage(player, WRONG_COLONY, compound.getInt(TAG_COLONY_ID));
            return;
        }

        if (block != null && player.getInventory().contains(new ItemStack(block)))
        {
            if (EventHandler.onBlockHutPlaced(world, player, block, buildPos))
            {
                if (tempColony != null)
                {
                    AdvancementUtils.TriggerAdvancementPlayersForColony(tempColony, playerMP -> AdvancementTriggers.PLACE_STRUCTURE.trigger(playerMP, sn));
                }
                else
                {
                    AdvancementTriggers.PLACE_STRUCTURE.trigger((ServerPlayer) player, sn);
                }

                world.destroyBlock(buildPos, true);
                world.setBlockAndUpdate(buildPos, state);
                ((AbstractBlockHut<?>) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());

                boolean complete = false;
                int level = 0;

                if (compound != null)
                {
                    if (compound.getAllKeys().contains(TAG_OTHER_LEVEL))
                    {
                        level = compound.getInt(TAG_OTHER_LEVEL);
                    }
                    if (compound.getAllKeys().contains(TAG_PASTEABLE))
                    {
                        String schematic = sn.toString();
                        schematic = schematic.substring(0, schematic.length() - 1);
                        schematic += level;
                        CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, schematic,
                          buildPos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, true, (ServerPlayer) player);
                        complete = true;
                    }
                }

                InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), stack, 1);
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
      @NotNull final Level world, @NotNull final Player player,
      final StructureName sn, final String workOrderName,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror,
      BlockPos builder)
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

            WorkOrderBuildDecoration woDeco = new WorkOrderBuildDecoration(schem, woName, rotation, buildPos, mirror);
            if (!builder.equals(BlockPos.ZERO))
            {
                woDeco.setClaimedBy(builder);
            }

            colony.getWorkManager().addWorkOrder(woDeco, false);
        }
        else
        {
            SoundUtils.playErrorSound(player, player.blockPosition());
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
      @NotNull final Level world, @NotNull final Player player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final int level, final boolean complete)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            if (!sn.getHutName().equals(ModBuildings.TOWNHALL_ID))
            {
            SoundUtils.playErrorSound(player, player.blockPosition());
                Log.getLogger().error("BuildTool: building is null!", new Exception());
            }
        }
        else
        {
            SoundUtils.playSuccessSound(player, player.blockPosition());
            if (building.getTileEntity() != null)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
                if (colony == null)
                {
                    Log.getLogger().info("No colony for " + player.getName().getString());
                }
                else
                {
                    building.getTileEntity().setColony(colony);
                }
            }


            building.setStyle(sn.getStyle());
            building.setBuildingLevel(level);
            if (level > 0)
            {
                building.setDeconstructed();
            }

            if (!(building instanceof IRSComponent))
            {
                ConstructionTapeHelper.placeConstructionTape(building.getCorners(), world);
            }

            building.setIsMirrored(mirror);

            if (complete)
            {
                building.onUpgradeComplete(building.getBuildingLevel());
            }
        }
    }
}
