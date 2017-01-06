package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.configuration.Configurations;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *  this command is made to TP a player to a safe random spot that is not to close to another colony
 */
public class ColonyTPCommand extends AbstractSingleCommand
{
    public static final  String DESC = "ctp";
    private static final int NUMBER_OF_TRIES = 10;
    private static final int UPPER_BOUNDS = 100_000;
    private static final int LOWER_BOUNDS = 10;
    private static final int STARTING_Y = 250;
    private static final double ADDS_TWENTY_PERCENT = 1.20;

    private ICommandSender sender;
    private EntityPlayer player = (EntityPlayer)sender;
    private World world = player.getEntityWorld();
    private String badTp = "";
    private Boolean isSafe = false;
    private Boolean colNear = true;
    private Random rnd = new Random();
    private int x = rnd.nextInt(UPPER_BOUNDS)-LOWER_BOUNDS;
    private int y = STARTING_Y;
    private int z = rnd.nextInt(UPPER_BOUNDS)-LOWER_BOUNDS;
    private BlockPos blockPos = new BlockPos(x,y,z);
    private Block blocks= sender.getEntityWorld().getBlockState(blockPos).getBlock();
    ColonyTPCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "ctp";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {
        /* This is where all the magic happens */
        if (sender instanceof EntityPlayer)
        {
            player.addChatMessage(new TextComponentString("Buckle up buttercup, this aint no joy ride!!!"));
            /* send info to look for land */
            findLand(blockPos);
            /* ok now we take the new coords and do our other checks */
            /* send info to look for lava or water */
            findLavaWater(sender,blockPos);
            /* Take the return and determine if good or bad */
            if(isSafe)
            {
                findColony(blockPos);
            }
                /* send info to look to see if another colony is near */
            if(!colNear)
            {
                player.setPositionAndUpdate(blockPos.getX(), blockPos.getY() + SAFETY_DROP, blockPos.getZ());
            }
            else
            {
                player.addChatMessage(new TextComponentString("" + badTp + "  Try again in a moment."));
            }
        }
    }
    /**
     *  this checks that you are not in the air or underground before TP using the BlockPos for the block LOC
     */
    private BlockPos findLand(BlockPos blockPos)
    {

        int a = 0;
        int ycheck = y;
        /* we are doing a binary check to limit the amount of checks (usually 9 this wa) */

        while (a < NUMBER_OF_TRIES)
        {
            a++;

            if (blocks == Blocks.AIR && world.canSeeSky(blockPos))
            {
                this.blockPos = blockPos.down(y/2);
                y=y/2;
            }
            else
            {
                this.blockPos = blockPos.up(y/2);
                y=y+y/2;
            }

            /* this jumps us out when the y doesn't change anymore */
            if (ycheck==y)
            {
                break;
            }
            ycheck=y;
        }

        return blockPos;
    }

    /**
     *  this checks for Lava or water before TP using the BlockPos for the block LOC
     */
    private boolean findLavaWater(@NotNull ICommandSender sender, BlockPos blockPos)
    {
        blocks= sender.getEntityWorld().getBlockState(blockPos).getBlock();

        /* take the coords and check to see if that block is water or lava*/
        if (blocks == Blocks.LAVA || blocks == Blocks.WATER)
        {
                        /* bad tp, bad -- abort TP  Just not safe water or lava present*/
            /*start it as Safe and change it in the IF statements if it is not safe */
            isSafe = true;
            if (blocks.equals(Blocks.LAVA))
            {
                badTp = "Yeah that would have been way to hot!";
                isSafe = false;
            }
            if (blocks.equals(Blocks.WATER))
            {
                badTp = "You would have gotten you shoes wet there!";
                isSafe = false;
            }
        }
        return isSafe;
    }
    /**
     *  this checks to see if we are too close to a colony before TP using the BlockPos for the block LOC
     */
    private boolean findColony(BlockPos blockPos)
    {
        Colony nearestCol = ColonyManager.getClosestColony(world, blockPos);
        /* get individual coords to do the math */
        int cx = nearestCol != null ? nearestCol.getCenter().getX() : 0;
        int cz = nearestCol != null ? nearestCol.getCenter().getZ() : 0;
        /* from the random X for the TP */
        double px = x;
        /* from the random Z for the TP */
        double pz = z;

        double dist = Math.sqrt(Math.pow(cx - px, 2.0) + (Math.pow(cz - pz, 2.0)));
                        /* grab the working distance and do our check now that we have distance from nearest colony*/
                        /* just to understand this better::::
                            I am taking the working distance from the town hall and doubling it this will give me
                            the distance needed from both town halls -if you placed it here.  Then im adding on 20%
                            just to get a padding between the two.*/
        double wd = (Configurations.workingRangeTownHall * 2) * ADDS_TWENTY_PERCENT;
        if (dist < wd)
        {
            /* bad tp, bad -- abort TP  Too close to a colony */
            badTp = "Trust me, you would not have liked your neighbors!";
            colNear = false;
        }
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



