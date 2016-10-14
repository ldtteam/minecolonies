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
 * List all colonies.
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
        final int coloniesOnPage = 8;
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
        final TextComponentString headerLine = new TextComponentString("§2----------page " + page + " of " + pageCount + "----------");
        sender.addChatMessage(headerLine);

        final String writeId = ("§2ID: §f");
        final String writeName = ("§2 Name: §f");
        final String writeCoords = ("§8Coordinates: ");
        int lastColonyNumber = coloniesOnPage * page - (coloniesOnPage - 1);
        int latestColonyNumber = coloniesOnPage * page;
        final int lastPageColonies = colonyCount % coloniesOnPage;
        if (page == pageCount)
        {
            if (coloniesOnPage == lastPageColonies)
            {
                for (int i = lastColonyNumber; i <= latestColonyNumber; i++)
                {
                    final TextComponentString colonyData =
                      new TextComponentString(writeId + ColonyManager.getColony(i).getID() + writeName + ColonyManager.getColony(i).getName());
                    sender.addChatMessage(colonyData);
                    final TextComponentString colonyCoords = new TextComponentString(writeCoords + ColonyManager.getColony(i).getCenter());
                    sender.addChatMessage(colonyCoords);
                }
            }
            else
            {
                for (int i = lastColonyNumber; i <= latestColonyNumber - (coloniesOnPage - lastPageColonies); i++)
                {
                    final TextComponentString colonyData =
                      new TextComponentString(writeId + ColonyManager.getColony(i).getID() + writeName + ColonyManager.getColony(i).getName());
                    sender.addChatMessage(colonyData);
                    final TextComponentString colonyCoords = new TextComponentString(writeCoords + ColonyManager.getColony(i).getCenter());
                    sender.addChatMessage(colonyCoords);
                }
            }
        }
        else
        {
            for (int i = lastColonyNumber; i <= latestColonyNumber; i++)
            {
                final TextComponentString colonyData = new TextComponentString(writeId + ColonyManager.getColony(i).getID()
                                                                                 + writeName + ColonyManager.getColony(i).getName());
                sender.addChatMessage(colonyData);
                final TextComponentString colonyCoords = new TextComponentString(writeCoords + ColonyManager.getColony(i).getCenter());
                sender.addChatMessage(colonyCoords);
            }
        }
        final TextComponentString footerLine = new TextComponentString("§2------------------------------");
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
