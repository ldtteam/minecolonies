package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.api.items.component.HutBlockData;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.WRONG_COLONY;

/**
 * Place a building directly without buildtool.
 */
public class DirectPlaceMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "direct_place", DirectPlaceMessage::new);

    /**
     * The state to be placed..
     */
    private final BlockState state;

    /**
     * The position to place it at.
     */
    private final BlockPos pos;

    /**
     * The stack which is going to be placed.
     */
    private final ItemStack stack;

    /**
     * Place the building.
     *
     * @param state the state to be placed.
     * @param pos   the pos to place it at.
     * @param stack the stack in the hand.
     */
    public DirectPlaceMessage(final BlockState state, final BlockPos pos, final ItemStack stack)
    {
        super(TYPE);
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Reads this packet from a {@link RegistryFriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    protected DirectPlaceMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        state = Block.stateById(buf.readInt());
        pos = buf.readBlockPos();
        stack = Utils.deserializeCodecMess(buf);
    }

    /**
     * Writes this packet to a {@link RegistryFriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(Block.getId(state));
        buf.writeBlockPos(pos);
        Utils.serializeCodecMess(buf, stack);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        final Level world = player.getCommandSenderWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), stack);

        if ((colony == null && state.getBlock() == ModBlocks.blockHutTownHall.get()) || (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)))
        {
            final ColonyId colonyId = ColonyId.readFromItemStack(stack);
            if (colony != null && colonyId.hasColonyId() && colony.getID() != colonyId.id())
            {
                MessageUtils.format(WRONG_COLONY, colonyId.id()).sendTo(player);
                return;
            }

            player.getCommandSenderWorld().setBlockAndUpdate(pos, state);
            if (world.getBlockEntity(pos) instanceof final TileEntityColonyBuilding hut)
            {
                hut.setStructurePack(StructurePacks.selectedPack);

                ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.findBlueprintFuture(StructurePacks.selectedPack.getName(), blueprint -> blueprint.getBlockState(blueprint.getPrimaryBlockOffset()).getBlock() == state.getBlock(), player.level().registryAccess()), world, (blueprint -> {
                    if (blueprint == null)
                    {
                        return;
                    }
                    String fullPath = blueprint.getFilePath().toString();
                    fullPath = fullPath.replace(StructurePacks.selectedPack.getPath().toString() + "/", "");
                    hut.setBlueprintPath(fullPath + "/" + blueprint.getFileName().substring(0, blueprint.getFileName().length() - 1) + "1.blueprint");
                    state.getBlock().setPlacedBy(world, pos, state, player, stack);

                    final HutBlockData hutComponent = HutBlockData.readFromItemStack(stack);
                    if (hutComponent != null)
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                        if (building != null)
                        {
                            building.setBuildingLevel(hutComponent.level());
                            building.setDeconstructed();
                        }
                    }
                })));
            }
        }
    }
}
