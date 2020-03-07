package com.minecolonies.coremod.event;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesChunkCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldColonyManagerCapabilityProvider;
import com.minecolonies.coremod.network.messages.OpenSuggestionWindowMessage;
import com.minecolonies.coremod.network.messages.UpdateChunkCapabilityMessage;
import com.minecolonies.coremod.network.messages.UpdateChunkRangeCapabilityMessage;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.TAG_EVENT_ID;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.commands.colonycommands.CommandDeleteColony.COLONY_DELETE_COMMAND;
import static com.minecolonies.coremod.commands.colonycommands.CommandSetAbandoned.COLONY_ABANDON_COMMAND;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

/**
 * Handles all forge events.
 */
public class EventHandler
{
    /**
     * On Entity join do this.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onEntityAdded(@NotNull final EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isRemote)
        {
            if (event.getEntity() instanceof EntityCitizen)
            {
                ((AbstractEntityCitizen) event.getEntity()).getCitizenColonyHandler().updateColonyServer();
            }
            else if (MineColonies.getConfig().getCommon().mobAttackCitizens.get() && (event.getEntity() instanceof IMob) && !(event.getEntity() instanceof LlamaEntity))
            {
                ((MobEntity) event.getEntity()).targetSelector.addGoal(6, new NearestAttackableTargetGoal<>((MobEntity) event.getEntity(), EntityCitizen.class, true));
                ((MobEntity) event.getEntity()).targetSelector.addGoal(7, new NearestAttackableTargetGoal((MobEntity) event.getEntity(), EntityMercenary.class, true));
            }
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed text on the screen, we only need it when f3 is clicked.
     *
     * @param event {@link net.minecraftforge.client.event.RenderGameOverlayEvent.Text}
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onDebugOverlay(final RenderGameOverlayEvent.Text event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.gameSettings.showDebugInfo)
        {
            final ClientWorld world = mc.world;
            final ClientPlayerEntity player = mc.player;
            IColony colony = IColonyManager.getInstance().getIColony(world, player.getPosition());
            if (colony == null)
            {
                if (!IColonyManager.getInstance().isTooCloseToColony(world, player.getPosition()))
                {
                    event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.noCloseColony"));
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, player.getPosition());

                if (colony == null)
                {
                    return;
                }

                event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.nextColony",
                  (int) Math.sqrt(colony.getDistanceSquared(player.getPosition())), IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()));
                return;
            }

            event.getLeft().add(colony.getName() + " : "
                                  + LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.blocksFromCenter",
              (int) Math.sqrt(colony.getDistanceSquared(player.getPosition()))));
        }
    }

    /**
     * Event called to attach capabilities on a chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onAttachingCapabilitiesChunk(@NotNull final AttachCapabilitiesEvent<Chunk> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "closecolony"), new MinecoloniesChunkCapabilityProvider());
    }

    /**
     * Event called to attach capabilities on the world.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onAttachingCapabilitiesWorld(@NotNull final AttachCapabilitiesEvent<World> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "chunkupdate"), new MinecoloniesWorldCapabilityProvider());
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "colonymanager"), new MinecoloniesWorldColonyManagerCapabilityProvider());
    }

    /**
     * Called when a chunk gets loaded for some reason.
     */
    @SubscribeEvent
    public static void onChunkLoad(@NotNull final ChunkEvent.Load event)
    {
        if (event.getWorld() instanceof ServerWorld)
        {
            ChunkDataHelper.loadChunk((Chunk) event.getChunk(), (ServerWorld) event.getWorld());
        }
    }

    /**
     * Called when a chunk gets unloaded
     */
    @SubscribeEvent
    public static void onChunkUnLoad(final ChunkEvent.Unload event)
    {
        if (event.getWorld() instanceof ServerWorld)
        {
            ChunkDataHelper.unloadChunk((Chunk) event.getChunk(), (ServerWorld) event.getWorld());
        }
    }

