package com.minecolonies.coremod.commands.citizencommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.colonycommands.CommandListColonies.START_PAGE_ARG;

/**
 * Lists all citizen of a given colony.
 */
public class CommandCitizenList implements IMCColonyOfficerCommand
{
    private static final String LIST_COMMAND_SUGGESTED = "/minecolonies citizens list %d %d";
    private static final String COMMAND_CITIZEN_INFO   = "/minecolonies citizens info %d %d";

    private static final int CITIZENS_ON_PAGE = 9;

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return displayListFor(context, 1);
    }

    private int executeWithPage(final CommandContext<CommandSource> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return displayListFor(context, IntegerArgumentType.getInteger(context, START_PAGE_ARG));
    }

    private int displayListFor(final CommandContext<CommandSource> context, int page)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        final List<ICitizenData> citizens = colony.getCitizenManager().getCitizens();
        final int citizenCount = citizens.size();

        // check to see if we have to add one page to show the half page
        final int halfPage = (citizenCount % CITIZENS_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((citizenCount) / CITIZENS_ON_PAGE) + halfPage;

        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = CITIZENS_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(CITIZENS_ON_PAGE * page, citizenCount);

        final List<ICitizenData> citizensPage = getCitizensOnPage(citizens, citizenCount, pageStartIndex, pageStopIndex);
        final ITextComponent headerLine = LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.pagetop", page, pageCount);
        context.getSource().sendFeedback(headerLine, true);

        drawCitizens(context, citizensPage);
        drawPageSwitcher(context, page, citizenCount, halfPage, colony.getID());
        return 1;
    }

    @NotNull
    private List<ICitizenData> getCitizensOnPage(final List<ICitizenData> citizens, final int citizenCount, final int pageStartIndex, final int pageStopIndex)
    {
        final List<ICitizenData> citizensPage;

        if (pageStartIndex < 0 || pageStartIndex >= citizenCount)
        {
            citizensPage = new ArrayList<>();
        }
        else
        {
            citizensPage = citizens.subList(pageStartIndex, pageStopIndex);
        }
        return citizensPage;
    }

    private void drawCitizens(@NotNull final CommandContext<CommandSource> context, final List<ICitizenData> citizensPage)
    {
        for (final ICitizenData citizen : citizensPage)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.desc", citizen.getId(), citizen.getName())
                                 .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                   String.format(COMMAND_CITIZEN_INFO, citizen.getColony().getID(), citizen.getId())))), true);

            citizen.getCitizenEntity().ifPresent(entityCitizen ->
            {
                final BlockPos position = entityCitizen.getPosition();
                context.getSource()
                  .sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.pos", position.getX(), position.getY(), position.getZ()), true);
            });
        }
    }

    /**
     * Draws the page switcher at the bottom.
     *
     * @param context  the command context.
     * @param page     the page number.
     * @param count    number of citizens.
     * @param halfPage the halfPage.
     * @param colonyId the colony id.
     */
    private static void drawPageSwitcher(@NotNull final CommandContext<CommandSource> context, final int page, final int count, final int halfPage, final int colonyId)
    {
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (count / CITIZENS_ON_PAGE) + halfPage);

        final ITextComponent prevButton =
          LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.prev").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, prevPage))
          ));
        final ITextComponent nextButton =
          LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.next").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, nextPage))
          ));

        final ITextComponent beginLine = LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.pageline");
        final ITextComponent endLine = LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.pageline");

        context.getSource().sendFeedback(beginLine.appendSibling(prevButton)
                             .appendSibling(LanguageHandler.buildChatComponent("com.minecolonies.command.citizenlist.pagestyle"))
                             .appendSibling(nextButton)
                                           .appendSibling(endLine), true);
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
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .executes(this::checkPreConditionAndExecute)
                         .then(IMCCommand.newArgument(START_PAGE_ARG, IntegerArgumentType.integer(1)).executes(this::executeWithPage)));
    }
}
