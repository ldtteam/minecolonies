package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandChangeOwner implements IMCColonyOfficerCommand
{

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
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

        final PlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUUID(profile.getId());
        if (player == null)
        {
            // could not find player with given name.
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.playernotfound", profile.getName()), true);
            return 0;
        }

        colony.getPermissions().setOwner(player);

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.ownerchange.success", profile.getName(), colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "setowner";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::checkPreConditionAndExecute)));
    }
}
