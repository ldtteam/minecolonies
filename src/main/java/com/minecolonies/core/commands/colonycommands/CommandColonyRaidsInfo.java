package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.events.raid.RaidManager;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandColonyRaidsInfo implements IMCOPCommand
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

        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseShowColonyInfoCommand.get())
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }

        List<RaidManager.RaidHistory> allRaids = ((RaidManager) colony.getRaiderManager()).getAllRaids();
        for (int i = 0; i < allRaids.size(); i++)
        {
            final RaidManager.RaidHistory history = allRaids.get(i);
            final double hoursSince = Math.round(100 * (colony.getWorld().getGameTime() - history.raidTime) / (20.0 * 60 * 60)) / 100.0;
            context.getSource().sendSuccess(() -> Component.literal(hoursSince + " hours ago:" + history), true);
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raidhistory";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
