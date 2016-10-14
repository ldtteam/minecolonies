package com.minecolonies.commands;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * List all colonies
 */
public class ListColonies extends SingleCommand
{

    public ListColonies(@NotNull final String[] parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args) throws CommandException
    {
        int page = 1;
        if (args.length != 0){
            try
            {
                page = Integer.parseInt(args[0]);
            }catch (NumberFormatException e){
                //ignore and keep page 1
            }
        }
        TextComponentString headerLine = new TextComponentString("----------page "+page+"----------");
        sender.addChatMessage(headerLine);
        for (Colony colony : ColonyManager.getColonies()){
            TextComponentString colonyData = new TextComponentString("§2ID: "  + "§f" + colony.getID() + "§2 Name: " + "§f" + colony.getName());
            sender.addChatMessage(colonyData);
        }
        TextComponentString footerLine = new TextComponentString("-------- <- back next -> --------");
        sender.addChatMessage(footerLine);
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
