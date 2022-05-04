package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_CITIZEN_SPAWN_SUCCESS;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Force-spawns a new citizen in the given colony.
 */
public class CommandCitizenSpawnNew implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        context.getSource()
          .sendSuccess(new TranslationTextComponent(COMMAND_CITIZEN_SPAWN_SUCCESS,
            colony.getCitizenManager().spawnOrCreateCivilian(null, colony.getWorld(), null, true).getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "spawnNew";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
