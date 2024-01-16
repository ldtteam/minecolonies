package com.minecolonies.core.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

import static com.minecolonies.core.commands.CommandArgumentNames.PLAYERNAME_ARG;

public class CommandRTP implements IMCCommand
{
    private static final int    STARTING_Y    = 250;
    private static final double SAFETY_DROP   = 6;
    private static final int    FALL_DISTANCE = 5;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        rtp((Player) context.getSource().getEntity());
        return 1;
    }

    /**
     * Executes rtp for a target
     *
     * @param context the command context
     * @return 1 if the command executed successfully, 0 otherwise
     * @throws CommandSyntaxException if the syntax isn't correct
     */
    private int executeOtherPlayerRTP(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final Entity sender = context.getSource().getEntity();

        if (!checkPreCondition(context) || !IMCCommand.isPlayerOped((Player) sender))
        {
            return 0;
        }

        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYERNAME_ARG).stream().findFirst().orElse(null);

        if (profile == null || context.getSource().getServer().getPlayerList().getPlayer(profile.getId()) == null)
        {
            // could not find player with given name.
            MessageUtils.format(CommandTranslationConstants.COMMAND_PLAYER_NOT_FOUND, profile != null ? profile.getName() : "null").sendTo((Player) sender);
            return 0;
        }

        rtp(context.getSource().getServer().getPlayerList().getPlayer(profile.getId()));
        return 1;
    }

    @Override
    public boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return false;
        }

        if (!MineColonies.getConfig().getServer().canPlayerUseRTPCommand.get())
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_RTP_NOT_ALLOWED).sendTo((Player) sender);
            return false;
        }
        else if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && context.getSource().getLevel().dimension() != Level.OVERWORLD)
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_RTP_WRONG_DIMENSION).sendTo((Player) sender);
            return false;
        }
        return true;
    }

    /**
     * Randomly teleports the given player.
     *
     * @param player player to teleport
     */
    private void rtp(final Player player)
    {
        //Now the position will be calculated, we will try up to 4 times to find a save position.
        for (int attCounter = 0; attCounter <= 4; attCounter++)
        {
            /* this math is to get negative numbers */
            final int x = getRandCoordinate();
            final int z = getRandCoordinate();
            final BlockPos spawnPoint = ((ServerLevel) player.getCommandSenderWorld()).getSharedSpawnPos();
            if (player.getCommandSenderWorld().getWorldBorder().getAbsoluteMaxSize()
                  < BlockPosUtil.getDistance2D(spawnPoint, spawnPoint.offset(x, 0, z)))
            {
                continue;
            }

            final BlockPos tpPos = new BlockPos(x, STARTING_Y, z);

            final IColony colony = IColonyManager.getInstance().getClosestColony(player.getCommandSenderWorld(), tpPos);
            /* Check for a close by colony*/
            if (colony != null
                  && BlockPosUtil.getDistance2D(colony.getCenter(), tpPos) < MineColonies.getConfig().getServer().maxColonySize.get() * 32)
            {
                continue;
            }

            /*Search for a ground position*/
            final BlockPos groundPosition = BlockPosUtil.findLand(tpPos, player.getCommandSenderWorld());

            /*If no position found*/
            if (groundPosition == null)
            {
                continue;
            }

            if (BlockPosUtil.isPositionSafe(player.getCommandSenderWorld(), groundPosition.below()))
            {
                player.setHealth(player.getMaxHealth());
                player.teleportTo(groundPosition.getX(), groundPosition.getY() + SAFETY_DROP, groundPosition.getZ());
                player.setHealth(player.getMaxHealth());

                MessageUtils.format(CommandTranslationConstants.COMMAND_RTP_SUCCESS).sendTo(player);

                //.fallDistance is used to cancel out fall damage  basically if you have -5 it will reduce fall damage by 2.5 hearts
                player.fallDistance = -FALL_DISTANCE;
                return;
            }
        }
        MessageUtils.format(CommandTranslationConstants.COMMAND_RTP_NO_POSITION).sendTo(player);
    }

    /**
     * Get a random coordinate to teleport to.
     *
     * @return a random coordinate.
     */
    private static int getRandCoordinate()
    {
        final Random rnd = new Random();
        int x = 0;

        /* keeping X out of the spawn radius */
        while (x > -MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get() && x < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
        {
            x = rnd.nextInt(MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get() * 2) - MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get();
        }
        return x;
    }

    /**
     * Name string of the command.
     *
     * @return this commands name.
     */
    @Override
    public String getName()
    {
        return "rtp";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(PLAYERNAME_ARG, GameProfileArgument.gameProfile()).executes(this::executeOtherPlayerRTP))
                 .executes(this::checkPreConditionAndExecute);
    }
}
