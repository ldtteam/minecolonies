package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandReclaimChunks implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        if (!(sender instanceof Player))
        {
            return 0;
        }

        final Colony colony = (Colony) ColonyIdArgument.getColony(context, COLONYID_ARG);
        BackUpHelper.reclaimChunks(colony);
        MessageUtils.format(CommandTranslationConstants.COMMAND_CLAIM_SUCCESS).sendTo((Player) sender);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "reclaimchunks";
    }

    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::checkPreConditionAndExecute));
    }
}
