package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.WindowSuggestBuildTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Open the suggestion window.
 */
public class OpenSuggestionWindowMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_suggestion_window", OpenSuggestionWindowMessage::new);

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
     * Open the window.
     *
     * @param state the state to be placed.
     * @param pos   the pos to place it at.
     * @param stack the stack in the hand.
     */
    public OpenSuggestionWindowMessage(final BlockState state, final BlockPos pos, final ItemStack stack)
    {
        super(TYPE);
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    protected OpenSuggestionWindowMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        state = Block.stateById(buf.readInt());
        pos = buf.readBlockPos();
        stack = buf.readItem();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(Block.getId(state));
        buf.writeBlockPos(pos);
        buf.writeItem(stack);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        new WindowSuggestBuildTool(pos, state, stack).open();
    }
}
