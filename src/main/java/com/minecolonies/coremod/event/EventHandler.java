package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.huts.BlockHutField;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesChunkCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.network.messages.UpdateChunkCapabilityMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Handles all forge events.
 */
public class EventHandler
{
    /**
     * Event when the debug screen is opened. Event gets called by displayed
     * text on the screen, we only need it when f3 is clicked.
     *
     * @param event {@link net.minecraftforge.client.event.RenderGameOverlayEvent.Text}
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDebugOverlay(final RenderGameOverlayEvent.Text event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.showDebugInfo)
            {
                final WorldClient world = mc.world;
                final EntityPlayerSP player = mc.player;
                IColony colony = ColonyManager.getIColony(world, player.getPosition());

                if (colony == null)
                {
                    if (!ColonyManager.isTooCloseToColony(world, player.getPosition()))
                    {
                        event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.noCloseColony"));
                        return;
                    }
                    colony = ColonyManager.getClosestIColony(world, player.getPosition());

                    if (colony == null)
                    {
                        return;
                    }

                    event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.nextColony",
                            (int) Math.sqrt(colony.getDistanceSquared(player.getPosition())), ColonyManager.getMinimumDistanceBetweenTownHalls()));
                    return;
                }

                event.getLeft().add(colony.getName() + " : "
                        + LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.blocksFromCenter",
                        (int) Math.sqrt(colony.getDistanceSquared(player.getPosition()))));
            }
        }
    }

    /**
     * Event called to attach capabilities on a chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public void onAttachingCapabilitiesChunk(@NotNull final AttachCapabilitiesEvent<Chunk> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "closeColony"), new MinecoloniesChunkCapabilityProvider());
    }

    /**
     * Event called to attach capabilities on the world.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public void onAttachingCapabilitiesWorld(@NotNull final AttachCapabilitiesEvent<World> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "chunkUpdate"), new MinecoloniesWorldCapabilityProvider());
    }

    /**
     * Called when a chunk gets loaded for some reason.
     */
    @SubscribeEvent
    public void onChunkLoad(@NotNull final ChunkEvent.Load event)
    {
        if (event.getWorld() instanceof WorldServer)
        {
            ColonyManager.loadChunk(event.getChunk(), event.getWorld());
        }
    }

