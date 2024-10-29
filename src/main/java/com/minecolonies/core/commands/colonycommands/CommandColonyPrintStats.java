package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.events.raid.RaidManager;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.research.LocalResearchTree;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Command to print statistics on a colony
 */
public class CommandColonyPrintStats implements IMCOPCommand
{
    public static final  String ID_TEXT           = "ID: ";
    public static final  String NAME_TEXT         = "Name: ";
    private static final String MAYOR_TEXT        = "Mayor: ";
    private static final String COORDINATES_TEXT  = "Coordinates: ";
    private static final String COORDINATES_XYZ   = "x=%s y=%s z=%s";
    private static final String CITIZENS          = "Citizens: ";
    private static final String LAST_CONTACT_TEXT = "Last contact with Owner or Officer: %d hours ago!";
    private static final String IS_DELETABLE      = "If true this colony cannot be deleted: ";
    private static final String CANNOT_BE_RAIDED  = "This colony is unable to be raided";
    private              String fullLog           = "";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        fullLog = "\n";
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), false);
            return 0;
        }

        final BlockPos position = colony.getCenter();
        context.getSource().sendSuccess(() -> literalAndRemember(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()), false);
        final String mayor = colony.getPermissions().getOwnerName();
        context.getSource().sendSuccess(() -> literalAndRemember(MAYOR_TEXT + mayor), false);
        context.getSource()
          .sendSuccess(() -> literalAndRemember(CITIZENS + colony.getCitizenManager().getCurrentCitizenCount() + "/" + colony.getCitizenManager().getMaxCitizens()), false);
        context.getSource()
          .sendSuccess(() -> literalAndRemember(
            COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())).setStyle(Style.EMPTY.withColor(
            ChatFormatting.GREEN)), false);
        context.getSource().sendSuccess(() -> literalAndRemember(String.format(LAST_CONTACT_TEXT, colony.getLastContactInHours())), false);
        context.getSource().sendSuccess(() -> literalAndRemember(IS_DELETABLE + !colony.canBeAutoDeleted()), false);

        if (!colony.getRaiderManager().canHaveRaiderEvents())
        {
            context.getSource().sendSuccess(() -> literalAndRemember(CANNOT_BE_RAIDED), false);
        }

        final RaidManager.RaidHistory last = ((RaidManager) colony.getRaiderManager()).getLastRaid();
        if (last != null)
        {
            context.getSource().sendSuccess(() -> literalAndRemember(last.toString()), false);
        }

        if (!colony.getBuildingManager().getBuildings().isEmpty())
        {
            int count = 0;
            int levels = 0;

            for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
            {
                if (building.getBuildingLevel() != 0)
                {
                    count++;
                    levels += building.getBuildingLevel();
                }
            }

            final double average = (double) levels / count;

            context.getSource()
                .sendSuccess(() -> literalAndRemember("Buildings:" + colony.getBuildingManager().getBuildings().size() + " average level:" + average), false);
            context.getSource()
              .sendSuccess(() -> literalAndRemember(colony.getBuildingManager()
                .getBuildings()
                .values()
                .stream()
                .filter(iBuilding -> iBuilding.getBuildingLevel() != 0)
                .map(building -> building.getSchematicName() + building.getBuildingLevel() + " pos:" + building.getPosition().toShortString())
                .collect(
                  Collectors.joining("\n"))), false);
        }

        List<ResourceLocation> completed = ((LocalResearchTree) colony.getResearchManager().getResearchTree()).getCompletedList();
        context.getSource().sendSuccess(() -> literalAndRemember("Reasearches completed count:" + completed.size()), false);
        context.getSource().sendSuccess(() -> literalAndRemember(completed.stream().map(r -> r.toString()).collect(Collectors.joining("\n"))), false);

        Log.getLogger().info(fullLog);
        return 1;
    }

    /**
     * Creates a literal component and remembers the string for logging
     *
     * @param message
     * @return
     */
    MutableComponent literalAndRemember(String message)
    {
        fullLog += message + "\n";
        return MutableComponent.create(new LiteralContents(message));
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "printStats";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
