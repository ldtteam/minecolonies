package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

import static com.minecolonies.coremod.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandRTP implements IMCCommand
{
    private static final int    STARTING_Y     = 250;
    private static final double SAFETY_DROP    = 6;
    private static final int    FALL_DISTANCE  = 5;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        rtp((PlayerEntity) context.getSource().getEntity());
        return 1;
    }

    /**
     * Executes rtp for a target
     */
    private int executeOtherPlayerRTP(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final Entity sender = context.getSource().getEntity();

        if (!checkPreCondition(context) || !IMCCommand.isPlayerOped((PlayerEntity) sender))
        {
            return 0;
        }

        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYERNAME_ARG).stream().findFirst().orElse(null);

        if (profile == null || context.getSource().getServer().getPlayerList().getPlayerByUUID(profile.getId()) == null)
        {
            // could not find player with given name.
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.playernotfound", profile != null ? profile.getName() : "null");
            return 0;
        }

        rtp(context.getSource().getServer().getPlayerList().getPlayerByUUID(profile.getId()));
        return 1;
    }

    /**
     * prechecks for both commands
     */
    @Override
    public boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof PlayerEntity))
        {
            return false;
        }

        if (!MineColonies.getConfig().getCommon().canPlayerUseRTPCommand.get())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.rtp.notallowed");
            return false;
        }
        else if (context.getSource().getWorld().dimension.getType().getId() != 0)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.rtp.wrongdim");
            return false;
        }
        return true;
    }

    /**
     * Randomly teleports the given player.
     *
     * @param player player to teleport
     */
    private void rtp(final PlayerEntity player)
    {
        //Now the position will be calculated, we will try up to 4 times to find a save position.
        for (int attCounter = 0; attCounter <= MineColonies.getConfig().getCommon().numberOfAttemptsForSafeTP.get(); attCounter++)
        {
            /* this math is to get negative numbers */
            final int x = getRandCoordinate();
            final int z = getRandCoordinate();

            if (player.getEntityWorld().getWorldBorder().getSize()
                  < BlockPosUtil.getDistance2D(player.getEntityWorld().getSpawnPoint(), player.getEntityWorld().getSpawnPoint().add(x, 0, z)))
            {
                continue;
            }

            final BlockPos tpPos = new BlockPos(x, STARTING_Y, z);

            final IColony colony = IColonyManager.getInstance().getClosestColony(player.getEntityWorld(), tpPos);
            /* Check for a close by colony*/
            if (colony != null
                  && BlockPosUtil.getDistance2D(colony.getCenter(), tpPos) < MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get() * 32 + MineColonies.getConfig()
                                                                                                                                                            .getCommon().townHallPadding
                                                                                                                                                            .get())
            {
                continue;
            }

            /*Search for a ground position*/
            final BlockPos groundPosition = BlockPosUtil.findLand(tpPos, player.getEntityWorld());

            /*If no position found*/
            if (groundPosition == null)
            {
                continue;
            }

            if (BlockPosUtil.isPositionSafe(player.getEntityWorld(), groundPosition.down()))
            {
                player.setHealth(player.getMaxHealth());
                player.setPositionAndUpdate(groundPosition.getX(), groundPosition.getY() + SAFETY_DROP, groundPosition.getZ());
                player.setHealth(player.getMaxHealth());

                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.rtp.success");

                //.fallDistance is used to cancel out fall damage  basically if you have -5 it will reduce fall damage by 2.5 hearts
                player.fallDistance = -FALL_DISTANCE;
                return;
            }
        }
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.command.rtp.nopositionfound");
    }

    /**
     * Get a random coordinate to teleport to.
     */
    private static int getRandCoordinate()
    {
        final Random rnd = new Random();
        int x = 0;

        /* keeping X out of the spawn radius */
        while (x > -MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get() && x < MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get())
        {
            x = rnd.nextInt(MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get() * 2) - MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get();
        }
        return x;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "rtp";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::executeOtherPlayerRTP))
                 .executes(this::checkPreConditionAndExecute);
    }
}
