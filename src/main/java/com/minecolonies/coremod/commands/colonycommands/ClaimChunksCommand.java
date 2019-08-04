package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
            final int colonyId = actionMenuState.getIntForArgument("colony");
            final int dimId = actionMenuState.getIntForArgument("dimension");

            final int range = actionMenuState.getIntValueForArgument("range", Configurations.gameplay.workingRangeTownHallChunks);
            final Boolean add = actionMenuState.getBooleanForArgument("add");

            final World senderWorld = sender.getEntityWorld();
            final BlockPos senderPos = sender.getPosition();

            if (range > Configurations.gameplay.workingRangeTownHallChunks * 2)
            {
                sender.sendMessage(new StringTextComponent(TOO_MANY_CHUNKS));
                return;
            }

            final IChunkmanagerCapability chunkManager = senderWorld.getCapability(CHUNK_STORAGE_UPDATE_CAP, null);
            if (chunkManager == null)
            {
                Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT);
                return;
            }

            if (chunkManager.getAllChunkStorages().size() > CHUNKS_TO_CLAM_THRESHOLD)
            {
                sender.sendMessage(new StringTextComponent(TOO_MANY_CHUNKS_CLAIMED));
                return;
            }

            ChunkDataHelper.claimChunksInRange(colonyId, dimId, add == null || add, senderPos, range, 0, senderWorld);
            sender.sendMessage(new StringTextComponent(SUCCESFULLY_CLAIMED_CHUNKS));
        }
        else
        {
            sender.sendMessage(new StringTextComponent(NO_PERMISSION_TO_CLAIM_MESSAGE));
        }
    }
}



