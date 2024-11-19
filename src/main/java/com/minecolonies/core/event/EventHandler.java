package com.minecolonies.core.event;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.interfaces.IRSComponentBlock;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.loot.EntityInBiomeTag;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.blocks.BlockScarecrow;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.blocks.huts.BlockHutTownHall;
import com.minecolonies.core.client.render.RenderBipedCitizen;
import com.minecolonies.core.colony.ColonyManager;
import com.minecolonies.core.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.core.colony.eventhooks.citizenEvents.VisitorSpawnedEvent;
import com.minecolonies.core.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobFarmer;
import com.minecolonies.core.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.core.commands.EntryPoint;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.event.capabilityproviders.MinecoloniesChunkCapabilityProvider;
import com.minecolonies.core.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import com.minecolonies.core.event.capabilityproviders.MinecoloniesWorldColonyManagerCapabilityProvider;
import com.minecolonies.core.items.ItemBannerRallyGuards;
import com.minecolonies.core.network.messages.client.OpenSuggestionWindowMessage;
import com.minecolonies.core.network.messages.client.UpdateChunkCapabilityMessage;
import com.minecolonies.core.network.messages.client.UpdateChunkRangeCapabilityMessage;
import com.minecolonies.core.util.ChunkClientDataHelper;
import com.minecolonies.core.util.ChunkDataHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.SOFT_SHOES;
import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_BED_OCCUPIED;
import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

/**
 * Handles all forge events.
 */
public class EventHandler
{
    /**
     * Player position map for watching chunk entries
     */
    private static Map<UUID, ChunkPos> playerPositions = new HashMap<>();

