package com.minecolonies.coremod.event;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGenerator;
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

import java.util.List;

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
     * String to abandon a colony.
     */
    private static final String ABANDON_COLONY_CONFIRM_COMMAND_SUGGESTED = "/mc colony ownerchange colony: %d player: [abandoned]";

    /**
     * On Entity join do this.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onEntityAdded(@NotNull final EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityCitizen)
        {
            ((AbstractEntityCitizen) event.getEntity()).getCitizenColonyHandler().updateColonyServer();
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed
     * text on the screen, we only need it when f3 is clicked.
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

            Network.getNetwork().sendToEveryone(new UpdateChunkCapabilityMessage(newCloseColonies, newChunk.getPos().x, newChunk.getPos().z));
            @NotNull final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            final Chunk oldChunk = world.getChunk(event.getOldChunkX(), event.getOldChunkZ());
            final IColonyTagCapability oldCloseColonies = oldChunk.getCapability(CLOSE_COLONY_CAP, null).orElse(null);

            // Add new subscribers to colony.
            for (final int colonyId : newCloseColonies.getAllCloseColonies())
            {
                final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, ((ServerPlayerEntity) entity).getServerWorld());
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
                    final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, ((ServerPlayerEntity) entity).getServerWorld());
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
                    final IColony colony = IColonyManager.getInstance().getColonyByWorld(oldCloseColonies.getOwningColony(), ((ServerPlayerEntity) entity).getServerWorld());
                    if (colony != null)
                    {
                        colony.removeVisitingPlayer(player);
                    }
                    return;
                }

                final IColony colony = IColonyManager.getInstance().getColonyByWorld(newCloseColonies.getOwningColony(), ((ServerPlayerEntity) entity).getServerWorld());
                if (colony != null)
                {
                    colony.addVisitingPlayer(player);
                }
            }
        }
    }

    /**
     * Add an AI task to attack Citizens to mobs
     *
     * @param event Spawnevent
     */
    @SubscribeEvent
    public static void onEvent(final EntityJoinWorldEvent event)
    {
        if (MineColonies.getConfig().getCommon().mobAttackCitizens.get() && (event.getEntity() instanceof MobEntity))
        {
            ((MobEntity) event.getEntity()).goalSelector.addGoal(6, new NearestAttackableTargetGoal<>((MobEntity) event.getEntity(), EntityCitizen.class, true));
            ((MobEntity) event.getEntity()).goalSelector.addGoal(7, new NearestAttackableTargetGoal((MobEntity) event.getEntity(), EntityMercenary.class, true));
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
            if (event.getEntity().world != null && !event.getEntity().world.isBlockLoaded(new BlockPos(event.getNewChunkX() * BLOCKS_PER_CHUNK,
              70,
              event.getNewChunkZ() * BLOCKS_PER_CHUNK)))
            {
                return;
            }
            final EntityCitizen entityCitizen = (EntityCitizen) event.getEntity();
            if (entityCitizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && entityCitizen.getEntityWorld().isBlockLoaded(entityCitizen.getPosition()))
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
     * Event when a block is broken.
     * Event gets cancelled when there no permission to break a hut.
     *
     * @param event {@link net.minecraftforge.event.world.BlockEvent.BreakEvent}
     */
    @SubscribeEvent
    public static void onBlockBreak(@NotNull final BlockEvent.BreakEvent event)
    {
        if (event.getPlayer() != null && event.getPlayer().getHeldItem(Hand.MAIN_HAND).getItem() == ModItems.scanTool)
        {
            final ItemStack itemstack = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
            if (!itemstack.hasTag())
            {
                itemstack.setTag(new CompoundNBT());
            }
            final CompoundNBT compound = itemstack.getTag();

            BlockPosUtil.write(compound, FIRST_POS_STRING, event.getPos());
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "item.scepterSteel.point", event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            itemstack.setTag(compound);

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
    public static void onPlayerInteract(@NotNull final PlayerInteractEvent.RightClickBlock event)
    {
        final PlayerEntity player = event.getPlayer();
        final World world = event.getWorld();
        BlockPos bedBlockPos = event.getPos();

        //Only execute for the main hand our colony events.
        if (event.getHand() == Hand.MAIN_HAND)
        {
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

            if (player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() == null)
            {
                return;
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
    public static void onBlockPlaced(@NotNull final BlockEvent.EntityPlaceEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) event.getEntity();
            final World world = player.world;
            if (event.getPlacedBlock().getBlock() instanceof AbstractBlockHut && event.getPlacedBlock().getBlock() != ModBlocks.blockPostBox)
            {
                final IColony colony = IColonyManager.getInstance().getIColony(world, event.getPos());
                if (colony != null && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                    return;
                }

                if (MineColonies.getConfig().getCommon().suggestBuildToolPlacement.get())
                {
                    final ItemStack stack = new ItemStack(event.getPlacedBlock().getBlock());
                    if (!stack.isEmpty() && !world.isRemote)
                    {
                        Network.getNetwork().sendToPlayer(new OpenSuggestionWindowMessage(event.getPlacedBlock(), event.getPos(), stack), (ServerPlayerEntity) player);
                    }
                    event.setCanceled(true);
                }
            }
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
        return !player.isSneaking() || player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() == null
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
        final Block heldBlock = Block.getBlockFromItem(player.getHeldItemMainhand().getItem());
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
     * Called when a player tries to place a AbstractBlockHut. Returns true if
     * successful and false to cancel the block placement.
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
            LanguageHandler.sendPlayerMessage(player, "tile.blockHut.warehouse.limit");
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
                //todo temporarely disabled because of missing commands
                /*final ITextComponent deleteButton = new TranslationTextComponent("tile.blockHutTownHall.deleteMessageLink")
                                                      .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                          String.format(DELETE_COLONY_CONFIRM_DELETE_COMMAND_SUGGESTED,
                                                            colony.getID(), true
                                                          ))));
                if (MineColonies.getConfig().getCommon().allowInfiniteColonies.get())
                {
                    final ITextComponent abandonButton = new TranslationTextComponent("tile.blockHutTownHall.abandonMessageLink")
                                                           .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD)
                                                                       .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                         String.format(ABANDON_COLONY_CONFIRM_COMMAND_SUGGESTED, colony.getID())))
                                                           );
                    player.sendMessage(new TranslationTextComponent("tile.blockHutTownHall.messagePlacedAlreadyInfi"));
                    player.sendMessage(abandonButton);
                }
                else
                {
                    player.sendMessage(new TranslationTextComponent("tile.blockHutTownHall.messagePlacedAlreadyDel"));
                }
                player.sendMessage(deleteButton);*/
            }

            //  Players are currently only allowed a single colony
            return false;
        }
        else if (colony.hasTownHall())
        {
            if (!world.isRemote)
            {
                player.sendMessage(new TranslationTextComponent("tile.blockHutTownHall.messagePlacedAlready"));
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
                    LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownHall.messageTooClose");
                }
                return false;
            }

            if (!closestColony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
            {
                if (!world.isRemote)
                {
                    //  No permission to place hut in colony
                    LanguageHandler.sendPlayerMessage(player, "tile.blockHut.messageNoPermissionPlace", closestColony.getName());
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
            LanguageHandler.sendPlayerMessage(player, "tile.blockHutTownHall.messageTooClose");
            return false;
        }


        if (!world.isRemote
              && MineColonies.getConfig().getCommon().protectVillages.get()
              && world.getChunkProvider().getChunkGenerator().findNearestStructure(world, "Village", pos, MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get() * BLOCKS_PER_CHUNK, false) != null)
        {
            Log.getLogger().warn("Village close by!");
            LanguageHandler.sendPlayerMessage(player,
              "tile.blockHutTownHall.messageTooCloseToVillage");
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
    public static void onEntityConstructing(@NotNull EntityEvent.EntityConstructing event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            @NotNull PlayerEntity player = (PlayerEntity) event.getEntity();
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
    public static void onLivingDeath(@NotNull LivingDeathEvent event)
    {
        if (!event.getEntity().worldObj.isRemote && event.getEntity() instanceof PlayerEntity)
        {
            PlayerProperties.saveProxyData((PlayerEntity) event.getEntity());
        }
    }*/

    /**
     * Called when an entity joins the world
     * Loads player property data when player enters
     *
     * @param event {@link EntityJoinWorldEvent}
     */
    /*@SubscribeEvent
    public static void onEntityJoinWorld(@NotNull EntityJoinWorldEvent event)
    {
        if (!event.getEntity().worldObj.isRemote && event.getEntity() instanceof PlayerEntity)
        {
            PlayerProperties.loadProxyData((PlayerEntity) event.getEntity());
        }
    }*/

    /**
     * Gets called when world loads.
     * Calls {@link ColonyManager#onWorldLoad(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Load}
     */
    @SubscribeEvent
    public static void onWorldLoad(@NotNull final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldLoad((World) event.getWorld());
        }
    }

    /**
     * Gets called when world unloads.
     * Calls {@link ColonyManager#onWorldUnload(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Unload}
     */
    @SubscribeEvent
    public static void onWorldUnload(@NotNull final WorldEvent.Unload event)
    {
        if (event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldUnload((World) event.getWorld());
        }
    }
}
