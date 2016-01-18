package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.pathfinding.PathResult;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Raycoms
 */

public class EntityAIWorkMiner extends EntityAIWork<JobMiner>
{
    //TODO ChunkCoordinates are call by reference!
    private static final String RENDER_META_TORCH = "Torch";

    private static Logger logger = LogManager.getLogger("Miner");
    public enum Stage
    {
        INVENTORY_FULL,
        SEARCHING_LADDER,
        MINING_VEIN,
        MINING_SHAFT,
        WORKING,
        MINING_NODE,
        FILL_VEIN
    }


    /**
     * Add blocks to this list to exclude mine checks.
     * They can be mined for free. (be cautions with this)
     */
    public static Set<Block> canBeMined = new HashSet<>(Arrays.asList(
            Blocks.air,Blocks.fence,Blocks.planks,Blocks.ladder,
            Blocks.torch,Blocks.chest,Blocks.mob_spawner,Blocks.grass,
            Blocks.tallgrass,Blocks.cactus,Blocks.log,Blocks.log2
    ));

    private int delay = 0;
    private String NEED_ITEM;
    private int tryThreeTimes = 3;
    private boolean hasDelayed = false;
    private int currentY=200;
    private int clear = 1;                   //Can be saved here for now
    private int blocksMined = 0;
    private Block hasToMine = Blocks.cobblestone;
    int neededPlanks = 64;
    int neededTorches = 4;

    public List<ChunkCoordinates> localVein;
    public ChunkCoordinates getLocation;
    private ChunkCoordinates miningBlock;
    private ChunkCoordinates loc;

    private int clearNode=0;
    private int canMineNode=0;
    private int currentLevel=-1;
    private PathResult cachedPathResult;


    public EntityAIWorkMiner(JobMiner job)
    {
        super(job);
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting()
    {
        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    public boolean isLadderFound(BuildingMiner ownBuilding){
        if(new ChunkCoordinates(0, 0, 0).equals(ownBuilding.ladderLocation)
                || new ChunkCoordinates(0, 0, 0).equals(ownBuilding.shaftStart)){
            /*
            Removed obsolete path, should never happen.
            Because it could be a transition case, tests are needed to detect, if it ever happens.
            If it happens, fail catastrophically to detect it immediately.
            Switched equals to dodge null check and still have it above the return.
            TODO: Remove by next release if error never happens!
            */
            logger.error("Wrong path in Miner code, should never happen. Please report bug!");
            throw new IllegalStateException("MineColonies Miner ladder/shaft coordinates are centered. " +
                    "This should never happen, please report the bug with a full forge log!");
        }
        return ownBuilding.ladderLocation != null
                && ownBuilding.shaftStart != null;
    }

    public boolean checkThreeTimes(){
        if(tryThreeTimes > 0){
            tryThreeTimes--;
        }
        return tryThreeTimes <= 0;
    }

    private String getRenderMetaTorch(){
        if(inventoryContains(Blocks.torch) != -1){
            return RENDER_META_TORCH;
        }
        return "";
    }

    private void renderChestBelt(){
        String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }



    @Override
    public void updateTask() {
        BuildingMiner ownBuilding = (BuildingMiner) worker.getWorkBuilding();
        if(ownBuilding == null){return;}

        renderChestBelt();

        if(currentLevel == -1)
        {
            currentLevel = ownBuilding.currentLevel;
        }

        if(job.getStage() != Stage.MINING_NODE && !isLadderFound(ownBuilding)) {
            if (checkThreeTimes()) {
                //Not found after three updateTask calls
                ownBuilding.foundLadder = false;
                job.setStage(Stage.SEARCHING_LADDER);
            }
            return;
        }
        if (job.getStage() == Stage.MINING_NODE || job.getStage() == Stage.WORKING) {
            if (ownBuilding.ladderLocation.posY > ownBuilding.getMaxY()) {
                ownBuilding.clearedShaft = false;
                job.setStage(Stage.MINING_SHAFT);
            }
        }

        if (delay > 0) {
            if(job.getStage() == Stage.MINING_NODE || job.getStage() == Stage.MINING_SHAFT || job.getStage() == Stage.MINING_VEIN) {
                worker.hitBlockWithToolInHand(miningBlock);
            }
            delay--;
        }
        else if(job.hasItemsNeeded())
        {
            if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ownBuilding.getLocation()))
            {
                List<ItemStack> l = new CopyOnWriteArrayList<>();
                l.addAll(job.getItemsNeeded());

                for (ItemStack e : l)
                {
                    if(isStackTool(e))
                    {
                        if(hasAllTheTools() || isInHut(ownBuilding, e.getItem()))
                        {
                            job.removeItemNeeded(e);
                            return;
                        }
                    }
                    else if(e.getItem().equals(new ItemStack(Blocks.torch).getItem()) || e.getItem().equals(Items.coal))
                    {
                        int slot = inventoryContains(Items.coal);
                        if(slot!=-1)
                        {
                            worker.getInventory().decrStackSize(slot, 1);
                            ItemStack stack = new ItemStack(e.getItem(), 4);
                            InventoryUtils.addItemStackToInventory(worker.getInventory(),stack);
                            job.removeItemNeeded(e);
                            return;
                        }
                        else if(isInHut(ownBuilding, Items.coal) || isInHut(ownBuilding, Blocks.torch))
                        {
                            return;
                        }
                        else if(inventoryContains(Blocks.torch)!=-1)
                        {
                            job.removeItemNeeded(e);
                            return;
                        }
                    }
                    else if (isInHut(ownBuilding, e.getItem()) || inventoryContains(e.getItem())!=-1)
                    {
                        if(e.getItem().equals(new ItemStack(ownBuilding.floorBlock).getItem()))
                        {
                            if((job.getStage() == Stage.MINING_SHAFT && inventoryContainsMany(e.getItem())>=64) || (job.getStage() == Stage.MINING_NODE && inventoryContainsMany(e.getItem())>=30))
                            {
                                job.removeItemNeeded(e);
                                return;
                            }
                            worker.sendLocalizedChat("entity.miner.messageMoreBlocks", e.getDisplayName());
                        }
                        else
                        {
                            job.removeItemNeeded(e);
                            return;
                        }
                    }
                    worker.sendLocalizedChat("entity.miner.messageNeedBlockAndItem", e.getDisplayName());
                }
                delay = 50;
            }
        }
        else {
            switch (job.getStage())
            {
                case MINING_NODE:
                    if(ownBuilding.levels!=null)
                    {
                        if(ownBuilding.startingLevelNode == 5)
                        {
                            if(canMineNode < 1)
                            {
                                //12 fences == 29 planks + 1 Torch  -> 3 Nodes
                                if (inventoryContainsMany(ownBuilding.floorBlock) >= 30 && (inventoryContains(Items.coal) != -1 || inventoryContainsMany(Blocks.torch) >= 3))
                                {
                                    canMineNode = 3;
                                }
                                else
                                {
                                    if (inventoryContains(Items.coal) == -1 && inventoryContainsMany(Blocks.torch) < 3)
                                    {
                                        job.addItemNeeded(new ItemStack(Items.coal));
                                    }
                                    else
                                    {
                                        job.addItemNeeded(new ItemStack(ownBuilding.floorBlock));
                                    }
                                }
                            }
                            else
                            {
                                mineNode(ownBuilding);
                            }
                        }
                        else
                        {
                            mineNode(ownBuilding);
                        }
                    }
                    else
                    {
                        createShaft(ownBuilding, ownBuilding.vectorX, ownBuilding.vectorZ);
                    }
                    break;
                case INVENTORY_FULL:
                    dumpInventory(ownBuilding);
                    break;
                case SEARCHING_LADDER:
                    findLadder(ownBuilding);
                    break;
                case MINING_VEIN:
                    mineVein(ownBuilding);
                    break;
                case FILL_VEIN:
                    fillVein();
                    break;
                case MINING_SHAFT:
                    createShaft(ownBuilding, ownBuilding.vectorX, ownBuilding.vectorZ);
                    break;
                case WORKING:
                    if (!ownBuilding.foundLadder)
                    {
                        job.setStage(Stage.SEARCHING_LADDER);
                    }
                    else if(ownBuilding.activeNode != null)
                    {
                        job.setStage(Stage.MINING_NODE);
                    }
                    else if (!ownBuilding.clearedShaft)
                    {
                        job.setStage(Stage.MINING_SHAFT);
                    }
                    else
                    {
                        job.setStage(Stage.MINING_NODE);
                    }
                    break;
            }
        }
    }

