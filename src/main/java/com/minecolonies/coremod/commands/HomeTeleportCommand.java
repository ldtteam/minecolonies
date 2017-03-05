package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.HOMETP;

/**
 * this command is made to TP a player to a safe random spot that is not to close to another colony.
 * Need to add a configs permissions check.
 * Need to allow OPs to send players ./mc ctp (Player) if player is not allowed.
 */
public class HomeTeleportCommand extends AbstractSingleCommand
{
    public static final  String DESC             = "home";
    private static final String CANT_FIND_COLONY = "No Colony found for teleport, please define one.";
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public HomeTeleportCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "home";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {

        //see if player is allowed to use in the configs
            if (!canCommandSenderUseCommand(HOMETP))
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString("This is not allowed on this server."));
                return;
            }
        EntityPlayer playerToTeleport;
        IColony colony;
        int colonyId;
        //see if sent by a player and grab their name and Get the players Colony ID that sent the command
            if (sender instanceof EntityPlayer)
            {

                playerToTeleport = (EntityPlayer) sender;
                colony = ColonyManager.getIColonyByOwner(((EntityPlayer) sender).worldObj, (EntityPlayer) sender);
                colonyId = colony.getID();

            }
            else
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_PLAYER));
                return;
            }


            if (colony.equals(null))
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_COLONY));
                return;
            }
        playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString("There's no place like home, there's no place like home..."));
        teleportPlayer(playerToTeleport, colonyId);
    }


    /**
     * Method used to teleport the player.
     *
     * @param colID           the senders colony ID.
     * @param playerToTeleport the player which shall be teleported.
     */
    private static void teleportPlayer(final EntityPlayer playerToTeleport,int colID)
    {
        final Colony colony = ColonyManager.getColony(colID);
        final BlockPos position = colony.getCenter();

        if (colID >= 1)
        {
            playerToTeleport.setPositionAndUpdate(position.getX(),position.getY(),position.getZ());
        }
        else
        {
            playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_COLONY));
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions (
    @NotNull final MinecraftServer server,
    @NotNull final ICommandSender sender,
    @NotNull final String[] args,
    final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull String[] args, int index)
    {
        return index == 0;
    }
}



