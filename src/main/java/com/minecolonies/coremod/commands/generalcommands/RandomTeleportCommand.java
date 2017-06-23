package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.RTP;

/**
 * this command is made to TP a player to a safe random spot that is not to close to another colony.
 * Need to add a configs permissions check.
 * Need to allow OPs to send players ./mc ctp (Player) if player is not allowed.
 */
public class RandomTeleportCommand extends AbstractSingleCommand
{
    public static final  String DESC             = "rtp";
    private static final int    ATTEMPTS         = Configurations.gameplay.numberOfAttemptsForSafeTP;
    private static final int    UPPER_BOUNDS     = Configurations.gameplay.maxDistanceFromWorldSpawn * 2;
    private static final int    LOWER_BOUNDS     = Configurations.gameplay.maxDistanceFromWorldSpawn;
    private static final int    SPAWN_NO_TP      = Configurations.gameplay.minDistanceFromWorldSpawn;
    private static final int    STARTING_Y       = 250;
    private static final double SAFETY_DROP      = 8;
    private static final int    FALL_DISTANCE    = 5;
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RandomTeleportCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "rtp" + "<playerName>";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {
        if (SPAWN_NO_TP >= LOWER_BOUNDS)
        {
            sender.getCommandSenderEntity().sendMessage(new TextComponentString("Please have an admin raise the maxDistanceFromWorldSpawn number in config."));
            return;
        }

        if (!canCommandSenderUseCommand(RTP))
        {
            sender.getCommandSenderEntity().sendMessage(new TextComponentString("Not happenin bro!!, ask an OP to TP you."));
            return;
        }

        EntityPlayer playerToTeleport = null;

        if (sender instanceof EntityPlayer)
        {
            playerToTeleport = (EntityPlayer) sender;
        }

        //If the arguments aren't empty, the sender probably wants to teleport another player.
        if (args.length != 0 && isPlayerOpped(sender))
        {
            final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            playerToTeleport =
                    ServerUtils.getPlayerFromUUID(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache()
                            .getGameProfileForUsername(args[0]).getId(), world);

            sender.getCommandSenderEntity().sendMessage(new TextComponentString("TPin Player: " + playerToTeleport.getName()));
        }

        if (playerToTeleport == null)
        {
            sender.getCommandSenderEntity().sendMessage(new TextComponentString(CANT_FIND_PLAYER));
            return;
        }
        playerToTeleport.getCommandSenderEntity().sendMessage(new TextComponentString("Buckle up buttercup, this ain't no joy ride!!!"));

        teleportPlayer(sender, playerToTeleport);
        //.fallDistance is used to cancel out fall damage  basically if you have -5 it will reduce fall damage by 2.5 hearts
        playerToTeleport.fallDistance=FALL_DISTANCE;
    }

    /**
     * Get a random coordinate to teleport to.
     * @return
     */
    private static int getRandCoordinate()
    {
        final Random rnd = new Random();

        int x = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;

        /* keeping X out of the spawn radius */
        while (x > -SPAWN_NO_TP && x < SPAWN_NO_TP)
        {
            x = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;
        }

        return x;
    }

    /**
     * Method used to teleport the player.
     * @param sender           the sender to have access to the world.
     * @param playerToTeleport the player which shall be teleported.
     */
    private static void teleportPlayer(final ICommandSender sender, final EntityPlayer playerToTeleport)
    {
        //Now the position will be calculated, we will try up to 4 times to find a save position.
        int attCounter = 0;
        while (attCounter <= ATTEMPTS)
        {
            attCounter++;
            /* this math is to get negative numbers */
            final int x = getRandCoordinate();
            final int z = getRandCoordinate();

            /* Check for a close by colony*/
            if (ColonyManager.getColony(sender.getEntityWorld(), new BlockPos(x, STARTING_Y, z)) != null)
            {
                continue;
            }

            /*Search for a ground position*/
            final BlockPos groundPosition = BlockPosUtil.findLand(new BlockPos(x, STARTING_Y, z), sender.getEntityWorld());

            /*If no position found*/
            if (groundPosition == null)
            {
                continue;
            }

            final boolean foundPosition = BlockPosUtil.isPositionSafe(sender, groundPosition);

            if (foundPosition)
            {
                if(MinecoloniesCommand.canExecuteCommand((EntityPlayer) sender))
                {

                    playerToTeleport.setPositionAndUpdate(groundPosition.getX(), groundPosition.getY() + SAFETY_DROP, groundPosition.getZ());
                }
                else
                {
                    sender.getCommandSenderEntity().sendMessage(new TextComponentString("Please wait at least " + Configurations.gameplay.teleportBuffer + " seconds to teleport again"));
                }
                return;
            }
        }
        playerToTeleport.getCommandSenderEntity().sendMessage(new TextComponentString("Couldn't find a safe spot.  Try again in a moment."));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
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



