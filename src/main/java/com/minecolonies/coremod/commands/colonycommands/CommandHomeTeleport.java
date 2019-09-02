package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

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

        if (!MineColonies.getConfig().getCommon().canPlayerUseHomeTPCommand.get())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.notenabledinconfig");
            return 0;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), (PlayerEntity) sender);
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.colonyidnotfound");
            return 0;
        }

        final ServerPlayerEntity player = (ServerPlayerEntity) sender;
        final BlockPos position = colony.getBuildingManager().getTownHall().getPosition();
        player.teleport(player.getServerWorld(), position.getX(), position.getY() + 2.0, position.getZ(), player.rotationYaw, player.rotationPitch);
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.teleport.success", colony.getName());
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "homeTP";
    }
}
