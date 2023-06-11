package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class CommandListColonies implements IMCCommand
{
    public static final  String DESC                   = "list";
    private static final String ID_AND_NAME_TEXT       = "ID: %s  Name: %s";
    private static final String COORDINATES_TEXT       = "Coordinates: ";
    private static final String COORDINATES_XYZ        = "x=%s y=%s z=%s";
    private static final String LIST_COMMAND_SUGGESTED = "/minecolonies colony list ";
    private static final String TELEPORT_COMMAND       = "/minecolonies colony teleport ";
    private static final String PAGE_TOP_LEFT          = "   ------------------ page ";
    private static final String PAGE_TOP_RIGHT         = " ------------------";
    private static final String PAGE_TOP_MIDDLE        = " of ";
    private static final String PREV_PAGE              = " <- prev";
    private static final String NEXT_PAGE              = "next -> ";
    private static final String PAGE_LINE              = " ----------------";
    private static final String PAGE_LINE_DIVIDER      = " | ";
    private static final String COMMAND_COLONY_INFO    = "/minecolonies colony info %d";
    private static final int    COLONIES_ON_PAGE       = 9;
    public static final  String START_PAGE_ARG         = "startpage";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        return executeCommand(context, 1);
    }

    private int executeWithPage(final CommandContext<CommandSourceStack> context)
    {
        if (checkPreCondition(context))
        {
            return executeCommand(context, IntegerArgumentType.getInteger(context, START_PAGE_ARG));
        }
        return 0;
    }

    private int executeCommand(final CommandContext<CommandSourceStack> context, final int startpage)
    {
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

        final Component headerLine = Component.literal(PAGE_TOP_LEFT + page + PAGE_TOP_MIDDLE + pageCount + PAGE_TOP_RIGHT);
        context.getSource().sendSuccess(() -> headerLine, true);


        for (final IColony colony : coloniesPage)
        {
            context.getSource().sendSuccess(() -> Component.literal(String.format(
              ID_AND_NAME_TEXT, colony.getID(), colony.getName())).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
              String.format(COMMAND_COLONY_INFO, colony.getID())))), true);
            final BlockPos center = colony.getCenter();

            final MutableComponent teleport = Component.literal(COORDINATES_TEXT + String.format(COORDINATES_XYZ, center.getX(), center.getY(), center.getZ()));
            teleport.setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, TELEPORT_COMMAND + colony.getID())));

            context.getSource().sendSuccess(() -> teleport, true);
        }

        final Component prevButton = Component.literal(PREV_PAGE).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + prevPage)));

        final Component nextButton = Component.literal(NEXT_PAGE).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + nextPage)
        ));

        final MutableComponent beginLine = Component.literal(PAGE_LINE);
        final MutableComponent endLine = Component.literal(PAGE_LINE);
        context.getSource()
          .sendSuccess(() -> beginLine.append(prevButton).append(Component.literal(PAGE_LINE_DIVIDER)).append(nextButton).append(endLine), true);
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
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(START_PAGE_ARG, IntegerArgumentType.integer(1)).executes(this::executeWithPage)).executes(this::checkPreConditionAndExecute);
    }
}
