package com.minecolonies.coremod.commands.generalcommands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionArgument;
import com.minecolonies.coremod.commands.IActionCommand;
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final List<ActionArgument> actionArgumentList,
            @NotNull final Map<String, Object> argumentValueByActionArgumentNameMap) throws CommandException
    {
        final int x1 = (int) argumentValueByActionArgumentNameMap.get("x1");
        final int y1 = (int) argumentValueByActionArgumentNameMap.get("y1");
        final int z1 = (int) argumentValueByActionArgumentNameMap.get("z1");
        final int x2 = (int) argumentValueByActionArgumentNameMap.get("x2");
        final int y2 = (int) argumentValueByActionArgumentNameMap.get("y2");
        final int z2 = (int) argumentValueByActionArgumentNameMap.get("z2");
        final BlockPos from = new BlockPos(x1, y1, z1);
        final BlockPos to = new BlockPos(x2, y2, z2);
        executeShared(server, sender, from, to);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
//        executeShared(server, sender, from, to);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender,
            @NotNull final BlockPos from, @NotNull final BlockPos to) throws CommandException
    {

        if (isPlayerOpped(sender))
        {
            server.addScheduledTask(() ->
            {
                @Nullable final World world = server.getEntityWorld();
                @NotNull final EntityPlayer player;
                if (sender instanceof EntityPlayer)
                {
                    player = (EntityPlayer) sender;
                }
                else
                {
                    // Not sure this is allowed in saveStructure()
                    player = null;
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