    /**
     * Event called when the player enters a new chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public void onEnteringChunk(@NotNull final PlayerEvent.EnteringChunk event)
    {
        final Entity entity = event.getEntity();

        //  Add nearby players
        if (entity instanceof EntityPlayerMP && entity.dimension == 0)
        {
            final World world = entity.getEntityWorld();
            final Chunk newChunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
            ColonyManager.loadChunk(newChunk, entity.world);

            final IColonyTagCapability newCloseColonies = newChunk.getCapability(CLOSE_COLONY_CAP, null);

            MineColonies.getNetwork().sendToAll(new UpdateChunkCapabilityMessage(newCloseColonies, newChunk.x, newChunk.z));
            @NotNull final EntityPlayerMP player = (EntityPlayerMP) entity;
            final Chunk oldChunk = world.getChunk(event.getOldChunkX(), event.getOldChunkZ());
            final IColonyTagCapability oldCloseColonies = oldChunk.getCapability(CLOSE_COLONY_CAP, null);

            // Add new subscribers to colony.
            for (final int colonyId : newCloseColonies.getAllCloseColonies())
            {
                final Colony colony = ColonyManager.getColony(colonyId);
                if (colony != null)
                {
                    colony.getPackageManager().addSubscribers(player);
                }
            }

            //Remove old subscribers from colony.
            for (final int colonyId : oldCloseColonies.getAllCloseColonies())
            {
                if (!newCloseColonies.getAllCloseColonies().contains(colonyId))
                {
                    final Colony colony = ColonyManager.getColony(colonyId);
                    if (colony != null)
                    {
                        colony.getPackageManager().removeSubscriber(player);
                    }
                }
            }

            if (newCloseColonies.getOwningColony() != oldCloseColonies.getOwningColony())
            {
                if (newCloseColonies.getOwningColony() == 0)
                {
                    final Colony colony = ColonyManager.getColony(oldCloseColonies.getOwningColony());
                    if (colony != null)
                    {
                        colony.removeVisitingPlayer(player);
                    }
                    return;
                }

                final Colony colony = ColonyManager.getColony(newCloseColonies.getOwningColony());
                if (colony != null)
                {
                    colony.addVisitingPlayer(player);
                }
            }
        }
    }


    /**
     * Event called when the player enters a new chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public void onEnteringChunkEntity(@NotNull final EntityEvent.EnteringChunk event)
    {
        if (Configurations.gameplay.pvp_mode && event.getEntity() instanceof EntityCitizen)
        {
            if (event.getEntity().world != null && !event.getEntity().world.isBlockLoaded(new BlockPos(event.getNewChunkX()*BLOCKS_PER_CHUNK, 70, event.getNewChunkZ()*BLOCKS_PER_CHUNK)))
            {
                return;
            }
            final EntityCitizen entityCitizen = (EntityCitizen) event.getEntity();
            if (entityCitizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && entityCitizen.getEntityWorld().isBlockLoaded(entityCitizen.getPosition()))
            {
                final World world = entityCitizen.getEntityWorld();
                final Chunk chunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
                final IColonyTagCapability chunkCapability = chunk.getCapability(CLOSE_COLONY_CAP, null);
                if (chunkCapability != null && chunkCapability.getOwningColony() != 0
                      && entityCitizen.getCitizenColonyHandler().getColonyId() != chunkCapability.getOwningColony())
                {
                    final Colony colony = ColonyManager.getColony(chunkCapability.getOwningColony());
                    if (colony != null)
                    {
                        colony.addGuardToAttackers(entityCitizen, ((AbstractBuildingGuards) entityCitizen.getCitizenColonyHandler().getWorkBuilding()).getFollowPlayer());
                    }
                }
            }
        }
    }

    /**
     * Event when a block is broken.
     * Event gets cancelled when there no permission to break a hut.
     *
     * @param event {@link net.minecraftforge.event.world.BlockEvent.BreakEvent}
     */
    @SubscribeEvent
    public void onBlockBreak(@NotNull final BlockEvent.BreakEvent event)
    {
        if (event.getPlayer() != null && event.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.scanTool)
        {
            final ItemStack itemstack = event.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
            if (!itemstack.hasTagCompound())
            {
                itemstack.setTagCompound(new NBTTagCompound());
            }
            final NBTTagCompound compound = itemstack.getTagCompound();

            BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, event.getPos());
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "item.scepterSteel.point", event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            itemstack.setTagCompound(compound);

            event.setCanceled(true);
        }
    }

    /**
     * Event when a player right clicks a block, or right clicks with an item.
     * Event gets cancelled when player has no permission. Event gets cancelled
     * when the player has no permission to place a hut, and tried it.
     *
     * @param event {@link PlayerInteractEvent.RightClickBlock}
     */
    @SubscribeEvent
    public void onPlayerInteract(@NotNull final PlayerInteractEvent.RightClickBlock event)
    {
        final EntityPlayer player = event.getEntityPlayer();
        final World world = event.getWorld();

        //Only execute for the main hand our colony events.
        if (event.getHand() == EnumHand.MAIN_HAND && !(event.getWorld().isRemote))
        {
            // this was the simple way of doing it, minecraft calls onBlockActivated
            // and uses that return value, but I didn't want to call it twice
            if (playerRightClickInteract(player, world, event.getPos()) && world.getBlockState(event.getPos()).getBlock() instanceof AbstractBlockHut)
            {
                final IColony colony = ColonyManager.getIColony(world, event.getPos());
                if (colony != null
                        && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                }

                return;
            }
            else if ("pmardle".equalsIgnoreCase(event.getEntityPlayer().getName())
                    && Block.getBlockFromItem(event.getItemStack().getItem()) instanceof BlockSilverfish)
            {
                LanguageHandler.sendPlayerMessage(event.getEntityPlayer(), "Stop that you twat!!!");
                event.setCanceled(true);
            }

            if (player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() == null)
            {
                return;
            }

            handleEventCancellation(event, player);
        }
    }

    /**
     * Called when the player makes a right click.
     *
     * @param player the player doing it.
     * @param world  the world he is clicking in.
     * @param pos    the position.
     * @return if should be executed.
     */
    private static boolean playerRightClickInteract(@NotNull final EntityPlayer player, final World world, final BlockPos pos)
    {
        return !player.isSneaking() || player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() == null
                || player.getHeldItemMainhand().getItem().doesSneakBypassUse(player.getHeldItemMainhand(), world, pos, player);
    }

    /**
     * Handles the cancellation of a certain event.
     *
     * @param event  the event.
     * @param player the player causing it.
     */
    private static void handleEventCancellation(@NotNull final PlayerInteractEvent event, @NotNull final EntityPlayer player)
    {
        final Block heldBlock = Block.getBlockFromItem(player.getHeldItemMainhand().getItem());
        if (heldBlock instanceof AbstractBlockHut || heldBlock instanceof BlockHutField)
        {
            event.setCanceled(!onBlockHutPlaced(event.getWorld(), player, heldBlock, event.getPos().offset(event.getFace())));
        }
    }

    /**
     * Called when a player tries to place a AbstractBlockHut. Returns true if
     * successful and false to cancel the block placement.
     *
     * @param world  The world the player is in
     * @param player The player
     * @param block  The block type the player is placing
     * @param pos    The location of the block
     * @return false to cancel the event
     */
    public static boolean onBlockHutPlaced(@NotNull final World world, @NotNull final EntityPlayer player, final Block block, final BlockPos pos)
    {
        if (!Configurations.gameplay.allowOtherDimColonies && world.provider.getDimension() != 0)
        {
            LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_IN_OTHER_DIM);
            return false;
        }

        if (block instanceof BlockHutTownHall)
        {
            return onTownHallPlaced(world, player, pos);
        }
        else if (block instanceof BlockHutWareHouse)
        {
            return onWareHousePlaced(world, player, pos);
        }
        else
        {
            return onBlockHutPlaced(world, player, pos);
        }
    }

    protected static boolean onTownHallPlaced(@NotNull final World world, @NotNull final EntityPlayer player, final BlockPos pos)
    {
        IColony colony = ColonyManager.getIColonyByOwner(world, player);
        if (colony != null)
        {
            return canOwnerPlaceTownHallHere(world, player, colony, pos);
        }

        if (Configurations.gameplay.restrictColonyPlacement)
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, world.getSpawnPoint()));
            if (spawnDistance < Configurations.gameplay.minDistanceFromWorldSpawn)
            {
                LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, Configurations.gameplay.minDistanceFromWorldSpawn);
                return false;
            }
            else if (spawnDistance > Configurations.gameplay.maxDistanceFromWorldSpawn)
            {
                LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, Configurations.gameplay.maxDistanceFromWorldSpawn);
                return false;
            }
        }

        colony = ColonyManager.getClosestIColony(world, pos);
        if (colony == null)
        {
            return true;
        }

        //  Town Halls must be far enough apart
        return canPlayerPlaceTownHallHere(world, player, pos, colony);
    }

    private static boolean onWareHousePlaced(final World world, final EntityPlayer player, final BlockPos pos)
    {
        if (onBlockHutPlaced(world, player, pos))
        {
            final IColony colony = ColonyManager.getClosestIColony(world, pos);
            if (colony != null && (!Configurations.gameplay.limitToOneWareHousePerColony || !colony.hasWarehouse()))
            {
                return true;
            }
            LanguageHandler.sendPlayerMessage(player, "tile.blockHut.warehouse.limit");
        }
        return false;
    }

    private static boolean onBlockHutPlaced(final World world, @NotNull final EntityPlayer player, final BlockPos pos)
    {
        final IColony colony = ColonyManager.getIColony(world, pos);

        if (colony == null)
        {
            //  Not in a colony
            if (ColonyManager.getIColonyByOwner(world, player) == null)
            {
                LanguageHandler.sendPlayerMessage(player, "tile.blockHut.messageNoTownHall");
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "tile.blockHut.messageTooFarFromTownHall");
            }
            return false;
        }
        else if (!colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            //  No permission to place hut in colony
            LanguageHandler.sendPlayerMessage(player, "tile.blockHut.messageNoPermission", colony.getName());
            return false;
        }
        else
        {
            return true;
        }
    }

    private static boolean canOwnerPlaceTownHallHere(final World world, @NotNull final EntityPlayer player, @NotNull final IColony colony, final BlockPos pos)
    {
        if (!colony.isCoordInColony(world, pos) || colony.hasTownHall())
        {
            //  Players are currently only allowed a single colony
            LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownHall.messagePlacedAlready");
            return false;
        }

        final IColony currentColony = ColonyManager.getIColony(world, pos);
        if (currentColony != colony)
        {
            LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownhall.messageTooFar");
            return false;
        }

        return true;
    }

    private static boolean canPlayerPlaceTownHallHere(@NotNull final World world, @NotNull final EntityPlayer player, final BlockPos pos, @NotNull final IColony closestColony)
    {
        // Is the player trying to place a town hall in a colony
        if (closestColony.isCoordInColony(world, pos))
        {
            if (closestColony.hasTownHall() || !closestColony.getPermissions().isColonyMember(player))
            {
                Log.getLogger().info("Can't place at: " + pos.getX() + "." + pos.getY() + "." + pos.getZ() + ". Because of townhall of: " + closestColony.getName() + " at "
                        + closestColony.getCenter().getX() + "." + closestColony.getCenter().getY() + "." + closestColony.getCenter().getZ());
                //Placing in a colony which already has a town hall
                LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownHall.messageTooClose");
                return false;
            }

            if (!closestColony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
            {
                //  No permission to place hut in colony
                LanguageHandler.sendPlayerMessage(player, "tile.blockHut.messageNoPermissionPlace", closestColony.getName());
                return false;
            }

            return true;
        }

        if (ColonyManager.isTooCloseToColony(world, pos))
        {
            Log.getLogger().info("Can't place at: " + pos.getX() + "." + pos.getY() + "." + pos.getZ() + ". Because of townhall of: " + closestColony.getName() + " at "
                    + closestColony.getCenter().getX() + "." + closestColony.getCenter().getY() + "." + closestColony.getCenter().getZ());
            //Placing too close to an existing colony
            LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownHall.messageTooClose");
            return false;
        }

        if (Configurations.gameplay.protectVillages
                && world.getVillageCollection().getNearestVillage(pos, Configurations.gameplay.workingRangeTownHallChunks * BLOCKS_PER_CHUNK) != null)
        {
            Log.getLogger().warn("Village close by!");
            return false;
        }

        return true;
    }

    /**
     * Called when an entity is being constructed
     * Used to register player properties
     *
     * @param event {@link net.minecraftforge.event.getEntity().getEntity()Event.getEntity()Constructing}
     */
    /*@SubscribeEvent
    public void onEntityConstructing(@NotNull EntityEvent.EntityConstructing event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            @NotNull EntityPlayer player = (EntityPlayer) event.getEntity();
            if (PlayerProperties.get(player) == null)
            {
                PlayerProperties.register(player);
            }

        }
    }*/

    /**
     * Called when an entity dies
     * Player property data is saved when a player dies
     *
     * @param event {@link LivingDeathEvent}
     */
    /*@SubscribeEvent
    public void onLivingDeath(@NotNull LivingDeathEvent event)
    {
        if (!event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityPlayer)
        {
            PlayerProperties.saveProxyData((EntityPlayer) event.getEntity());
        }
    }*/

    /**
     * Called when an entity joins the world
     * Loads player property data when player enters
     *
     * @param event {@link EntityJoinWorldEvent}
     */
    /*@SubscribeEvent
    public void onEntityJoinWorld(@NotNull EntityJoinWorldEvent event)
    {
        if (!event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityPlayer)
        {
            PlayerProperties.loadProxyData((EntityPlayer) event.getEntity());
        }
    }*/

    /**
     * Gets called when world loads.
     * Calls {@link ColonyManager#onWorldLoad(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Load}
     */
    @SubscribeEvent
    public void onWorldLoad(@NotNull final WorldEvent.Load event)
    {
        ColonyManager.onWorldLoad(event.getWorld());
    }

    /**
     * Gets called when world unloads.
     * Calls {@link ColonyManager#onWorldUnload(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Unload}
     */
    @SubscribeEvent
    public void onWorldUnload(@NotNull final WorldEvent.Unload event)
    {
        ColonyManager.onWorldUnload(event.getWorld());
    }
}
