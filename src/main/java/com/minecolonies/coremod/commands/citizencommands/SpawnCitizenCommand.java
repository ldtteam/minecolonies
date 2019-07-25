package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.SPAWNCITZENS;

/**
 * List all colonies.
 */
public class SpawnCitizenCommand extends AbstractCitizensCommands implements IActionCommand
{

    public static final  String DESC                = "spawn";
    private static final String CITIZEN_DESCRIPTION = "§2ID: §f %d §2 Name: §f %s";
    private static final String SPAWN_MESSAGE       = "Has been spawned";
    private static final String COORDINATES_XYZ     = "§4x=§f%s §4y=§f%s §4z=§f%s";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public SpawnCitizenCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public SpawnCitizenCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <CitizenId>";
    }

    @Override
    public void executeSpecializedCode(@NotNull final MinecraftServer server, final ICommandSender sender, final Colony colony, final int citizenId)
    {
        if (isPlayerOpped(sender))
        {
            colony.getCitizenManager().spawnOrCreateCitizen(null, colony.getWorld(), null, true);
        }
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

    @Override
    public Commands getCommand()
    {
        return SPAWNCITZENS;
    }

    /**
     * Indicates if this command requires a citizen.
     *
     * @return True for yes, false for optional or no.
     */
    @Override
    protected boolean requiresCitizen()
    {
        return false;
    }

    @Override
    public boolean canPlayerUseCommand(final PlayerEntity player, final Commands theCommand, final int colonyId)
    {
        final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        return super.canPlayerUseCommand(player, theCommand, colonyId)
                 && ColonyManager.getColonyByWorld(colonyId, world) != null && ColonyManager.getColonyByWorld(colonyId, world).getPermissions().getRank(player).equals(Rank.OWNER);
    }
}