    private int unsignVector(int i)
    {
        if(i == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    private void mineNode(BuildingMiner b)
    {
        if(b.levels.size() <= currentLevel)
        {
            if(b.clearedShaft)
            {
                currentLevel = b.currentLevel = b.levels.size()-1;
                return;
            }

            if(currentLevel != b.currentLevel)
            {
                currentLevel = b.currentLevel;
            }

            b.activeNode = null;
            job.setStage(Stage.MINING_SHAFT);
            return;
        }

        if (inventoryContainsMany(b.floorBlock)<= 10 )
        {
            canMineNode = 0;
            job.addItemNeeded(new ItemStack(b.floorBlock));
            return;
        }
        else if(inventoryContainsMany(Blocks.torch)<3 && inventoryContains(Items.coal)==-1)
        {
            canMineNode = 0;
            job.addItemNeeded(new ItemStack(Items.coal));
            return;
        }

        if(b.levels.get(currentLevel).getNodes().size() == 0)
        {
            b.currentLevel++;
            currentLevel++;

            if(currentLevel >= b.levels.size())
            {
                b.currentLevel = 0;
                currentLevel = 0;
            }
            return;
        }

        int depth = b.levels.get(currentLevel).getDepth();

        if(b.activeNode == null || b.activeNode.getStatus() == Node.Status.COMPLETED || b.activeNode.getStatus() == Node.Status.AVAILABLE)
        {
            currentLevel = b.currentLevel;
            if(b.levels.get(currentLevel).getNodes().size() == 0)
            {
                b.currentLevel++;
                currentLevel++;

                if(currentLevel >= b.levels.size())
                {
                    b.currentLevel = 0;
                    currentLevel = 0;
                }
                return;
            }

            int rand1 = (int) Math.floor(Math.random()*4);
            int randomNum;

            if(b.levels.get(currentLevel).getNodes() == null)
            {
                if(b.levels.size()<currentLevel+1)
                {
                    b.currentLevel = currentLevel = b.levels.size()-1;
                    b.activeNode = null;
                    job.setStage(Stage.MINING_SHAFT);
                    return;
                }
                else
                {
                    b.currentLevel++;
                    currentLevel++;
                    return;
                }
            }
            else if(rand1 == 1)
            {
                randomNum = (int) Math.floor(Math.random()*b.levels.get(currentLevel).getNodes().size());
            }
            else if(rand1 == 2)
            {
                randomNum = (int) (Math.random() * 3);
            }
            else
            {
                randomNum = b.levels.get(currentLevel).getNodes().size()-1;
            }

            if(b.levels.get(currentLevel).getNodes().size() > randomNum)
            {
                Node node = b.levels.get(currentLevel).getNodes().get(randomNum);

                if (node.getStatus() == Node.Status.AVAILABLE)
                {
                    int x = node.getX();
                    int y = b.levels.get(currentLevel).getDepth();
                    int z = node.getZ();
                    Block block = world.getBlock(x,y,z);

                    if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x+node.getVectorX(),y,z+node.getVectorZ()) || !canWalkOn(x+node.getVectorX(),y,z+node.getVectorZ())))
                    {
                        logger.info("Removed Node because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode.getVectorZ());
                        b.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    if (node.getX() > b.shaftStart.posX + b.getMaxX() || node.getZ() > b.shaftStart.posZ + b.getMaxZ() || node.getX() < b.shaftStart.posX - b.getMaxX() || node.getZ() < b.shaftStart.posZ - b.getMaxZ())
                    {
                        b.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    logger.info("Starting Node: " + randomNum);

                    loc = new ChunkCoordinates(node.getX(), depth, node.getZ());
                    b.activeNode = node;
                    b.active = randomNum;
                    b.activeNode.setStatus(Node.Status.IN_PROGRESS);
                    clearNode = 0;
                    b.startingLevelNode = 0;
                    b.markDirty();
                }
            }
        }
        else if(b.activeNode.getStatus() == Node.Status.IN_PROGRESS)
        {
            if (loc == null)
            {
                loc = new ChunkCoordinates(b.activeNode.getX() + b.startingLevelNode * b.activeNode.getVectorX(), depth, b.activeNode.getZ() + b.startingLevelNode * b.activeNode.getVectorZ());
            }

            if (cachedPathResult != null && !cachedPathResult.isComputing())
            {
                if (!cachedPathResult.getPathReachesDestination())
                {
                    b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
                    b.activeNode.setStatus(Node.Status.COMPLETED);
                    b.levels.get(currentLevel).getNodes().remove(b.active);
                    currentLevel = b.currentLevel;
                    logger.info("Unreachable Node!");
                    b.markDirty();
                }
                cachedPathResult = null;
            }

            if (cachedPathResult == null && worker.getNavigator().noPath())
            {
                if (Utils.isWorkerAtSiteWithMove(worker, loc.posX - b.activeNode.getVectorX(), loc.posY - 1, loc.posZ - b.activeNode.getVectorZ()))
                {
                    int uVX = 0;
                    int uVZ = 0;

                    if (b.startingLevelNode == 5)
                    {
                        b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
                        b.activeNode.setStatus(Node.Status.COMPLETED);
                        b.levels.get(currentLevel).getNodes().remove(b.active);

                        if (b.activeNode.getVectorX() == 0)
                        {
                            if (!world.isAirBlock(b.activeNode.getX() + 2, b.levels.get(currentLevel).getDepth(), b.activeNode.getZ() + 4 * b.activeNode.getVectorZ()))
                            {
                                b.levels.get(currentLevel).addNewNode(b.activeNode.getX() + 2, b.activeNode.getZ() + 4 * b.activeNode.getVectorZ(), unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                            }

                            if (!world.isAirBlock(b.activeNode.getX() - 2, b.levels.get(currentLevel).getDepth(), b.activeNode.getZ() + 4 * b.activeNode.getVectorZ()))
                            {
                                b.levels.get(currentLevel).addNewNode(b.activeNode.getX() - 2, b.activeNode.getZ() + 4 * b.activeNode.getVectorZ(), -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                            }
                        }
                        else
                        {
                            if (!world.isAirBlock(b.activeNode.getX() + 4 * b.activeNode.getVectorX(), b.levels.get(currentLevel).getDepth(), b.activeNode.getZ() + 2))
                            {
                                b.levels.get(currentLevel).addNewNode(b.activeNode.getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getZ() + 2, unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                            }

                            if (!world.isAirBlock(b.activeNode.getX() + 4 * b.activeNode.getVectorX(), b.levels.get(currentLevel).getDepth(), b.activeNode.getZ() - 2))
                            {
                                b.levels.get(currentLevel).addNewNode(b.activeNode.getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getZ() - 2, -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                            }
                        }

                        if (!world.isAirBlock((b.activeNode.getX() + 5 * b.activeNode.getVectorX()), b.levels.get(currentLevel).getDepth(), b.activeNode.getZ() + 5 * b.activeNode.getVectorZ()))
                        {
                            b.levels.get(currentLevel).addNewNode(b.activeNode.getX() + 5 * b.activeNode.getVectorX(), b.activeNode.getZ() + 5 * b.activeNode.getVectorZ(), b.activeNode.getVectorX(), b.activeNode.getVectorZ());
                        }
                        logger.info("Finished Node: " + b.active);
                        currentLevel = b.currentLevel;

                        b.markDirty();
                    }
                    else
                    {
                        if (b.activeNode.getVectorX() == 0)
                        {
                            uVX = 1;
                        }
                        else
                        {
                            uVZ = 1;
                        }

                        switch (clearNode)
                        {
                            case 0:
                                clearNode += mineCarefully(loc.posX, loc.posY + 1, loc.posZ,0,0,true,false,false,b);
                                break;
                            case 1:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ,-uVX,-uVZ,true,false,true,b);
                                break;
                            case 2:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ,uVX,uVZ,true,false,true,b);
                                break;
                            case 3:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY, loc.posZ + uVZ, uVX, uVZ, false, false, true, b);
                                break;
                            case 4:
                                clearNode += mineCarefully(loc.posX, loc.posY, loc.posZ, 0, 0, false, false, false, b);
                                break;
                            case 5:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY, loc.posZ - uVZ, -uVX, -uVZ, false, false, true, b);
                                break;
                            case 6:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY-1, loc.posZ - uVZ, -uVX, -uVZ, false, true, true, b);
                                break;
                            case 7:
                                clearNode += mineCarefully(loc.posX , loc.posY-1, loc.posZ, 0, 0, false, true, false, b);
                                break;
                            case 8:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY-1, loc.posZ + uVZ, uVX, uVZ, false, true, true, b);
                                break;
                            case 9:
                                if (b.startingLevelNode == 2)
                                {
                                    int neededPlanks = 10;
                                    canMineNode -= 1;

                                    world.setBlock(loc.posX, loc.posY + 1, loc.posZ, Blocks.planks);
                                    world.setBlock(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ, Blocks.planks);
                                    world.setBlock(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ, Blocks.planks);
                                    world.setBlock(loc.posX + uVX, loc.posY, loc.posZ + uVZ, Blocks.fence);
                                    world.setBlock(loc.posX - uVX, loc.posY, loc.posZ - uVZ, Blocks.fence);
                                    world.setBlock(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ, Blocks.fence);
                                    world.setBlock(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ, Blocks.fence);

                                    int meta = 0;

                                    if (b.activeNode.getVectorZ() < 0)
                                    {
                                        meta = 3;
                                    }
                                    else if (b.activeNode.getVectorZ() > 0)
                                    {
                                        meta = 4;
                                    }
                                    else if (b.activeNode.getVectorX() < 0)
                                    {
                                        meta = 1;
                                    }
                                    else if (b.activeNode.getVectorX() > 0)
                                    {
                                        meta = 2;
                                    }

                                    while (neededPlanks > 0)
                                    {
                                        int slot = inventoryContains(b.floorBlock);
                                        int size = worker.getInventory().getStackInSlot(slot).stackSize;

                                        if (size > neededPlanks)
                                        {
                                            worker.getInventory().decrStackSize(slot, 10);
                                            neededPlanks = 0;
                                        }
                                        else
                                        {
                                            worker.getInventory().decrStackSize(slot, size);
                                            neededPlanks -= size;
                                        }
                                    }

                                    if (inventoryContainsMany(Blocks.torch) > 0)
                                    {
                                        int slot = inventoryContains(Blocks.torch);
                                        worker.getInventory().decrStackSize(slot, 1);
                                    }
                                    else if (inventoryContainsMany(Items.coal) > 0)
                                    {
                                        int slot = inventoryContains(Items.coal);
                                        worker.getInventory().decrStackSize(slot, 1);
                                    }

                                    world.setBlock(loc.posX - b.activeNode.getVectorX(), loc.posY + 1, loc.posZ - b.activeNode.getVectorZ(), Blocks.torch, meta, 0x3);
                                }
                                b.startingLevelNode += 1;
                                loc.set(loc.posX + b.activeNode.getVectorX(), loc.posY, loc.posZ + b.activeNode.getVectorZ());

                                clearNode = 0;
                                b.markDirty();
                                break;
                        }
                    }
                }
                else
                {
                    cachedPathResult = worker.getNavigator().moveToXYZ(loc.posX - b.activeNode.getVectorX(), loc.posY - 1, loc.posZ - b.activeNode.getVectorZ(), 2.0F);
                }
            }
        }
    }

