package com.minecolonies.coremod.commands;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.commands.citizencommands.*;
import com.minecolonies.coremod.commands.colonycommands.*;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.CommandRSReset;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.CommandRSResetAll;
import com.minecolonies.coremod.commands.generalcommands.*;
import com.minecolonies.coremod.commands.killcommands.*;
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
        /**
         * Kill commands subtree
         */
        final CommandTree killCommands = new CommandTree("kill")
                                           .addNode(new CommandKillAnimal().build())
                                           .addNode(new CommandKillChicken().build())
                                           .addNode(new CommandKillCow().build())
                                           .addNode(new CommandKillMob().build())
                                           .addNode(new CommandKillPig().build())
                                           .addNode(new CommandKillRaider().build())
                                           .addNode(new CommandKillSheep().build());

        /**
         * Colony commands subtree
         */
        final CommandTree colonyCommands = new CommandTree("colony")
                                             .addNode(new CommandAddOfficer().build())
                                             .addNode(new CommandChangeOwner().build())
                                             .addNode(new CommandClaimChunks().build())
                                             .addNode(new CommandTeleport().build())
                                             .addNode(new CommandDeleteColony().build())
                                             .addNode(new CommandCanRaiderSpawn().build())
                                             .addNode(new CommandRaidNow().build())
                                             .addNode(new CommandRaidTonight().build())
                                             .addNode(new CommandHomeTeleport().build())
                                             .addNode(new CommandListColonies().build())
                                             .addNode(new CommandSetDeletable().build())
                                             .addNode(new CommandLoadBackup().build())
                                             .addNode(new CommandLoadAllBackups().build())
                                             .addNode(new CommandColonyInfo().build())
                                             .addNode(new CommandRSReset().build())
                                             .addNode(new CommandRSResetAll().build())
                                             .addNode(new CommandSetAbandoned().build());

        /**
         * Citizen commands subtree
         */
        final CommandTree citizenCommands = new CommandTree("citizens")
                                              .addNode(new CommandCitizenInfo().build())
                                              .addNode(new CommandCitizenKill().build())
                                              .addNode(new CommandCitizenList().build())
                                              .addNode(new CommandCitizenReload().build())
                                              .addNode(new CommandCitizenSpawnNew().build());

        /**
         * Root minecolonies command tree, all subtrees are added here.
         */
        final CommandTree minecoloniesRoot = new CommandTree(Constants.MOD_ID)
                                               .addNode(killCommands)
                                               .addNode(colonyCommands)
                                               .addNode(new CommandHomeTeleport().build())
                                               .addNode(citizenCommands)
                                               .addNode(new CommandWhereAmI().build())
                                               .addNode(new CommandWhoAmI().build())
                                               .addNode(new CommandRTP().build())
                                               .addNode(new CommandRaidAllTonight().build())
                                               .addNode(new CommandRaidAllNow().build())
                                               .addNode(new CommandBackup().build())
                                               .addNode(new CommandResetPlayerSupplies().build());

        /**
         * Root minecolonies alias command tree, all subtrees are added here.
         */
        final CommandTree minecoloniesRootAlias = new CommandTree("mc")
                                                    .addNode(killCommands)
                                                    .addNode(colonyCommands)
                                                    .addNode(new CommandHomeTeleport().build())
                                                    .addNode(citizenCommands)
                                                    .addNode(new CommandWhereAmI().build())
                                                    .addNode(new CommandWhoAmI().build())
                                                    .addNode(new CommandRTP().build())
                                                    .addNode(new CommandRaidAllTonight().build())
                                                    .addNode(new CommandRaidAllNow().build())
                                                    .addNode(new CommandBackup().build())
                                                    .addNode(new CommandResetPlayerSupplies().build());

        // Adds all command trees to the dispatcher to register the commands.
        dispatcher.register(minecoloniesRoot.build());
        dispatcher.register(minecoloniesRootAlias.build());
    }
}
