package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.ActionMenu;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CommandConstants.NO_COLONY_MESSAGE;
import static com.minecolonies.api.util.constant.CommandConstants.NO_PERMISSION_TO_CLAIM_MESSAGE;
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenu actionMenu) throws CommandException
    {
        //See if the player is opped.
        if (sender instanceof EntityPlayerMP && isPlayerOpped(sender))
        {
            final Colony colony = actionMenu.getColonyForArgument("colony");

            if (colony == null)
            {
                sender.sendMessage(new TextComponentString(NO_COLONY_MESSAGE));
                return;
            }

            final int range = actionMenu.getIntegerForArgument("range");
            final Boolean add = actionMenu.getBooleanForArgument("add");

            final Chunk chunk = ((EntityPlayerMP) sender).getServerWorld().getChunkFromBlockCoords(sender.getPosition());
            ColonyManager.claimChunksInRange(colony.getID(), colony.getDimension(), add == null ? true : add, chunk.x, chunk.z, range, 0);
            return;
        }
        else
        {
            sender.sendMessage(new TextComponentString(NO_PERMISSION_TO_CLAIM_MESSAGE));
        }
    }
}