    private int mineCarefully(int x, int y, int z,int uVX, int uVZ,boolean above, boolean side, boolean under,BuildingMiner b)
    {
        Block block = world.getBlock(x, y, z);

        if(above)
        {
            checkAbove(x, y+1, z);
        }
        if(under)
        {
            checkUnder(x, y-1, z);
        }

        if(side && isALiquid(x + uVX, y, z + uVZ))
        {
            setBlockFromInventory(x + uVX, y, z + uVZ, Blocks.cobblestone);
        }

        if(doMining(b, block, x, y, z))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private boolean isALiquid(int x, int y, int z)
    {
       return world.getBlock(x,y,z).getMaterial().isLiquid();
    }

    private void checkAbove(int x,int y,int z)
    {
        Block blockAbove = world.getBlock(x, y, z);
        isValuable(x,y,z);
        if(blockAbove == Blocks.sand || blockAbove == Blocks.gravel || !canWalkOn(x,y,z))
        {
           setBlockFromInventory(x,y,z,Blocks.cobblestone);
        }
    }

    private void checkUnder(int x,int y,int z)
    {
        isValuable(x,y,z);
        ifNotAirSetBlock(x,y,z,Blocks.cobblestone);
    }

    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getItem().getToolClasses(stack).contains("pickaxe") || stack.getItem().getToolClasses(stack).contains("shovel"));
    }

    private boolean isInHut(BuildingMiner b, Block block)
    {
        if(b.getTileEntity()==null)
        {
            return false;
        }

        int size = b.getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = b.getTileEntity().getStackInSlot(i);
            if(stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if (content.equals(block))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null)
                    {
                        b.getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        b.getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInHut(BuildingMiner b, Item item)
    {
        if(isInHut(b, Blocks.torch) && item == Items.coal)
        {
            return true;
        }

        if(b.getTileEntity()==null)
        {
            return false;
        }

        int size = b.getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = b.getTileEntity().getStackInSlot(i);
            if(stack != null)
            {
                Item content = stack.getItem();
                if(content.equals(item) || content.getToolClasses(stack).contains(NEED_ITEM))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null)
                    {
                        b.getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        b.getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void dumpInventory(BuildingMiner b)
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.getLocation()))
        {
            for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = worker.getInventory().getStackInSlot(i);
                if (stack != null && !isStackTool(stack))
                {
                    if (b.getTileEntity() != null)
                    {
                        ItemStack returnStack = InventoryUtils.setStack(b.getTileEntity(), stack);
                        if (returnStack == null)
                        {
                            worker.getInventory().decrStackSize(i, stack.stackSize);
                        }
                        else
                        {
                            worker.getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }
                    }
                }
            }
            job.setStage(Stage.WORKING);
            blocksMined = 0;
        }
    }

    private void mineVein(BuildingMiner b)
    {
        if(job.vein == null)
        {
            job.setStage(Stage.WORKING);
            return;
        }

        if(job.vein.size() == 0)
        {
            job.vein = null;
            job.veinId = 0;
            job.setStage(Stage.FILL_VEIN);
        }
        else
        {
            if(localVein==null)
            {
                localVein = new ArrayList<>();
            }

            //Can finish Mining vein in every distance at the moment
            ChunkCoordinates nextLoc = job.vein.get(0);
            localVein.add(nextLoc);
            Block block = ChunkCoordUtils.getBlock(world, nextLoc);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;

            if(!doMining(b, block, nextLoc.posX, nextLoc.posY, nextLoc.posZ))
            {
                return;
            }

            job.vein.remove(0);

            ifNotAirSetBlock(x,y-1,z,Blocks.dirt);

            if (world.getBlock(x,y,z) == Blocks.sand || world.getBlock(x,y,z) == Blocks.gravel)
            {
                setBlockFromInventory(x, y + 1, z, Blocks.dirt);
            }

            avoidCloseLiquid(x,y,z);
        }
    }

    private void fillVein()
    {
        if(localVein.size() == 0)
        {
            localVein = null;
            job.setStage(Stage.WORKING);
        }
        else
        {
            ChunkCoordinates nextLoc = localVein.get(0);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;
            localVein.remove(0);

            ifNotAirSetBlock(x,y,z,Blocks.cobblestone);
        }
    }

    private void avoidCloseLiquid(int centerX, int centerY, int centerZ)
    {
        for (int x = centerX - 3; x <= centerX + 3; x++)
        {
            for (int z = centerZ - 3; z <= centerZ + 3; z++)
            {
                for (int y = centerY ; y <= centerY + 3; y++)
                {
                    Block block = world.getBlock(x,y,z);

                    if (block.getMaterial().isLiquid())
                    {
                        setBlockFromInventory(x, y, z, Blocks.dirt);
                    }
                }
            }
        }
    }

    private boolean hasAllTheTools()
    {
        boolean hasPickAxeInHand;
        boolean hasSpadeInHand;

        if (worker.getHeldItem() == null)
        {
            hasPickAxeInHand = false;
            hasSpadeInHand = false;
        }
        else
        {
            hasPickAxeInHand = worker.getHeldItem().getItem().getToolClasses(worker.getHeldItem()).contains("pickaxe");
            hasSpadeInHand = worker.getHeldItem().getItem().getToolClasses(worker.getHeldItem()).contains("shovel");
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel");
        int hasPickAxe = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "pickaxe");

        boolean Spade = hasSpade > -1 || hasSpadeInHand;
        boolean Pickaxe = hasPickAxeInHand || hasPickAxe > -1;

        if (!Spade)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_shovel));
            NEED_ITEM = "shovel";
        }
        else if (!Pickaxe)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_pickaxe));
            NEED_ITEM = "pickaxe";
        }

        if(!Pickaxe || !Spade)
        {
            return false;
        }

        boolean canMine =  false;

        if(canBeMined.contains(hasToMine))
        {
            canMine = true;
        }


        String tool = hasToMine.getHarvestTool(0);
        int level = getMiningLevel(worker.getHeldItem(),tool);
        int required = hasToMine.getHarvestLevel(0);

        if(level > required){
            holdEfficientPickaxe(hasToMine);
        }

        if(!canMine)
        {
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());
        }
        if(!canMine)
        {
            if(hasPickAxeInHand)
            {
                holdShovel();
            }
            else
            {
                if(!holdEfficientPickaxe(hasToMine)){
                    return false;
                }
            }
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());
        }
        return canMine;
    }

    int getMiningLevel(ItemStack stack, String tool){
        if (stack == null || tool == null) return -1;
        return stack.getItem().getHarvestLevel(stack, tool);
    }

    void holdShovel()
    {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel"));
    }

    int getMostEfficientTool(Block target){
        String tool = target.getHarvestTool(0);
        int required = target.getHarvestLevel(0);
        int bestSlot = -1;
        int bestlevel = Integer.MAX_VALUE;
        InventoryCitizen inventory = worker.getInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null && (item.getItem().getToolClasses(item).contains("pickaxe")))
            {
                int level = getMiningLevel(item,tool);
                if(level >= required && level < bestlevel)
                {
                    bestSlot = i;
                    bestlevel = level;
                }
            }
        }
        return bestSlot;
    }

    boolean holdEfficientPickaxe(Block target)
    {
        int bestSlot = getMostEfficientTool(target);
        if(bestSlot >= 0)
        {
            worker.setHeldItem(bestSlot);
            return true;
        }
        return false;
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
    }

    private void findLadder(BuildingMiner b)
    {
        int posX = b.getLocation().posX;
        int posY = b.getLocation().posY + 2;
        int posZ = b.getLocation().posZ;

        for (int x = posX - 10; x < posX + 10; x++)
        {
            for (int z = posZ - 10; z < posZ + 10; z++)
            {
                for (int y = posY - 10; y < posY; y++)
                {
                    if (b.foundLadder)
                    {
                        job.setStage(Stage.MINING_SHAFT);
                        return;
                    }
                    else
                    if (world.getBlock(x, y, z).equals(Blocks.ladder))//Parameters unused
                    {
                        int lastY = getLastLadder(x, y, z);
                        b.ladderLocation = new ChunkCoordinates(x, lastY, z);
                        logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                        delay = 10;

                        if(getLocation == null)
                        {
                            getLocation = new ChunkCoordinates(b.ladderLocation.posX,b.ladderLocation.posY,b.ladderLocation.posZ);
                        }
                        if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation)) {
                            b.cobbleLocation = new ChunkCoordinates(x, lastY, z);

                            if (world.getBlock(b.ladderLocation.posX - 1, b.ladderLocation.posY, b.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x - 1, lastY, z);
                                b.vectorX = 1;
                                b.vectorZ = 0;
                                logger.info("Found cobble - West");
                                //West
                            }
                            else if (world.getBlock(b.ladderLocation.posX + 1, b.ladderLocation.posY, b.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x + 1, lastY, z);
                                b.vectorX = -1;
                                b.vectorZ = 0;
                                logger.info("Found cobble - East");
                                //East
                            }
                            else if (world.getBlock(b.ladderLocation.posX, b.ladderLocation.posY, b.ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x, lastY, z - 1);
                                b.vectorZ = 1;
                                b.vectorX = 0;
                                logger.info("Found cobble - South");
                                //South
                            }
                            else if (world.getBlock(b.ladderLocation.posX, b.ladderLocation.posY, b.ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x, lastY, z + 1);
                                b.vectorZ = -1;
                                b.vectorX = 0;
                                logger.info("Found cobble - North");
                                //North
                            }
                            //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            getLocation = new ChunkCoordinates(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ);
                            b.shaftStart = new ChunkCoordinates(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ);
                            b.foundLadder = true;
                            hasAllTheTools();
                            job.setStage(Stage.WORKING);
                            b.markDirty();
                        }
                    }
                }
            }
        }
    }

    private void createShaft(BuildingMiner b, int vectorX, int vectorZ)
    {
        if(b.clearedShaft)
        {
            job.setStage(Stage.WORKING);
            return;
        }

        if(b.ladderLocation == null)
        {
            job.setStage(Stage.SEARCHING_LADDER);
            return;
        }

        if(getLocation == null)
        {
            getLocation = new ChunkCoordinates(b.ladderLocation.posX,b.ladderLocation.posY-1,b.ladderLocation.posZ);
        }

        int x = getLocation.posX;
        int y = getLocation.posY;
        int z = getLocation.posZ;
        currentY = getLocation.posY;

        if (y <= b.getMaxY())
        {
            b.clearedShaft = true;
            job.setStage(Stage.MINING_NODE);
        }

        //Needs 39+25 Planks + 4 Torches + 14 fence 5 to create floor-structure
        if (b.startingLevelShaft % 5 == 0 && b.startingLevelShaft != 0)
        {
            if (inventoryContainsMany(b.floorBlock) >= 64 && (inventoryContains(Items.coal) != -1 || inventoryContainsMany(Blocks.torch) >= 4))
            {
                if (clear < 50)
                {
                    switch (clear)
                    {
                    case 1:

                        break;
                    case 2:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        break;
                    case 6:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                    case 7:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 8:
                        if (vectorX == 0)
                        {
                            x += 1;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z += 1;
                            x -= 7*vectorX;
                        }
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 9:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        break;
                    case 13:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                    case 14:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 15:
                        if (vectorX == 0)
                        {
                            x += 1;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z += 1;
                            x -= 7*vectorX;
                        }
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 16:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        world.setBlock(x, y + 5, z, Blocks.torch);
                        break;
                    case 17:
                    case 18:
                    case 19:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        break;
                    case 20:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        world.setBlock(x, y + 5, z, Blocks.torch);
                    case 21:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 22:
                        if (vectorX == 0)
                        {
                            x += 1;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z += 1;
                            x -= 7*vectorX;
                        }
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 29:
                        if (vectorX == 0)
                        {
                            x = x - 4;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z = z - 4;
                            x -= 7*vectorX;
                        }
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 30:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 34:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                    case 35:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 36:
                        if (vectorX == 0)
                        {
                            x -= 1;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z -= 1;
                            x -= 7*vectorX;
                        }
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 37:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        world.setBlock(x, y + 5, z, Blocks.torch);
                        break;
                    case 38:
                    case 39:
                    case 40:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        break;
                    case 41:
                        world.setBlock(x, y + 4, z, b.fenceBlock);
                        world.setBlock(x, y + 5, z, Blocks.torch);
                    case 42:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    case 43:
                        if (vectorX == 0)
                        {
                            x -= 1;
                            z -= 7*vectorZ;
                        }
                        else if (vectorZ == 0)
                        {
                            z -= 1;
                            x -= 7*vectorX;
                        }
                    case 44:
                    case 45:
                    case 46:
                    case 47:
                    case 48:
                    case 49:
                        world.setBlock(x, y + 3, z, b.floorBlock);
                        break;
                    }
                    x = x + vectorX;
                    z = z + vectorZ;

                    getLocation.set(x, y, z);
                    clear += 1;
                }
                else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation))
                {
                    while (neededPlanks > 0)
                    {
                        int slot = inventoryContains(b.floorBlock);
                        int size = worker.getInventory().getStackInSlot(slot).stackSize;
                        worker.getInventory().decrStackSize(slot, size);
                        neededPlanks -= size;
                    }

                    if (inventoryContains(Items.coal) != -1)
                    {
                        int slot = inventoryContains(Items.coal);
                        worker.getInventory().decrStackSize(slot, 1);
                    }
                    else if (inventoryContainsMany(Blocks.torch) >= 4)
                    {
                        while (neededTorches > 0)
                        {
                            int slot = inventoryContains(Blocks.torch);
                            int size = worker.getInventory().getStackInSlot(slot).stackSize;

                            if (size > 4)
                            {
                                worker.getInventory().decrStackSize(slot, 4);
                                neededTorches -= 4;
                            }
                            else
                            {
                                worker.getInventory().decrStackSize(slot, size);
                                neededTorches -= size;
                            }
                        }
                    }

                    if (b.levels == null)
                    {
                        b.levels = new ArrayList<>();
                    }
                    if (vectorX == 0)
                    {
                        b.levels.add(new Level(b.shaftStart.posX, y + 5, b.shaftStart.posZ + 3*vectorZ, b));

                    }
                    else if (vectorZ == 0)
                    {
                        b.levels.add(new Level(b.shaftStart.posX + 3*vectorX, y + 5, b.shaftStart.posZ, b));

                    }
                    clear = 1;
                    b.startingLevelShaft++;
                    getLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                    b.markDirty();
                }
            }
            else
            {
                if (inventoryContainsMany(b.floorBlock) >= 64)
                {
                    job.addItemNeeded(new ItemStack(Items.coal));
                }
                else
                {
                    job.addItemNeeded(new ItemStack(b.floorBlock));
                }
            }
        }
        //Mining shaft
        else if (inventoryContains(Blocks.dirt) != -1 && inventoryContains(Blocks.cobblestone) != -1)
        {
            if (clear >= 50)
            {
                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation))
                {
                    b.cobbleLocation.set(b.cobbleLocation.posX, b.ladderLocation.posY - 1, b.cobbleLocation.posZ);
                    b.ladderLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                    clear = 1;
                    b.startingLevelShaft++;
                    b.markDirty();

                    getLocation.set(b.shaftStart.posX, getLocation.posY - 1, b.shaftStart.posZ);
                }
            }
            else if (Utils.isWorkerAtSiteWithMove(worker, x, y, z,3))
            {
                worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());
                hasToMine = world.getBlock(x, y, z);

                if (hasAllTheTools())
                {
                    if (!world.getBlock(x, y, z).isAir(world, x, y, z))
                    {
                        if (!doMining(b, world.getBlock(x, y, z), x, y, z) || job.getStage() != Stage.MINING_SHAFT)
                        {
                            return;
                        }
                    }

                    if (clear < 50)
                    {
                        //Check if Block after End is empty (Block of Dungeons...)
                        if ((clear - 1) % 7 == 0)
                        {
                            isValuable(x - vectorX, y, z - vectorZ);
                            ifNotAirSetBlock(x - vectorX, y, z - vectorZ,Blocks.cobblestone);
                        }
                        else if (clear % 7 == 0)
                        {
                            isValuable(x + vectorX, y, z + vectorZ);
                            ifNotAirSetBlock(x + vectorX, y, z + vectorZ,Blocks.cobblestone);
                        }

                        switch (clear)
                        {
                        case 1:
                            int meta = world.getBlockMetadata(b.ladderLocation.posX, b.ladderLocation.posY + 1, b.ladderLocation.posZ);
                            setBlockFromInventory(b.cobbleLocation.posX, b.ladderLocation.posY - 1, b.cobbleLocation.posZ, Blocks.cobblestone);
                            world.setBlock(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ, Blocks.ladder, meta, 0x3);
                            break;
                        case 7:
                        case 14:
                            if (vectorX == 0)
                            {
                                x += 1;
                                z -= 7*vectorZ;
                            }
                            else if (vectorZ == 0)
                            {
                                z += 1;
                                x -= 7*vectorX;
                            }
                            break;
                        case 21:
                            if (vectorX == 0)
                            {
                                x += 1;
                                z -= 7*vectorZ;
                            }
                            else if (vectorZ == 0)
                            {
                                z += 1;
                                x -= 7*vectorX;
                            }
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                        case 26:
                        case 27:
                            if (vectorX == 0)
                            {
                                isValuable(x + 1, y, z);
                                ifNotAirSetBlock(x+1,y,z,Blocks.cobblestone);
                            }
                            else if (vectorZ == 0)
                            {
                                isValuable(x, y, z + 1);
                                ifNotAirSetBlock(x,y,z+1,Blocks.cobblestone);
                            }
                            break;
                        case 28:
                            if (vectorX == 0)
                            {
                                isValuable(x + 1, y, z);
                                ifNotAirSetBlock(x+1,y,z,Blocks.cobblestone);
                            }
                            else if (vectorZ == 0)
                            {
                                isValuable(x, y, z + 1);
                                ifNotAirSetBlock(x,y,z+1,Blocks.cobblestone);
                            }
                            if (vectorX == 0)
                            {
                                x = x - 4;
                                z -= 7*vectorZ;
                            }
                            else if (vectorZ == 0)
                            {
                                z = z - 4;
                                x -= 7*vectorX;
                            }
                            break;
                        case 35:
                            if (clear == 35)
                            {
                                if (vectorX == 0)
                                {
                                    x -= 1;
                                    z -= 7*vectorZ;
                                }
                                else if (vectorZ == 0)
                                {
                                    z -= 1;
                                    x -= 7*vectorX;
                                }
                            }
                            break;
                        case 42:
                            if (clear == 42)
                            {
                                if (vectorX == 0)
                                {
                                    x -= 1;
                                    z -= 7*vectorZ;
                                }
                                else if (vectorZ == 0)
                                {
                                    z -= 1;
                                    x -= 7*vectorX;
                                }
                            }
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                            if (vectorX == 0)
                            {
                                isValuable(x - 1, y, z);
                                ifNotAirSetBlock(x-1,y,z,Blocks.cobblestone);
                            }
                            else if (vectorZ == 0)
                            {
                                isValuable(x, y, z - 1);
                                ifNotAirSetBlock(x,y,z-1,Blocks.cobblestone);
                            }
                            break;
                        }
                        x = x + vectorX;
                        z = z + vectorZ;

                        ifNotAirSetBlock( x,  y-1, z, Blocks.dirt);

                        getLocation.set(x, y, z);
                        clear += 1;
                    }
                }
            }
        }
        else
        {
            if (inventoryContains(Blocks.cobblestone) == -1)
            {
                job.addItemNeeded(new ItemStack(Blocks.cobblestone));
            }
            else
            {
                job.addItemNeeded(new ItemStack(Blocks.dirt));
            }
        }
    }

    private int getDelay(Block block,int x, int y, int z)
    {
        return (int)(worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0) * block.getBlockHardness(world,x,y,z));
    }


    private void ifNotAirSetBlock(int x, int y, int z, Block block)
    {
        if (!canWalkOn(x, y , z))
        {
            setBlockFromInventory(x, y , z, block);
        }
    }

    private void setBlockFromInventory(int x, int y, int z, Block block)
    {
        world.setBlock(x, y, z, block);
        int slot = inventoryContains(block);

        if(slot == -1)
        {
            if(block == Blocks.torch)
            {
                int slot2 = inventoryContains(Items.coal);
                if (slot2 != -1)
                {
                    worker.getInventory().decrStackSize(slot, 1);
                    ItemStack stack = new ItemStack(block, 4);
                    InventoryUtils.addItemStackToInventory(worker.getInventory(), stack);
                }
            }
            else
            {

                job.addItemNeeded(new ItemStack(block));
                return;
            }
        }
        worker.getInventory().decrStackSize(slot,1);
    }

    private int inventoryContains(Block block)
    {
        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    return slot;
                }
            }
        }

        if(!(inventoryContainsMany(Items.coal)>0) && block == Blocks.torch)
        {
            job.addItemNeededIfNotAlready(new ItemStack(block));
        }
        return -1;
    }

    private int inventoryContains(Item item)
    {
        if(item == null)
        {
            return -1;
        }

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if(content.equals(item))
                {
                    return slot;
                }
            }
        }

        if(!(inventoryContainsMany(Blocks.torch)>0) && item == Items.coal)
        {
            job.addItemNeededIfNotAlready(new ItemStack(item));
        }
        return -1;
    }

    private int inventoryContainsMany(Block block)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    private int inventoryContainsMany(Item item)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Item content =  stack.getItem();
                if(content.equals(item))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    private boolean doMining(BuildingMiner b, Block block, int x, int y, int z)
    {
        if(!hasAllTheTools()){return false;}

        if(job.getStage() == Stage.MINING_NODE && b.activeNode == null)
        {
            return false;
        }

        ChunkCoordinates bk = new ChunkCoordinates(x,y,z);

        if (InventoryUtils.getOpenSlot(worker.getInventory()) == -1)    //inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                               //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return false;
        }
        /*if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()) || !canWalkOn(x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()))) //-164 58 -225
        {
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode.getVectorZ());
            b.levels.get(currentLevel).getNodes().remove(b.active);

            return true;
        }*/

        if(job.getStage() == Stage.MINING_NODE && b.shaftStart.posX == x && b.shaftStart.posZ == z)
        {
            b.activeNode.setStatus(Node.Status.COMPLETED);
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Ladder Node: " + b.active);
            b.levels.get(currentLevel).getNodes().remove(b.active);
            return true;
        }

        if(job.getStage() == Stage.MINING_NODE)
        {
            isValuable(x, y+1, z);
            isValuable(x, y-1, z);
            isValuable(x+b.activeNode.getVectorZ(), y, z+b.activeNode.getVectorX());
            isValuable(x-b.activeNode.getVectorZ(), y, z-b.activeNode.getVectorX());
        }

        if(currentY == 200)
        {
            currentY = y;
        }

        if (block == Blocks.dirt || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.grass)
        {
            holdShovel();
        }
        else
        {
            holdEfficientPickaxe(block);
        }
        hasToMine = block;
        ItemStack tool = worker.getInventory().getHeldItem();
        if (tool == null || !hasAllTheTools())
        {
            return false;
        }
        else if(!ForgeHooks.canToolHarvestBlock(block,0,tool) && !canBeMined.contains(hasToMine))
        {
            hasToMine = block;
            return false;
        }
        else
        {
            avoidCloseLiquid(x,y,z);

            if(!hasDelayed)
            {
                miningBlock = new ChunkCoordinates(x,y,z);
                delay = getDelay(block,x,y,z);
                hasDelayed = true;
                return false;
            }
            miningBlock = null;
            hasDelayed = false;

            tool.getItem().onBlockDestroyed(tool, world, block, x, y, z, worker);//Dangerous
            if (tool.stackSize < 1)//if Tool breaks
            {
                worker.setCurrentItemOrArmor(0, null);
                worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
            }

            Utils.blockBreakSoundAndEffect(world, x, y, z, block, world.getBlockMetadata(x, y, z));

            if(job.vein == null)
            {
                if(!isValuable(x, y, z))
                {
                    int fortune = 0;
                    if(tool.isItemEnchanted())
                    {
                        NBTTagList t = tool.getEnchantmentTagList();

                        for(int i=0;i<t.tagCount();i++)
                        {
                            short id = t.getCompoundTagAt(i).getShort("id");
                            if(id == 35)
                            {
                                fortune = t.getCompoundTagAt(i).getShort("lvl");
                            }
                        }
                    }

                    List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                    for (ItemStack item : items)
                    {
                        InventoryUtils.setStack(worker.getInventory(), item);
                    }

                    world.setBlockToAir(x, y, z);
                    blocksMined+=1;
                }
            }
            else
            {
                int fortune = 0;
                if(tool.isItemEnchanted())
                {
                    NBTTagList t = tool.getEnchantmentTagList();

                    for(int i=0;i<t.tagCount();i++)
                    {
                        short id = t.getCompoundTagAt(0).getShort("id");
                        if(id == 35)
                        {
                            fortune = t.getCompoundTagAt(0).getShort("lvl");
                        }
                    }
                }

                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(worker.getInventory(), item);
                }

                world.setBlockToAir(x, y, z);
                blocksMined+=1;
            }

            if(job.getStage() == Stage.MINING_VEIN && MathHelper.floor_double(worker.getPosition().xCoord) == x && MathHelper.floor_double(worker.getPosition().yCoord) == y+1 && MathHelper.floor_double(worker.getPosition().zCoord) == z)
            {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if((y < currentY && job.getStage() != Stage.MINING_NODE) && job.getStage() != Stage.MINING_VEIN)
            {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if(job.getStage()!=Stage.MINING_VEIN)
            {
                ifNotAirSetBlock(x, y - 1, z, Blocks.cobblestone);
            }
        }
        hasAllTheTools();

        if(blocksMined == 150)
        {
            job.setStage(Stage.INVENTORY_FULL);
        }

        return true;
    }

    private void findVein(int x, int y, int z)
    {
        job.setStage(Stage.MINING_VEIN);

        for (int x1 = x - 1; x1 <= x + 1; x1++)
        {
            for (int z1 = z - 1; z1 <= z + 1; z1++)
            {
                for (int y1 = y - 1; y1 <= y + 1; y1++)
                {
                    if (isValuable(x1, y1, z1))
                    {
                        ChunkCoordinates ore = new ChunkCoordinates(x1, y1, z1);
                        if (!job.vein.contains(ore))
                        {
                            job.vein.add(ore);
                        }
                    }
                }
            }
        }

        if ((job.veinId < job.vein.size()))
        {
            ChunkCoordinates v = job.vein.get(job.veinId++);

            findVein(v.posX, v.posY, v.posZ);
        }
    }

    private int getLastLadder(int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null))//Parameters unused
        {
            return getLastLadder(x, y - 1, z);
        }
        else
        {
            return y + 1;
        }
    }

    private boolean canWalkOn(int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        return block.getMaterial().isSolid() && !block.equals(Blocks.web) && !world.isAirBlock(x,y,z);
    }

    private boolean isValuable(int x, int y, int z)
    {
        Block block = world.getBlock(x,y,z);
        String findOre = block.toString();

        if(job.vein == null && (findOre.contains("ore") || findOre.contains("Ore")))
        {
            job.vein = new ArrayList<ChunkCoordinates>();
            job.vein.add(new ChunkCoordinates(x, y, z));
            logger.info("Found ore");
            findVein(x, y, z);
            logger.info("finished finding ores: " + job.vein.size());

            job.setStage(Stage.MINING_VEIN);
        }

        return findOre.contains("ore") || findOre.contains("Ore");
    }
}