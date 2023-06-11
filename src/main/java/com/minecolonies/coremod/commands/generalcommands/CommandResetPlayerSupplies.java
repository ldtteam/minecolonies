package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.stats.Stats;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_PLAYER_NOT_FOUND;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_RESET_SUPPLY_SUCCESS;
import static com.minecolonies.coremod.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandResetPlayerSupplies implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final String username = StringArgumentType.getString(context, PLAYERNAME_ARG);
        final Player player = context.getSource().getServer().getPlayerList().getPlayerByName(username);
        if (player == null)
        {
            if (context.getSource().getEntity() instanceof Player)
            {
                // could not find player with given name.
                MessageUtils.format(COMMAND_PLAYER_NOT_FOUND, username).sendTo((Player) context.getSource().getEntity());
            }
            else
            {
                context.getSource().sendSuccess(() -> Component.translatable(COMMAND_PLAYER_NOT_FOUND, username), true);
            }
            return 0;
        }

        player.awardStat(Stats.ITEM_USED.get(ModItems.supplyChest), -1);
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_RESET_SUPPLY_SUCCESS), true);
        MessageUtils.format(COMMAND_RESET_SUPPLY_SUCCESS).sendTo(player);
        return 1;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName()).then(IMCCommand.newArgument(PLAYERNAME_ARG, StringArgumentType.string()).executes(this::checkPreConditionAndExecute));
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "resetsupplies";
    }
}