    /**
     * Event called when the player enters a new chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onEnteringChunk(@NotNull final PlayerEvent.EnteringChunk event)
    {
        final Entity entity = event.getEntity();

        //  Add nearby players
        if (entity instanceof ServerPlayerEntity)
        {
            final World world = entity.getEntityWorld();
            Network.getNetwork()
              .sendToPlayer(new UpdateChunkRangeCapabilityMessage(world,
                event.getNewChunkX(),
                event.getNewChunkZ(),
                MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get()), (ServerPlayerEntity) event.getEntity());

            final Chunk newChunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
            ChunkDataHelper.loadChunk(newChunk, entity.world);

            final IColonyTagCapability newCloseColonies = newChunk.getCapability(CLOSE_COLONY_CAP, null).orElse(null);

            Network.getNetwork().sendToPlayer(new UpdateChunkCapabilityMessage(newCloseColonies, newChunk.getPos().x, newChunk.getPos().z), (ServerPlayerEntity) entity);
            @NotNull final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            final Chunk oldChunk = world.getChunk(event.getOldChunkX(), event.getOldChunkZ());
            final IColonyTagCapability oldCloseColonies = oldChunk.getCapability(CLOSE_COLONY_CAP, null).orElse(null);

            // Check if we get into a differently claimed chunk
            if (newCloseColonies.getOwningColony() != oldCloseColonies.getOwningColony())
            {
                // Remove visiting/subscriber from old colony
                final IColony oldColony = IColonyManager.getInstance().getColonyByWorld(oldCloseColonies.getOwningColony(), world);
                if (oldColony != null)
                {
                    oldColony.removeVisitingPlayer(player);
                    oldColony.getPackageManager().removeCloseSubscriber(player);
                }

                // Add visiting/subscriber to new colony
                final IColony newColony = IColonyManager.getInstance().getColonyByWorld(newCloseColonies.getOwningColony(), world);
                if (newColony != null)
                {
                    newColony.addVisitingPlayer(player);
                    newColony.getPackageManager().addCloseSubscriber(player);
                }
            }
        }
    }

    /**
     * Event called when a player enters the world.
     *
     * @param event player enter world event
     */
    @SubscribeEvent
    public static void onPlayerEnterWorld(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                if (colony.getPermissions().hasPermission(player, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY)
                      || colony.getPermissions().hasPermission(player, Action.RECEIVE_MESSAGES_FAR_AWAY))
                {
                    colony.getPackageManager().addImportantColonyPlayer(player);
                }
            }

