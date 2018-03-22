package com.minecolonies.coremod.commands.generalcommands;

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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.isPlayerOpped;

/**
 * Created by asie on 2/16/17.
 */
public class ScanCommand implements IActionCommand
{
    public static final String DESC                   = "scan";
    public static final String NO_PERMISSION_MESSAGE  = "You do not have permission to scan structures!";
    public static final String SCAN_SUCCESS_MESSAGE = "Successfully scan structure!";
    public static final String SCAN_FAILURE_MESSAGE = "Failed to scan structure!";
    public static final String MISSING_PLAYER = "Failed to scan structure, missing player to store the file!";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ScanCommand()
    {
        super();
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenu actionMenu) throws CommandException
    {
        final EntityPlayerMP playerArgument = actionMenu.getPlayerForArgument("player");

        // Will throw ClassCastException if null, but should never be null as these values are required.
        final int x1 = actionMenu.getIntegerForArgument("x1");
        final int y1 = actionMenu.getIntegerForArgument("y1");
        final int z1 = actionMenu.getIntegerForArgument("z1");
        final int x2 = actionMenu.getIntegerForArgument("x2");
        final int y2 = actionMenu.getIntegerForArgument("y2");
        final int z2 = actionMenu.getIntegerForArgument("z2");

        final BlockPos from = new BlockPos(x1, y1, z1);
        final BlockPos to = new BlockPos(x2, y2, z2);
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
}
