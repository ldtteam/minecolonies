package com.minecolonies.core.commands.querycommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;



/**
 * Displays the list of builders in the colony that currently have not taken on a build job and are idle.
 */
public class CommandQueryIdleBuilders implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        if (sender == null)
            return 0;

        // Colony
        BlockPos playerPos = sender.blockPosition();
        IColony colony = IColonyManager.getInstance().getClosestColony(sender.level(), playerPos);

        if (colony == null || !IColonyManager.getInstance().isCoordinateInAnyColony(sender.level(), playerPos)) {
            context.getSource().sendSuccess(() ->
                Component.literal("❌ You are not currently in a colony."), false);
            return 0;
        }


        final Collection<ICitizenData> allCitizens = colony.getCitizenManager().getCitizens();

        if (allCitizens == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_NOT_FOUND), false);
            return 0;
        }


        // Identify all citizens with a builder job
        final List<ICitizenData> builderCitizens = new ArrayList<>();

        for (final ICitizenData citizen : allCitizens)
        {
            if (citizen.getJob() instanceof JobBuilder)
            {
                builderCitizens.add(citizen);
            }
        }


        // then we can check each builder's job status
        final List<ICitizenData> idleBuilders = new ArrayList<>();

        for (final ICitizenData builder : builderCitizens)
        {
            final JobBuilder job = (JobBuilder) builder.getJob();

            // If the builder has no work order, consider them idle
            if (!job.hasWorkOrder())
            {
                idleBuilders.add(builder);
            }
        }


        // for any builders that are 'idle' we will report their name and their building location
        //  and if all builders are busy we will just say "✅ All builders currently have active building assignments."
        if (idleBuilders.isEmpty())
        {
            context.getSource().sendSuccess(
                () -> Component.literal("✅ All builders currently have active building assignments."),
                false
            );
        }
        else
        {
            context.getSource().sendSuccess(
                () -> Component.literal("⚠️ Idle builders detected:"),
                false
            );

            for (final ICitizenData builder : idleBuilders)
            {
                // Get the work building; skip this builder if it's missing (just in case)
                if (builder.getWorkBuilding() == null)
                    continue;

                final BlockPos pos = builder.getWorkBuilding().getPosition();
                context.getSource().sendSuccess(
                    () -> Component.literal("- " + builder.getName() +
                            " at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                        .withStyle(styleWithTeleport(pos)),
                    false
                );
            }
        }

        return 1;
    }

    /**
     * Name string of the command.
     * Used as the subcommand: /mc query idlebuilders
     */
    @Override
    public String getName()
    {
        return "idlebuilders";
    }


    /**
     * Creates a style with clickable teleport
     *
     * @param pos the position to teleport to
     * @return the style with a click event
     */
    private static Style styleWithTeleport(final BlockPos pos)
    {
        return Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
    }
}

