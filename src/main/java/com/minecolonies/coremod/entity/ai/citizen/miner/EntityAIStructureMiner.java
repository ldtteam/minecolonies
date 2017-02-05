package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIStructureMiner extends AbstractEntityAIStructure<JobMiner>
{

    private static final String     RENDER_META_TORCH         = "Torch";
    private static final int        NODE_DISTANCE       = 7;
    /**
     * Return to chest after 3 stacks.
     */
    private static final int        MAX_BLOCKS_MINED    = 3 * 64;
    private static final int        LADDER_SEARCH_RANGE = 10;
    private static final int        SHAFT_RADIUS        = 3;
    private static final int        SAFE_CHECK_RANGE    = 5;

    /**
     * Possible rotations.
     */
    private static final int ROTATE_ONCE        = 1;
    private static final int ROTATE_TWICE       = 2;
    private static final int ROTATE_THREE_TIMES = 3;
    private static final int ROTATE_FOUR_TIMES  = 4;

    /**
     * Check for liquids in the following range.
     */
    private static final int LIQUID_CHECK_RANGE = 5;

    //The current block to mine
    @Nullable
    private BlockPos minerWorkingLocation;
    //the last safe location now being air
    @Nullable
    private BlockPos currentStandingPosition;
    @Nullable
    private Node    workingNode    = null;

    /**
     * Constructor for the Miner.
     * Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIStructureMiner(@NotNull final JobMiner job)
    {
        super(job);
        super.registerTargets(
          /**
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForMining),
          new AITarget(MINER_SEARCHING_LADDER, this::lookForLadder),
          new AITarget(MINER_WALKING_TO_LADDER, this::goToLadder),
          new AITarget(MINER_CHECK_MINESHAFT, this::checkMineShaft),
          new AITarget(MINER_MINING_SHAFT, this::doShaftMining),
          new AITarget(MINER_BUILDING_SHAFT, this::doShaftBuilding),
          new AITarget(MINER_MINING_NODE, this::executeNodeMining)
        );
        worker.setSkillModifier(
          2 * worker.getCitizenData().getStrength()
            + worker.getCitizenData().getEndurance());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public IBlockState getSolidSubstitution(BlockPos ignored)
    {
        return Blocks.COBBLESTONE.getDefaultState();
    }

    private static boolean isOre(final Block block)
    {
        //TODO make this more sophisticated
        return block instanceof BlockOre;
    }

    //Miner wants to work but is not at building
    @NotNull
    private AIState startWorkingAtOwnBuilding()
    {
        if (worker.posY >= getOwnBuilding().getLocation().getY() && walkToBuilding())
        {
            return START_WORKING;
        }
        //Miner is at building
        return PREPARING;
    }

    @NotNull
    private AIState prepareForMining()
    {
        if (getOwnBuilding() != null && !getOwnBuilding().hasFoundLadder())
        {
            return MINER_SEARCHING_LADDER;
        }
        return MINER_CHECK_MINESHAFT;
    }

    /**
     * Walking to the ladder to check out the mine.
     *
     * @return next AIState.
     */
    @NotNull
    private AIState goToLadder()
    {
        if (walkToLadder())
        {
            return MINER_WALKING_TO_LADDER;
        }
        return MINER_CHECK_MINESHAFT;
    }

    private boolean walkToLadder()
    {
        return walkToBlock(getOwnBuilding().getLadderLocation());
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     * <p>
     * Override this to change the value.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected BuildingMiner getOwnBuilding()
    {
        return (BuildingMiner) worker.getWorkBuilding();
    }

    @Override
    protected void updateRenderMetaData()
    {
        @NotNull final String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }

    @NotNull
    private String getRenderMetaTorch()
    {
        if (worker.hasItemInInventory(Blocks.TORCH, -1))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    @NotNull
    private AIState checkMineShaft()
    {
        //Check if we reached the mineshaft depth limit
        if (getLastLadder(getOwnBuilding().getLadderLocation()) < getOwnBuilding().getDepthLimit())
        {
            //If the miner hut has been placed too deep.
            if (getOwnBuilding().getNumberOfLevels() == 0)
            {
                chatSpamFilter.talkWithoutSpam("entity.miner.messageRequiresBetterHut");
                getOwnBuilding().clearedShaft = false;
                return IDLE;
            }
            getOwnBuilding().clearedShaft = true;
            return MINER_MINING_NODE;
        }
        getOwnBuilding().clearedShaft = false;
        return MINER_MINING_SHAFT;
    }

    @NotNull
    private AIState lookForLadder()
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();

        //Check for already found ladder
        if (buildingMiner.hasFoundLadder() && buildingMiner.getLadderLocation() != null)
        {
            if (world.getBlockState(buildingMiner.getLadderLocation()).getBlock() == Blocks.LADDER)
            {
                return MINER_WALKING_TO_LADDER;
            }
            else
            {
                buildingMiner.setFoundLadder(false);
                buildingMiner.setLadderLocation(null);
            }
        }

        final int posX = buildingMiner.getLocation().getX();
        final int posY = buildingMiner.getLocation().getY() + 2;
        final int posZ = buildingMiner.getLocation().getZ();
        for (int y = posY - LADDER_SEARCH_RANGE; y < posY; y++)
        {
            for (int x = posX - LADDER_SEARCH_RANGE; x < posX + LADDER_SEARCH_RANGE; x++)
            {
                for (int z = posZ - LADDER_SEARCH_RANGE; z < posZ + LADDER_SEARCH_RANGE; z++)
                {
                    tryFindLadderAt(new BlockPos(x, y, z));
                }
            }
        }

        return MINER_SEARCHING_LADDER;
    }

    private void tryFindLadderAt(@NotNull final BlockPos pos)
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.hasFoundLadder())
        {
            return;
        }
        if (world.getBlockState(pos).getBlock().equals(Blocks.LADDER))
        {
            final int firstLadderY = getFirstLadder(pos);
            buildingMiner.setLadderLocation(new BlockPos(pos.getX(), firstLadderY, pos.getZ()));
            validateLadderOrientation();
        }
    }

    private void validateLadderOrientation()
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();
        final EnumFacing ladderOrientation = world.getBlockState(buildingMiner.getLadderLocation()).getValue(BlockLadder.FACING);

        if (ladderOrientation == EnumFacing.WEST)
        {
            buildingMiner.setVectorX(-1);
            buildingMiner.setVectorZ(0);
        }
        else if (ladderOrientation == EnumFacing.EAST)
        {
            buildingMiner.setVectorX(1);
            buildingMiner.setVectorZ(0);
        }
        else if (ladderOrientation == EnumFacing.SOUTH)
        {
            buildingMiner.setVectorX(0);
            buildingMiner.setVectorZ(1);
        }
        else if (ladderOrientation == EnumFacing.NORTH)
        {
            buildingMiner.setVectorX(0);
            buildingMiner.setVectorZ(-1);
        }
        else
        {
            throw new IllegalStateException("Ladder metadata was " + ladderOrientation);
        }

        final int x = buildingMiner.getLadderLocation().getX();
        final int y = buildingMiner.getLadderLocation().getY();
        final int z = buildingMiner.getLadderLocation().getZ();

        buildingMiner.setCobbleLocation(new BlockPos(x - buildingMiner.getVectorX(), y, z - buildingMiner.getVectorZ()));
        buildingMiner.setShaftStart(new BlockPos(x, getLastLadder(buildingMiner.getLadderLocation()) - 1, z));
        buildingMiner.setFoundLadder(true);
    }

    private AIState doShaftMining()
    {
        minerWorkingLocation = getNextBlockInShaftToMine();
        if (minerWorkingLocation == null)
        {
            return advanceLadder(MINER_MINING_SHAFT);
        }

        //Note for future me:
        //we have to return; on false of this method
        //but omitted because end of method.
        mineBlock(minerWorkingLocation, currentStandingPosition);

        return MINER_MINING_SHAFT;
    }

    private AIState advanceLadder(final AIState state)
    {
        if (getOwnBuilding().getStartingLevelShaft() >= 4)
        {
            return MINER_BUILDING_SHAFT;
        }

        if (checkOrRequestItems(new ItemStack(Blocks.COBBLESTONE, 2), new ItemStack(Blocks.LADDER)))
        {
            return state;
        }

        @NotNull final BlockPos safeCobble =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 2, getOwnBuilding().getLadderLocation().getZ());

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();
        //Check for safe floor
        for (int x = -SAFE_CHECK_RANGE + xOffset; x <= SAFE_CHECK_RANGE + xOffset; x++)
        {
            for (int z = -SAFE_CHECK_RANGE + zOffset; z <= SAFE_CHECK_RANGE + zOffset; z++)
            {
                @NotNull final BlockPos curBlock = new BlockPos(safeCobble.getX() + x, safeCobble.getY(), safeCobble.getZ() + z);
                if (!secureBlock(curBlock, currentStandingPosition))
                {
                    return state;
                }
            }
        }

        @NotNull final BlockPos safeStand =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()), getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextLadder =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextCobble =
          new BlockPos(getOwnBuilding().getCobbleLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getCobbleLocation().getZ());

        if (!mineBlock(nextCobble, safeStand) || !mineBlock(nextLadder, safeStand))
        {
            //waiting until blocks are mined
            return state;
        }


        //Get ladder orientation
        final IBlockState metadata = getBlockState(safeStand);

        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.COBBLESTONE);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.LADDER, metadata);
        getOwnBuilding().incrementStartingLevelShaft();
        return MINER_CHECK_MINESHAFT;
    }

    private IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return world.getBlockState(pos);
    }

    /**
     * Calculates the next non-air block to mine.
     * Will take the nearest block it finds.
     */
    @Nullable
    private BlockPos getNextBlockInShaftToMine()
    {

        final BlockPos ladderPos = getOwnBuilding().getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos);
        if (minerWorkingLocation == null)
        {
            minerWorkingLocation = new BlockPos(ladderPos.getX(), lastLadder + 1, ladderPos.getZ());
        }
        Block block = getBlock(minerWorkingLocation);
        if (block != null
              && block != Blocks.AIR
              && block != Blocks.LADDER
              && !(block.equals(Blocks.FLOWING_WATER)
                     || block.equals(Blocks.FLOWING_LAVA)))
        {
            return minerWorkingLocation;
        }
        currentStandingPosition = minerWorkingLocation;
        @Nullable BlockPos nextBlockToMine = null;
        double bestDistance = Double.MAX_VALUE;

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();

        //remove water for safety
        for (int x = SHAFT_RADIUS + xOffset + 2; x >= -SHAFT_RADIUS + xOffset - 2; x--)
        {
            for (int z = -SHAFT_RADIUS + zOffset - 2; z <= SHAFT_RADIUS + zOffset + 2; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                block = getBlock(curBlock);
                if (block.equals(Blocks.WATER)
                      || block.equals(Blocks.LAVA)
                      || block.equals(Blocks.FLOWING_WATER)
                      || block.equals(Blocks.FLOWING_LAVA))
                {
                    setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
                }
            }
        }


        //7x7 shaft find nearest block
        //Beware from positive to negative! to draw the miner to a wall to go down
        for (int x = SHAFT_RADIUS + xOffset; x >= -SHAFT_RADIUS + xOffset; x--)
        {
            for (int z = -SHAFT_RADIUS + zOffset; z <= SHAFT_RADIUS + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                final double distance = curBlock.distanceSq(ladderPos) + Math.pow(curBlock.distanceSq(minerWorkingLocation), 2);
                block = getBlock(curBlock);
                if (distance < bestDistance
                      && !world.isAirBlock(curBlock))
                {
                    if (block.equals(Blocks.WATER)
                          || block.equals(Blocks.LAVA)
                          || block.equals(Blocks.FLOWING_WATER)
                          || block.equals(Blocks.FLOWING_LAVA))
                    {
                        setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
                    }
                    nextBlockToMine = curBlock;
                    bestDistance = distance;
                }
            }
        }
        //find good looking standing position
        bestDistance = Double.MAX_VALUE;
        if (nextBlockToMine != null)
        {
            for (int x = 1; x >= -1; x--)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && 0 == z)
                    {
                        continue;
                    }
                    @NotNull final BlockPos curBlock = new BlockPos(nextBlockToMine.getX() + x, lastLadder, nextBlockToMine.getZ() + z);
                    final double distance = curBlock.distanceSq(ladderPos);
                    if (distance < bestDistance && world.isAirBlock(curBlock))
                    {
                        currentStandingPosition = curBlock;
                        bestDistance = distance;
                    }
                }
            }
        }
        return nextBlockToMine;
    }

    @NotNull
    private AIState doShaftBuilding()
    {
        if (walkToBuilding())
        {
            return MINER_BUILDING_SHAFT;
        }

        final BlockPos ladderPos = getOwnBuilding().getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos) + 1;

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();

        initStructure(null, 0, new BlockPos(ladderPos.getX() + xOffset, lastLadder, ladderPos.getZ() + zOffset));

        return CLEAR_STEP;
    }

    @NotNull
    private AIState executeNodeMining()
    {
        @Nullable final Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null)
        {
            Log.getLogger().warn("Current Level not set, resetting...");
            getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels() - 1);
            return executeNodeMining();
        }
        return searchANodeToMine(currentLevel);
    }

    private AIState searchANodeToMine(@NotNull final Level currentLevel)
    {
        if (workingNode == null || workingNode.getStatus() == Node.NodeStatus.COMPLETED)
        {
            workingNode = currentLevel.getRandomNode();
            return MINER_CHECK_MINESHAFT;
        }

        //normal facing +x
        int rotation = 0;

        final int vectorX = workingNode.getX() < workingNode.getParent().getX() ? -1 : (workingNode.getX() > workingNode.getParent().getX() ? 1 : 0);
        final int vectorZ = workingNode.getZ() < workingNode.getParent().getY() ? -1 : (workingNode.getZ() > workingNode.getParent().getY() ? 1 : 0);

        if(vectorX == -1)
        {
            rotation = ROTATE_TWICE;
        }
        else if(vectorZ == -1)
        {
            rotation = ROTATE_THREE_TIMES;
        }
        else if(vectorZ == 1)
        {
            rotation = ROTATE_ONCE;
        }

        @NotNull final BlockPos standingPosition = new BlockPos(workingNode.getParent().getX(), currentLevel.getDepth(), workingNode.getParent().getY());
        currentStandingPosition = standingPosition;
        if ((workingNode.getStatus() == Node.NodeStatus.AVAILABLE || workingNode.getStatus() == Node.NodeStatus.IN_PROGRESS) && !walkToBlock(standingPosition))
        {
            return executeStructurePlacement(workingNode, standingPosition, rotation);
        }
        return MINER_CHECK_MINESHAFT;
    }

    private boolean secureBlock(@NotNull final BlockPos curBlock, @NotNull final BlockPos safeStand)
    {
        if ((!getBlockState(curBlock).getMaterial().blocksMovement() && getBlock(curBlock) != Blocks.TORCH) || isOre(getBlock(curBlock)))
        {

            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (checkOrRequestItems(new ItemStack(Blocks.COBBLESTONE)))
            {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }

    /**
     * Initiates structure loading.
     * @param mineNode the node to load it for.
     * @param direction the direction it faces.
     */
    private void initStructure(final Node mineNode, final int rotateTimes, BlockPos structurePos)
    {
        if(mineNode == null)
        {
            loadStructure("miner/minerMainShaft", getRotationFromVector(), structurePos);
        }
        else
        {
            //todo we can add other nodeTypes without a problem.
            if (mineNode.getStyle() == Node.NodeType.CROSSROAD)
            {
                loadStructure("miner/minerX4", rotateTimes, structurePos);
            }
            if (mineNode.getStyle() == Node.NodeType.BEND)
            {
                loadStructure("miner/minerX2Right", rotateTimes, structurePos);
            }
            if (mineNode.getStyle() == Node.NodeType.TUNNEL)
            {
                loadStructure("miner/minerX2Top", rotateTimes, structurePos);
            }
        }
    }

    /**
     * Return number of rotation for our building, for the main shaft.
     * @return the rotation.
     */
    private int getRotationFromVector()
    {
        if(getOwnBuilding().getVectorX() == 1)
        {
            return ROTATE_ONCE;
        }
        else if(getOwnBuilding().getVectorZ() == 1)
        {
            return ROTATE_TWICE;
        }
        else if(getOwnBuilding().getVectorX() == -1)
        {
            return ROTATE_THREE_TIMES;
        }
        else if(getOwnBuilding().getVectorZ() == -1)
        {
            return ROTATE_FOUR_TIMES;
        }
        return 0;
    }

    private AIState executeStructurePlacement(@NotNull final Node mineNode, @NotNull final BlockPos standingPosition, final int rotation)
    {
        mineNode.setStatus(Node.NodeStatus.IN_PROGRESS);
        //Preload structures
        if (job.getStructure() == null)
        {
            initStructure(mineNode, rotation, new BlockPos(mineNode.getX(), getOwnBuilding().getCurrentLevel().getDepth(), mineNode.getZ()));
        }

        //Check for liquids
        for (int x = -NODE_DISTANCE / 2 - 1; x <= NODE_DISTANCE / 2 + 1; x++)
        {
            for (int z = -NODE_DISTANCE / 2 - 1; z <= NODE_DISTANCE / 2 + 1; z++)
            {
                for (int y = -1; y <= LIQUID_CHECK_RANGE; y++)
                {
                    @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    final Block block = getBlock(curBlock);
                    if (block.equals(Blocks.WATER)
                          || block.equals(Blocks.LAVA)
                          || block.equals(Blocks.FLOWING_WATER)
                          || block.equals(Blocks.FLOWING_LAVA))
                    {
                        setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
                    }
                }
            }
        }

        workingNode = null;

        if(job.getStructure() != null)
        {
            return CLEAR_STEP;
        }

        return MINER_MINING_NODE;
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, @NotNull final Block block)
    {
        worker.swingArm(worker.getActiveHand());
        setBlockFromInventory(location, block, block.getDefaultState());
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block, final IBlockState metadata)
    {
        final int slot;
        if(block instanceof BlockLadder)
        {
            slot = worker.findFirstSlotInInventoryWith(block, -1);
        }
        else
        {
            slot = worker.findFirstSlotInInventoryWith(block, block.getMetaFromState(metadata));
        }
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
            world.setBlockState(location, metadata, 3);
        }
    }

    private Block getBlock(@NotNull final BlockPos loc)
    {
        return world.getBlockState(loc).getBlock();
    }

    private int getLastLadder(@NotNull final BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getLastLadder(pos.down());
        }
        else
        {
            return pos.getY() + 1;
        }
    }

    private int getFirstLadder(@NotNull final BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getFirstLadder(pos.up());
        }
        else
        {
            return pos.getY() - 1;
        }
    }
}
