package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.util.TeleportHelper;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG;

public class CommandHomeTeleport implements IMCCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();

        if (!MineColonies.getConfig().getServer().canPlayerUseHomeTPCommand.get())
        {
            MessageUtils.sendPlayerMessage((PlayerEntity) sender, COMMAND_DISABLED_IN_CONFIG);
            return 0;
        }

        TeleportHelper.homeTeleport((ServerPlayerEntity) sender);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "home";
    }
}
