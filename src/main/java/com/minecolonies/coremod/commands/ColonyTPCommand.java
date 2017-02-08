package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.COLONYTP;

/**
 * this command is made to TP a player to a safe random spot that is not to close to another colony
 * Need to add a configs permissions check
 * Need to allow OPs to send players ./mc ctp (Player) if player is not allowed
 */
public class ColonyTPCommand extends AbstractSingleCommand
{
    public static final  String DESC = "ctp";
    private static final int ATTEMPTS = Configurations.numberOfAttemptsForSafeTP;
    private static final int UPPER_BOUNDS = Configurations.maxDistanceFromWorldSpawn * 2;
    private static final int LOWER_BOUNDS = Configurations.maxDistanceFromWorldSpawn;
    private static final int SPAWN_NO_TP = Configurations.minDistanceFromWorldSpawn;
    private static final int STARTING_Y = 250;
    private static final double ADDS_TWENTY_PERCENT = 1.20;
    private static final double SAFETY_DROP = 4;
    private static final String CANT_FIND_PLAYER = "No player found for teleport, please define one.";

    private final Random rnd = new Random();

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ColonyTPCommand( @NotNull final String...parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "ctp" + "<playerName>";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {
        if (SPAWN_NO_TP > LOWER_BOUNDS)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Please have an admin raise the maxDistanceFromWorldSpawn number in config."));
            return;
        }
        if (!canCommandSenderUseCommand(COLONYTP))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Not happenin bro!!, ask an OP to TP you."));
            return;
        }

        EntityPlayer playerToTeleport = null;

        if(sender instanceof EntityPlayer)
        {
            playerToTeleport = (EntityPlayer) sender;
        }

        //If the arguments aren't empty, the sender probably wants to teleport another player.
        if (args.length != 0 && isPlayerOpped(sender, "ctp"))
        {
            final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            playerToTeleport =
                    ServerUtils.getPlayerFromUUID(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache()
                            .getGameProfileForUsername(args[0]).getId(), world);

            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("TPin Player: "+ playerToTeleport.getName()));
        }

        if(playerToTeleport == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(CANT_FIND_PLAYER));
            return;
        }

        playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString("Buckle up buttercup, this ain't no joy ride!!!"));
        teleportPlayer(sender, playerToTeleport);
    }

    /**
     * Method used to teleport the player.
     * @param sender the sender to have access to the world.
     * @param playerToTeleport the player which shall be teleported.
     */
    private void teleportPlayer(final ICommandSender sender, final EntityPlayer playerToTeleport)
    {
        //Now the position will be calculated, we will try up to 4 times to find a save position.
        int attCounter = 0;
        while (attCounter <= ATTEMPTS)
        {
            attCounter++;
            /* this math is to get negative numbers */
            int x = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;
                /* keeping X out of the spawn radius */
                while (x > -SPAWN_NO_TP && x < SPAWN_NO_TP)
                {
                    x = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;
                }

            /* keeping Z out of the spawn radius */
            int z = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;
                while (z > -SPAWN_NO_TP && z < SPAWN_NO_TP)
                {
                    z = rnd.nextInt(UPPER_BOUNDS) - LOWER_BOUNDS;
                }

                /* Check for a close by colony*/
            if (!findColony(new BlockPos(x, STARTING_Y, z), sender.getEntityWorld()))
            {
                return;
            }

            /*Search for a ground position*/
            final BlockPos groundPosition = findLand(new BlockPos(x, STARTING_Y, z), sender.getEntityWorld());

            /*If no position found*/
            if (groundPosition == null)
            {
                attCounter++;
                continue;
            }

            boolean foundPosition = isPositionSafe(sender, groundPosition);

            /* Take the return and determine if good or bad */
             /* send info to look to see if another colony is near */

            if (foundPosition)
            {
                /* everything checks out good make the TP and jump out*/
                playerToTeleport.setPositionAndUpdate(groundPosition.getX(), groundPosition.getY() + SAFETY_DROP, groundPosition.getZ());
                return;
            }
        }
        playerToTeleport.getCommandSenderEntity().addChatMessage(new TextComponentString("Couldn't find a safe spot.  Try again in a moment."));
    }

    /**
     * this checks that you are not in the air or underground
     * and if so it will look up and down for a good landing spot
     * before TP
     *
     * @param blockPos for the current block LOC
     * @return blockPos to be used for the TP
     */
    private static BlockPos findLand(final BlockPos blockPos, final World world)
    {
        int top = STARTING_Y;
        int bot = 0;
        int mid = STARTING_Y;

        BlockPos foundland = null;
        BlockPos tempPos = blockPos;
        //We are doing a binary search to limit the amount of checks (usually at most 9 this way)
        while (top >= bot)
        {
            tempPos = new BlockPos( tempPos.getX(),mid, tempPos.getZ());
            Block blocks = world.getBlockState(tempPos).getBlock();
            if (blocks == Blocks.AIR && world.canSeeSky(tempPos))
            {
                top = mid - 1;
                foundland = tempPos;
            }
            else
            {
                bot = mid + 1;
                foundland = tempPos;
            }
            mid = (bot + top)/2;
        }

        return foundland;
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even those from other mods
     * before TP
     *
     * @param blockPos for the current block LOC
     * @param sender uses the player to get the world
     * @return isSafe true=safe false=water or lava
     */
    private static boolean isPositionSafe(@NotNull ICommandSender sender, BlockPos blockPos)
    {
        return sender.getEntityWorld().getBlockState(blockPos).getBlock() != Blocks.AIR
                && !sender.getEntityWorld().getBlockState(blockPos).getMaterial().isLiquid()
                && !sender.getEntityWorld().getBlockState(blockPos.up()).getMaterial().isLiquid();
    }

    /**
     * this checks that you are not too close to another colony
     * before TP
     *
     * @param blockPos for the current block LOC
     * @return colNear false=no true=yes
     */
    private static boolean findColony(BlockPos blockPos, World world)
    {
        Colony nearestCol = ColonyManager.getClosestColony(world, blockPos);
        Boolean colNear;
        /* get individual coords to do the math */
        int cx = nearestCol != null ? nearestCol.getCenter().getX() : 0;
        int cz = nearestCol != null ? nearestCol.getCenter().getZ() : 0;
        /* from the random X for the TP */
        double px = blockPos.getX();
        /* from the random Z for the TP */
        double pz = blockPos.getZ();

        double dist = Math.sqrt(Math.pow(cx - px, 2.0) + (Math.pow(cz - pz, 2.0)));
                        /* grab the working distance and do our check now that we have distance from nearest colony*/
                        /* just to understand this better::::
                            I am taking the working distance from the town hall and doubling it this will give me
                            the distance needed from both town halls -if you placed it here.  Then im adding on 20%
                            just to get a padding between the two.*/
        double wd = (Configurations.workingRangeTownHall * 2) * ADDS_TWENTY_PERCENT;
        /* bad tp, bad -- abort TP  Too close to a colony */
        colNear = dist < wd;
        return colNear;
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
        return false;
    }

}



