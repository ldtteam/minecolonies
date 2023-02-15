package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Cleanup task to remove the force flag from all loaded chunks.
 */
public class CommandUnloadForcedChunks implements IMCCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        if (context.getSource().getEntity() instanceof final Player sender)
        {
            final ServerLevel world = (ServerLevel) sender.level;
            for (long chunk : world.getChunkSource().chunkMap.visibleChunkMap.keySet())
            {
                world.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false);
            }
            MessageUtils.format(Component.literal("Successfully removed forceload flag!")).sendTo(sender);
            return 1;
        }
        return 0;
    }

    @Override
    public boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        return context.getSource().getEntity() instanceof final Player sender && sender.isCreative();
    }

    /**
     * Name string of the command.
     *
     * @return this commands name.
     */
    @Override
    public String getName()
    {
        return "forceunloadchunks";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName()).executes(this::checkPreConditionAndExecute);
    }
}
