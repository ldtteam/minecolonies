package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
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
public class ListColoniesCommand extends AbstractSingleCommand
{

    private static final String ID_AND_NAME_TEXT       = "§2ID: §f%s §2 Name: §f%s";
    private static final String COORDINATES_TEXT       = "§2Coordinates: §f";
    private static final String COORDINATES_XYZ        = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String LIST_COMMAND_SUGGESTED = "/mc colonies list ";
    private static final String TELEPORT_COMMAND       = "/mc colony teleport ";
    private static final String PAGE_TOP_LEFT          = "§2   ------------------ page ";
    private static final String PAGE_TOP_RIGHT         = " ------------------";
    private static final String PAGE_TOP_MIDDLE        = " of ";
    private static final String PREV_PAGE              = " <- prev";
    private static final String NEXT_PAGE              = "next -> ";
    private static final String PAGE_LINE              = "§2 ----------------";
    private static final String PAGE_LINE_DIVIDER      = "§2 | ";
    private static final String COMMAND_COLONY_INFO    = "/mc colony info %d";
    private static final int    COLONIES_ON_PAGE       = 9;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ListColoniesCommand(@NotNull final String... parents)
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
        int page = getIthArgument(args, 0, 1);
        final int abandonedSince = getIthArgument(args, 1, 0);

        final List<Colony> colonies;
        if (abandonedSince > 0)
        {
            colonies = ColonyManager.getColoniesAbandonedSince(abandonedSince);
        }
        else
        {
            colonies = ColonyManager.getColonies();
        }

        final int colonyCount = colonies.size();

        // check to see if we have to add one page to show the half page
        final int halfPage = (colonyCount % COLONIES_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((colonyCount) / COLONIES_ON_PAGE) + halfPage;


        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = COLONIES_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(COLONIES_ON_PAGE * page, colonyCount);
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (colonyCount / COLONIES_ON_PAGE) + halfPage);

        final List<Colony> coloniesPage;

        if (pageStartIndex < 0 || pageStartIndex >= colonyCount)
        {
            coloniesPage = new ArrayList<>();
        }
        else
        {
            coloniesPage = colonies.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new TextComponentString(PAGE_TOP_LEFT + page + PAGE_TOP_MIDDLE + pageCount + PAGE_TOP_RIGHT);
        sender.addChatMessage(headerLine);

        for (final Colony colony : coloniesPage)
        {
            sender.addChatMessage(new TextComponentString(String.format(
                    ID_AND_NAME_TEXT, colony.getID(), colony.getName())).setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    String.format(COMMAND_COLONY_INFO, colony.getID())))));
            final BlockPos center = colony.getCenter();

            final ITextComponent teleport = new TextComponentString(COORDINATES_TEXT + String.format(COORDINATES_XYZ, center.getX(), center.getY(), center.getZ()))
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, TELEPORT_COMMAND + colony.getID())
                    ));

            sender.addChatMessage(teleport);
        }

        final ITextComponent prevButton = new TextComponentString(PREV_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + prevPage)
        ));
        final ITextComponent nextButton = new TextComponentString(NEXT_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + nextPage)
        ));

        final ITextComponent beginLine = new TextComponentString(PAGE_LINE);
        final ITextComponent endLine = new TextComponentString(PAGE_LINE);
        sender.addChatMessage(beginLine.appendSibling(prevButton).appendSibling(new TextComponentString(PAGE_LINE_DIVIDER)).appendSibling(nextButton).appendSibling(endLine));
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
