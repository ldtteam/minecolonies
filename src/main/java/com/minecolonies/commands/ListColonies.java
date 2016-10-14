package com.minecolonies.commands;

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

    public ListColonies(@NotNull final String... parents)
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int page = 1;
        int colonyCount = ColonyManager.getColonies().size();
        int pageCount;
        int coloniesOnPage = 8;
        if (colonyCount % coloniesOnPage == 0)
        {
            pageCount = colonyCount / coloniesOnPage;
        }
        else
        {
            pageCount = colonyCount / coloniesOnPage + 1;
        }
        if (args.length != 0)
        {
            try
            {
                page = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                //ignore and keep page 1.
            }
        }
        TextComponentString headerLine = new TextComponentString("§2----------page "+page+" of "+pageCount+"----------");
        sender.addChatMessage(headerLine);

        int lastColonyNumber = coloniesOnPage * page - (coloniesOnPage - 1);
        int latestColonyNumber = coloniesOnPage * page;
        int lastPageColonies = colonyCount % coloniesOnPage;
        if (page == pageCount)
        {
            if (coloniesOnPage == lastPageColonies)
            {
                for (int i = lastColonyNumber; i <= latestColonyNumber; i++)
                {
                    TextComponentString colonyData =
                      new TextComponentString("§2ID: " + "§f" + ColonyManager.getColony(i).getID() + "§2 Name: " + "§f" + ColonyManager.getColony(i).getName());
                    sender.addChatMessage(colonyData);
                    TextComponentString colonyCoords = new TextComponentString("§8Coordinates: " + ColonyManager.getColony(i).getCenter());
                    sender.addChatMessage(colonyCoords);
                }
            }
            else
            {
                for (int i = lastColonyNumber; i <= latestColonyNumber - (coloniesOnPage - lastPageColonies); i++)
                {
                    TextComponentString colonyData =
                      new TextComponentString("§2ID: " + "§f" + ColonyManager.getColony(i).getID() + "§2 Name: " + "§f" + ColonyManager.getColony(i).getName());
                    sender.addChatMessage(colonyData);
                    TextComponentString colonyCoords = new TextComponentString("§8Coordinates: " + ColonyManager.getColony(i).getCenter());
                    sender.addChatMessage(colonyCoords);
                }
            }
        }
        else
            {
            for (int i = lastColonyNumber; i <= latestColonyNumber; i++)
            {
                TextComponentString colonyData = new TextComponentString("§2ID: "  + "§f" + ColonyManager.getColony(i).getID()
                                                                           + "§2 Name: " + "§f" + ColonyManager.getColony(i).getName());
                sender.addChatMessage(colonyData);
                TextComponentString colonyCoords = new TextComponentString("§8Coordinates: " + ColonyManager.getColony(i).getCenter());
                sender.addChatMessage(colonyCoords);
            }
        }
        TextComponentString footerLine = new TextComponentString("§2------------------------------");
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
