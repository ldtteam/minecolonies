package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
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
            if (context.getSource().getEntity() instanceof PlayerEntity)
            {
                // could not find player with given name.
                LanguageHandler.sendPlayerMessage((PlayerEntity) context.getSource().getEntity(), "com.minecolonies.command.playernotfound", profile.getName());
            }
            else
            {
                Log.getLogger().warn(LanguageHandler.format("com.minecolonies.command.playernotfound", profile.getName()));
            }
            return 0;
        }

        player.addStat(Stats.ITEM_USED.get(ModItems.supplyChest), 0);
        return 1;
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName()).then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::checkPreConditionAndExecute));
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
