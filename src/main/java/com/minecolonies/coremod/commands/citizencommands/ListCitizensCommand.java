package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.LISTCITIZENS;

/**
 * List all colonies.
 */
public class ListCitizensCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String DESC                   = "list";
    private static final String CITIZEN_DESCRIPTION    = "§2ID: §f %d §2 Name: §f %s";
    private static final String COORDINATES_XYZ        = "§2Coordinates: §f §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String LIST_COMMAND_SUGGESTED = "/mc citizens list colony: %d page: %d";
    private static final String COMMAND_CITIZEN_INFO   = "/mc citizens info colony: %s citizen: %s";
    private static final String PAGE_TOP               = "§2   ------------------ page %d of %d ------------------";
    private static final String PREV_PAGE              = " <- prev";
    private static final String NEXT_PAGE              = "next -> ";
    private static final String PAGE_LINE              = "§2 ----------------";
    private static final String PAGE_LINE_DIVIDER      = "§2 | ";
    private static final int    CITIZENS_ON_PAGE       = 9;

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ListCitizensCommand()
    {
        super();
    }

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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final IColony colony = actionMenuState.getColonyForArgument("colony");
        final Integer page = actionMenuState.getIntForArgument("page");
        executeShared(server, sender, colony, page);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId = getIthArgument(args, 0, getColonyId(sender));
        final Integer page = getIthArgument(args, 1, 1);

        IColony colony = null;
        if (sender instanceof PlayerEntity)
        {
            if (colonyId == -1)
            {
                final IColony icolony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), (PlayerEntity) sender);
                if (icolony != null)
                {
                    colonyId = icolony.getID();
                }
            }
        }
        colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().world.getDimension().getType().getId()));

        executeShared(server, sender, colony, page);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, final IColony colony, final Integer pageProvided) throws CommandException
    {
        int page;
        if (null != pageProvided)
        {
            page = pageProvided.intValue();
        }
        else
        {
            page = 1;
        }

        if (sender instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) sender;
            if ((null != colony) && !canPlayerUseCommand(player, LISTCITIZENS, colony.getID()))
            {
                sender.sendMessage(new StringTextComponent("Not happenin bro!!, You are not permitted to do that!"));
                return;
            }
        }

        final List<ICitizenData> citizens = colony.getCitizenManager().getCitizens();
        final int citizenCount = citizens.size();

        // check to see if we have to add one page to show the half page
        final int halfPage = (citizenCount % CITIZENS_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((citizenCount) / CITIZENS_ON_PAGE) + halfPage;

        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = CITIZENS_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(CITIZENS_ON_PAGE * page, citizenCount);

        final List<ICitizenData> citizensPage;

        if (pageStartIndex < 0 || pageStartIndex >= citizenCount)
        {
            citizensPage = new ArrayList<>();
        }
        else
        {
            citizensPage = citizens.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new StringTextComponent(String.format(PAGE_TOP, page, pageCount));
        sender.sendMessage(headerLine);

        for (final ICitizenData citizen : citizensPage)
        {
            sender.sendMessage(new StringTextComponent(String.format(CITIZEN_DESCRIPTION,
              citizen.getId(),
              citizen.getName())).setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                     String.format(COMMAND_CITIZEN_INFO, citizen.getColony().getID(), citizen.getId())))));

            citizen.getCitizenEntity().ifPresent(entityCitizen ->
            {
                final BlockPos position = entityCitizen.getPosition();
                sender.sendMessage(new StringTextComponent(String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())));
            });
        }
        drawPageSwitcher(sender, page, citizenCount, halfPage, (null != colony ? colony.getID() : -1));
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
        final IColony tempColony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), sender.getCommandSenderEntity().getUniqueID());
        if (tempColony != null)
        {
            final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, tempColony.getCenter());
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
     * @param colonyId the colony id.
     */
    private static void drawPageSwitcher(@NotNull final ICommandSender sender, final int page, final int count, final int halfPage, final int colonyId)
    {
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (count / CITIZENS_ON_PAGE) + halfPage);

        final ITextComponent prevButton = new StringTextComponent(PREV_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, prevPage))
        ));
        final ITextComponent nextButton = new StringTextComponent(NEXT_PAGE).setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
          new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(LIST_COMMAND_SUGGESTED, colonyId, nextPage))
        ));

        final ITextComponent beginLine = new StringTextComponent(PAGE_LINE);
        final ITextComponent endLine = new StringTextComponent(PAGE_LINE);
        sender.sendMessage(beginLine.appendSibling(prevButton).appendSibling(new StringTextComponent(PAGE_LINE_DIVIDER)).appendSibling(nextButton).appendSibling(endLine));
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
