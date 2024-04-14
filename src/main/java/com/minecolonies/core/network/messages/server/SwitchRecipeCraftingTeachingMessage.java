package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a message to switch recipe outputs when multiple are available.
 */
public class SwitchRecipeCraftingTeachingMessage implements IMessage
{
    /**
     * Create message.
     */
    public SwitchRecipeCraftingTeachingMessage()
    {
        super();
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
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
        if (player.containerMenu instanceof ContainerCrafting)
        {
            final ContainerCrafting container = (ContainerCrafting) player.containerMenu;
            container.switchRecipes();
        }
    }
}
