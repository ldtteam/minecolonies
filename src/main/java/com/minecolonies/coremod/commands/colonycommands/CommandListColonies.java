package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListColonies implements IMCCommand
{
    public static final  String DESC                   = "list";
    private static final String ID_AND_NAME_TEXT       = "ID: %s  Name: %s";
    private static final String COORDINATES_TEXT       = "Coordinates: ";
    private static final String COORDINATES_XYZ        = "x=%s y=%s z=%s";
    private static final String LIST_COMMAND_SUGGESTED = "/minecolonies colony list ";
    private static final String TELEPORT_COMMAND       = "/minecolonies colony teleport ";
    private static final String PAGE_TOP_LEFT          = "   ------------------ page ";
    private static final String PAGE_TOP_RIGHT      = " ------------------";
    private static final String PAGE_TOP_MIDDLE     = " of ";
    private static final String PREV_PAGE           = " <- prev";
    private static final String NEXT_PAGE           = "next -> ";
    private static final String PAGE_LINE           = " ----------------";
    private static final String PAGE_LINE_DIVIDER   = " | ";
    private static final String COMMAND_COLONY_INFO = "/minecolonies colony info ";
    private static final int    COLONIES_ON_PAGE    = 9;
    public static final  String START_PAGE_ARG      = "startpage";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return executeCommand(context, 1);
    }

    private int executeWithPage(final CommandContext<CommandSource> context)
    {
        if (checkPreCondition(context))
        {
            return executeCommand(context, IntegerArgumentType.getInteger(context, START_PAGE_ARG));
        }
        return 0;
    }

    private int executeCommand(final CommandContext<CommandSource> context, final int startpage)
    {
        final Entity sender = context.getSource().getEntity();

        int page = startpage;
        final List<IColony> colonies = IColonyManager.getInstance().getAllColonies();


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
        final int prevPage = Math.max(1, page - 1);
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
        context.getSource().sendFeedback(headerLine, true);


        for (final IColony colony : coloniesPage)
        {
            context.getSource().sendFeedback(new StringTextComponent(String.format(
              ID_AND_NAME_TEXT, colony.getID(), colony.getName())).setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
              String.format(COMMAND_COLONY_INFO, colony.getID())))), true);
            final BlockPos center = colony.getCenter();

            final ITextComponent teleport = new StringTextComponent(COORDINATES_TEXT + String.format(COORDINATES_XYZ, center.getX(), center.getY(), center.getZ()));
            teleport.setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, TELEPORT_COMMAND + colony.getID())));

            context.getSource().sendFeedback(teleport, true);
        }

        final ITextComponent prevButton = new StringTextComponent("click").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + prevPage)));

        final ITextComponent nextButton = new StringTextComponent(NEXT_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + nextPage)
        ));

        final ITextComponent beginLine = new StringTextComponent(PAGE_LINE);
        final ITextComponent endLine = new StringTextComponent(PAGE_LINE);
        context.getSource()
          .sendFeedback(beginLine.appendSibling(prevButton).appendSibling(new StringTextComponent(PAGE_LINE_DIVIDER)).appendSibling(nextButton).appendSibling(endLine), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(START_PAGE_ARG, IntegerArgumentType.integer(1)).executes(this::executeWithPage)).executes(this::checkPreConditionAndExecute);
    }
}
