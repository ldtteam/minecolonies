package com.minecolonies.coremod.entity.ai.citizen.beekeeper;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.colony.jobs.JobBeekeeper;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_IDLING;

public class EntityAIWorkBeekeeper extends AbstractEntityAIInteract<JobBeekeeper> {
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkBeekeeper(@NotNull JobBeekeeper job) {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING, 1),
                new AITarget(START_WORKING, this::decideWhatToDo, TICKS_SECOND)
        );
    }

    private IAIState decideWhatToDo() {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_IDLING));

        if(walkToBuilding())
        {
            setDelay(2);
            return getState();
        }

        final BuildingBeekeeper building = this.getOwnBuilding();
        for (BlockPos hive : building.getHives())
        {
            BlockState hiveState = world.getBlockState(hive);
            if (hiveState.getBlock() == Blocks.BEEHIVE &&
                    hiveState.has(BlockStateProperties.field_227036_ao_) &&
                    hiveState.get(BlockStateProperties.field_227036_ao_) >= 5)
            {
                setDelay(40);
                return BEEKEEPER_HARVEST;
            }
        }
        return null; //TODO
    }
}
