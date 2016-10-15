package com.minecolonies.commands;

import com.blockout.Log;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * List all colonies.
 */
public class ListColonies extends AbstractSingleCommand
{

    private static final String ID_TEXT                = "§2ID: §f";
    private static final String NAME_TEXT              = "§2 Name: §f";
    private static final String COORDINATES_TEXT       = "§2Coordinates: §f";
    private static final String LIST_COMMAND_SUGGESTED = "/mc colonies list ";
    private static final int    COLONIES_ON_PAGE       = 9;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
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
        final List<Colony> colonies = ColonyManager.getColonies();
        final int colonyCount = colonies.size();

        // check to see if we have to add one page to show the half page
        final int halfPage = (colonyCount % COLONIES_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((colonyCount) / COLONIES_ON_PAGE) + halfPage;

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
        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = COLONIES_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(COLONIES_ON_PAGE * page, colonyCount);
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (colonyCount / COLONIES_ON_PAGE) + halfPage);

        List<Colony> coloniesPage;

        if (pageStartIndex < 0 || pageStartIndex >= colonyCount)
        {
            coloniesPage = new ArrayList<>();
        }
        else
        {
            coloniesPage = colonies.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new TextComponentString("§2   ------------------ page " + page + " of " + pageCount + " ------------------");
        sender.addChatMessage(headerLine);

        for (final Colony colony : coloniesPage)
        {
            sender.addChatMessage(new TextComponentString(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()));
            final BlockPos center = colony.getCenter();
            sender.addChatMessage(new TextComponentString(COORDINATES_TEXT + String.format("§4x=§f%s §4y=§f%s §4z=§f%s", center.getX(), center.getY(), center.getZ())));
        }

        final ITextComponent prevButton = new TextComponentString(" <- prev").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED+prevPage)
        ));
        final ITextComponent nextButton = new TextComponentString("next -> ").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED+nextPage)
        ));

        final ITextComponent beginLine = new TextComponentString("§2 ----------------");
        final ITextComponent endLine = new TextComponentString("§2---------------- ");
        sender.addChatMessage(beginLine.appendSibling(prevButton).appendSibling(new TextComponentString("§2 | ")).appendSibling(nextButton).appendSibling(endLine));
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
