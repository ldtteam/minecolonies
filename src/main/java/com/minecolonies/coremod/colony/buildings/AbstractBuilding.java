package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.registry.BuildingRegistry;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.ProviderHandler;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.RequestHandler;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.ResolverHandler;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.util.ColonyUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.NO_WORK_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;

/**
 * Base building class, has all the foundation for what a building stores and does.
 * <p>
 * We suppress the warning which warns you about referencing child classes in the parent because that's how we register the instances of the childClasses
 * to their views and blocks.
 */
@SuppressWarnings("squid:S2390")
public abstract class AbstractBuilding extends AbstractBuildingContainer implements IRequestResolverProvider, IRequester
{
    /**
     * The data store id for request system related data.
     */
    @NotNull
    private IToken<?> rsDataStoreToken;

    /**
     * The ID of the building. Needed in the request system to identify it.
     */
    private IRequester requester;

    /**
     * Is being gathered right now
     */
    private boolean beingGathered = false;

    /**
     * If the building has been built already.
     */
    private boolean isBuilt = false;

    /**
     * The custom name of the building, empty by default.
     */
    private String customName = "";

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected AbstractBuilding(@NotNull final Colony colony, final BlockPos pos)
    {
        super(pos, colony);

        this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        setupRsDataStore();
    }

    /**
     * Getter for the custom name of a building.
     * @return the custom name.
     */
    @NotNull
    public String getCustomBuildingName()
    {
        return this.customName;
    }

    /**
     * Executed when a new day start.
     */
    public void onWakeUp()
    {
        /*
         * Buildings override this if required.
         */
    }

    /**
     * Executed every time when citizen finish inventory cleanup called after citizen got paused.
     * Use for cleaning a state only.
     */
    public void onCleanUp(final CitizenData citizen)
    {
        // Cancel all open requests
        //Why is this next line here!!!?!?!?!?!?!?
        //getOpenRequestsOfBuilding(citizen).forEach(r -> colony.getRequestManager().updateRequestState(r.getToken(), RequestState.CANCELLED));

        /*
         * Buildings override this if required.
         */
    }

    /**
     * Executed when RestartCitizenMessage is called and worker is paused.
     * Use for reseting, onCleanUp is called before this
     */
    public void onRestart(final CitizenData citizen)
    {
        // Unpause citizen
        citizen.setPaused(false);
        /*
         * Buildings override this if required.
         */
    }

    public void onPlacement()
    {
        if (Configurations.gameplay.enableDynamicColonySizes)
        {
            ChunkDataHelper.claimColonyChunks(colony.getWorld(), true, colony.getID(), getLocation(), colony.getDimension(), getClaimRadius());
        }
    }

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    public boolean isMatchingBlock(@NotNull final Block block)
    {
        final Class<?> c = BuildingRegistry.getBlockClassToBuildingClassMap().get(block.getClass());
        return getClass().equals(c);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        loadRequestSystemFromNBT(compound);
        if (compound.hasKey(TAG_IS_BUILT))
        {
            isBuilt = compound.getBoolean(TAG_IS_BUILT);
        }
        else if (getBuildingLevel() > 0)
        {
            isBuilt = true;
        }
        if (compound.hasKey(TAG_CUSTOM_NAME))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        writeRequestSystemToNBT(compound);
        compound.setBoolean(TAG_IS_BUILT, isBuilt);
        compound.setString(TAG_CUSTOM_NAME, customName);
    }

    /**
     * Destroys the block.
     * Calls {@link #onDestroyed()}.
     */
    public final void destroy()
    {
        onDestroyed();
        colony.getBuildingManager().removeBuilding(this, colony.getPackageManager().getSubscribers());
    }

    @Override
    public void onDestroyed()
    {
        final TileEntityColonyBuilding tileEntityNew = this.getTileEntity();
        final World world = colony.getWorld();
        final Block block = world.getBlockState(this.getLocation()).getBlock();

        if (tileEntityNew != null)
        {
            InventoryHelper.dropInventoryItems(world, this.getLocation(), (IInventory) tileEntityNew);
            world.updateComparatorOutputLevel(this.getLocation(), block);
        }

        if (Configurations.gameplay.enableDynamicColonySizes)
        {
            ChunkDataHelper.claimColonyChunks(world, false, colony.getID(), this.getID(), colony.getDimension(), getClaimRadius());
        }
        ConstructionTapeHelper.removeConstructionTape(getCorners(), world);
    }

