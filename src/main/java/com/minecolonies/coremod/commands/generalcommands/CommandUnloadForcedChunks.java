package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

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
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (sender instanceof PlayerEntity)
        {
            final World world = sender.level;
            for (long chunk : ((ServerChunkProvider) sender.level.getChunkSource()).chunkMap.visibleChunkMap.keySet())
            {
                ((ServerWorld) world).setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false);
            }
            MessageUtils.format(new StringTextComponent("Successfully removed forceload flag!")).sendTo((PlayerEntity) sender);
            return 1;
        }
        return 0;
    }

    @Override
    public boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();
        return sender instanceof PlayerEntity && ((PlayerEntity) sender).isCreative();
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
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName()).executes(this::checkPreConditionAndExecute);
    }
}
