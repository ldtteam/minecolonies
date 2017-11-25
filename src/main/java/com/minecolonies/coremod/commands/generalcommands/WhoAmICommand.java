package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by asie on 2/16/17.
 */
public class WhoAmICommand extends AbstractSingleCommand
{
    /**
     * Command description.
     */
    public static final String DESC = "whoami";

    /**
     * Player description string.
     */
    public static final String TELL_HIM = "You are %s, your colony is %s at position %s";


    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public WhoAmICommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
        {
            Log.getLogger().info("Very funny, you're a console!");
            return;
        }

        final IColony colony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), ((EntityPlayer) sender).getUniqueID());
        final BlockPos pos = colony.getCenter();
        final String colonyName = colony.getName();
        final String playerName = sender.getDisplayName().getFormattedText();
        final String posString = "x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ();
        sender.sendMessage(new TextComponentString(String.format(TELL_HIM, playerName, colonyName, posString)));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
