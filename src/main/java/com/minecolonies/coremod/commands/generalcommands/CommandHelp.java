package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeHooks;

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
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof PlayerEntity))
        {
            return 0;
        }

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.help.wiki"), true);
        context.getSource().sendFeedback(ForgeHooks.newChatWithLinks(wikiUrl).appendText("\n"), true);
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.help.discord"), true);
        context.getSource().sendFeedback(ForgeHooks.newChatWithLinks(discordUrl), true);

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
