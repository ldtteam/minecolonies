package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_CITIZEN_INFO;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.colonycommands.CommandListColonies.START_PAGE_ARG;

/**
 * Lists all citizen of a given colony.
 */
public class CommandCitizenList implements IMCColonyOfficerCommand
{
    private static final String LIST_COMMAND_SUGGESTED         = "/minecolonies citizens list %d %d";
    private static final String COMMAND_CITIZEN_INFO_SUGGESTED = "/minecolonies citizens info %d %d";

    private static final int CITIZENS_ON_PAGE = 9;

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        return displayListFor(context, 1);
    }

    private int executeWithPage(final CommandContext<CommandSourceStack> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return displayListFor(context, IntegerArgumentType.getInteger(context, START_PAGE_ARG));
    }

    private int displayListFor(final CommandContext<CommandSourceStack> context, int page)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
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
        final Component headerLine = Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_PAGE_TOP, page, pageCount);
        context.getSource().sendSuccess(headerLine, true);

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

    private void drawCitizens(@NotNull final CommandContext<CommandSourceStack> context, final List<ICitizenData> citizensPage)
    {
        for (final ICitizenData citizen : citizensPage)
        {
            context.getSource().sendSuccess(Component.translatable(COMMAND_CITIZEN_INFO, citizen.getId(), citizen.getName())
                                              .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                String.format(COMMAND_CITIZEN_INFO_SUGGESTED, citizen.getColony().getID(), citizen.getId())))), true);

            citizen.getEntity().ifPresent(entityCitizen ->
            {
                final BlockPos position = entityCitizen.blockPosition();
                context.getSource()
                  .sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_POSITION, position.getX(), position.getY(), position.getZ()), true);
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
    private static void drawPageSwitcher(@NotNull final CommandContext<CommandSourceStack> context, final int page, final int count, final int halfPage, final int colonyId)
    {
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (count / CITIZENS_ON_PAGE) + halfPage);

        final Component prevButton =
          Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_PREVIOUS).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, prevPage))
          ));
        final Component nextButton =
          Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_NEXT).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, nextPage))
          ));

        final MutableComponent beginLine = Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_PAGE_LINE);
        final MutableComponent endLine = Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_PAGE_LINE);

        context.getSource().sendSuccess(beginLine.append(prevButton)
                                          .append(Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_LIST_PAGE_STYLE))
                                          .append(nextButton)
                                          .append(endLine), true);
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
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .executes(this::checkPreConditionAndExecute)
                         .then(IMCCommand.newArgument(START_PAGE_ARG, IntegerArgumentType.integer(1)).executes(this::executeWithPage)));
    }
}
