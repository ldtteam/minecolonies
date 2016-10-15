package com.minecolonies.commands;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * List all colonies.
 */
public class KillCitizen extends AbstractSingleCommand
{

    private static final String ID_TEXT                = "§2ID: §f";
    private static final String NAME_TEXT              = "§2 Name: §f";
    private static final String COORDINATES_TEXT       = "§2Coordinates: §f";
    private static final String LIST_COMMAND_SUGGESTED = "/mc colony list ";
    private static final int    COLONIES_ON_PAGE       = 9;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public KillCitizen(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId = 1;
        int citizenId = 1;

        final IColony tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), sender.getCommandSenderEntity().getUniqueID());
        if(tempColony != null)
        {
            final Colony colony = ColonyManager.getColony(sender.getEntityWorld(), tempColony.getCenter());
            if(colony != null)
            {
                colonyId = colony.getID();
            }
        }

        if (args.length != 0)
        {
            if(args.length >= 2)
            {
                try
                {
                    colonyId = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e)
                {
                    //ignore and keep page 1.
                }
            }
            else
            {
                citizenId = Integer.parseInt(args[0]);
            }
        }

        final List<Colony> colonies = ColonyManager.getColonies();
        final int colonyCount = colonies.size();

        // check to see if we have to add one page to show the half page
        final int halfPage = (colonyCount % COLONIES_ON_PAGE == 0) ? 0 : 1;
        
        //todo add has been removed.
        sender.addChatMessage(new TextComponentString(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()));
        final BlockPos center = colony.getCenter();
        sender.addChatMessage(new TextComponentString(COORDINATES_TEXT + String.format("§4x=§f%s §4y=§f%s §4z=§f%s", center.getX(), center.getY(), center.getZ())));

    }


    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
