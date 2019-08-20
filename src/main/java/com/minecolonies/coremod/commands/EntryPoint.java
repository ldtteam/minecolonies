package com.minecolonies.coremod.commands;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.commands.killcommands.CommandKillAnimal;
import com.minecolonies.coremod.commands.killcommands.CommandKillChicken;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

/**
 * Entry point to commands.
 */
public class EntryPoint
{

    private EntryPoint()
    {
        // Intentionally left empty
    }

    public static void register(final CommandDispatcher<CommandSource> dispatcher)
    {
        final CommandTree killCommands = new CommandTree("kill").addNode(new CommandKillAnimal().build()).addNode(new CommandKillChicken().build());

        final CommandTree structurizeRoot = new CommandTree(Constants.MOD_ID)
                                              .addNode(new TestCMD().build())
                                              .addNode(killCommands);

        dispatcher.register(structurizeRoot.build());
    }
}
