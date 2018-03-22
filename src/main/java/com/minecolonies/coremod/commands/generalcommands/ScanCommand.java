package com.minecolonies.coremod.commands.generalcommands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.minecolonies.coremod.commands.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.items.ItemScanTool;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Created by asie on 2/16/17.
 */
public class ScanCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final String DESC                   = "scan";
    public static final String NO_PERMISSION_MESSAGE  = "You do not have permission to scan structures!";
    public static final String SCAN_SUCCESS_MESSAGE = "Successfully scan structure!";
    public static final String SCAN_FAILURE_MESSAGE = "Failed to scan structure!";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ScanCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ScanCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenu actionMenu) throws CommandException
    {
        // Will throw ClassCastException if null, but should never be null as these values are required.
        final EntityPlayerMP player = actionMenu.getPlayerForArgument("player");
        final int x1 = actionMenu.getIntegerForArgument("x1");
        final int y1 = actionMenu.getIntegerForArgument("y1");
        final int z1 = actionMenu.getIntegerForArgument("z1");
        final int x2 = actionMenu.getIntegerForArgument("x2");
        final int y2 = actionMenu.getIntegerForArgument("y2");
        final int z2 = actionMenu.getIntegerForArgument("z2");

        final BlockPos from = new BlockPos(x1, y1, z1);
        final BlockPos to = new BlockPos(x2, y2, z2);
        executeShared(server, sender, from, to, player);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        //executeShared(server, sender, from, to);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender,
            @NotNull final BlockPos from, @NotNull final BlockPos to, @Nullable final EntityPlayerMP playerArgument) throws CommandException
    {

        if (isPlayerOpped(sender))
        {
            server.addScheduledTask(() ->
            {
                @Nullable final World world = server.getEntityWorld();
                @NotNull final EntityPlayerMP player;
                if(playerArgument != null)
                {
                    player = playerArgument;
                }
                else if (sender instanceof EntityPlayer)
                {
                    player = (EntityPlayerMP) sender;
                }
                else
                {
                    sender.sendMessage(new TextComponentString(SCAN_FAILURE_MESSAGE));
                    return;
                }
                ItemScanTool.saveStructure(world, from, to, player);
                sender.sendMessage(new TextComponentString(SCAN_SUCCESS_MESSAGE));
            });
        }
        else
        {
            sender.sendMessage(new TextComponentString(NO_PERMISSION_MESSAGE));
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
