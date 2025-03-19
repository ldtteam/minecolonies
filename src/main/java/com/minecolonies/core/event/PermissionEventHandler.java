package com.minecolonies.core.event;

import com.ldtteam.structurize.items.ItemScanTool;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.blocks.huts.BlockHutTownHall;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.event.level.ExplosionEvent.Start;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.PERMISSION_DENIED;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class PermissionEventHandler
{
    /**
     * The last time the player was notified about not having permission.
     */
    private static final Map<UUID, Long> lastPlayerNotificationTick = new HashMap<>();

    /**
     * Number of attempts within a notif tick.
     */
    private static final Object2IntMap<UUID> playerAttempts = new Object2IntOpenHashMap<>();

    /**
     * BlockEvent.EntityPlaceEvent handler.
     * <p>
     * Allow under the following circumstances:
     *     <ul>
     *         <li>The entity is not a player.</li>
     *         <li>Colony protection is off.</li>
     *         <li>The event did not happen inside the current colony.</li>
     *     </ul>
     * Deny under the following circumstances:
     * <ul>
     *     <li>The block is a hut block, but the player has no {@link Action#PLACE_HUTS} permission.</li>
     *     <li>Anything else, but the player has no {@link Action#PLACE_BLOCKS} permission.</li>
     * </ul>
     * </p>
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final EntityPlaceEvent event)
    {
        if (!(event.getLevel() instanceof Level level) || !(event.getEntity() instanceof Player player))
        {
            return;
        }

        validatePrecondition(level, event.getPos(), (colony) -> {
            final Action action = event.getPlacedBlock().getBlock() instanceof AbstractBlockHut ? Action.PLACE_HUTS : Action.PLACE_BLOCKS;
            if (!colony.getPermissions().hasPermission(player, action))
            {
                cancelEvent(event, event.getEntity(), colony, action, event.getPos());
            }
        });
    }

    /**
     * Cancel an event and record the denial details in the colony's town hall.
     *
     * @param event  the event to cancel
     * @param entity the player whose action was denied
     * @param colony the colony where the event took place
     * @param action the action which was denied
     * @param pos    the location of the action which was denied
     */
    private static void cancelEvent(final Event event, @Nullable final Entity entity, final IColony colony, final Action action, final BlockPos pos)
    {
        event.setResult(Result.DENY);
        if (event.isCancelable())
        {
            event.setCanceled(true);

            final UUID uuid = entity != null ? entity.getUUID() : null;
            final String name = entity != null ? entity.getName().getString() : "-";
            if (colony.hasTownHall())
            {
                colony.getBuildingManager().getTownHall().addPermissionEvent(new PermissionEvent(uuid, name, action, pos));
            }

            if (entity instanceof FakePlayer)
            {
                return;
            }

            if (entity instanceof Player player)
            {
                MessageUtils.format(PERMISSION_DENIED).sendTo(player);

                final long worldTime = entity.level.getGameTime();
                if (!lastPlayerNotificationTick.containsKey(uuid) || lastPlayerNotificationTick.get(uuid) + (TICKS_SECOND * 10) < worldTime)
                {
                    lastPlayerNotificationTick.put(uuid, worldTime);
                    playerAttempts.put(uuid, 0);
                }
                else
                {
                    if (playerAttempts.merge(uuid, 1, Integer::sum) > 10)
                    {
                        playerAttempts.removeInt(uuid);
                        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TICKS_SECOND * 10));
                    }
                }
            }
        }
    }

    /**
     * BlockEvent.BreakEvent handler.
     *
     * @param event BlockEvent.BreakEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final BreakEvent event)
    {
        final LevelAccessor world = event.getLevel();
        if (!(world instanceof Level level) || level.isClientSide)
        {
            return;
        }

        validatePrecondition(level, event.getPos(), (colony) -> {
            if (event.getState().getBlock() instanceof AbstractBlockHut)
            {
                final IBuilding building = IColonyManager.getInstance().getBuilding(event.getPlayer().level, event.getPos());
                if (building == null)
                {
                    return;
                }

                if (!MineColonies.getConfig().getServer().enableColonyProtection.get())
                {
                    building.destroy();
                    return;
                }

                if (event.getState().getBlock() == ModBlocks.blockHutTownHall && !((BlockHutTownHall) event.getState().getBlock()).getValidBreak() && !event.getPlayer()
                                                                                                                                                         .isCreative())
                {
                    cancelEvent(event, event.getPlayer(), colony, Action.BREAK_HUTS, event.getPos());
                    return;
                }

                if (!building.getColony().getPermissions().hasPermission(event.getPlayer(), Action.BREAK_HUTS))
                {
                    if (checkEventCancellation(Action.BREAK_HUTS, colony, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos()))
                    {
                        return;
                    }
                }

                building.destroy();

                if (MineColonies.getConfig().getServer().pvp_mode.get() && event.getState().getBlock() == ModBlocks.blockHutTownHall)
                {
                    IColonyManager.getInstance().deleteColonyByWorld(building.getColony().getID(), false, event.getPlayer().level);
                }
            }
            else if (event.getState().getBlock() instanceof BlockDecorationController)
            {
                if (checkEventCancellation(Action.BREAK_HUTS, colony, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos()))
                {
                    return;
                }
                colony.getBuildingManager().removeLeisureSite(event.getPos());
            }
            else
            {
                checkEventCancellation(Action.BREAK_BLOCKS, colony, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos());
            }
        });
    }

    /**
     * ExplosionEvent.Detonate handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final Detonate event)
    {
        if (!MineColonies.getConfig().getServer().enableColonyProtection.get())
        {
            return;
        }

        final Explosions explosionTolerance = MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get();
        if (explosionTolerance == Explosions.DAMAGE_EVERYTHING)
        {
            return;
        }

        final Predicate<BlockPos> colonyPositionFilter = pos -> IColonyManager.getInstance().isCoordinateInAnyColony(event.getLevel(), pos);
        final Predicate<Entity> blockCheck = entity -> entity instanceof ArmorStand || entity instanceof HangingEntity || entity instanceof AbstractMinecart;
        final Predicate<Entity> playerCheck = entity -> entity instanceof ServerPlayer;
        final Predicate<Entity> enemyCheck = entity -> entity instanceof Enemy;

        // Blocks can be cleared under any condition
        event.getAffectedBlocks().removeIf(colonyPositionFilter);

        final Predicate<Entity> entityFilter = switch (explosionTolerance)
        {
            case DAMAGE_PLAYERS -> playerCheck.and(enemyCheck);
            case DAMAGE_ENTITIES -> enemyCheck;
            default -> entity -> true;
        };
        event.getAffectedEntities().removeIf(entity -> entityFilter.or(blockCheck).test(entity) && colonyPositionFilter.test(entity.blockPosition()));
    }

    /**
     * ExplosionEvent.Start handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final Start event)
    {
        final Explosions explosionTolerance = MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get();
        if (explosionTolerance != Explosions.DAMAGE_NOTHING)
        {
            return;
        }

        validatePrecondition(event.getLevel(),
          BlockPos.containing(event.getExplosion().getPosition()),
          (colony) -> cancelEvent(event, null, colony, Action.EXPLODE, BlockPos.containing(event.getExplosion().getPosition())));
    }

    /**
     * PlayerInteractEvent.RightClickBlock handler.
     * <p>
     * Allow under the following circumstances:
     *     <ul>
     *         <li>Colony protection is off.</li>
     *         <li>The event did not happen inside the current colony.</li>
     *     </ul>
     * Deny under the following circumstances:
     * <ul>
     *     <li>The block is a hut block, but the player has no {@link Action#ACCESS_HUTS} permission.</li>
     *     <li>The block is a toggleable (door, trapdoor, fence gate, etc.), but the player has no {@link Action#ACCESS_TOGGLEABLES} permission.</li>
     *     <li>The block is a container block (chest, furnace, etc.), but the player has no {@link Action#OPEN_CONTAINER} permission.</li>
     *     <li>The block is free to interact block/position, but the player has no {@link Action#ACCESS_FREE_BLOCKS} permission.</li>
     *     <li>Anything else, but the player has no {@link Action#RIGHTCLICK_BLOCK} permission.</li>
     * </ul>
     * </p>
     *
     * @param event the event instance.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final RightClickBlock event)
    {
        validatePrecondition(event.getLevel(), event.getPos(), (colony) -> {
            final IPermissions perms = colony.getPermissions();
            final BlockState state = event.getLevel().getBlockState(event.getPos());
            final BlockEntity entity = event.getLevel().getBlockEntity(event.getPos());
            final Block block = state.getBlock();

            // Interaction with hut blocks
            if (block instanceof AbstractBlockHut)
            {
                if (!perms.hasPermission(event.getEntity(), Action.ACCESS_HUTS))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.ACCESS_HUTS, event.getPos());
                }
            }
            // Interact with toggleables
            else if (state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(BlockTags.FENCE_GATES))
            {
                if (!perms.hasPermission(event.getEntity(), Action.ACCESS_TOGGLEABLES))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.ACCESS_TOGGLEABLES, event.getPos());
                }
            }
            // Interact with any container
            else if (entity instanceof Container)
            {
                if (!perms.hasPermission(event.getEntity(), Action.OPEN_CONTAINER))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.OPEN_CONTAINER, event.getPos());
                }
            }
            // Free interaction blocks
            else if (isFreeToInteractWith(colony, block, event.getPos()))
            {
                if (!perms.hasPermission(event.getEntity(), Action.ACCESS_FREE_BLOCKS))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.ACCESS_FREE_BLOCKS, event.getPos());
                }
            }
            // Interact with anything else
            else
            {
                if (!perms.hasPermission(event.getEntity(), Action.RIGHTCLICK_BLOCK) && !state.isAir())
                {
                    cancelEvent(event, event.getEntity(), colony, Action.RIGHTCLICK_BLOCK, event.getPos());
                }
            }
        });
    }

    /**
     * PlayerInteractEvent.RightClickItem handler.
     * <p>
     * Allow under the following circumstances:
     *     <ul>
     *         <li>Colony protection is off.</li>
     *         <li>The event did not happen inside the current colony.</li>
     *     </ul>
     * Denies under the following circumstances:
     * <ul>
     *     <li>The player attempts to throw a potion, but the player has no {@link Action#THROW_POTION} permission.</li>
     *     <li>The player attempts to use the scan tool, but the player has no {@link Action#USE_SCAN_TOOL} permission.</li>
     * </ul>
     * </p>
     *
     * @param event the event instance.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final RightClickItem event)
    {
        validatePrecondition(event.getLevel(), event.getPos(), (colony) -> {
            final ItemStack stack = event.getItemStack();

            // Potion throwing
            if (stack.getItem() instanceof ThrowablePotionItem)
            {
                checkEventCancellation(Action.THROW_POTION, colony, event.getEntity(), event.getLevel(), event, event.getPos());
            }
            // Using scan tool
            else if (stack.getItem() instanceof ItemScanTool)
            {
                checkEventCancellation(Action.USE_SCAN_TOOL, colony, event.getEntity(), event.getLevel(), event, event.getPos());
            }
        });
    }

    /**
     * Check in the config if that block can be interacted with freely.
     *
     * @param block the block to check.
     * @param pos   the position of the interaction
     * @return true if so.
     */
    private static boolean isFreeToInteractWith(@NonNull final IColony colony, @Nullable final Block block, final BlockPos pos)
    {
        return colony instanceof Colony serverColony && (
          (block != null && (serverColony.getFreeBlocks().contains(block) || block.defaultBlockState().is(ModTags.colonyProtectionException))) || serverColony.getFreePositions()
                                                                                                                                                    .contains(pos));
    }

    /**
     * PlayerInteractEvent.EntityInteract handler.
     * <p>
     * Check, if a player right-clicked an entity. Deny if: - If the entity is in colony - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final EntityInteract event)
    {
        onInteractEntity(event, event.getTarget());
    }

    /**
     * Check if the event should be canceled for a given player and minimum rank.
     *
     * @param action   the action that was performed on the position.
     * @param colony   the colony the action was performed in.
     * @param playerIn the player performing the event.
     * @param world    the world the event was performed in.
     * @param event    the event instance itself.
     * @param pos      the position. Can be null if no target was provided to the event.
     * @return true if canceled.
     */
    private static boolean checkEventCancellation(
      @NotNull final Action action,
      @NotNull final IColony colony,
      @NotNull final Player playerIn,
      @NotNull final Level world,
      @NotNull final Event event,
      @Nullable final BlockPos pos)
    {
        @NotNull final Player player = EntityUtils.getPlayerOfFakePlayer(playerIn, world);

        final BlockPos positionToCheck = pos != null ? pos : player.blockPosition();
        if (!colony.getPermissions().hasPermission(player, action))
        {
            if (MineColonies.getConfig().getServer().pvp_mode.get() && !world.isClientSide && colony.isValidAttackingPlayer(playerIn))
            {
                return false;
            }
            else
            {
                cancelEvent(event, player, colony, action, positionToCheck);
                return true;
            }
        }
        return false;
    }

    /**
     * PlayerInteractEvent.EntityInteractSpecific handler.
     * <p>
     * Check, if a player right clicked a entity. Deny if: - If the entity is in colony - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final EntityInteractSpecific event)
    {
        onInteractEntity(event, event.getTarget());
    }

    /**
     * Handles entity interaction events.
     *
     * @param event  the original event instance.
     * @param target the target entity.
     */
    private static void onInteractEntity(final PlayerInteractEvent event, final Entity target)
    {
        validatePrecondition(event.getLevel(), event.getPos(), (colony) -> {
            if (isFreeToInteractWith(colony, null, event.getPos()) && colony.getPermissions().hasPermission(event.getEntity(), Action.ACCESS_FREE_BLOCKS))
            {
                return;
            }

            if (target.getType().is(ModTags.freeToInteractWith))
            {
                return;
            }

            checkEventCancellation(Action.RIGHTCLICK_ENTITY, colony, event.getEntity(), event.getLevel(), event, event.getPos());
        });
    }

    /**
     * ItemTossEvent handler.
     * <p>
     * Check, if a player tossed a block. Deny if: - If the tossing happens in the colony - player is hostile to colony
     *
     * @param event ItemTossEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final ItemTossEvent event)
    {
        validatePrecondition(event.getPlayer().level, event.getPlayer().blockPosition(), (colony) -> {
            if (checkEventCancellation(Action.TOSS_ITEM, colony, event.getPlayer(), event.getPlayer().level, event, event.getPlayer().blockPosition()))
            {
                event.getPlayer().getInventory().add(event.getEntity().getItem());
            }
        });
    }

    /**
     * ItemEntityPickupEvent handler.
     * <p>
     * Check, if a player tries to pickup a block. Deny if: - If the pickUp happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final EntityItemPickupEvent event)
    {
        validatePrecondition(event.getEntity().level,
          event.getEntity().blockPosition(),
          (colony) -> checkEventCancellation(Action.PICKUP_ITEM, colony, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, event.getEntity().blockPosition()));
    }

    /**
     * FillBucketEvent handler.
     * <p>
     * Check, if a player tries to fill a bucket. Deny if: - If the fill happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final FillBucketEvent event)
    {
        validatePrecondition(event.getEntity().level, event.getEntity().blockPosition(), (colony) -> {
            BlockPos targetBlockPos = null;
            if (event.getTarget() instanceof BlockHitResult result)
            {
                targetBlockPos = result.getBlockPos();
            }
            else if (event.getTarget() instanceof EntityHitResult result)
            {
                targetBlockPos = result.getEntity().blockPosition();
            }
            checkEventCancellation(Action.FILL_BUCKET, colony, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, targetBlockPos);
        });
    }

    /**
     * ArrowLooseEvent handler.
     * <p>
     * Check if a player tries to shoot an arrow. Deny if: - If the shooting happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final ArrowLooseEvent event)
    {
        validatePrecondition(event.getLevel(),
          event.getEntity().blockPosition(),
          (colony) -> checkEventCancellation(Action.SHOOT_ARROW, colony, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, event.getEntity().blockPosition()));
    }

    /**
     * LivingHurtEvent handler.
     * <p>
     * Check if the entity that is getting hurt is a player,
     * players that get hurt by other players are handled elsewhere,
     * this here is handling players getting hurt by citizens.
     *
     * @param event the event instance.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final LivingHurtEvent event)
    {
        validatePrecondition(event.getEntity().level, event.getEntity().blockPosition(), (colony) -> {
            if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof EntityCitizen
                  && ((EntityCitizen) event.getSource().getEntity()).getCitizenColonyHandler().getColonyId() == colony.getID() && colony.getRaiderManager().isRaided()
                  && !colony.getPermissions().hasPermission(player, Action.GUARDS_ATTACK))
            {
                event.setCanceled(true);
            }
        });
    }

    /**
     * AttackEntityEvent handler.
     * <p>
     * Check, if a player tries to attack an entity. Deny if: - If the attacking happens in the colony - Player is less than officer to the colony.
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void on(final AttackEntityEvent event)
    {
        validatePrecondition(event.getEntity().level, event.getEntity().blockPosition(), (colony) -> {
            if (event.getTarget() instanceof Monster)
            {
                return;
            }

            final Player player = EntityUtils.getPlayerOfFakePlayer(event.getEntity(), event.getEntity().getCommandSenderWorld());

            if (MineColonies.getConfig().getServer().enableColonyProtection.get() && colony.isCoordInColony(player.getCommandSenderWorld(), player.blockPosition()))
            {
                final IPermissions perms = colony.getPermissions();
                if (event.getTarget() instanceof final EntityCitizen citizen)
                {
                    if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && perms.hasPermission(event.getEntity(), Action.GUARDS_ATTACK))
                    {
                        return;
                    }

                    if (perms.hasPermission(event.getEntity(), Action.ATTACK_CITIZEN))
                    {
                        return;
                    }

                    cancelEvent(event, event.getEntity(), colony, Action.ATTACK_CITIZEN, event.getTarget().blockPosition());
                    return;
                }

                if (!(event.getTarget() instanceof Enemy) && !perms.hasPermission(event.getEntity(), Action.ATTACK_ENTITY))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.ATTACK_ENTITY, event.getTarget().blockPosition());
                }
            }
        });
    }

    /**
     * Checks if colony protection is enabled and the event occurs within a colony, before invoking the handler method.
     *
     * @param level    the level where the event was called.
     * @param position the relevant position for the event.
     * @param handler  the callback function, giving back the colony the event occurred in.
     */
    private static void validatePrecondition(final Level level, final BlockPos position, final Consumer<IColony> handler)
    {
        if (!IMinecoloniesAPI.getInstance().getConfig().getServer().enableColonyProtection.get())
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getIColony(level, position);
        if (colony == null)
        {
            return;
        }

        handler.accept(colony);
    }
}
