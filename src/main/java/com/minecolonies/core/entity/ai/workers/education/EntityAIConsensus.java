package com.minecolonies.core.entity.ai.workers.education;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.component.ModDataComponents;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.ConsensusBuildingModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.colony.jobs.JobConsensus;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAISkill;
import com.minecolonies.core.tileentities.TileEntityRack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The Entity AI study class.
 */
public class EntityAIConsensus extends AbstractEntityAISkill<JobConsensus, BuildingBuilder>
{
    private BlockPos workPosition;
    private int dropOffIndex = 0;
    private ItemStack currentProposal = null;

    /**
     * Constructor for the student. Defines the tasks the student executes.
     *
     * @param job a student job to use.
     */
    public EntityAIConsensus(@NotNull final JobConsensus job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(CONSENSUS_INIT, this::init, STANDARD_DELAY),
          new AITarget(LEADER_CREATE_PROPOSAL, this::leaderCreateProposal, STANDARD_DELAY),
          new AITarget(LEADER_BROADCAST_PROPOSAL, this::leaderBroadCastProposal, STANDARD_DELAY),
          new AITarget(FOLLOWER_GET_PROPOSAL, this::followerGetProposal, STANDARD_DELAY),
          new AITarget(FOLLOWER_VOTE_PROPOSAL, this::followerVoteProposal, STANDARD_DELAY),
          new AITarget(FOLLOWER_PLACE_PROPOSAL, this::followerPlaceProposal, STANDARD_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    // --------------------------- Consensus Logic ---------------------------

    private BlockPos defaultOffset()
    {
        return building.getPosition().east(20).south(10);
    }
    
    private IAIState init()
    {
        final ConsensusBuildingModule buildingModule = building.getModule(BuildingModules.CONSENSUS_WORK);
        int myIndex = buildingModule.getAssignedCitizen().indexOf(worker.getCitizenData());
        if (workPosition == null)
        {
            workPosition = defaultOffset().north(myIndex * 2);
        }
        if (walkToBlock(workPosition))
        {
            return getState();
        }

        if (world.getBlockState(workPosition).getBlock() != ModBlocks.blockRack)
        {
            world.setBlock(workPosition, ModBlocks.blockRack.defaultBlockState(), 3);
        }

        if (buildingModule.getBlockIndex() % buildingModule.getAssignedCitizen().size() == myIndex)
        {
            return LEADER_CREATE_PROPOSAL;
        }

        return FOLLOWER_GET_PROPOSAL;
    }

    private IAIState leaderCreateProposal()
    {
        final ConsensusBuildingModule buildingModule = building.getModule(BuildingModules.CONSENSUS_WORK);
        final ItemStack stack = Sheep.ITEM_BY_DYE.get(DyeColor.values()[MathUtils.RANDOM.nextInt(DyeColor.values().length)]).asItem().getDefaultInstance();
        new ConsensusData(buildingModule.getBlockIndex()).writeToItemStack(stack);
        currentProposal = stack;

        return LEADER_BROADCAST_PROPOSAL;
    }

    private IAIState leaderBroadCastProposal()
    {
        final ConsensusBuildingModule buildingModule = building.getModule(BuildingModules.CONSENSUS_WORK);
        if (dropOffIndex < 10)
        {
            final BlockPos dropOffPos = defaultOffset().north(dropOffIndex*2);
            if (walkToBlock(dropOffPos))
            {
                return getState();
            }

            if (world.getBlockState(dropOffPos).getBlock() == ModBlocks.blockRack)
            {
                InventoryUtils.addItemStackToItemHandler(((TileEntityRack) world.getBlockEntity(dropOffPos)).getItemHandlerCap(), currentProposal.copy());
            }
            dropOffIndex++;
            return getState();
        }

        // Time for the next round
        buildingModule.incrementBlockIndex();
        currentProposal = null;
        dropOffIndex = 0;
        return CONSENSUS_INIT;
    }

    private IAIState followerGetProposal()
    {
        if (walkToBlock(workPosition))
        {
            return getState();
        }

        if (world.getBlockState(workPosition).getBlock() != ModBlocks.blockRack)
        {
            return CONSENSUS_INIT;
        }

        final ItemStack proposalStack = InventoryUtils.transferProposal(((TileEntityRack) world.getBlockEntity(workPosition)).getItemHandlerCap(), stack -> ConsensusData.readFromItemStack(stack).index == job.view, worker.getInventoryCitizen());
        if (proposalStack != null)
        {
            currentProposal = proposalStack;
            worker.setItemInHand(InteractionHand.MAIN_HAND, currentProposal);
            return FOLLOWER_VOTE_PROPOSAL;
        }
        return CONSENSUS_INIT;
    }

    private IAIState followerVoteProposal()
    {
        if (dropOffIndex < 10)
        {
            final BlockPos dropOffPos = defaultOffset().north(dropOffIndex*2);
            if (walkToBlock(dropOffPos))
            {
                return getState();
            }

            if (world.getBlockState(dropOffPos).getBlock() == ModBlocks.blockRack)
            {
                final ItemStack vote = currentProposal.copy();
                vote.setCount(1);
                InventoryUtils.addItemStackToItemHandler(((TileEntityRack) world.getBlockEntity(dropOffPos)).getItemHandlerCap(), vote);
            }
            dropOffIndex++;
            return getState();
        }

        if (walkToBlock(workPosition))
        {
            return getState();
        }

        if (InventoryUtils.getItemCountInItemHandler(((TileEntityRack) world.getBlockEntity(workPosition)).getItemHandlerCap(), stack -> ConsensusData.readFromItemStack(stack).index == ConsensusData.readFromItemStack(currentProposal).index) >= 6)
        {
            InventoryUtils.reduceStackInItemHandler(((TileEntityRack) world.getBlockEntity(workPosition)).getItemHandlerCap(), stack -> ConsensusData.readFromItemStack(stack).index <= ConsensusData.readFromItemStack(currentProposal).index, 64);
            dropOffIndex = 0;
            return FOLLOWER_PLACE_PROPOSAL;
        }

        return getState();
    }


    private IAIState followerPlaceProposal()
    {
        final BlockPos proposalPos = workPosition.east(2 + job.view * 2);
        if (walkToBlock(proposalPos))
        {
            return getState();
        }

        world.setBlock(proposalPos.west(), Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.X), 3);
        if (currentProposal.getItem() instanceof BlockItem blockItem)
        {
            world.destroyBlock(proposalPos, false);
            blockItem.place(new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, currentProposal, new BlockHitResult(Vec3.ZERO, Direction.DOWN, proposalPos, true)));
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            InventoryUtils.clearItemHandler(worker.getInventoryCitizen());
        }

        currentProposal = null;
        job.view++;
        return CONSENSUS_INIT;
    }


    /**
     * Redirects the student to his library.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        final ConsensusBuildingModule buildingModule = building.getModule(BuildingModules.CONSENSUS_WORK);
        if (buildingModule.getAssignedCitizen().size() != 10)
        {
            // we need exactly 10 members.
            return getState();
        }
        return CONSENSUS_INIT;
    }

    public record ConsensusData(int index)
    {
        public static final ConsensusData EMPTY = new ConsensusData(-1);
        public static final Codec<ConsensusData>  CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.INT.fieldOf("index").forGetter(ConsensusData::index))
                       .apply(builder, ConsensusData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConsensusData> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.VAR_INT,
            ConsensusData::index,
            ConsensusData::new);

        public boolean isEmpty()
        {
            return index == EMPTY.index;
        }

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.CONSENSUS_DATA, this);
        }

        public static ConsensusData readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.getOrDefault(ModDataComponents.CONSENSUS_DATA, ConsensusData.EMPTY);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<ConsensusData> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }
}
