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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
        final Entity sender = context.getSource().getEntity();

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender.dimension.getId());
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.colonyidnotfound", colonyID);
            return 0;
        }

        if (!MineColonies.getConfig().getCommon().canPlayerUseShowColonyInfoCommand.get())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.notenabledinconfig");
            return 0;
        }

        final BlockPos position = colony.getCenter();
        sender.sendMessage(new StringTextComponent(ID_TEXT + colony.getID() + NAME_TEXT + colony.getName()));
        final String mayor = colony.getPermissions().getOwnerName();
        sender.sendMessage(new StringTextComponent(MAYOR_TEXT + mayor));
        sender.sendMessage(new StringTextComponent(CITIZENS + colony.getCitizenManager().getCitizens().size() + "/" + colony.getCitizenManager().getMaxCitizens()));
        sender.sendMessage(new StringTextComponent(COORDINATES_TEXT + String.format(COORDINATES_XYZ, position.getX(), position.getY(), position.getZ())).setStyle(new Style().setColor(
          TextFormatting.GREEN)));
        sender.sendMessage(new StringTextComponent(String.format(LAST_CONTACT_TEXT, colony.getLastContactInHours())));
        sender.sendMessage(new StringTextComponent(IS_DELETABLE + !colony.canBeAutoDeleted()));

        if (!colony.getRaiderManager().canHaveRaiderEvents())
        {
            sender.sendMessage(new StringTextComponent(CANNOT_BE_RAIDED));
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
