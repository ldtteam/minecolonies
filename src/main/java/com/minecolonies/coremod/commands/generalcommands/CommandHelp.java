package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeHooks;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_HELP_INFO_DISCORD;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_HELP_INFO_WIKI;

public class CommandHelp implements IMCCommand
{

    private static final String wikiUrl    = "https://wiki.minecolonies.ldtteam.com";
    private static final String discordUrl = "https://discord.minecolonies.com";

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        if (!(context.getSource().getEntity() instanceof Player))
        {
            return 0;
        }

        context.getSource().sendSuccess(Component.translatable(COMMAND_HELP_INFO_WIKI), true);
        context.getSource().sendSuccess(((MutableComponent) ForgeHooks.newChatWithLinks(wikiUrl)).append(Component.literal("\n")), true);
        context.getSource().sendSuccess(Component.translatable(COMMAND_HELP_INFO_DISCORD), true);
        context.getSource().sendSuccess(ForgeHooks.newChatWithLinks(discordUrl), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "help";
    }
}
