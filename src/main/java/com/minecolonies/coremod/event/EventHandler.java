package com.minecolonies.coremod.event;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.interfaces.IRSComponentBlock;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.VisitorSpawnedEvent;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.commands.EntryPoint;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesChunkCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldColonyManagerCapabilityProvider;
import com.minecolonies.coremod.items.ItemBannerRallyGuards;
import com.minecolonies.coremod.loot.SupplyLoot;
import com.minecolonies.coremod.network.messages.client.OpenSuggestionWindowMessage;
import com.minecolonies.coremod.network.messages.client.UpdateChunkCapabilityMessage;
import com.minecolonies.coremod.network.messages.client.UpdateChunkRangeCapabilityMessage;
import com.minecolonies.coremod.util.ChunkClientDataHelper;
import com.minecolonies.coremod.util.ChunkDataHelper;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_IN_OTHER_DIM;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

/**
 * Handles all forge events.
 */
public class EventHandler
{
    /**
     * Adds our custom loot tables to vanilla tables.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event)
    {
        SupplyLoot.getInstance().addLootToEvent(event);
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event)
    {
        EntryPoint.register(event.getDispatcher());
    }

    /**
     * On Entity join do this.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onEntityAdded(@NotNull final EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isClientSide())
        {
            if (MineColonies.getConfig().getServer().mobAttackCitizens.get() && (event.getEntity() instanceof IMob) && !(event.getEntity() instanceof LlamaEntity)
                  && !(event.getEntity() instanceof EndermanEntity))
            {
                ((MobEntity) event.getEntity()).targetSelector.addGoal(6, new NearestAttackableTargetGoal<>((MobEntity) event.getEntity(), EntityCitizen.class, true));
                ((MobEntity) event.getEntity()).targetSelector.addGoal(7, new NearestAttackableTargetGoal<>((MobEntity) event.getEntity(), EntityMercenary.class, true));
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
        if (mc.options.renderDebug)
        {
            final ClientWorld world = mc.level;
            final ClientPlayerEntity player = mc.player;
            final BlockPos pos = new BlockPos(player.position());
            IColony colony = IColonyManager.getInstance().getIColony(world, pos);
            if (colony == null)
            {
                if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
                {
                    event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.noCloseColony"));
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, pos);

                if (colony == null)
                {
                    return;
                }

                event.getLeft().add(LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.nextColony",
                  (int) Math.sqrt(colony.getDistanceSquared(pos)), IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()));
                return;
            }

            event.getLeft().add(colony.getName() + " : "
                                  + LanguageHandler.format("com.minecolonies.coremod.gui.debugScreen.blocksFromCenter",
              (int) Math.sqrt(colony.getDistanceSquared(pos))));
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
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onChunkLoad(@NotNull final ChunkEvent.Load event)
    {
        if (event.getWorld() instanceof ServerWorld)
        {
            ChunkDataHelper.loadChunk((Chunk) event.getChunk(), (ServerWorld) event.getWorld());
        }
        else if (event.getWorld() instanceof ClientWorld)
        {
            if (event.getChunk() instanceof Chunk)
            {
                ChunkClientDataHelper.applyLate((Chunk) event.getChunk());
            }
        }
    }

    /**
     * Called when a chunk gets unloaded
     *
     * @param event the event.
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
     * Called right before dimension change event, used to remove the player from an existing colony
     *
     * @param event dim travel event.
     */
    @SubscribeEvent(priority = LOWEST)
    public static void onEntityTravelToDimensionEvent(final EntityTravelToDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayerEntity && !event.isCanceled())
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            final Chunk oldChunk = player.level.getChunk(player.xChunk, player.zChunk);
            final IColonyTagCapability oldCloseColonies = oldChunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);

            if (oldCloseColonies != null)
            {
                // Remove visiting/subscriber from old colony
                if (oldCloseColonies.getOwningColony() != 0)
                {
                    final IColony oldColony = IColonyManager.getInstance().getColonyByWorld(oldCloseColonies.getOwningColony(), player.level);
                    if (oldColony != null)
                    {
                        oldColony.removeVisitingPlayer(player);
                        oldColony.getPackageManager().removeCloseSubscriber(player);
                    }
                }
            }
        }
    }

    /**
     * Adds the player to the new colony on dim enter.
     *
     * @param event DimChangedEvent
     */
    @SubscribeEvent
    public static void playerChangeDim(final PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            final Chunk newChunk = player.level.getChunk(player.xChunk, player.zChunk);
            final IColonyTagCapability closeColonyCap = newChunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            if (closeColonyCap != null)
            {
                // Add visiting/subscriber to new colony
                final IColony newColony = IColonyManager.getInstance().getColonyByWorld(closeColonyCap.getOwningColony(), player.level);
                if (newColony != null)
                {
                    newColony.addVisitingPlayer(player);
                    newColony.getPackageManager().addCloseSubscriber(player);
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
    public static void onEnteringChunk(@NotNull final PlayerEvent.EnteringChunk event)
    {
        final Entity entity = event.getEntity();
        final BlockPos pos = new BlockPos(entity.position());

        if (event.getOldChunkX() == 0 && event.getOldChunkZ() == 0 && pos.distSqr(BlockPos.ZERO) > 100 * 100)
        {
            return;
        }

        //  Add nearby players
        if (entity instanceof ServerPlayerEntity)
        {
            final World world = entity.getCommandSenderWorld();

            final Chunk newChunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
            ChunkDataHelper.loadChunk(newChunk, entity.level);

            Network.getNetwork()
              .sendToPlayer(new UpdateChunkRangeCapabilityMessage(world,
                event.getNewChunkX(),
                event.getNewChunkZ(),
                8, true), (ServerPlayerEntity) event.getEntity());

            final IColonyTagCapability newCloseColonies = newChunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            if (newCloseColonies == null)
            {
                return;
            }
            Network.getNetwork().sendToPlayer(new UpdateChunkCapabilityMessage(newCloseColonies, newChunk.getPos().x, newChunk.getPos().z), (ServerPlayerEntity) entity);
            @NotNull final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            final Chunk oldChunk = world.getChunk(event.getOldChunkX(), event.getOldChunkZ());
            final IColonyTagCapability oldCloseColonies = oldChunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            if (oldCloseColonies == null)
            {
                return;
            }
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
            }

            // Add visiting/subscriber to new colony
            if (newCloseColonies.getOwningColony() != 0)
            {
                final IColony newColony = IColonyManager.getInstance().getColonyByWorld(newCloseColonies.getOwningColony(), world);
                if (newColony != null && !newColony.getPackageManager().getCloseSubscribers().contains(player))
                {
                    newColony.addVisitingPlayer(player);
                    newColony.getPackageManager().addCloseSubscriber(player);
                }
            }

            // Alert nearby buildings of close player
            if (newCloseColonies.getOwningColony() != 0)
            {
                for (final Map.Entry<Integer, Set<BlockPos>> entry : newCloseColonies.getAllClaimingBuildings().entrySet())
                {
                    final IColony newColony = IColonyManager.getInstance().getColonyByWorld(entry.getKey(), world);
                    if (newColony != null)
                    {
                        for (final BlockPos buildingPos : entry.getValue())
                        {
                            IBuilding building = newColony.getBuildingManager().getBuilding(buildingPos);
                            if (building != null)
                            {
                                building.onPlayerEnterNearby(player);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Join world event.
     * @param event the join world event.
     */
    @SubscribeEvent
    public static void on(final LivingSpawnEvent.CheckSpawn event)
    {
        if (!(event.getEntity() instanceof IMob))
        {
            return;
        }

        final BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        if (event.isSpawner() || event.getWorld().isClientSide() || !WorldUtil.isEntityBlockLoaded(event.getWorld(), pos))
        {
            return;
        }

        final IColonyTagCapability closeColonyCap = ((World) event.getWorld()).getChunkAt(pos).getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        if (closeColonyCap == null || closeColonyCap.getOwningColony() == 0)
        {
            return;
        }
        final IColony newColony = IColonyManager.getInstance().getColonyByWorld(closeColonyCap.getOwningColony(), (World) event.getWorld());
        if (newColony == null)
        {
            return;
        }

        for (final BlockPos buildingPos : closeColonyCap.getAllClaimingBuildings().getOrDefault(closeColonyCap.getOwningColony(), Collections.emptySet()))
        {
            final IBuilding building = newColony.getBuildingManager().getBuilding(buildingPos);
            if (building != null && building.getBuildingLevel() >= 1 && building.isInBuilding(pos))
            {
                event.setResult(Event.Result.DENY);
                break;
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
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                if (colony.getPermissions().hasPermission(player, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY)
                      || colony.getPermissions().hasPermission(player, Action.RECEIVE_MESSAGES_FAR_AWAY))
                {
                    colony.getPackageManager().addImportantColonyPlayer(player);
                }
            }

            // Add visiting/subscriber to colony we're logging into
            final Chunk chunk = (Chunk) player.level.getChunk(new BlockPos(player.position()));
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            if (cap != null && cap.getOwningColony() != 0)
            {
                IColony colony = IColonyManager.getInstance().getColonyByDimension(cap.getOwningColony(), player.level.dimension());
                if (colony != null)
                {
                    colony.addVisitingPlayer(player);
                    colony.getPackageManager().addCloseSubscriber(player);
                }
            }

            final int size = player.inventory.getContainerSize();
            for (int i = 0; i < size; i++)
            {
                final ItemStack stack = player.inventory.getItem(i);
                if (stack.getItem() instanceof ItemBannerRallyGuards)
                {
                    ItemBannerRallyGuards.broadcastPlayerToRally(stack, player.getLevel(), player);
                }
            }

            IGlobalResearchTree.getInstance().sendGlobalResearchTreePackets((ServerPlayerEntity) event.getPlayer());
            CustomRecipeManager.getInstance().sendCustomRecipeManagerPackets((ServerPlayerEntity) event.getPlayer());
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
        if (MineColonies.getConfig().getServer().pvp_mode.get() && event.getEntity() instanceof EntityCitizen)
        {
            if (event.getEntity().level == null
                  || !WorldUtil.isEntityChunkLoaded(event.getEntity().level, new ChunkPos(event.getNewChunkX(), event.getNewChunkZ()))
                  || !WorldUtil.isEntityChunkLoaded(event.getEntity().level, new ChunkPos(event.getOldChunkX(), event.getOldChunkZ())))
            {
                return;
            }

            final EntityCitizen entityCitizen = (EntityCitizen) event.getEntity();
            if (entityCitizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            {
                final World world = entityCitizen.getCommandSenderWorld();

                final Chunk chunk = world.getChunk(event.getNewChunkX(), event.getNewChunkZ());
                final IColonyTagCapability chunkCapability = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
                if (chunkCapability != null && chunkCapability.getOwningColony() != 0
                      && entityCitizen.getCitizenColonyHandler().getColonyId() != chunkCapability.getOwningColony())
                {
                    final IColony colony = IColonyManager.getInstance().getColonyByWorld(chunkCapability.getOwningColony(), entityCitizen.level);
                    if (colony != null)
                    {
                        colony.addGuardToAttackers(entityCitizen, ((IGuardBuilding) entityCitizen.getCitizenColonyHandler().getWorkBuilding()).getPlayerToFollowOrRally());
                    }
                }
            }
        }
    }

    /**
     * Event called on player block breaks.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onBlockBreak(@NotNull final BlockEvent.BreakEvent event)
    {
        if (!(event.getWorld() instanceof World))
            return;

        final World world = (World) event.getWorld();

        if (event.getState().getBlock() instanceof SpawnerBlock)
        {
            final TileEntity spawner = event.getWorld().getBlockEntity(event.getPos());
            if (spawner instanceof MobSpawnerTileEntity)
            {
                final IColony colony = IColonyManager.getInstance()
                                         .getColonyByDimension(((MobSpawnerTileEntity) spawner).getSpawner().nextSpawnData.getTag().getInt(TAG_COLONY_ID),
                                           world.dimension());
                if (colony != null)
                {
                    colony.getEventManager().onTileEntityBreak(((MobSpawnerTileEntity) spawner).getSpawner().nextSpawnData.getTag().getInt(TAG_EVENT_ID), spawner);
                }
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
        else if ("pmardle".equalsIgnoreCase(event.getPlayer().getName().getString())
                   && Block.byItem(event.getItemStack().getItem()) instanceof SilverfishBlock)
        {
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "Stop that you twat!!!");
            event.setCanceled(true);
        }

        if (world.getBlockState(event.getPos()).getBlock().isBed(world.getBlockState(event.getPos()), world, event.getPos(), player))
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, bedBlockPos);
            //Checks to see if player tries to sleep in a bed belonging to a Citizen, ancels the event, and Notifies Player that bed is occuppied
            if (colony != null && world.getBlockState(event.getPos()).hasProperty(BedBlock.PART))
            {
                final List<ICitizenData> citizenList = colony.getCitizenManager().getCitizens();
                final BlockState potentialBed = world.getBlockState(event.getPos());
                if (potentialBed.getBlock() instanceof BedBlock && potentialBed.getValue(BedBlock.PART) == BedPart.FOOT)
                {
                    bedBlockPos = bedBlockPos.relative(world.getBlockState(event.getPos()).getValue(BedBlock.FACING));
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
        if (!event.isCanceled() && event.getEntity() instanceof PlayerEntity && event.getItemStack().getItem() instanceof BlockItem)
        {
            final Block block = ((BlockItem) event.getItemStack().getItem()).getBlock().getBlock();
            if (block instanceof AbstractBlockHut && !(block instanceof IRSComponentBlock))
            {
                final IColony colony = IColonyManager.getInstance().getIColony(world, event.getPos());
                if (colony != null && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                    return;
                }

                if (MineColonies.getConfig().getServer().suggestBuildToolPlacement.get())
                {
                    final ItemStack stack = event.getItemStack();
                    if (!stack.isEmpty() && !world.isClientSide)
                    {
                        Network.getNetwork()
                          .sendToPlayer(new OpenSuggestionWindowMessage(block.defaultBlockState().setValue(AbstractBlockHut.FACING,
                            event.getPlayer().getDirection()), event.getPos().relative(event.getFace()), stack), (ServerPlayerEntity) player);
                    }
                    event.setCanceled(true);
                }
                return;
            }
        }

        if (!event.isCanceled() && event.getItemStack().getItem() == ModItems.buildTool.get())
        {
            if (event.getWorld().isClientSide())
            {
                if (event.getUseBlock() == Event.Result.DEFAULT && event.getFace() != null)
                {
                    final IColonyView view = IColonyManager.getInstance().getClosestColonyView(event.getWorld(), event.getPos().relative(event.getFace()));
                    if (view != null && Settings.instance.getStyle().isEmpty())
                    {
                        Settings.instance.setStyle(view.getStyle());
                    }
                    MineColonies.proxy.openBuildToolWindow(event.getPos().relative(event.getFace()));
                }
            }
            event.setCanceled(true);
        }
    }

    /**
     * Event when a player right-clicks with a build tool.
     * @param event   {@link PlayerInteractEvent.RightClickItem}
     */
    @SubscribeEvent
    public static void onPlayerInteract(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        if (!event.isCanceled() && event.getItemStack().getItem() == ModItems.buildTool.get() && event.getWorld().isClientSide)
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
        return !player.isShiftKeyDown() || player.getMainHandItem() == null || player.getMainHandItem().getItem() == null
                 || player.getMainHandItem().getItem().doesSneakBypassUse(player.getMainHandItem(), world, pos, player);
    }

    /**
     * Handles the cancellation of a certain event.
     *
     * @param event  the event.
     * @param player the player causing it.
     */
    private static void handleEventCancellation(@NotNull final PlayerInteractEvent event, @NotNull final PlayerEntity player)
    {
        final Block heldBlock = Block.byItem(event.getItemStack().getItem());
        if (heldBlock instanceof AbstractBlockHut || heldBlock instanceof BlockScarecrow)
        {
            if (event.getWorld().isClientSide())
            {
                event.setCanceled(MineColonies.getConfig().getServer().suggestBuildToolPlacement.get());
            }
            else
            {
                event.setCanceled(!onBlockHutPlaced(event.getWorld(), player, heldBlock, event.getPos().relative(event.getFace())));
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
        if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(world))
        {
            LanguageHandler.sendPlayerMessage(player, CANT_PLACE_COLONY_IN_OTHER_DIM);
            return false;
        }

        return onBlockHutPlaced(world, player, pos, block);
    }

    private static boolean onBlockHutPlaced(final World world, @NotNull final PlayerEntity player, final BlockPos pos, final Block block)
    {
        final IColony colony = IColonyManager.getInstance().getIColony(world, pos);

        if (colony == null)
        {
            if (block instanceof BlockHutTownHall)
            {
                return true;
            }

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
            return player.isCreative() || colony.getBuildingManager().canPlaceAt(block, pos, player);
        }
    }

    /**
     * Gets called when world loads. Calls {@link ColonyManager#onWorldLoad(World)}
     *
     * @param event {@link net.minecraftforge.event.world.WorldEvent.Load}
     */
    @SubscribeEvent(priority = HIGHEST)
    public static void onWorldLoad(@NotNull final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldLoad((World) event.getWorld());
        }

        // Global events
        // Halloween ghost mode
        if (event.getWorld().isClientSide() && MineColonies.getConfig().getServer().holidayFeatures.get() &&
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
        if (!event.getWorld().isClientSide() && event.getWorld() instanceof World)
        {
            IColonyManager.getInstance().onWorldUnload((World) event.getWorld());
        }
        if (event.getWorld().isClientSide())
        {
            IColonyManager.getInstance().resetColonyViews();
            Log.getLogger().info("Removed all colony views");
        }
    }

    /**
     * Gets called when a Hoglin, Pig, Piglin, Villager, or ZombieVillager gets converted to something else.
     * 
     * @param event the event to handle.
     */
    @SubscribeEvent
    public static void onEntityConverted(@NotNull final LivingConversionEvent.Pre event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ZombieVillagerEntity && event.getOutcome() == EntityType.VILLAGER)
        {
            final World world = entity.getCommandSenderWorld();
            final IColony colony = IColonyManager.getInstance().getIColony(world, entity.blockPosition());
            if (colony != null && colony.hasBuilding("tavern", 1, false))
            {
                event.setCanceled(true);
                if (ForgeEventFactory.canLivingConvert(entity, ModEntities.VISITOR, null))
                {
                    IVisitorData visitorData = (IVisitorData) colony.getVisitorManager().createAndRegisterCivilianData();
                    BlockPos tavernPos = colony.getBuildingManager().getRandomBuilding(b -> !b.getModules(TavernBuildingModule.class).isEmpty());
                    IBuilding tavern = colony.getBuildingManager().getBuilding(tavernPos);

                    visitorData.setHomeBuilding(tavern);
                    visitorData.setBedPos(tavernPos);
                    tavern.getModules(TavernBuildingModule.class).forEach(mod -> mod.getExternalCitizens().add(visitorData.getId()));

                    int recruitLevel = world.random.nextInt(10 * tavern.getBuildingLevel()) + 15;
                    List<com.minecolonies.api.util.Tuple<Item, Integer>> recruitCosts = IColonyManager.getInstance().getCompatibilityManager().getRecruitmentCostsWeights();

                    visitorData.getCitizenSkillHandler().init(recruitLevel);
                    colony.getVisitorManager().spawnOrCreateCivilian(visitorData, world, entity.blockPosition(), false);
                    colony.getEventDescriptionManager().addEventDescription(new VisitorSpawnedEvent(entity.blockPosition(), visitorData.getName()));

                    if (visitorData.getEntity().isPresent())
                    {
                        AbstractEntityCitizen visitorEntity = visitorData.getEntity().get();
                        for(EquipmentSlotType slotType : EquipmentSlotType.values())
                        {
                            ItemStack itemstack = entity.getItemBySlot(slotType);
                            if (slotType.getType() == EquipmentSlotType.Group.ARMOR && !itemstack.isEmpty())
                            {
                                visitorEntity.setItemSlot(slotType, itemstack);
                            }
                        }
                    }

                    if (!entity.isSilent())
                    {
                        world.levelEvent((PlayerEntity) null, 1027, entity.blockPosition(), 0);
                    }

                    entity.remove();
                    Tuple<Item, Integer> cost = recruitCosts.get(world.random.nextInt(recruitCosts.size()));
                    visitorData.setRecruitCosts(new ItemStack(cost.getA(), (int)(recruitLevel * 3.0 / cost.getB())));
                    visitorData.triggerInteraction(new RecruitmentInteraction(new TranslationTextComponent(
                            "com.minecolonies.coremod.gui.chat.recruitstorycured", visitorData.getName().split(" ")[0]), ChatPriority.IMPORTANT));
                }
            }
        }
    }
}
