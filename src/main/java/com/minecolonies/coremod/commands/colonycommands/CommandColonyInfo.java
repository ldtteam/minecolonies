package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandColonyInfo implements IMCCommand
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

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseShowColonyInfoCommand.get())
        {
            context.getSource().sendSuccess(Component.translatable(COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }

        final BlockPos position = colony.getCenter();
        context.getSource().sendSuccess(Component.literal(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()), true);
        final String mayor = colony.getPermissions().getOwnerName();
        context.getSource().sendSuccess(Component.literal(MAYOR_TEXT + mayor), true);
        context.getSource()
          .sendSuccess(Component.literal(CITIZENS + colony.getCitizenManager().getCurrentCitizenCount() + "/" + colony.getCitizenManager().getMaxCitizens()), true);
        context.getSource()
          .sendSuccess(Component.literal(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())).setStyle(Style.EMPTY.withColor(
            ChatFormatting.GREEN)), true);
        context.getSource().sendSuccess(Component.literal(String.format(LAST_CONTACT_TEXT, colony.getLastContactInHours())), true);
        context.getSource().sendSuccess(Component.literal(IS_DELETABLE + !colony.canBeAutoDeleted()), true);

        if (!colony.getRaiderManager().canHaveRaiderEvents())
        {
            context.getSource().sendSuccess(Component.literal(CANNOT_BE_RAIDED), true);
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "info";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