    /**
     * Ticks once a second(once per 20 ticks) for calculations which do not need to be checked each tick.
     */
    public void secondsWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        // Empty, override to use
    }

    /**
     * Adds work orders to the {@link Colony#getWorkManager()}.
     *
     * @param level Desired level.
     * @param builder the assigned builder.
     */
    protected void requestWorkOrder(final int level, final BlockPos builder)
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return;
            }
        }

        final WorkOrderBuildBuilding workOrderBuildBuilding = new WorkOrderBuildBuilding(this, level);
        if (!canBeBuiltByBuilder(level) && !workOrderBuildBuilding.canBeResolved(colony, level))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageBuilderNecessary", Integer.toString(level));
            return;
        }

        if (workOrderBuildBuilding.tooFarFromAnyBuilder(colony, level) && builder.equals(BlockPos.ORIGIN))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageBuildersTooFar");
            return;
        }
        
        if(getLocation().getY() + getHeight() >= 256)
        {
        	LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
        	  "entity.builder.messageBuildTooHigh");
            return;
        }
        else if(getLocation().getY() <= 1)
        {
        	LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
        	  "entity.builder.messageBuildTooLow");
            return;
        }

        if (!builder.equals(BlockPos.ORIGIN))
        {
             final AbstractBuilding building =  colony.getBuildingManager().getBuilding(builder);
             if (building instanceof AbstractBuildingStructureBuilder && (building.getBuildingLevel() >= level || canBeBuiltByBuilder(level)))
             {
                 workOrderBuildBuilding.setClaimedBy(builder);
             }
             else
             {
                 LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
                   "entity.builder.messageBuilderNecessary", Integer.toString(level));
                 return;
             }
        }

        colony.getWorkManager().addWorkOrder(workOrderBuildBuilding, false);
        colony.getProgressManager().progressWorkOrderPlacement(workOrderBuildBuilding);

        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        markDirty();
    }

    /**
     * Method to define if a builder can build this although the builder is not level 1 yet.
     *
     * @return true if so.
     */
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return false;
    }

    @Override
    public final void markDirty()
    {
        super.markDirty();
        if (colony != null)
        {
            colony.getBuildingManager().markBuildingsDirty();
        }
    }

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    public boolean hasWorkOrder()
    {
        return getCurrentWorkOrderLevel() != NO_WORK_ORDER;
    }

    /**
     * Get the current level of the work order.
     *
     * @return NO_WORK_ORDER if not current work otherwise the level requested.
     */
    private int getCurrentWorkOrderLevel()
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return o.getUpgradeLevel();
            }
        }

        return NO_WORK_ORDER;
    }

    /**
     * Remove the work order for the building.
     * <p>
     * Remove either the upgrade or repair work order
     */
    public void removeWorkOrder()
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                colony.getWorkManager().removeWorkOrder(o.getID());
                markDirty();

                final BlockPos buildingPos = o.getClaimedBy();
                final AbstractBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
                if (building != null && building.getMainCitizen() != null)
                {
                    building.cancelAllRequestsOfCitizen(building.getMainCitizen());
                }
                return;
            }
        }
    }

    /**
     * Method to calculate the radius to be claimed by this building depending on the level.
     * @return the radius.
     */
    public int getClaimRadius()
    {
        switch(getBuildingLevel())
        {
            case 3:
                return 1;
            case 5:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Serializes to view.
     * Sends 3 integers.
     * 1) hashcode of the name of the class.
     * 2) building level.
     * 3) max building level.
     *
     * @param buf ByteBuf to write to.
     */
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.getClass().getName().hashCode());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
        buf.writeInt(getPickUpPriority());
        buf.writeBoolean(isPriorityStatic());
        buf.writeInt(getCurrentWorkOrderLevel());
        ByteBufUtils.writeUTF8String(buf, getStyle());
        ByteBufUtils.writeUTF8String(buf, this.getSchematicName());
        ByteBufUtils.writeUTF8String(buf, this.getCustomBuildingName());

        buf.writeInt(getRotation());
        buf.writeBoolean(isMirrored());
        buf.writeInt(getClaimRadius());

        final NBTTagCompound requestSystemCompound = new NBTTagCompound();
        writeRequestSystemToNBT(requestSystemCompound);

        final ImmutableCollection<IRequestResolver<?>> resolvers = getResolvers();
        buf.writeInt(resolvers.size());
        for (final IRequestResolver<?> resolver : resolvers)
        {
            ByteBufUtils.writeTag(buf, StandardFactoryController.getInstance().serialize(resolver.getRequesterId()));
        }
        ByteBufUtils.writeTag(buf, StandardFactoryController.getInstance().serialize(getRequesterId()));
        ByteBufUtils.writeTag(buf, requestSystemCompound);

    }

    /**
     * Check if a building is being gathered.
     *
     * @return true if so.
     */
    public boolean isBeingGathered()
    {
        return this.beingGathered;
    }

    /**
     * Set the custom building name of the building.
     * @param name the name to set.
     */
    public void setCustomBuildingName(final String name)
    {
        this.customName = name;
        this.markDirty();
    }

    /**
     * Check if the building should be gathered by the dman.
     * @return true if so.
     */
    public boolean canBeGathered()
    {
        return true;
    }

    /**
     * Set if a building is being gathered.
     *
     * @param gathering value to set.
     */
    public void setBeingGathered(final boolean gathering)
    {
        this.beingGathered = gathering;
    }

    /**
     * Requests an upgrade for the current building.
     *
     * @param player the requesting player.
     * @param builder the assigned builder.
     */
    public void requestUpgrade(final EntityPlayer player, final BlockPos builder)
    {
        if (getBuildingLevel() < getMaxBuildingLevel())
        {
            requestWorkOrder(getBuildingLevel() + 1, builder);
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.worker.noUpgrade"));
        }
    }

    /**
     * Requests a repair for the current building.
     * @param builder
     * @param builder the assigned builder.
     */
    public void requestRepair(final BlockPos builder)
    {
        if (getBuildingLevel() > 0)
        {
            requestWorkOrder(getBuildingLevel(), builder);
        }
    }

    /**
     * Check if the building was built already.
     *
     * @return true if so.
     */
    public boolean isBuilt()
    {
        return isBuilt;
    }

    /**
     * Deconstruct the building on destroyed.
     */
    public void deconstruct()
    {
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> tuple = getCorners();
        for (int x = tuple.getFirst().getFirst(); x < tuple.getFirst().getSecond(); x++)
        {
            for (int z = tuple.getSecond().getFirst(); z < tuple.getSecond().getSecond(); z++)
            {
                for (int y = getLocation().getY() - 1; y < getLocation().getY() + this.getHeight(); y++)
                {
                    getColony().getWorld().destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
    }

    /**
     * Called upon completion of an upgrade process.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */

    private int fireWorkCounter = 0;

    public void onUpgradeComplete(final int newLevel)
    {
        if (Configurations.gameplay.enableDynamicColonySizes)
        {
            ChunkDataHelper.claimColonyChunks(colony.getWorld(), true, colony.getID(), this.getID(), colony.getDimension(), getClaimRadius());
        }
        ConstructionTapeHelper.removeConstructionTape(getCorners(), colony.getWorld());
        colony.getProgressManager().progressBuildBuilding(this,
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building instanceof AbstractBuildingWorker).mapToInt(AbstractSchematicProvider::getBuildingLevel).sum(),
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building instanceof BuildingHome).mapToInt(AbstractSchematicProvider::getBuildingLevel).sum()
        );
        final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(this, newLevel);
        final Structure wrapper = new Structure(colony.getWorld(), workOrder.getStructureName(), new PlacementSettings());
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(this.getLocation(),
          colony.getWorld(),
          wrapper,
          workOrder.getRotation(colony.getWorld()),
          workOrder.isMirrored());
        this.setHeight(wrapper.getHeight());
        this.setCorners(corners.getFirst().getFirst(), corners.getFirst().getSecond(), corners.getSecond().getFirst(), corners.getSecond().getSecond());

        // firework spawning possibly maybe

        //ItemStack items = new ItemStack(f);



        fireWorkCounter++;

        if (fireWorkCounter % 2 == 1) {
            AxisAlignedBB realaabb = getTargetableArea(colony.getWorld());
            final EntityFireworkRocket firework = new EntityFireworkRocket(colony.getWorld(), realaabb.maxX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack());

            colony.getWorld().spawnEntity(firework);
            final EntityFireworkRocket fireworka = new EntityFireworkRocket(colony.getWorld(), realaabb.maxX, realaabb.maxY, realaabb.minZ, genFireworkItemStack());

            colony.getWorld().spawnEntity(fireworka);
            final EntityFireworkRocket fireworkb = new EntityFireworkRocket(colony.getWorld(), realaabb.minX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack());

            colony.getWorld().spawnEntity(fireworkb);
            final EntityFireworkRocket fireworkc = new EntityFireworkRocket(colony.getWorld(), realaabb.minX, realaabb.maxY, realaabb.minZ, genFireworkItemStack());

            colony.getWorld().spawnEntity(fireworkc);
            System.out.println("POSITIONS: " + fireworkc.posX + ", " + fireworkc.posY + ", " + fireworkc.posZ);

        }




        //System.out.println("HEY THIS IS THE LOCATION:" + corners.getFirst().getFirst() + " " + corners.getFirst().getSecond() + " " + corners.getSecond().getFirst() + " " + corners.getSecond().getSecond());
        //Minecraft.getMinecraft().player.playSound(SoundEvents.ENTITY_FIREWORK_LAUNCH, 1.0F, 1.0F);

        this.isBuilt = true;
    }
    //------------------------- Starting Required Tools/Item handling -------------------------//
    private ItemStack genFireworkItemStack() {


        final Random rand = new Random();

        final ItemStack fireworkItem = new ItemStack(new ItemFirework());
        final NBTTagCompound itemStackCompound = fireworkItem.getTagCompound() != null ? fireworkItem.getTagCompound() : new NBTTagCompound();
        final NBTTagCompound fireworksCompound = new NBTTagCompound();
        final NBTTagList explosionsTagList = new NBTTagList();

        final NBTTagCompound explosionTag = new NBTTagCompound();

        explosionTag.setBoolean("Flicker", rand.nextInt(1) == 0);
        explosionTag.setBoolean("Trail", rand.nextInt(1) == 0);
        explosionTag.setInteger("Type", rand.nextInt(3) + 1);

        final int numberOfColours = rand.nextInt(5) + 1;
        final int[] colors = new int[numberOfColours];

        for (int i = 0; i < numberOfColours; i++)
        {
            colors[i] = ItemDye.DYE_COLORS[rand.nextInt(15)];
        }

        explosionTag.setIntArray("Colors", colors);
        explosionsTagList.appendTag(explosionTag);

        fireworksCompound.setTag("Explosions", explosionsTagList);
        itemStackCompound.setTag("Fireworks", fireworksCompound);

        fireworkItem.setTagCompound(itemStackCompound);
        return fireworkItem;

    }
    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @return the amount which can get dumped or 0 if not.
     */
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        for (final Map.Entry<Predicate<ItemStack>, Tuple<Integer, Boolean>> entry : getRequiredItemsAndAmount().entrySet())
        {
            if (inventory && !entry.getValue().getSecond())
            {
                continue;
            }

            if (entry.getKey().test(stack))
            {
                final ItemStorage kept = ItemStorage.getItemStackOfListMatchingPredicate(localAlreadyKept, entry.getKey());
                final int toKeep = entry.getValue().getFirst();
                int rest = stack.getCount() - toKeep;
                if (kept != null)
                {
                    if (kept.getAmount() >= toKeep)
                    {
                        return stack.getCount();
                    }

                    rest = kept.getAmount() + stack.getCount() - toKeep;

                    localAlreadyKept.remove(kept);
                    kept.setAmount(kept.getAmount() + ItemStackUtils.getSize(stack) - Math.max(0, rest));
                    localAlreadyKept.add(kept);
                }
                else
                {
                    final ItemStorage newStorage = new ItemStorage(stack);
                    newStorage.setAmount(ItemStackUtils.getSize(stack) - Math.max(0, rest));
                    localAlreadyKept.add(newStorage);
                }

                if (rest <= 0)
                {
                    return 0;
                }

                return Math.min(rest, ItemStackUtils.getSize(stack));
            }
        }
        return stack.getCount();
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(keepX);
        final IRequestManager manager = colony.getRequestManager();
        toKeep.put(stack -> this.getOpenRequestsByCitizen().values().stream()
                              .anyMatch(list -> list.stream().filter(token -> manager.getRequestForToken(token) != null)
                                                  .anyMatch(token -> manager.getRequestForToken(token).getRequest() instanceof IDeliverable
                                                                       && ((IDeliverable) manager.getRequestForToken(token).getRequest()).matches(stack))),
          new Tuple<>(Integer.MAX_VALUE, true));

        return toKeep;
    }

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Nullable
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        if (getTileEntity() == null)
        {
            for (final BlockPos pos : containerList)
            {
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    return forceItemStackToProvider(tempTileEntity, stack);
                }
            }
        }
        else
        {
            return forceItemStackToProvider(getTileEntity(), stack);
        }
        return stack;
    }

    @Nullable
    private ItemStack forceItemStackToProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack itemStack)
    {
        final List<ItemStorage> localAlreadyKept = new ArrayList<>();
        return InventoryUtils.forceItemStackToProvider(provider,
          itemStack,
          (ItemStack stack) -> EntityAIWorkDeliveryman.workerRequiresItem(this, stack, localAlreadyKept) != stack.getCount());
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    //------------------------- !START! RequestSystem handling for minecolonies buildings -------------------------//

    protected void writeRequestSystemToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_RS_BUILDING_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
    }

    protected void setupRsDataStore()
    {
        this.rsDataStoreToken = colony.getRequestManager()
                                  .getDataStoreManager()
                                  .get(
                                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                                    TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE
                                  )
                                  .getId();
    }

    private void loadRequestSystemFromNBT(final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.hasKey(TAG_RS_BUILDING_DATASTORE))
        {
            this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_RS_BUILDING_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
    }

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    private Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return getDataStore().getOpenRequestsByRequestableType();
    }

    protected Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return getDataStore().getOpenRequestsByCitizen();
    }

    private Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen()
    {
        return getDataStore().getCompletedRequestsByCitizen();
    }

    private Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return getDataStore().getCitizensByRequest();
    }

    /**
     * Create a request for a citizen.
     *
     * @param citizenData the data of the citizen.
     * @param requested   the request to create.
     * @param async       if async or not.
     * @param <R>         the type of the request.
     * @return the Token of the request.
     */
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final CitizenData citizenData, @NotNull final R requested, final boolean async)
    {
        final IToken requestToken = colony.getRequestManager().createRequest(requester, requested);
        if (async)
        {
            citizenData.getJob().getAsyncRequests().add(requestToken);
        }
        addRequestToMaps(citizenData.getId(), requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Create a request for the building.
     *
     * @param requested   the request to create.
     * @param async       if async or not.
     * @param <R>         the type of the request.
     * @return the Token of the request.
     */
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final R requested, final boolean async)
    {
        final IToken requestToken = colony.getRequestManager().createRequest(requester, requested);
        addRequestToMaps(-1, requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Internal method used to register a new Request to the request maps.
     * Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(@NotNull final Integer citizenId, @NotNull final IToken requestToken, @NotNull final TypeToken requested)
    {
        if (!getOpenRequestsByRequestableType().containsKey(requested))
        {
            getOpenRequestsByRequestableType().put(requested, new ArrayList<>());
        }
        getOpenRequestsByRequestableType().get(requested).add(requestToken);

        getCitizensByRequest().put(requestToken, citizenId);

        if (!getOpenRequestsByCitizen().containsKey(citizenId))
        {
            getOpenRequestsByCitizen().put(citizenId, new ArrayList<>());
        }
        getOpenRequestsByCitizen().get(citizenId).add(requestToken);
    }

    public boolean hasWorkerOpenRequests(@NotNull final CitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getOpenRequests(@NotNull final CitizenData data)
    {
        if (!getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getOpenRequestsByCitizen().get(data.getId())
                                      .stream()
                                      .map(getColony().getRequestManager()::getRequestForToken)
                                      .filter(Objects::nonNull)
                                      .iterator());
    }

    @SuppressWarnings(RAWTYPES)
    public boolean hasWorkerOpenRequestsFiltered(@NotNull final CitizenData citizen, @NotNull final Predicate<IRequest> selectionPredicate)
    {
        return getOpenRequests(citizen).stream().anyMatch(selectionPredicate);
    }

    public <R> boolean hasWorkerOpenRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenData, requestType).isEmpty();
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    public boolean hasCitizenCompletedRequests(@NotNull final CitizenData data)
    {
        return !getCompletedRequests(data).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getCompletedRequests(@NotNull final CitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getCompletedRequestsByCitizen().get(data.getId()).stream()
                                      .map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    public void markRequestAsAccepted(@NotNull final CitizenData data, @NotNull final IToken<?> token)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()) || !getCompletedRequestsByCitizen().get(data.getId()).contains(token))
        {
            throw new IllegalArgumentException("The given token " + token + " is not known as a completed request waiting for acceptance by the citizen.");
        }

        getCompletedRequestsByCitizen().get(data.getId()).remove(token);
        if (getCompletedRequestsByCitizen().get(data.getId()).isEmpty())
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        getColony().getRequestManager().updateRequestState(token, RequestState.RECEIVED);
        markDirty();
    }

    public void cancelAllRequestsOfCitizen(@NotNull final CitizenData data)
    {
        getOpenRequests(data).forEach(request ->
        {
            getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.CANCELLED);

            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getToken());
                if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
                }
            }

            getCitizensByRequest().remove(request.getToken());
        });

        getCompletedRequests(data).forEach(request -> getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.RECEIVED));

        if (getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            getOpenRequestsByCitizen().remove(data.getId());
        }

        if (getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        markDirty();
    }

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here
     * out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    public void overruleNextOpenRequestWithStack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        final Set<Integer> citizenIdsWithRequests = getOpenRequestsByCitizen().keySet();

        if (citizenIdsWithRequests.isEmpty())
        {
            final Collection<IRequestResolver<?>> resolvers = getResolvers();

            for (final IRequestResolver<?> resolver :
              resolvers)
            {
                final List<IRequest<? extends IDeliverable>> deliverableRequests =
                  RequestHandler.getRequestsMadeByRequester((IStandardRequestManager) getColony().getRequestManager(), resolver)
                    .stream()
                    .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                    .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                    .collect(Collectors.toList());

                final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(deliverableRequests, stack);

                if (target == null)
                {
                    continue;
                }

                getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
                return;
            }

            return;
        }

        for (final int citizenId : citizenIdsWithRequests)
        {
            final CitizenData data = getColony().getCitizenManager().getCitizen(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data, TypeConstants.DELIVERABLE), stack);

            if (target == null)
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
            return;
        }
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    public boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull final CitizenData citizenData, @NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData, TypeConstants.DELIVERABLE), stack);

        if (target == null)
        {
            if (citizenData.getJob() instanceof AbstractJobCrafter)
            {
                final AbstractJobCrafter crafterJob = citizenData.getJob(AbstractJobCrafter.class);

                if (!crafterJob.getAssignedTasks().isEmpty())
                {
                    final IRequest<? extends IDeliverable> deliverableChildRequest = crafterJob.getAssignedTasks()
                                                                                       .stream()
                                                                                       .map(getColony().getRequestManager()::getRequestForToken)
                                                                                       .map(IRequest::getChildren)
                                                                                       .flatMap(Collection::stream)
                                                                                       .map(getColony().getRequestManager()::getRequestForToken)
                                                                                       .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                                                                                       .filter(iRequest -> ((IRequest<? extends IDeliverable>) iRequest).getRequest()
                                                                                                             .matches(stack))
                                                                                       .findFirst()
                                                                                       .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                                                                                       .orElse(null);

                    if (deliverableChildRequest != null)
                    {
                        deliverableChildRequest.overrideCurrentDeliveries(ImmutableList.of(stack));
                        getColony().getRequestManager().overruleRequest(deliverableChildRequest.getToken(), stack.copy());
                        return true;
                    }
                }
            }

            return false;
        }

        target.overrideCurrentDeliveries(ImmutableList.of(stack));
        getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
        return true;
    }

    private IRequest<? extends IDeliverable> getFirstOverullingRequestFromInputList(
      @NotNull final Collection<IRequest<? extends IDeliverable>> queue,
      @NotNull final ItemStack stack)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        List<IToken<?>> validRequesterTokens = Lists.newArrayList();
        validRequesterTokens.add(this.getRequesterId());
        this.getResolvers().forEach(iRequestResolver -> validRequesterTokens.add(iRequestResolver.getRequesterId()));

        return queue
                 .stream()
                 .filter(request -> validRequesterTokens.contains(request.getRequester().getRequesterId()) && request.getRequest().matches(stack))
                 .findFirst()
                 .orElse(null);
    }

    private Collection<IRequest<? extends IDeliverable>> flattenDeliverableChildRequests(@NotNull final IRequest<? extends IDeliverable> request)
    {
        if (!request.hasChildren())
        {
            return ImmutableList.of();
        }

        return request.getChildren()
                 .stream()
                 .map(getColony().getRequestManager()::getRequestForToken)
                 .filter(Objects::nonNull)
                 .filter(request1 -> request1.getRequest() instanceof IDeliverable)
                 .map(request1 -> (IRequest<? extends IDeliverable>) request1)
                 .collect(Collectors.toList());
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return getToken();
    }

    @Override
    public IToken<?> getToken()
    {
        return requester.getRequesterId();
    }

    @Override
    public final ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        if (this.getColony() != null
              && this.getColony().getRequestManager() != null
              && this.getColony().getRequestManager() instanceof IStandardRequestManager
              && !ProviderHandler.getRegisteredResolvers((IStandardRequestManager) this.getColony().getRequestManager(), this).isEmpty())
        {
            return ImmutableList.copyOf(ProviderHandler.getRegisteredResolvers((IStandardRequestManager) this.getColony().getRequestManager(), this)
                                          .stream()
                                          .map(token -> ResolverHandler.getResolver((IStandardRequestManager) this.getColony().getRequestManager(), token))
                                          .collect(
                                            Collectors.toList()));
        }

        return createResolvers();
    }

    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager().getFactoryController().getNewInstance(
          TypeConstants.ITOKEN)));
    }

    public IRequester getRequester()
    {
        return requester;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return getRequester().getRequesterLocation();
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);

        if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
        }

        if (!getCompletedRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getCompletedRequestsByCitizen().put(citizenThatRequested, new ArrayList<>());
        }
        getCompletedRequestsByCitizen().get(citizenThatRequested).add(token);

        markDirty();
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken token)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        if (requestThatCompleted != null && getOpenRequestsByRequestableType().containsKey(TypeToken.of(requestThatCompleted.getRequest().getClass())))
        {
            getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);
            if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
            {
                getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
            }
        }

        //Check if the citizen did not die.
        if (getColony().getCitizenManager().getCitizen(citizenThatRequested) != null)
        {
            getColony().getCitizenManager().getCitizen(citizenThatRequested).onRequestCancelled(token);
        }
        markDirty();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token))
        {
            return new TextComponentString("<UNKNOWN>");
        }

        final Integer citizenData = getCitizensByRequest().get(token);
        return new TextComponentString(this.getSchematicName() + " " + getColony().getCitizenManager().getCitizen(citizenData).getName());
    }

    public Optional<CitizenData> getCitizenForRequest(@NotNull final IToken token)
    {
        if (!getCitizensByRequest().containsKey(token) || getColony() == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if (getColony().getCitizenManager().getCitizen(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(getColony().getCitizenManager().getCitizen(citizenID));
    }
    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//
}
