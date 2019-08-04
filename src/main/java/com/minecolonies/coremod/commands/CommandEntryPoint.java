package com.minecolonies.coremod.commands;

import com.minecolonies.api.configuration.Configurations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Command entry point to make minecraft inheritance happy.
 */
public class CommandEntryPoint extends CommandBase
{
    /**
     * The level required to execute /mc commands.
     * private static final int OP_PERMISSION_LEVEL = 3;
     */

    @NotNull
    private final MinecoloniesCommand root;

    /**
     * Create our entry point once.
     */
    public CommandEntryPoint()
    {
        super();
        root = new MinecoloniesCommand();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "minecolonies";
    }

    @NotNull
    @Override
    public String getUsage(final CommandSource sender)
    {
        return root.getCommandUsage(sender);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String[] args) throws CommandException
    {
        // We can pass this without stripping as mc does that for us with the alias
        root.execute(server, sender, args);
    }

    /**
     * Removed so we can control this at the config
     * Only allow OP's execute the commands.
     *
     * @return the int permission level (3 for OP).
     * public int getRequiredPermissionLevel(){return OP_PERMISSION_LEVEL;}
     */
    @NotNull
    @Override
    public List<String> getAliases()
    {
        return Arrays.asList("mc_old");
    }

    /**
     * Check if the player has the permission to use commands.
     *
     * @param server the server to check for.
     * @param sender the sender of the command.
     * @return true if so.
     */
    @Override
    public boolean checkPermission(final MinecraftServer server, final CommandSource sender)
    {
        if (sender instanceof PlayerEntity)
        {
            return AbstractSingleCommand.isPlayerOpped(sender) || Configurations.gameplay.opLevelForServer <= 0;
        }
        return true;
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(
                                           @NotNull final MinecraftServer server,
                                           @NotNull final CommandSource sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        return root.getTabCompletionOptions(server, sender, args, pos);
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return root.isUsernameIndex(args, index);
    }
}
