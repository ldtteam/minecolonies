package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.ldtteam.structurize.items.ItemScanTool;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.CommandConstants.*;
import static com.minecolonies.coremod.commands.AbstractSingleCommand.isPlayerOpped;

/**
 * Created by asie on 2/16/17.
 */
public class ScanCommand implements IActionCommand
{
    public static final String DESC                   = "scan";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ScanCommand()
    {
        super();
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final String playerArgument = actionMenuState.getStringForArgument("player");

        // Will throw ClassCastException if null, but should never be null as these values are required.
        final int x1 = actionMenuState.getIntForArgument("x1");
        final int y1 = actionMenuState.getIntForArgument("y1");
        final int z1 = actionMenuState.getIntForArgument("z1");
        final int x2 = actionMenuState.getIntForArgument("x2");
        final int y2 = actionMenuState.getIntForArgument("y2");
        final int z2 = actionMenuState.getIntForArgument("z2");
        final String name = actionMenuState.getStringForArgument("name");

        if (!name.matches("^[\\w]*"))
        {
            sender.sendMessage(new TextComponentString(SPECIAL_CHARACTERS_ADVICE));
            return;
        }

        final BlockPos from = new BlockPos(x1, y1, z1);
        final BlockPos to = new BlockPos(x2, y2, z2);
        if (isPlayerOpped(sender))
        {
            server.addScheduledTask(() ->
            {
                @Nullable final World world = server.getEntityWorld();
                final PlayerEntity player;
                if (playerArgument != null)
                {
                    if (playerArgument.equalsIgnoreCase("@p"))
                    {
                        final BlockPos pos = sender.getPosition();
                        player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 50, false);
                    }
                    else
                    {
                        player = world.getPlayerEntityByName(playerArgument);
                    }
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

                if (player == null)
                {
                    sender.sendMessage(new TextComponentString(SCAN_FAILURE_MESSAGE));
                    return;
                }

                ItemScanTool.saveStructure(world, from, to, player, name == null ? "" : name);
                sender.sendMessage(new TextComponentString(SCAN_SUCCESS_MESSAGE));
            });
        }
        else
        {
            sender.sendMessage(new TextComponentString(NO_PERMISSION_TO_SCAN_MESSAGE));
        }
    }
}
