package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandColonyInfo implements IMCCommand
{
    private static final String ID_TEXT                    = "ID: ";
    private static final String NAME_TEXT                  = "Name: ";
    private static final String MAYOR_TEXT                 = "Mayor: ";
    private static final String COORDINATES_TEXT           = "Coordinates: ";
    private static final String COORDINATES_XYZ            = "x=%s y=%s z=%s";
    private static final String CITIZENS                   = "Citizens: ";
    private static final String LAST_CONTACT_TEXT          = "Last contact with Owner or Officer: %d hours ago!";
    private static final String IS_DELETABLE               = "If true this colony cannot be deleted: ";
    private static final String CANNOT_BE_RAIDED           = "This colony is unable to be raided";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        if (!context.getSource().hasPermissionLevel(OP_PERM_LEVEL) && !MineColonies.getConfig().getCommon().canPlayerUseShowColonyInfoCommand.get())
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.notenabledinconfig"), true);
            return 0;
        }

        final BlockPos position = colony.getCenter();
        context.getSource().sendFeedback(new StringTextComponent(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()), true);
        final String mayor = colony.getPermissions().getOwnerName();
        context.getSource().sendFeedback(new StringTextComponent(MAYOR_TEXT + mayor), true);
        context.getSource()
          .sendFeedback(new StringTextComponent(CITIZENS + colony.getCitizenManager().getCitizens().size() + "/" + colony.getCitizenManager().getMaxCitizens()), true);
        context.getSource()
          .sendFeedback(new StringTextComponent(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())).setStyle(new Style().setColor(
            TextFormatting.GREEN)), true);
        context.getSource().sendFeedback(new StringTextComponent(String.format(LAST_CONTACT_TEXT, colony.getLastContactInHours())), true);
        context.getSource().sendFeedback(new StringTextComponent(IS_DELETABLE + !colony.canBeAutoDeleted()), true);

        if (!colony.getRaiderManager().canHaveRaiderEvents())
        {
            context.getSource().sendFeedback(new StringTextComponent(CANNOT_BE_RAIDED), true);
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
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
