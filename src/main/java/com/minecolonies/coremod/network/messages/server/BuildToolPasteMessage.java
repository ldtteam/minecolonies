package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.INSTANT_PLACEMENT;
import static com.minecolonies.api.util.constant.Constants.PLACEMENT_NBT;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage implements IMessage
{
    /**
     * The state at the offset position.
     */
    private BlockState state;

    private boolean  complete;
    private String   structureName;
    private String   workOrderName;
    private int      rotation;
    private BlockPos pos;
    private boolean  isHut;
    private boolean  mirror;

    /**
     * Empty constructor used when registering the
     */
    public BuildToolPasteMessage()
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
     * @param complete      paste it complete (with structure blocks) or without.
     * @param state         the state.
     */
    public BuildToolPasteMessage(
      final String structureName,
      final String workOrderName, final BlockPos pos,
      final int rotation, final boolean isHut,
      final Mirror mirror, final boolean complete, final BlockState state)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
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

        complete = buf.readBoolean();

        state = Block.stateById(buf.readInt());
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

        buf.writeBoolean(complete);

        buf.writeInt(Block.getId(state));
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
        final ServerPlayer player = ctxIn.getSender();
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponent("Can not build " + workOrderName + ": schematic missing!"), player.getUUID());
            return;
        }

        if (player.isCreative())
        {
            if (isHut)
            {
                handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state, complete);
                CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, structureName,
                  pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, player);

                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(CompatibilityUtils.getWorldFromEntity(player), pos);
                if (building != null)
                {
                    final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
                    ConstructionTapeHelper.removeConstructionTape(workOrder, CompatibilityUtils.getWorldFromEntity(player));
                }
            }
            else
            {
                StructurePlacementUtils.loadAndPlaceStructureWithRotation(ctxIn.getSender().level, structureName,
                  pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, ctxIn.getSender());
            }
        }
        else if (structureName.contains("supply"))
        {
            if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) > 0 && !MineColonies.getConfig().getServer().allowInfiniteSupplyChests.get()
                  && !isFreeInstantPlacementMH(player))
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.error.supplyChestAlreadyPlaced");
                return;
            }

            Predicate<ItemStack> searchPredicate = stack -> !stack.isEmpty();
            if (structureName.contains("supplyship"))
            {
                searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyChest), true, false));
            }
            if (structureName.contains("supplycamp"))
            {
                searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyCamp), true, false));
            }

            if (isFreeInstantPlacementMH(player))
            {
                searchPredicate =
                  searchPredicate.and(stack -> stack.hasTag() && stack.getTag().get(PLACEMENT_NBT) != null && stack.getTag().getString(PLACEMENT_NBT).equals(INSTANT_PLACEMENT));
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(new InvWrapper(player.getInventory()), searchPredicate);

            if (slot != -1 && !ItemStackUtils.isEmpty(player.getInventory().removeItemNoUpdate(slot)))
            {
                if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) < 1)
                {
                    LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.progress.supplies_placed");
                    player.awardStat(Stats.ITEM_USED.get(ModItems.supplyChest), 1);
                    AdvancementTriggers.PLACE_SUPPLY.trigger(player);
                }

                CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, structureName,
                  pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, player);
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "item.supplyChestDeployer.missing");
            }
        }
    }

    /**
     * Whether the itemstack used allows a free placement.
     *
     * @param playerEntity the player to check
     * @return whether the itemstack used allows a free placement.
     */
    private boolean isFreeInstantPlacementMH(ServerPlayer playerEntity)
    {
        final ItemStack mhItem = playerEntity.getMainHandItem();
        return !ItemStackUtils.isEmpty(mhItem) && mhItem.getTag() != null && mhItem.getTag().getString(PLACEMENT_NBT).equals(INSTANT_PLACEMENT);
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
     * @param state    The state of the hut.
     * @param complete If complete or not.
     */
    private static void handleHut(
      @NotNull final Level world, @NotNull final Player player,
      final StructureName sn,
      final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockState state, final boolean complete)
    {
        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (!complete && tempColony != null
              && !tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
              && IColonyManager.getInstance().isFarEnoughFromColonies(world, buildPos))
        {
            return;
        }

        final String hut = sn.getSection();

        final ItemStack stack = BuildingUtils.getItemStackForHutFromInventory(player.getInventory(), hut);
        final Block block = stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock() : null;
        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(buildPos, true);
            world.setBlockAndUpdate(buildPos, state);
            if (!complete)
            {
                ((AbstractBlockHut<?>) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle());
                setupBuilding(world, player, sn, rotation, buildPos, mirror);
            }
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
     * @param mirror   Whether or not the strcture is mirrored.
     */
    private static void setupBuilding(
      @NotNull final Level world,
      @NotNull final Player player,
      final StructureName sn,
      final int rotation,
      @NotNull final BlockPos buildPos,
      final boolean mirror)
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
                    Log.getLogger().info("No colony for " + player.getName().getString());
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

            if (!(building instanceof IRSComponent))
            {
                ConstructionTapeHelper.removeConstructionTape(building.getCorners(), world);
            }

            building.setIsMirrored(mirror);
        }
    }
}
