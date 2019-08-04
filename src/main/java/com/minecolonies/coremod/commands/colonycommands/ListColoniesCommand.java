package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * List all colonies.
 */
public class ListColoniesCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final  String DESC                   = "list";
    private static final String ID_AND_NAME_TEXT       = "§2ID: §f%s §2 Name: §f%s";
    private static final String COORDINATES_TEXT       = "§2Coordinates: §f";
    private static final String COORDINATES_XYZ        = "§4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String LIST_COMMAND_SUGGESTED = "/mc colonies list page: ";
    public static final  String TELEPORT_COMMAND       = "/mc colony teleport colony: ";
    private static final String PAGE_TOP_LEFT          = "§2   ------------------ page ";
    private static final String PAGE_TOP_RIGHT         = " ------------------";
    private static final String PAGE_TOP_MIDDLE     = " of ";
    private static final String PREV_PAGE           = " <- prev";
    private static final String NEXT_PAGE           = "next -> ";
    private static final String PAGE_LINE           = "§2 ----------------";
    private static final String PAGE_LINE_DIVIDER   = "§2 | ";
    private static final String COMMAND_COLONY_INFO = "/mc colony info colony: %d";
    private static final int    COLONIES_ON_PAGE    = 9;

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ListColoniesCommand()
    {
        super();
    }

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
    public String getCommandUsage(@NotNull final CommandSource sender)
    {
        return super.getCommandUsage(sender);
    }

    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        @Nullable final Integer page = actionMenuState.getIntForArgument("page");
        @Nullable final Integer abandonedSinceTimeInHours = actionMenuState.getIntForArgument("abandonedSinceTimeInHours");
        executeShared(server, sender, page, abandonedSinceTimeInHours);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args) throws CommandException
    {
        final int page = getIthArgument(args, 0, 1);
        final int abandonedSinceTimeInHours = getIthArgument(args, 1, 0);

        executeShared(server, sender, page, abandonedSinceTimeInHours);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final CommandSource sender,
            @Nullable final Integer pageProvided, @Nullable final Integer abandonedSinceTimeInHoursProvided) throws CommandException
    {
        int page;
        if (null != pageProvided)
        {
            page = pageProvided.intValue();
        }
        else
        {
            page = 1;
        }

        int abandonedSinceTimeInHours;
        if (null != abandonedSinceTimeInHoursProvided)
        {
            abandonedSinceTimeInHours = abandonedSinceTimeInHoursProvided.intValue();
        }
        else
        {
            abandonedSinceTimeInHours = 0;
        }

        final List<IColony> colonies;
        if (abandonedSinceTimeInHours > 0)
        {
            colonies = IColonyManager.getInstance().getColoniesAbandonedSince(abandonedSinceTimeInHours);
        }
        else
        {
            colonies = IColonyManager.getInstance().getAllColonies();
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

        final List<IColony> coloniesPage;

        if (pageStartIndex < 0 || pageStartIndex >= colonyCount)
        {
            coloniesPage = new ArrayList<>();
        }
        else
        {
            coloniesPage = colonies.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new StringTextComponent(PAGE_TOP_LEFT + page + PAGE_TOP_MIDDLE + pageCount + PAGE_TOP_RIGHT);
        sender.sendMessage(headerLine);

        for (final IColony colony : coloniesPage)
        {
            sender.sendMessage(new StringTextComponent(String.format(
              ID_AND_NAME_TEXT, colony.getID(), colony.getName())).setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                                                      String.format(COMMAND_COLONY_INFO, colony.getID())))));
            final BlockPos center = colony.getCenter();

            final ITextComponent teleport = new StringTextComponent(COORDINATES_TEXT + String.format(COORDINATES_XYZ, center.getX(), center.getY(), center.getZ()));
            if(isPlayerOpped(sender))
            {
                teleport.setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, TELEPORT_COMMAND + colony.getID())));
            }

            sender.sendMessage(teleport);
        }

        final ITextComponent prevButton = new StringTextComponent(PREV_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + prevPage)
        ));
        final ITextComponent nextButton = new StringTextComponent(NEXT_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + nextPage)
        ));

        final ITextComponent beginLine = new StringTextComponent(PAGE_LINE);
        final ITextComponent endLine = new StringTextComponent(PAGE_LINE);
        sender.sendMessage(beginLine.appendSibling(prevButton).appendSibling(new StringTextComponent(PAGE_LINE_DIVIDER)).appendSibling(nextButton).appendSibling(endLine));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final CommandSource sender,
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
