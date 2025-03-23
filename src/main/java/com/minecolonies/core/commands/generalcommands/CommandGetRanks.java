package com.minecolonies.core.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.core.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandGetRanks implements IMCOPCommand
{
    private static final String COLONY_INFO_TEXT       = "ID: %s  Name: %s  Rank: %s";
    private static final String COORDINATES_TEXT       = "Coordinates: ";
    private static final String COORDINATES_XYZ         = "x=%s y=%s z=%s";
    private static final String RANKS_COMMAND_SUGGESTED = "/minecolonies ranks ";
    private static final String PAGE_TOP_LEFT           = "   ------------------ page ";
    private static final String PAGE_TOP_RIGHT         = " ------------------";
    private static final String PAGE_TOP_MIDDLE        = " of ";
    private static final String PREV_PAGE              = " <- prev";
    private static final String NEXT_PAGE              = "next -> ";
    private static final String PAGE_LINE              = " ----------------";
    private static final String PAGE_LINE_DIVIDER      = " | ";
    private static final int    COLONIES_ON_PAGE       = 10;
    private static final String START_PAGE_ARG         = "startpage";

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
        GameProfile profile;
        try
        {
            profile = GameProfileArgument.getGameProfiles(context, PLAYERNAME_ARG).stream().findFirst().orElse(null);
        }
        catch (CommandSyntaxException e)
        {
            return 0;
        }

        if (context.getSource().getServer().getPlayerList().getPlayer(profile.getId()) == null)
        {
            // could not find player with given name.
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_PLAYER_NOT_FOUND, profile.getName()), true);
            return 0;
        }

        int page = startpage;
        final List<IColony> colonies = IColonyManager.getInstance()
            .getAllColonies()
            .stream()
            .filter(colony -> !colony.getPermissions().getRankNeutral().equals(colony.getPermissions().getRank(profile.getId())))
            .toList();
        final int colonyCount = colonies.size();
        final int halfPage = (colonyCount % COLONIES_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = (int) (Math.floor((double) colonyCount / COLONIES_ON_PAGE) + halfPage);

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
            final Rank rank = colony.getPermissions().getRank(profile.getId());
            context.getSource().sendSuccess(() -> Component.literal(String.format(COLONY_INFO_TEXT, colony.getID(), colony.getName(), rank.getName())), true);
        }

        final Component prevButton = Component.literal(PREV_PAGE)
            .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, RANKS_COMMAND_SUGGESTED + prevPage)));

        final Component nextButton = Component.literal(NEXT_PAGE)
            .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, RANKS_COMMAND_SUGGESTED + nextPage)));

        final MutableComponent beginLine = Component.literal(PAGE_LINE);
        final MutableComponent endLine = Component.literal(PAGE_LINE);
        context.getSource().sendSuccess(() -> beginLine.append(prevButton).append(Component.literal(PAGE_LINE_DIVIDER)).append(nextButton).append(endLine), true);
        return 1;
    }

    @Override
    public String getName()
    {
        return "ranks";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile())
                .then(IMCCommand.newArgument(START_PAGE_ARG, IntegerArgumentType.integer()).executes(this::executeWithPage))
                .executes(this::checkPreConditionAndExecute));
    }
}
