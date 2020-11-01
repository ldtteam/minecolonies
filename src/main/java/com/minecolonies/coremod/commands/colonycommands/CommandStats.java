package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import static com.minecolonies.coremod.commands.CommandArgumentNames.*;

public class CommandStats implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);

        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().getDimensionKey().getLocation());
        if (colony == null)
        {
            return 0;
        }

        double wayToWorkSum = 0;
        double workerStatsSum = 0;
        double intelligenceSum = 0;
        int total = 0;

        for (final ICitizenData data : colony.getCitizenManager().getCitizens())
        {
            if (data.getWorkBuilding() != null)
            {
                workerStatsSum += data.getCitizenSkillHandler().getJobModifier(data);
                if (data.getHomeBuilding() == null)
                {
                    wayToWorkSum += 200;
                }
                else
                {
                    wayToWorkSum += data.getHomeBuilding().getPosition().distanceSq(data.getWorkBuilding().getPosition());
                }
                total++;
             }
            intelligenceSum += data.getCitizenSkillHandler().getLevel(Skill.Intelligence);
        }

        double totalLevel5Count = 0;

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (building.getBuildingLevel() >= 5)
            {
                totalLevel5Count += 1;
            }
        }

        final double intelligenceAvg = intelligenceSum / colony.getCitizenManager().getCitizens().size();
        final double wayToWorkAvg = wayToWorkSum/total;
        final double workerStatsAvg = workerStatsSum/total;
        final double overallHappiness = colony.getOverallHappiness();
        final double researchTotal = colony.getResearchManager().getResearchTree().getResearchCount();

        context.getSource().sendFeedback(new StringTextComponent("Avg Intelligence: " + intelligenceAvg), true);
        context.getSource().sendFeedback(new StringTextComponent("Way to Work Sq: " + wayToWorkAvg), true);
        context.getSource().sendFeedback(new StringTextComponent("Avg Worker Stats: " + workerStatsAvg), true);
        context.getSource().sendFeedback(new StringTextComponent("Overall Happiness: " + overallHappiness), true);
        context.getSource().sendFeedback(new StringTextComponent("Total Research: " + researchTotal), true);
        context.getSource().sendFeedback(new StringTextComponent("Total Level 5 Buildings: " + totalLevel5Count), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "stats";
    }

    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                        .executes(this::checkPreConditionAndExecute));
    }
}