            // Add visiting/subscriber to colony we're logging into
            final Chunk chunk = (Chunk) player.world.getChunk(player.getPosition());
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElse(null);
            if (cap != null && cap.getOwningColony() != 0)
            {
                IColony colony = IColonyManager.getInstance().getColonyByDimension(cap.getOwningColony(), player.dimension.getId());
                if (colony != null)
                {
                    colony.addVisitingPlayer(player);
                    colony.getPackageManager().addCloseSubscriber(player);
                }
            }
        }
    }

    /**
     * Event called when a player leaves the world.
     *
     * @param event player leaves world event
     */
    @SubscribeEvent
    public static void onPlayerLeaveWorld(final PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                colony.getPackageManager().removeCloseSubscriber(player);
                colony.getPackageManager().removeImportantColonyPlayer(player);
            }
        }
    }

    /**
     * Event called when the player enters a new chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onEnteringChunkEntity(@NotNull final EntityEvent.EnteringChunk event)
    {
        if (MineColonies.getConfig().getCommon().pvp_mode.get() && event.getEntity() instanceof EntityCitizen)
        {
            if (event.getEntity().world == null
                  || !event.getEntity().world.chunkExists(event.getNewChunkX(), event.getNewChunkZ())
                  || !event.getEntity().world.chunkExists(event.getOldChunkX(), event.getOldChunkZ()))
            {
                return;
            }

            final EntityCitizen entityCitizen = (EntityCitizen) event.getEntity();
            if (entityCitizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            {
                final World world = entityCitizen.getEntityWorld();

                final Chunk chunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
                final IColonyTagCapability chunkCapability = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                if (chunkCapability != null && chunkCapability.getOwningColony() != 0
                      && entityCitizen.getCitizenColonyHandler().getColonyId() != chunkCapability.getOwningColony())
                {
                    final IColony colony = IColonyManager.getInstance().getColonyByWorld(chunkCapability.getOwningColony(), entityCitizen.world);
                    if (colony != null)
                    {
                        colony.addGuardToAttackers(entityCitizen, ((IGuardBuilding) entityCitizen.getCitizenColonyHandler().getWorkBuilding()).getFollowPlayer());
                    }
                }
            }
        }
    }

    /**
     * Event called on player block breaks.
     *
     * @param event
     */
    @SubscribeEvent
    public static void onBlockBreak(@NotNull final BlockEvent.BreakEvent event)
    {
        if (event.getState().getBlock() instanceof SpawnerBlock)
        {
            final MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) event.getWorld().getTileEntity(event.getPos());

            final IColony colony = IColonyManager.getInstance()
                                     .getColonyByDimension(spawner.getSpawnerBaseLogic().spawnData.getNbt().getInt(TAG_COLONY_ID),
                                       event.getWorld().getDimension().getType().getId());
            if (colony != null)
            {
                colony.getEventManager().onTileEntityBreak(spawner.getSpawnerBaseLogic().spawnData.getNbt().getInt(TAG_EVENT_ID), spawner);
            }
        }
    }

    /**
     * Event when a player right clicks a block, or right clicks with an item. Event gets cancelled when player has no permission. Event gets cancelled when the player has no
     * permission to place a hut, and tried it.
     *
     * @param event {@link PlayerInteractEvent.RightClickBlock}
     */
    @SubscribeEvent
    public static void onPlayerInteract(@NotNull final PlayerInteractEvent.RightClickBlock event)
    {
        final PlayerEntity player = event.getPlayer();
        final World world = event.getWorld();
        BlockPos bedBlockPos = event.getPos();

        // this was the simple way of doing it, minecraft calls onBlockActivated
        // and uses that return value, but I didn't want to call it twice
        if (playerRightClickInteract(player, world, event.getPos()) && world.getBlockState(event.getPos()).getBlock() instanceof AbstractBlockHut)
        {
            final IColony colony = IColonyManager.getInstance().getIColony(world, event.getPos());
            if (colony != null
                  && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                event.setCanceled(true);
            }

            return;
        }
        else if ("pmardle".equalsIgnoreCase(event.getPlayer().getName().getFormattedText())
                   && Block.getBlockFromItem(event.getItemStack().getItem()) instanceof SilverfishBlock)
        {
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "Stop that you twat!!!");
            event.setCanceled(true);
        }

        if (world.getBlockState(event.getPos()).getBlock().isBed(world.getBlockState(event.getPos()), world, event.getPos(), player))
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, bedBlockPos);
            //Checks to see if player tries to sleep in a bed belonging to a Citizen, ancels the event, and Notifies Player that bed is occuppied
            if (colony != null && world.getBlockState(event.getPos()).getProperties().contains(BedBlock.PART))
            {
                final List<ICitizenData> citizenList = colony.getCitizenManager().getCitizens();
                if (world.getBlockState(event.getPos()).isBedFoot(world, event.getPos()))
                {
                    bedBlockPos = bedBlockPos.offset(world.getBlockState(event.getPos()).get(BedBlock.HORIZONTAL_FACING));
                }
                //Searches through the nearest Colony's Citizen and sees if the bed belongs to a Citizen, and if the Citizen is asleep

                for (final ICitizenData citizen : citizenList)
                {
                    if (citizen.getBedPos().equals(bedBlockPos) && citizen.isAsleep())
                    {
                        event.setCanceled(true);
                        LanguageHandler.sendPlayerMessage(player, "tile.bed.occupied");
                    }
                }
            }
        }

        handleEventCancellation(event, player);
        if (event.getEntity() instanceof PlayerEntity && event.getItemStack().getItem() instanceof BlockItem)
        {
            final Block block = ((BlockItem) event.getItemStack().getItem()).getBlock().getBlock();
            if (block instanceof AbstractBlockHut && block != ModBlocks.blockPostBox)
            {
                final IColony colony = IColonyManager.getInstance().getIColony(world, event.getPos());
                if (colony != null && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                    return;
                }

                if (MineColonies.getConfig().getCommon().suggestBuildToolPlacement.get())
                {
                    final ItemStack stack = new ItemStack(block);
                    if (!stack.isEmpty() && !world.isRemote)
                    {
                        Network.getNetwork().sendToPlayer(new OpenSuggestionWindowMessage(block.getDefaultState(), event.getPos().up(), stack), (ServerPlayerEntity) player);
                    }
                    event.setCanceled(true);
                }
                return;
            }
        }

        if (event.getHand() == Hand.MAIN_HAND && event.getItemStack().getItem() == ModItems.buildTool)
        {
            if (event.getWorld().isRemote)
            {
                if (event.getUseBlock() == Event.Result.DEFAULT && event.getFace() != null)
                {
                    final IColonyView view = IColonyManager.getInstance().getClosestColonyView(event.getWorld(), event.getPos().offset(event.getFace()));
                    if (view != null && Settings.instance.getStyle().isEmpty())
                    {
                        Settings.instance.setStyle(view.getStyle());
                    }
                    MineColonies.proxy.openBuildToolWindow(event.getPos().offset(event.getFace()));
                }
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        if (event.getHand() == Hand.MAIN_HAND && event.getItemStack().getItem() == ModItems.buildTool && event.getWorld().isRemote)
        {
            final IColonyView view = IColonyManager.getInstance().getClosestColonyView(event.getWorld(), event.getPos());
            if (view != null && Settings.instance.getStyle().isEmpty())
            {
                Settings.instance.setStyle(view.getStyle());
            }
            MineColonies.proxy.openBuildToolWindow(null);
            event.setCanceled(true);
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
    private static boolean playerRightClickInteract(@NotNull final PlayerEntity player, final World world, final BlockPos pos)
    {
        return !player.isShiftKeyDown() || player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() == null
                 || player.getHeldItemMainhand().getItem().doesSneakBypassUse(player.getHeldItemMainhand(), world, pos, player);
    }

    /**
     * Handles the cancellation of a certain event.
     *
     * @param event  the event.
     * @param player the player causing it.
     */
    private static void handleEventCancellation(@NotNull final PlayerInteractEvent event, @NotNull final PlayerEntity player)
    {
        final Block heldBlock =Block.getBlockFromItem( event.getItemStack().getItem() );
        if (heldBlock instanceof AbstractBlockHut || heldBlock instanceof BlockScarecrow)
        {
            if (event.getWorld().isRemote)
            {
                event.setCanceled(MineColonies.getConfig().getCommon().suggestBuildToolPlacement.get());
            }
            else
            {
                event.setCanceled(!onBlockHutPlaced(event.getWorld(), player, heldBlock, event.getPos().offset(event.getFace())));
            }
        }
    }

    /**
     * Called when a player tries to place a AbstractBlockHut. Returns true if successful and false to cancel the block placement.
     *
     * @param world  The world the player is in
     * @param player The player
     * @param block  The block type the player is placing
     * @param pos    The location of the block
     * @return false to cancel the event
     */
    public static boolean onBlockHutPlaced(@NotNull final World world, @NotNull final PlayerEntity player, final Block block, final BlockPos pos)
    {
        if (!MineColonies.getConfig().getCommon().allowOtherDimColonies.get() && world.getDimension().getType().getId() != 0)
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

    protected static boolean onTownHallPlaced(@NotNull final World world, @NotNull final PlayerEntity player, final BlockPos pos)
    {
        IColony colony = IColonyManager.getInstance().getIColonyByOwner(world, player);
        if (colony != null)
        {
            return canOwnerPlaceTownHallHere(world, player, colony, pos);
        }

        if (MineColonies.getConfig().getCommon().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, world.getSpawnPoint()));
            if (spawnDistance < MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get())
            {
                if (!world.isRemote)
                {
                    LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get());
                }
                return false;
            }
            else if (spawnDistance > MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get())
            {
                if (!world.isRemote)
                {
                    LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get());
                }
                return false;
            }
        }

        colony = IColonyManager.getInstance().getClosestIColony(world, pos);
        if (colony == null)
        {
            return true;
        }

        //  Town Halls must be far enough apart
        return canPlayerPlaceTownHallHere(world, player, pos, colony);
    }

    private static boolean onWareHousePlaced(final World world, final PlayerEntity player, final BlockPos pos)
    {
        if (onBlockHutPlaced(world, player, pos))
        {
            final IColony colony = IColonyManager.getInstance().getClosestIColony(world, pos);
            if (colony != null && (!MineColonies.getConfig().getCommon().limitToOneWareHousePerColony.get() || !colony.hasWarehouse()))
            {
                return true;
            }
            LanguageHandler.sendPlayerMessage(player, "tile.blockhut.warehouse.limit");
        }
        return false;
    }

    private static boolean onBlockHutPlaced(final World world, @NotNull final PlayerEntity player, final BlockPos pos)
    {
        final IColony colony = IColonyManager.getInstance().getIColony(world, pos);

        if (colony == null)
        {
            //  Not in a colony
            if (IColonyManager.getInstance().getIColonyByOwner(world, player) == null)
            {
                LanguageHandler.sendPlayerMessage(player, "tile.blockhut.messagenotownhall");
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "tile.blockhut.messagetoofarfromtownhall");
            }
            return false;
        }
        else if (!colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            //  No permission to place hut in colony
            LanguageHandler.sendPlayerMessage(player, "tile.blockhut.messagenopermission", colony.getName());
            return false;
        }
        else
        {
            return true;
        }
    }

    private static boolean canOwnerPlaceTownHallHere(final World world, @NotNull final PlayerEntity player, @NotNull final IColony colony, final BlockPos pos)
    {
        final IColony currentColony = IColonyManager.getInstance().getIColony(world, pos);
        if (currentColony != null && currentColony != colony)
        {
            if (!world.isRemote)
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.permission.no"));
            }
            return false;
        }

        if (!colony.isCoordInColony(world, pos) && (!MineColonies.getConfig().getCommon().enableDynamicColonySizes.get() || colony.hasTownHall()))
        {
            if (!world.isRemote)
            {
                final ITextComponent deleteButton = new TranslationTextComponent("tile.blockhuttownhall.deletemessagelink")
                                                      .setStyle(new Style()
                                                                  .setBold(true)
                                                                  .setColor(TextFormatting.GOLD)
                                                                  .setClickEvent(
                                                                    new ClickEvent(
                                                                      ClickEvent.Action.RUN_COMMAND,
                                                                      String.format(COLONY_DELETE_COMMAND, colony.getID(), false))));

                if (MineColonies.getConfig().getCommon().allowInfiniteColonies.get())
                {
                    player.sendMessage(new TranslationTextComponent("tile.blockhuttownhall.messageplacedalreadyinfi"));

                    final ITextComponent abandonButton = new TranslationTextComponent("tile.blockhuttownhall.abandonmessagelink")
                                                           .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD)
                                                                       .setClickEvent(new ClickEvent(
                                                                         ClickEvent.Action.RUN_COMMAND,
                                                                         String.format(COLONY_ABANDON_COMMAND, colony.getID()))));


                    player.sendMessage(abandonButton);
                }
                else
                {
                    player.sendMessage(new TranslationTextComponent("tile.blockhuttownhall.messageplacedalreadydel"));
                }
                player.sendMessage(deleteButton);
            }

            //  Players are currently only allowed a single colony
            return false;
        }
        else if (colony.hasTownHall())
        {
            if (!world.isRemote)
            {
                player.sendMessage(new TranslationTextComponent("tile.blockhuttownhall.messageplacedalready"));
            }
            return false;
        }

        return true;
    }

    private static boolean canPlayerPlaceTownHallHere(@NotNull final World world, @NotNull final PlayerEntity player, final BlockPos pos, @NotNull final IColony closestColony)
    {
        // Is the player trying to place a town hall in a colony
        if (closestColony.isCoordInColony(world, pos))
        {
            if (closestColony.hasTownHall() || !closestColony.getPermissions().isColonyMember(player))
            {
                if (!world.isRemote)
                {
                    Log.getLogger().info("Can't place at: " + pos.getX() + "." + pos.getY() + "." + pos.getZ() + ". Because of townhall of: " + closestColony.getName() + " at "
                                           + closestColony.getCenter().getX() + "." + closestColony.getCenter().getY() + "." + closestColony.getCenter().getZ());
                    //Placing in a colony which already has a town hall
                    LanguageHandler.sendPlayerMessage(player, "block.blockhuttownhall.messagetooclose");
                }
                return false;
            }

            if (!closestColony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
            {
                if (!world.isRemote)
                {
                    //  No permission to place hut in colony
                    LanguageHandler.sendPlayerMessage(player, "block.blockHut.messagenopermissionplace", closestColony.getName());
                }
                return false;
            }

            return true;
        }

        if (IColonyManager.getInstance().isTooCloseToColony(world, pos))
        {
            Log.getLogger().info("Can't place at: " + pos.getX() + "." + pos.getY() + "." + pos.getZ() + ". Because of townhall of: " + closestColony.getName() + " at "
                                   + closestColony.getCenter().getX() + "." + closestColony.getCenter().getY() + "." + closestColony.getCenter().getZ());
            //Placing too close to an existing colony
            LanguageHandler.sendPlayerMessage(player, "block.blockhuttownhall.messagetooclose");
            return false;
        }


        if (!world.isRemote
              && MineColonies.getConfig().getCommon().protectVillages.get()
              && ((ServerChunkProvider) world.getChunkProvider())
                   .getChunkGenerator()
                   .findNearestStructure(world, "Village", pos, MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get() * BLOCKS_PER_CHUNK, false) != null)
        {
            Log.getLogger().warn("Village close by!");
            LanguageHandler.sendPlayerMessage(player,
              "block.blockhuttownhall.messagetooclosetovillage");
            return false;
        }
        return true;
    }

    /**
     * Gets called when world loads. Calls {@link ColonyManager#onWorldLoad(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Load}
     */
    @SubscribeEvent(priority = LOWEST)
    public static void onWorldLoad(@NotNull final WorldEvent.Load event)
    {
        Log.getLogger().warn("World load");
        if (event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldLoad((World) event.getWorld());
        }

        // Global events
        // Halloween ghost mode
        if (event.getWorld().isRemote() && MineColonies.getConfig().getCommon().holidayFeatures.get() &&
              (LocalDateTime.now().getDayOfMonth() == 31 && LocalDateTime.now().getMonth() == Month.OCTOBER
                 || LocalDateTime.now().getDayOfMonth() == 1 && LocalDateTime.now().getMonth() == Month.NOVEMBER
                 || LocalDateTime.now().getDayOfMonth() == 2 && LocalDateTime.now().getMonth() == Month.NOVEMBER))
        {
            RenderBipedCitizen.isItGhostTime = true;
        }
    }

    /**
     * Gets called when world unloads. Calls {@link ColonyManager#onWorldUnload(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Unload}
     */
    @SubscribeEvent
    public static void onWorldUnload(@NotNull final WorldEvent.Unload event)
    {
        Log.getLogger().warn("World unload");

        if (event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldUnload((World) event.getWorld());
        }
    }
}
