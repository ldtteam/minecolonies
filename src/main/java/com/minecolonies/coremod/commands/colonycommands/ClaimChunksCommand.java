package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.UNABLE_TO_FIND_WORLD_CAP_TEXT;
import static com.minecolonies.api.util.constant.CommandConstants.*;
import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;
import static com.minecolonies.coremod.commands.AbstractSingleCommand.isPlayerOpped;

/**
 * This command lets a player claim a range of chunks.
 */
public class ClaimChunksCommand implements IActionCommand
{
    /**
     * The description.
     */
    public static final String DESC = "claim";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ClaimChunksCommand()
    {
        super();
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        //See if the player is opped.
        if (sender instanceof EntityPlayerMP && isPlayerOpped(sender))
        {
            final int colonyId = actionMenuState.getIntegerForArgument("colony");
            final int dimId = actionMenuState.getIntegerForArgument("dimension");

            final int range = actionMenuState.getIntValueForArgument("range", Configurations.gameplay.workingRangeTownHallChunks);
            final Boolean add = actionMenuState.getBooleanForArgument("add");

            if (range > Configurations.gameplay.workingRangeTownHallChunks * 2)
            {
                sender.sendMessage(new TextComponentString(TOO_MANY_CHUNKS));
                return;
            }

            final IChunkmanagerCapability chunkManager = sender.getEntityWorld().getCapability(CHUNK_STORAGE_UPDATE_CAP, null);
            if (chunkManager == null)
            {
                Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT);
                return;
            }

            if (chunkManager.getAllChunkStorages().size() > CHUNKS_TO_CLAM_THRESHOLD)
            {
                sender.sendMessage(new TextComponentString(TOO_MANY_CHUNKS_CLAIMED));
                return;
            }

            final Chunk chunk = ((EntityPlayerMP) sender).getServerWorld().getChunk(sender.getPosition());
            ChunkDataHelper.claimChunksInRange(colonyId, dimId, add == null || add, chunk.x, chunk.z, range, 0, sender.getEntityWorld());
            sender.sendMessage(new TextComponentString(SUCCESFULLY_CLAIMED_CHUNKS));
        }
        else
        {
            sender.sendMessage(new TextComponentString(NO_PERMISSION_TO_CLAIM_MESSAGE));
        }
    }
}



