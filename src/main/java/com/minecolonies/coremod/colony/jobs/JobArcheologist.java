package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.entity.ai.citizen.archeologist.EntityAIWorkArcheologist;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class JobArcheologist extends AbstractJob<EntityAIWorkArcheologist, JobArcheologist>
{

    /**
     * The current position of the gate the archeologist is using.
     */
    private Tuple<BlockPos, BlockPos> gate;

    /**
     * The state of the entity AI before event handling started.
     */
    private AIWorkerState preEventHandlingState = null;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobArcheologist(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ARCHEOLOGIST_ID;
    }

    @Override
    public EntityAIWorkArcheologist generateAI()
    {
        return new EntityAIWorkArcheologist(this);
    }

    @Override
    public int getDiseaseModifier()
    {
        final int skill = getCitizen().getCitizenSkillHandler().getLevel(Objects.requireNonNull(getCitizen().getWorkBuilding())
          .getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == this.getJobRegistryEntry()).getPrimarySkill());
        return (int) ((100 - skill)/25.0);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = super.serializeNBT();

        final CompoundTag gateTag = new CompoundTag();
        if (gate != null) {
            BlockPosUtil.write(gateTag, NbtTagConstants.TAG_GATE_POS, Objects.requireNonNull(gate.getA()));
            BlockPosUtil.write(gateTag, NbtTagConstants.TAG_GATE_PARENT_POS, Objects.requireNonNull(gate.getB()));
        }
        compoundTag.put(TAG_GATE_POS, gateTag);
        if (preEventHandlingState != null) {
            compoundTag.put(NbtTagConstants.PRE_TRAVEL_STATE, this.preEventHandlingState.serializeNBT());
        }

        return compoundTag;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        gate = null;
        preEventHandlingState = null;
        if (compound.contains(TAG_GATE_POS))
        {
            final CompoundTag gateTag = compound.getCompound(TAG_GATE_POS);
            gate = new Tuple<>(BlockPosUtil.read(gateTag, TAG_GATE_POS), BlockPosUtil.read(gateTag, TAG_GATE_PARENT_POS));
        }
        if (compound.contains(NbtTagConstants.PRE_TRAVEL_STATE))
        {
            preEventHandlingState = AIWorkerState.deserializeNBT((StringTag) Objects.requireNonNull(compound.get(PRE_TRAVEL_STATE)));
        }
    }

    /**
     * Getter for the current gate.
     *
     * @return Location of the current gate.
     */
    public Tuple<BlockPos, BlockPos> getGate()
    {
        return gate;
    }

    /**
     * Setter for the current gate.
     *
     * @param gateAndParentPosition New location for the current gate block.
     */
    public void setGate(final Tuple<BlockPos, BlockPos> gateAndParentPosition)
    {
        this.gate = gateAndParentPosition;
    }

    public AIWorkerState getPreEventHandlingState()
    {
        return preEventHandlingState;
    }

    public void setPreEventHandlingState(final AIWorkerState preEventHandlingState)
    {
        this.preEventHandlingState = preEventHandlingState;
    }
}
