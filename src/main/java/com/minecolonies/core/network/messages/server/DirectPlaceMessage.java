package com.minecolonies.core.network.messages.server;

import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OTHER_LEVEL;
import static com.minecolonies.api.util.constant.TranslationConstants.WRONG_COLONY;

/**
 * Place a building directly without buildtool.
 */
public class DirectPlaceMessage implements IMessage
{
    /**
     * The state to be placed..
     */
    private BlockState state;

    /**
     * The position to place it at.
     */
    private BlockPos pos;

    /**
     * The stack which is going to be placed.
     */
    private ItemStack stack;

    /**
     * Empty constructor used when registering the
     */
    public DirectPlaceMessage()
    {
        super();
    }

    /**
     * Place the building.
     *
     * @param state the state to be placed.
     * @param pos   the pos to place it at.
     * @param stack the stack in the hand.
     */
    public DirectPlaceMessage(final BlockState state, final BlockPos pos, final ItemStack stack)
    {
        super();
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        state = Block.stateById(buf.readInt());
        pos = buf.readBlockPos();
        stack = buf.readItem();
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(Block.getId(state));
        buf.writeBlockPos(pos);
        buf.writeItem(stack);
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
        final ServerPlayer player = ctxIn.getSender();
        final Level world = player.getCommandSenderWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), stack);

        if ((colony == null && state.getBlock() == ModBlocks.blockHutTownHall) || (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)))
        {
            final CompoundTag compound = stack.getTag();
            if (colony != null && compound != null && compound.contains(TAG_COLONY_ID) && colony.getID() != compound.getInt(TAG_COLONY_ID))
            {
                MessageUtils.format(WRONG_COLONY, compound.getInt(TAG_COLONY_ID)).sendTo(player);
                return;
            }

            player.getCommandSenderWorld().setBlockAndUpdate(pos, state);
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityColonyBuilding)
            {
                ((TileEntityColonyBuilding) tileEntity).setStructurePack(StructurePacks.selectedPack);

                ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.findBlueprintFuture(StructurePacks.selectedPack.getName(), blueprint -> blueprint.getBlockState(blueprint.getPrimaryBlockOffset()).getBlock() == state.getBlock()), world, (blueprint -> {
                    if (blueprint == null)
                    {
                        return;
                    }
                    String fullPath = blueprint.getFilePath().toString();
                    fullPath = fullPath.replace(StructurePacks.selectedPack.getPath().toString() + "/", "");
                    ((TileEntityColonyBuilding) tileEntity).setBlueprintPath(fullPath + "/" + blueprint.getFileName().substring(0, blueprint.getFileName().length() - 1) + "1.blueprint");
                    state.getBlock().setPlacedBy(world, pos, state, player, stack);

                    if (compound != null && compound.contains(TAG_OTHER_LEVEL))
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                        if (building != null)
                        {
                            building.setBuildingLevel(compound.getInt(TAG_OTHER_LEVEL));
                            building.setDeconstructed();
                        }
                    }
                })));
            }
        }
    }
}
