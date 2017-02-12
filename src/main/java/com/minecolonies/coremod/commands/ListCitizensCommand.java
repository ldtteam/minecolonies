package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.LISTCITIZENS;

/**
 * List all colonies.
 */
public class ListCitizensCommand extends AbstractSingleCommand
{

    public static final  String DESC                    = "list";
    private static final String CITIZEN_DESCRIPTION     = "§2ID: §f %d §2 Name: §f %s";
    private static final String COORDINATES_XYZ         = "§2Coordinates: §f §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String LIST_COMMAND_SUGGESTED  = "/mc citizens list ";
    private static final String COMMAND_CITIZEN_INFO    = "/mc colony citizenInfo %s %s";
    private static final String PAGE_TOP                = "§2   ------------------ page %d of %d ------------------";
    private static final String PREV_PAGE               = " <- prev";
    private static final String NEXT_PAGE               = "next -> ";
    private static final String PAGE_LINE               = "§2 ----------------";
    private static final String PAGE_LINE_DIVIDER       = "§2 | ";
    private static final int    CITIZENS_ON_PAGE        = 9;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ListCitizensCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<colonyId>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId = getIthArgument(args, 0, getColonyId(sender));

        if(sender instanceof EntityPlayer)
        {
            final EntityPlayer player = (EntityPlayer) sender;
            if (!canPlayerUseCommand(player, LISTCITIZENS, colonyId))
            {
                sender.getCommandSenderEntity().sendMessage(new TextComponentString("Not happenin bro!!, You are not permitted to do that!"));
                return;
            }
        }

        final Colony colony = ColonyManager.getColony(colonyId);

        final List<CitizenData> citizens = new ArrayList<>(colony.getCitizens().values());
        final int citizenCount = citizens.size();

        // check to see if we have to add one page to show the half page
        int page = getIthArgument(args, 0, 1);
        final int halfPage = (citizenCount % CITIZENS_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((citizenCount) / CITIZENS_ON_PAGE) + halfPage;

        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = CITIZENS_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(CITIZENS_ON_PAGE * page, citizenCount);

        final List<CitizenData> citizensPage;

        if (pageStartIndex < 0 || pageStartIndex >= citizenCount)
        {
            citizensPage = new ArrayList<>();
        }
        else
        {
            citizensPage = citizens.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new TextComponentString(String.format(PAGE_TOP, page, pageCount));
        sender.sendMessage(headerLine);

        for (final CitizenData citizen : citizensPage)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION,
              citizen.getId(),
              citizen.getName())).setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                     String.format(COMMAND_CITIZEN_INFO, citizen.getColony().getID(), citizen.getId())))));

            if (citizen.getCitizenEntity() != null)
            {
                final BlockPos position = citizen.getCitizenEntity().getPosition();
                sender.sendMessage(new TextComponentString(String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
            }
        }
        drawPageSwitcher(sender, page, citizenCount, halfPage);
    }

    /**
     * Returns the colony of the owner or if not available colony 1.
     * First tries to get the IColony and then the Colony from the ColonyManager.
     *
     * @param sender the sender of the command.
     * @return the colonyId.
     */
    private static int getColonyId(@NotNull final ICommandSender sender)
    {
        final IColony tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), sender.getCommandSenderEntity().getUniqueID());
        if (tempColony != null)
        {
            final Colony colony = ColonyManager.getColony(sender.getEntityWorld(), tempColony.getCenter());
            if (colony != null)
            {
                return colony.getID();
            }
        }

        return 1;
    }

    /**
     * Draws the page switcher at the bottom.
     *
     * @param sender   the sender.
     * @param page     the page number.
     * @param count    number of citizens.
     * @param halfPage the halfPage.
     */
    private static void drawPageSwitcher(@NotNull final ICommandSender sender, final int page, final int count, final int halfPage)
    {
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (count / CITIZENS_ON_PAGE) + halfPage);

        final ITextComponent prevButton = new TextComponentString(PREV_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + prevPage)
        ));
        final ITextComponent nextButton = new TextComponentString(NEXT_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + nextPage)
        ));

        final ITextComponent beginLine = new TextComponentString(PAGE_LINE);
        final ITextComponent endLine = new TextComponentString(PAGE_LINE);
        sender.sendMessage(beginLine.appendSibling(prevButton).appendSibling(new TextComponentString(PAGE_LINE_DIVIDER)).appendSibling(nextButton).appendSibling(endLine));
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