    /**
     * Cache of loot table -> crops.
     */
    private static Map<ResourceLocation, List<MinecoloniesCropBlock>> cropDrops;

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
    @SubscribeEvent(priority = HIGHEST)
    public static void onEntityAdded(@NotNull final EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide())
        {
            if (MineColonies.getConfig().getServer().mobAttackCitizens.get() && event.getEntity() instanceof Mob && event.getEntity() instanceof Enemy && !(event.getEntity()
              .getType()
                .is(ModTags.mobAttackBlacklist))
                && !(event.getEntity() instanceof AbstractFastMinecoloniesEntity))
            {
                ((Mob) event.getEntity()).targetSelector.addGoal(6,
                  new NearestAttackableTargetGoal<>((Mob) event.getEntity(), EntityCitizen.class, true, citizen -> !citizen.isInvisible()));
                ((Mob) event.getEntity()).targetSelector.addGoal(7, new NearestAttackableTargetGoal<>((Mob) event.getEntity(), EntityMercenary.class, true));
            }

            if (event.getEntity() instanceof AbstractFastMinecoloniesEntity && ((ServerLevel) event.getLevel()).getEntity(event.getEntity().getUUID()) != null)
            {
                event.setCanceled(true);
            }

            if (event.getEntity() instanceof EntityCitizen citizen && citizen.getCitizenColonyHandler().getColonyId() == 0)
            {
                Log.getLogger().info("Prevented citizen with colony id 0 from joining world");
                event.setCanceled(true);
            }
        }
    }

    private static void buildCropDrops()
    {
        cropDrops = new HashMap<>();

        for (final MinecoloniesCropBlock crop : ModBlocks.getCrops())
        {
            for (final Block source : crop.getDroppedFrom())
            {
                cropDrops.computeIfAbsent(source.getLootTable(), t -> new ArrayList<>()).add(crop);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(@NotNull final LootTableLoadEvent event)
    {
        if (cropDrops == null)
        {
            buildCropDrops();
        }

        final List<MinecoloniesCropBlock> crops = cropDrops.get(event.getName());
        if (crops != null)
        {
            // grass blocks have a lot of crops (both MineColonies and vanilla) so the base drop chance is reduced
            final float baseChance = event.getName().equals(Blocks.GRASS.getLootTable()) ? 0.001f : 0.01f;
            // hoes have a boosted chance
            final float hoeChance = 0.1f;

            for (final MinecoloniesCropBlock crop : crops)
            {
                final LootPool.Builder pool = LootPool.lootPool();
                if (crop.getPreferredBiome() != null)
                {
                    pool.when(EntityInBiomeTag.of(crop.getPreferredBiome()));
                }
                pool.when(ModLootConditions.HAS_NO_SHEARS_OR_SILK_TOUCH);

                pool.add(AlternativesEntry.alternatives()
                    .otherwise(LootItem.lootTableItem(crop)
                            .when(ModLootConditions.HAS_HOE)
                            .when(LootItemRandomChanceCondition.randomChance(hoeChance)))
                    .otherwise(LootItem.lootTableItem(crop)
                            .when(LootItemRandomChanceCondition.randomChance(baseChance))));

                event.getTable().addPool(pool.build());
            }
        }

        if (event.getName().equals(BuiltInLootTables.SIMPLE_DUNGEON))
        {
            final LootPool.Builder pool = LootPool.lootPool();
            for (final MinecoloniesCropBlock crop : ModBlocks.getCrops())
            {
                pool.add(LootItem.lootTableItem(crop)
                        .when(LootItemRandomChanceCondition.randomChance(0.005f)));
            }
            event.getTable().addPool(pool.build());
        }
    }

    /**
     * Event called to attach capabilities on a chunk.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onAttachingCapabilitiesChunk(@NotNull final AttachCapabilitiesEvent<LevelChunk> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "closecolony"), new MinecoloniesChunkCapabilityProvider());
    }

    /**
     * Event called to attach capabilities on the world.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onAttachingCapabilitiesWorld(@NotNull final AttachCapabilitiesEvent<Level> event)
    {
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "chunkupdate"), new MinecoloniesWorldCapabilityProvider());
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "colonymanager"), new MinecoloniesWorldColonyManagerCapabilityProvider(event.getObject().dimension() == Level.OVERWORLD));
    }

    /**
     * Called when a chunk gets loaded for some reason.
     *
     * @param event the event.
     */
    @SubscribeEvent
    public static void onChunkLoad(@NotNull final ChunkEvent.Load event)
    {
        if (event.getLevel() instanceof ServerLevel)
        {
            ChunkDataHelper.loadChunk((LevelChunk) event.getChunk(), (ServerLevel) event.getLevel());
        }
        else if (event.getLevel() instanceof ClientLevel)
        {
            if (event.getChunk() instanceof LevelChunk)
            {
                ChunkClientDataHelper.applyLate((LevelChunk) event.getChunk());
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
        if (event.getLevel() instanceof ServerLevel)
        {
            ChunkDataHelper.unloadChunk((LevelChunk) event.getChunk(), (ServerLevel) event.getLevel());
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
        if (event.getEntity() instanceof ServerPlayer && !event.isCanceled())
        {
            final ServerPlayer player = (ServerPlayer) event.getEntity();
            final LevelChunk oldChunk = player.level.getChunk(player.chunkPosition().x, player.chunkPosition().z);
            final int owningColony = ColonyUtils.getOwningColony(oldChunk);

            // Remove visiting/subscriber from old colony
            if (owningColony != 0)
            {
                final IColony oldColony = IColonyManager.getInstance().getColonyByWorld(owningColony, player.level);
                if (oldColony != null)
                {
                    oldColony.removeVisitingPlayer(player);
                    oldColony.getPackageManager().removeCloseSubscriber(player);
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
        if (event.getEntity() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) event.getEntity();

            final LevelChunk newChunk = player.level.getChunk(player.chunkPosition().x, player.chunkPosition().z);

            // Add visiting/subscriber to new colony
            final IColony newColony = IColonyManager.getInstance().getColonyByWorld(ColonyUtils.getOwningColony(newChunk), player.level);
            if (newColony != null)
            {
                newColony.addVisitingPlayer(player);
                newColony.getPackageManager().addCloseSubscriber(player);
            }
        }
    }

    /**
     * Event called when the player enters a new chunk.
     */
    @SubscribeEvent
    public static void onEnteringChunk(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide() || event.player.level.getGameTime() % 100 != 0)
        {
            return;
        }

        final Level world = event.player.level;
        final ChunkPos chunkPos = event.player.chunkPosition();

        final ChunkPos oldPos = playerPositions.get(event.player.getUUID());
        if (oldPos != null && oldPos.equals(chunkPos))
        {
            return;
        }

        playerPositions.put(event.player.getUUID(), chunkPos);

        final LevelChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

        if (chunk.isEmpty())
        {
            return;
        }

        ChunkDataHelper.loadChunk(chunk, world);

        Network.getNetwork()
          .sendToPlayer(new UpdateChunkRangeCapabilityMessage(world,
            chunkPos.x,
            chunkPos.z,
            8, true), (ServerPlayer) event.player);

        final ChunkCapData chunkCapData = ColonyUtils.getChunkCapData(chunk);
        Network.getNetwork().sendToPlayer(new UpdateChunkCapabilityMessage(chunkCapData), (ServerPlayer) event.player);

        // Check if we get into a differently claimed chunk
        if (chunkCapData.getOwningColony() != -1)
        {
            // Remove visiting/subscriber from old colony
            final IColony colony = IColonyManager.getInstance().getColonyByWorld(chunkCapData.getOwningColony(), world);
            if (colony != null)
            {
                colony.addVisitingPlayer(event.player);
                colony.getPackageManager().addCloseSubscriber((ServerPlayer) event.player);
            }
        }

        // Alert nearby buildings of close player
        if (chunkCapData.getOwningColony() != 0)
        {
            for (final Map.Entry<Integer, Set<BlockPos>> entry : chunkCapData.getAllClaimingBuildings().entrySet())
            {
                final IColony newColony = IColonyManager.getInstance().getColonyByWorld(entry.getKey(), world);
                if (newColony != null)
                {
                    for (final BlockPos buildingPos : entry.getValue())
                    {
                        IBuilding building = newColony.getBuildingManager().getBuilding(buildingPos);
                        if (building != null)
                        {
                            building.onPlayerEnterNearby(event.player);
                        }
                    }
                }
            }
        }
    }

    /**
     * Join world event.
     *
     * @param event the join world event.
     */
    @SubscribeEvent
    public static void on(final MobSpawnEvent.PositionCheck event)
    {
        if (!(event.getEntity() instanceof Enemy) || !(event.getLevel() instanceof Level))
        {
            return;
        }

        final BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (event.getSpawnType() == MobSpawnType.SPAWNER || event.getLevel().isClientSide() || !WorldUtil.isEntityBlockLoaded(event.getLevel(), pos))
        {
            return;
        }

        final LevelChunk chunk = ((Level) event.getLevel()).getChunkAt(pos);
        final int owningColony = ColonyUtils.getOwningColony(chunk);
        if (owningColony == NO_COLONY_ID)
        {
            return;
        }
        final IColony newColony = IColonyManager.getInstance().getColonyByWorld(owningColony, (Level) event.getLevel());
        if (newColony == null)
        {
            return;
        }

        for (final BlockPos buildingPos : ColonyUtils.getAllClaimingBuildings(chunk).getOrDefault(owningColony, Collections.emptySet()))
        {
            final IBuilding building = newColony.getBuildingManager().getBuilding(buildingPos);
            if (building != null && building.getBuildingLevel() >= 1 && building.isInBuilding(pos))
            {
                event.setResult(Event.Result.DENY);
                return;
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
        if (event.getEntity() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) event.getEntity();
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                if (colony.getPermissions().hasPermission(player, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY)
                      || colony.getPermissions().hasPermission(player, Action.RECEIVE_MESSAGES_FAR_AWAY))
                {
                    colony.getPackageManager().addImportantColonyPlayer(player);
                    colony.getPackageManager().sendColonyViewPackets();
                    colony.getPackageManager().sendPermissionsPackets();
                }
            }

            final int size = player.getInventory().getContainerSize();
            for (int i = 0; i < size; i++)
            {
                final ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof ItemBannerRallyGuards)
                {
                    ItemBannerRallyGuards.broadcastPlayerToRally(stack, player.level, new EntityLocation(player.getUUID()));
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
        if (event.getEntity() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) event.getEntity();
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                colony.getPackageManager().removeCloseSubscriber(player);
                colony.getPackageManager().removeImportantColonyPlayer(player);
                playerPositions.remove(player.getUUID());
            }
        }
    }

    /**
     * Event called when a citizen enters a new chunk.
     */
    public static void onEnteringChunkEntity(@NotNull final EntityCitizen entityCitizen, final ChunkPos newChunkPos)
    {
        if (MineColonies.getConfig().getServer().pvp_mode.get() && newChunkPos != null)
        {
            if (entityCitizen.level == null || !WorldUtil.isEntityChunkLoaded(entityCitizen.level, new ChunkPos(newChunkPos.x, newChunkPos.z)))
            {
                return;
            }

            if (entityCitizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            {
                final Level world = entityCitizen.getCommandSenderWorld();

                final LevelChunk chunk = world.getChunk(newChunkPos.x, newChunkPos.z);
                final int owningColony = ColonyUtils.getOwningColony(chunk);
                if (owningColony != NO_COLONY_ID
                      && entityCitizen.getCitizenColonyHandler().getColonyId() != owningColony)
                {
                    final IColony colony = IColonyManager.getInstance().getColonyByWorld(owningColony, entityCitizen.level);
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
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof Level))
        {
            return;
        }

        final Level world = (Level) event.getLevel();

        if (event.getState().getBlock() instanceof SpawnerBlock)
        {
            final BlockEntity spawner = event.getLevel().getBlockEntity(event.getPos());
            if (spawner instanceof SpawnerBlockEntity spawnerBE && spawnerBE.getSpawner().nextSpawnData != null)
            {
                final IColony colony = IColonyManager.getInstance()
                                         .getColonyByDimension(spawnerBE.getSpawner().nextSpawnData.getEntityToSpawn().getInt(TAG_COLONY_ID),
                    world.dimension());
                if (colony != null)
                {
                    colony.getEventManager().onTileEntityBreak(spawnerBE.getSpawner().nextSpawnData.getEntityToSpawn().getInt(TAG_EVENT_ID), spawner);
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
        final Player player = event.getEntity();
        final Level world = event.getLevel();
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

        if (world.getBlockState(event.getPos()).getBlock().isBed(world.getBlockState(event.getPos()), world, event.getPos(), player))
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, bedBlockPos);
            //Checks to see if player tries to sleep in a bed belonging to a Citizen, cancels the event, and Notifies Player that bed is occupied
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
                        MessageUtils.format(BASE_BED_OCCUPIED).sendTo(player);
                    }
                }
            }
        }

        handleEventCancellation(event, player);
        if (!event.isCanceled() && event.getEntity() instanceof Player && event.getItemStack().getItem() instanceof BlockItem)
        {
            final Block block = ((BlockItem) event.getItemStack().getItem()).getBlock();
            if (block instanceof AbstractBlockHut && !(block instanceof IRSComponentBlock))
            {
                final IColony colony = IColonyManager.getInstance().getIColony(world, event.getPos());
                if (colony != null && !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                    return;
                }

                if (!(player.isCreative() && player.isShiftKeyDown()))
                {
                    final ItemStack stack = event.getItemStack();
                    if (!stack.isEmpty() && !world.isClientSide)
                    {
                        Network.getNetwork()
                          .sendToPlayer(new OpenSuggestionWindowMessage(block.defaultBlockState().setValue(AbstractBlockHut.FACING,
                            event.getEntity().getDirection()), event.getPos().relative(event.getFace()), stack), (ServerPlayer) player);
                    }
                    event.setCanceled(true);
                }
                return;
            }
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
    private static boolean playerRightClickInteract(@NotNull final Player player, final Level world, final BlockPos pos)
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
    private static void handleEventCancellation(@NotNull final PlayerInteractEvent event, @NotNull final Player player)
    {
        final Block heldBlock = Block.byItem(event.getItemStack().getItem());
        if (heldBlock instanceof AbstractBlockHut || heldBlock instanceof BlockScarecrow)
        {
            if (event.getLevel().isClientSide())
            {
                event.setCanceled(true);
            }
            else
            {
                event.setCanceled(!onBlockHutPlaced(event.getLevel(), player, heldBlock, event.getPos().relative(event.getFace())));
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
    public static boolean onBlockHutPlaced(@NotNull final Level world, @NotNull final Player player, final Block block, final BlockPos pos)
    {
        if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(world))
        {
            MessageUtils.format(CANT_PLACE_COLONY_IN_OTHER_DIM).sendTo(player);
            return false;
        }

        return onBlockHutPlaced(world, player, pos, block);
    }

    private static boolean onBlockHutPlaced(final Level world, @NotNull final Player player, final BlockPos pos, final Block block)
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
                MessageUtils.format(MESSAGE_WARNING_TOWN_HALL_NOT_PRESENT).sendTo(player);
            }
            else
            {
                MessageUtils.format(MESSAGE_WARNING_TOWN_HALL_TOO_FAR_AWAY).sendTo(player);
            }

            return player.isCreative();
        }
        else if (!colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            //  No permission to place hut in colony
            MessageUtils.format(PERMISSION_OPEN_HUT, colony.getName()).sendTo(player);
            return false;
        }
        else
        {
            return player.isCreative() || colony.getBuildingManager().canPlaceAt(block, pos, player);
        }
    }

    /**
     * Gets called when world loads. Calls {@link ColonyManager#onWorldLoad(Level)})}
     *
     * @param event {@link net.minecraftforge.event.level.LevelEvent.Load}
     */
    @SubscribeEvent(priority = HIGHEST)
    public static void onWorldLoad(@NotNull final LevelEvent.Load event)
    {
        if (event.getLevel() instanceof Level)
        {
            IColonyManager.getInstance().onWorldLoad((Level) event.getLevel());
        }

        // Global events
        // Halloween ghost mode
        if (event.getLevel().isClientSide() && MineColonies.getConfig().getServer().holidayFeatures.get() &&
              (LocalDateTime.now().getDayOfMonth() == 31 && LocalDateTime.now().getMonth() == Month.OCTOBER
                 || LocalDateTime.now().getDayOfMonth() == 1 && LocalDateTime.now().getMonth() == Month.NOVEMBER
                 || LocalDateTime.now().getDayOfMonth() == 2 && LocalDateTime.now().getMonth() == Month.NOVEMBER))
        {
            // Re-enable for ghostly halloween
            RenderBipedCitizen.isItGhostTime = false;
        }
    }

    /**
     * Gets called when world unloads. Calls {@link ColonyManager#onWorldLoad(Level)}
     *
     * @param event {@link net.minecraftforge.event.level.LevelEvent.Unload}
     */
    @SubscribeEvent
    public static void onWorldUnload(@NotNull final LevelEvent.Unload event)
    {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level)
        {
            IColonyManager.getInstance().onWorldUnload((Level) event.getLevel());
        }
        if (event.getLevel().isClientSide())
        {
            IColonyManager.getInstance().resetColonyViews();
            Log.getLogger().info("Removed all colony views");
        }
    }

    /**
     * Gets called when farmland is trampled
     *
     * @param event the event to handle
     */
    @SubscribeEvent
    public static void onCropTrample(BlockEvent.FarmlandTrampleEvent event)
    {
        if (!event.getLevel().isClientSide()
              && event.getEntity() instanceof AbstractEntityCitizen
              && ((AbstractEntityCitizen) event.getEntity()).getCitizenJobHandler().getColonyJob() instanceof JobFarmer
              && ((AbstractEntityCitizen) event.getEntity()).getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SOFT_SHOES) > 0
        )
        {
            event.setCanceled(true);
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
        LivingEntity entity = event.getEntity();
        if (entity instanceof ZombieVillager && event.getOutcome() == EntityType.VILLAGER)
        {
            final Level world = entity.getCommandSenderWorld();
            final IColony colony = IColonyManager.getInstance().getIColony(world, entity.blockPosition());
            if (colony != null && colony.hasBuilding("tavern", 1, false))
            {
                event.setCanceled(true);
                if (ForgeEventFactory.canLivingConvert(entity, ModEntities.VISITOR, null))
                {
                    IVisitorData visitorData = (IVisitorData) colony.getVisitorManager().createAndRegisterCivilianData();
                    BlockPos tavernPos = colony.getBuildingManager().getRandomBuilding(b -> !b.getModulesByType(TavernBuildingModule.class).isEmpty());
                    IBuilding tavern = colony.getBuildingManager().getBuilding(tavernPos);

                    visitorData.setHomeBuilding(tavern);
                    visitorData.setBedPos(tavernPos);
                    tavern.getModulesByType(TavernBuildingModule.class).forEach(mod -> mod.getExternalCitizens().add(visitorData.getId()));

                    int recruitLevel = world.random.nextInt(10 * tavern.getBuildingLevel()) + 15;
                    List<com.minecolonies.api.util.Tuple<Item, Integer>> recruitCosts = IColonyManager.getInstance().getCompatibilityManager().getRecruitmentCostsWeights();

                    visitorData.getCitizenSkillHandler().init(recruitLevel);
                    colony.getVisitorManager().spawnOrCreateCivilian(visitorData, world, entity.blockPosition(), false);
                    colony.getEventDescriptionManager().addEventDescription(new VisitorSpawnedEvent(entity.blockPosition(), visitorData.getName()));

                    if (visitorData.getEntity().isPresent())
                    {
                        AbstractEntityCitizen visitorEntity = visitorData.getEntity().get();
                        for (EquipmentSlot slotType : EquipmentSlot.values())
                        {
                            ItemStack itemstack = entity.getItemBySlot(slotType);
                            if (slotType.getType() == EquipmentSlot.Type.ARMOR && !itemstack.isEmpty())
                            {
                                visitorEntity.setItemSlot(slotType, itemstack);
                            }
                        }
                    }

                    if (!entity.isSilent())
                    {
                        world.levelEvent((Player) null, 1027, entity.blockPosition(), 0);
                    }

                    entity.remove(Entity.RemovalReason.DISCARDED);
                    Tuple<Item, Integer> cost = recruitCosts.get(world.random.nextInt(recruitCosts.size()));
                    visitorData.setRecruitCosts(new ItemStack(cost.getA(), (int)(recruitLevel * 3.0 / cost.getB())));
                    visitorData.triggerInteraction(new RecruitmentInteraction(Component.translatable(
                            "com.minecolonies.coremod.gui.chat.recruitstorycured", visitorData.getName().split(" ")[0]), ChatPriority.IMPORTANT));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        final double lastTickMs = event.getServer().tickTimes[event.getServer().getTickCount() % 100] * 1.0E-6D;
        if (lastTickMs > 50)
        {
            TickRateStateMachine.slownessFactor = Mth.clamp(lastTickMs / 50, 1.0D, 5.0D);
        } else
        {
            TickRateStateMachine.slownessFactor = 1.0D;
        }
    }
}
