package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.core.commands.colonycommands.CommandColonyInfo.ID_TEXT;
import static com.minecolonies.core.commands.colonycommands.CommandColonyInfo.NAME_TEXT;

public class CommandColonyChunks implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }


        Set<TicketType> types = new HashSet<>();

        for (final Long chunkLong : colony.getLoadedChunks())
        {
            final SortedArraySet<Ticket<?>> tickets = context.getSource().getLevel().getChunkSource().chunkMap.getDistanceManager().tickets.get((long) chunkLong);
            if (tickets != null)
            {
                for (final Ticket<?> ticket : tickets)
                {
                    types.add(ticket.getType());
                }
            }
        }

        StringBuilder ticketString = new StringBuilder();
        for (final TicketType type : types)
        {
            ticketString.append("[").append(type).append("]");
        }

        context.getSource()
          .sendSuccess(() -> Component.literal(ID_TEXT)
            .append(Component.literal("" + colony.getID()).withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" " + NAME_TEXT))
            .append(Component.literal("" + colony.getName()).withStyle(ChatFormatting.YELLOW)), true);
        context.getSource().sendSuccess(() -> Component.literal("Loaded chunks:").append(Component.literal(" " + colony.getLoadedChunkCount()).withStyle(ChatFormatting.YELLOW)), true);
        context.getSource().sendSuccess(() -> Component.translatable("Ticket types: ").append(Component.literal(ticketString.toString()).withStyle(ChatFormatting.YELLOW)), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "chunkstatus";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
