package com.minecolonies.core.debug.command;

import com.minecolonies.core.Network;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.debug.DebugPlayerManager;
import com.minecolonies.core.debug.messages.DebugEnableMessage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.minecolonies.core.commands.CommandArgumentNames.PLAYERNAME_ARG;

/**
 * Command to toggle debug mode for a given player
 */
public class CommandToggleDebug implements IMCOPCommand
{
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
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

        final boolean enabled = DebugPlayerManager.toggleDebugModeFor(profile.getId());

        if (enabled)
        {
            context.getSource().sendSuccess(() -> Component.literal("Enabled minecolonies debugging for:" + profile.getName()).withStyle(ChatFormatting.GREEN), true);
        }
        else
        {
            context.getSource().sendSuccess(() -> Component.literal("Disabled minecolonies debugging for:" + profile.getName()).withStyle(ChatFormatting.RED), true);
        }

        final ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(profile.getId());
        if (player != null)
        {
            if (enabled)
            {
                Network.getNetwork().sendToPlayer(new DebugEnableMessage(true), player);
                player.sendSystemMessage(Component.literal("Enabled minecolonies debugging").withStyle(ChatFormatting.GREEN));
            }
            else
            {
                Network.getNetwork().sendToPlayer(new DebugEnableMessage(false), player);
                player.sendSystemMessage(Component.literal("Disabled minecolonies debugging").withStyle(ChatFormatting.RED));
            }
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "toggleDebugging";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::checkPreConditionAndExecute));
    }
}
