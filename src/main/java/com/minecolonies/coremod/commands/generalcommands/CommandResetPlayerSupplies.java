package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;

import static com.minecolonies.coremod.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandResetPlayerSupplies implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final String username = StringArgumentType.getString(context, PLAYERNAME_ARG);
        final PlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUsername(username);
        if (player == null)
        {
            if (context.getSource().getEntity() instanceof PlayerEntity)
            {
                // could not find player with given name.
                LanguageHandler.sendPlayerMessage((PlayerEntity) context.getSource().getEntity(), "com.minecolonies.command.playernotfound", username);
            }
            else
            {
                context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.playernotfound", username), true);
            }
            return 0;
        }

        player.addStat(Stats.ITEM_USED.get(ModItems.supplyChest), 0);
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.resetsupply"), true);
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.resetsupply");
        return 1;
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
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
