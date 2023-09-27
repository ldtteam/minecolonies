package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandAddOfficer implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseAddOfficerCommand.get())
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }


        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

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
        colony.getPermissions().addPlayer(profile, colony.getPermissions().getRank(colony.getPermissions().OFFICER_RANK_ID));
        colony.getPackageManager().addImportantColonyPlayer(context.getSource().getServer().getPlayerList().getPlayer(profile.getId()));

        context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_OFFICER_ADD_SUCCESS, profile.getName(), colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "addOfficer";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::checkPreConditionAndExecute)));
    }
}
