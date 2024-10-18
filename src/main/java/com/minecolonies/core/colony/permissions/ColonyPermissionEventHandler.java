package com.minecolonies.core.colony.permissions;

import com.ldtteam.structurize.items.ItemScanTool;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.PERMISSION_DENIED;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
public class ColonyPermissionEventHandler
{
    /**
     * The colony involved in this permission-check event
     */
    private final Colony colony;

    /**
     * The last time the player was notified about not having permission.
     */
    private final Map<UUID, Long> lastPlayerNotificationTick = new HashMap<>();

    /**
     * Number of attempts within a notif tick.
     */
    private final Object2IntMap<UUID> playerAttempts = new Object2IntOpenHashMap<>();

    /**
     * Create this EventHandler.
     *
     * @param colony the colony to check on.
     */
    public ColonyPermissionEventHandler(final Colony colony)
    {
        this.colony = colony;
    }

    /**
     * BlockEvent.PlaceEvent handler.
     *
     * @param event BlockEvent.PlaceEvent
     */
    @SubscribeEvent
    public void on(final BlockEvent.EntityPlaceEvent event)
    {
        final Action action = event.getPlacedBlock().getBlock() instanceof AbstractBlockHut ? Action.PLACE_HUTS : Action.PLACE_BLOCKS;
        if (MineColonies.getConfig().getServer().enableColonyProtection.get() && checkBlockEventDenied(event.getLevel(),
          event.getPos(),
          event.getEntity(),
          event.getPlacedBlock(),
          action))
        {
            cancelEvent(event, event.getEntity(), colony, action, event.getPos());
        }
    }

    /**
     * This method returns TRUE if this event should be denied.
     *
     * @param worldIn    the world to check in
     * @param posIn      the block to check
     * @param entity     the player who tries
     * @param blockState the state that block is in
     * @param action     the action that was performed on the position
     * @return true if canceled
     */
    private boolean checkBlockEventDenied(
      final LevelAccessor worldIn, final BlockPos posIn, final Entity entity, final BlockState blockState,
      final Action action)
    {
        if (entity instanceof Player)
        {
            @NotNull final Player player = EntityUtils.getPlayerOfFakePlayer((Player) entity, entity.level);
            if (colony.isCoordInColony(entity.level, posIn))
            {
                if (blockState.getBlock() instanceof AbstractBlockHut
                      && colony.getPermissions().hasPermission(player, action))
                {
                    return false;
                }

                return !colony.getPermissions().hasPermission(player, action);
            }
        }
        /*
         * - We are not inside the colony
         * - We are in but not denied
         * - The placer is not a player.
         */
        return false;
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
    private void cancelEvent(final Event event, @Nullable final Entity entity, final Colony colony, final Action action, final BlockPos pos)
    {
        event.setResult(Event.Result.DENY);
        if (event.isCancelable())
        {
            event.setCanceled(true);
            if (entity == null)
            {
                if (colony.hasTownHall())
                {
                    colony.getBuildingManager().getTownHall().addPermissionEvent(new PermissionEvent(null, "-", action, pos));
                }
                return;
            }
            if (colony.hasTownHall())
            {
                colony.getBuildingManager().getTownHall().addPermissionEvent(new PermissionEvent(entity.getUUID(), entity.getName().getString(), action, pos));
            }


            if (entity instanceof FakePlayer)
            {
                return;
            }

            final long worldTime = entity.level.getGameTime();
            if (!lastPlayerNotificationTick.containsKey(entity.getUUID())
                  || lastPlayerNotificationTick.get(entity.getUUID()) + (TICKS_SECOND * 10)
                       < worldTime)
            {
                MessageUtils.format(PERMISSION_DENIED).sendTo((Player) entity);
                lastPlayerNotificationTick.put(entity.getUUID(), worldTime);
                playerAttempts.put(entity.getUUID(), 0);
            }
            else
            {
                if (playerAttempts.compute(entity.getUUID(), (uuid, count) -> count == null ? 1 : count + 1) > 10)
                {
                    if (entity instanceof LivingEntity living)
                    {
                        playerAttempts.put(entity.getUUID(), 0);
                        living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TICKS_SECOND * 10));
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
    @SubscribeEvent
    public void on(final BlockEvent.BreakEvent event)
    {
        final LevelAccessor world = event.getLevel();
        if (world.isClientSide())
        {
            return;
        }

        if (event.getState().getBlock() instanceof AbstractBlockHut)
        {
            @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(event.getPlayer().level, event.getPos());
            if (building == null)
            {
                return;
            }

            if (!MineColonies.getConfig().getServer().enableColonyProtection.get())
            {
                building.destroy();
                return;
            }

            if (event.getState().getBlock() == ModBlocks.blockHutTownHall && !((BlockHutTownHall)event.getState().getBlock()).getValidBreak() && !event.getPlayer().isCreative())
            {
                cancelEvent(event, event.getPlayer(), colony, Action.BREAK_HUTS, event.getPos());
                return;
            }

            if (!building.getColony().getPermissions().hasPermission(event.getPlayer(), Action.BREAK_HUTS))
            {
                if (checkEventCancelation(Action.BREAK_HUTS, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos()))
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
            if (checkEventCancelation(Action.BREAK_HUTS, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos()))
            {
                return;
            }
            colony.getBuildingManager().removeLeisureSite(event.getPos());
        }
        else
        {
            checkEventCancelation(Action.BREAK_BLOCKS, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPos());
        }
    }

    /**
     * ExplosionEvent.Detonate handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(final ExplosionEvent.Detonate event)
    {
        if (MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get() == Explosions.DAMAGE_EVERYTHING)
        {
            return;
        }

        final Level eventWorld = event.getLevel();
        final Predicate<BlockPos> getBlocksInColony = pos -> colony.isCoordInColony(eventWorld, pos);
        Predicate<Entity> getEntitiesInColony = entity -> (!(entity instanceof Enemy) || (entity instanceof Llama))
                                                            && colony.isCoordInColony(entity.getCommandSenderWorld(), entity.blockPosition());
        switch(MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get())
        {
            case DAMAGE_NOTHING:
                // if any entity is in colony -> remove from list
                getEntitiesInColony = entity -> colony.isCoordInColony(entity.getCommandSenderWorld(), entity.blockPosition());
                // intentional fall-through to next case.
            case DAMAGE_PLAYERS:
                // if non-mob or llama entity is in colony -> remove from list
                final List<Entity> entitiesToRemove = event.getAffectedEntities().stream()
                                                          .filter(getEntitiesInColony)
                                                          .filter(entity -> !(entity instanceof ServerPlayer))
                                                          .collect(Collectors.toList());
                event.getAffectedEntities().removeAll(entitiesToRemove);
                // intentional fall-through to next case.
            case DAMAGE_ENTITIES:
                // if block is in colony -> remove from list
                final List<BlockPos> blocksToRemove = event.getAffectedBlocks().stream()
                                                        .filter(getBlocksInColony)
                                                        .collect(Collectors.toList());
                event.getAffectedBlocks().removeAll(blocksToRemove);
                break;
            case DAMAGE_EVERYTHING:
            default:
                break;
        }
    }

    /**
     * ExplosionEvent.Start handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(final ExplosionEvent.Start event)
    {
        if (MineColonies.getConfig().getServer().enableColonyProtection.get()
              && MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get() == Explosions.DAMAGE_NOTHING
              && colony.isCoordInColony(event.getLevel(), BlockPos.containing(event.getExplosion().getPosition())))
        {
            cancelEvent(event, null, colony, Action.EXPLODE, BlockPos.containing(event.getExplosion().getPosition()));
        }
    }

    /**
     * PlayerInteractEvent handler.
     * <p>
     * Check, if a player right clicked a block. Deny if: - If the block is in colony - block is AbstractBlockHut - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent event)
    {
        if (colony.isCoordInColony(event.getLevel(), event.getPos())
              && !(event instanceof PlayerInteractEvent.EntityInteract || event instanceof PlayerInteractEvent.EntityInteractSpecific))
        {
            final BlockState state = event.getLevel().getBlockState(event.getPos());
            final Block block = state.getBlock();

            // Huts
            if (event instanceof PlayerInteractEvent.RightClickBlock && block instanceof AbstractBlockHut
                  && !colony.getPermissions().hasPermission(event.getEntity(), Action.ACCESS_HUTS))
            {
                cancelEvent(event, event.getEntity(), colony, Action.ACCESS_HUTS, event.getPos());
                return;
            }

            final Permissions perms = colony.getPermissions();

            if (isFreeToInteractWith(block, event.getPos())
                  && perms.hasPermission(event.getEntity(), Action.ACCESS_FREE_BLOCKS))
            {
                return;
            }

            if ((state.is(BlockTags.DOORS) || state.is(BlockTags.FENCE_GATES)) && perms.hasPermission(event.getEntity(), Action.ACCESS_TOGGLEABLES))
            {
                return;
            }

            if (MineColonies.getConfig().getServer().enableColonyProtection.get())
            {
                if (!perms.hasPermission(event.getEntity(), Action.RIGHTCLICK_BLOCK) && !(block instanceof AirBlock))
                {
                    checkEventCancelation(Action.RIGHTCLICK_BLOCK, event.getEntity(), event.getLevel(), event, event.getPos());
                    return;
                }

                if (block instanceof BaseEntityBlock && !perms.hasPermission(event.getEntity(),
                  Action.OPEN_CONTAINER))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.OPEN_CONTAINER, event.getPos());
                    return;
                }

                if (event.getLevel().getBlockEntity(event.getPos()) != null && !perms.hasPermission(event.getEntity(), Action.RIGHTCLICK_ENTITY))
                {
                    checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntity(), event.getLevel(), event, event.getPos());
                    return;
                }

                final ItemStack stack = event.getItemStack();
                if (ItemStackUtils.isEmpty(stack) || stack.isEdible())
                {
                    return;
                }


                if (stack.getItem() instanceof PotionItem)
                {
                    checkEventCancelation(Action.THROW_POTION, event.getEntity(), event.getLevel(), event, event.getPos());
                    return;
                }

                if (stack.getItem() instanceof ItemScanTool
                      && !perms.hasPermission(event.getEntity(), Action.USE_SCAN_TOOL))
                {
                    cancelEvent(event, event.getEntity(), colony, Action.USE_SCAN_TOOL, event.getPos());
                }
            }
        }
    }

    /**
     * Check in the config if that block can be interacted with freely.
     *
     * @param block the block to check.
     * @param pos   the position of the interaction
     * @return true if so.
     */
    private boolean isFreeToInteractWith(@Nullable final Block block, final BlockPos pos)
    {
        return (block != null && (colony.getFreeBlocks().contains(block) || block.defaultBlockState().is(ModTags.colonyProtectionException))) || colony.getFreePositions().contains(pos);
    }

    /**
     * PlayerInteractEvent.EntityInteract handler.
     * <p>
     * Check, if a player right clicked an entity. Deny if: - If the entity is in colony - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent.EntityInteract event)
    {
        if (isFreeToInteractWith(null, event.getPos())
              && colony.getPermissions().hasPermission(event.getEntity(), Action.ACCESS_FREE_BLOCKS))
        {
            return;
        }

        if (event.getEntity().getType().is(ModTags.freeToInteractWith))
        {
            return;
        }

        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntity(), event.getLevel(), event, event.getPos());
    }

    /**
     * Check if the event should be canceled for a given player and minimum rank.
     *
     * @param action   the action that was performed on the position
     * @param playerIn the player.
     * @param world    the world.
     * @param event    the event.
     * @param pos      the position.  Can be null if no target was provided to the event.
     * @return true if canceled.
     */
    private boolean checkEventCancelation(
      final Action action, @NotNull final Player playerIn, @NotNull final Level world, @NotNull final Event event,
      @Nullable final BlockPos pos)
    {
        @NotNull final Player player = EntityUtils.getPlayerOfFakePlayer(playerIn, world);

        BlockPos positionToCheck = pos;
        if (null == positionToCheck)
        {
            positionToCheck = player.blockPosition();
        }
        if (MineColonies.getConfig().getServer().enableColonyProtection.get()
              && colony.isCoordInColony(player.getCommandSenderWorld(), positionToCheck)
              && !colony.getPermissions().hasPermission(player, action))
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
    @SubscribeEvent
    public void on(final PlayerInteractEvent.EntityInteractSpecific event)
    {
        if (isFreeToInteractWith(null, event.getPos())
              && colony.getPermissions().hasPermission(event.getEntity(), Action.ACCESS_FREE_BLOCKS))
        {
            return;
        }
        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntity(), event.getLevel(), event, event.getPos());
    }

    /**
     * ItemTossEvent handler.
     * <p>
     * Check, if a player tossed a block. Deny if: - If the tossing happens in the colony - player is hostile to colony
     *
     * @param event ItemTossEvent
     */
    @SubscribeEvent
    public void on(final ItemTossEvent event)
    {
        if (checkEventCancelation(Action.TOSS_ITEM, event.getPlayer(), event.getPlayer().getCommandSenderWorld(), event, event.getPlayer().blockPosition()))
        {
            event.getPlayer().getInventory().add(event.getEntity().getItem());
        }
    }

    /**
     * ItemEntityPickupEvent handler.
     * <p>
     * Check, if a player tries to pickup a block. Deny if: - If the pickUp happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent
    public void on(final EntityItemPickupEvent event)
    {
        checkEventCancelation(Action.PICKUP_ITEM, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, event.getEntity().blockPosition());
    }

    /**
     * FillBucketEvent handler.
     * <p>
     * Check, if a player tries to fill a bucket. Deny if: - If the fill happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent
    public void on(final FillBucketEvent event)
    {
        @Nullable BlockPos targetBlockPos = null;
        if (event.getTarget() instanceof BlockHitResult)
        {
            targetBlockPos = ((BlockHitResult) event.getTarget()).getBlockPos();
        }
        else if (event.getTarget() instanceof EntityHitResult)
        {
            targetBlockPos = ((EntityHitResult) event.getTarget()).getEntity().blockPosition();
        }
        checkEventCancelation(Action.FILL_BUCKET, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, targetBlockPos);
    }

    /**
     * ArrowLooseEvent handler.
     * <p>
     * Check if a player tries to shoot an arrow. Deny if: - If the shooting happens in the colony - player is neutral or hostile to colony
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent
    public void on(final ArrowLooseEvent event)
    {
        checkEventCancelation(Action.SHOOT_ARROW, event.getEntity(), event.getEntity().getCommandSenderWorld(), event, event.getEntity().blockPosition());
    }

    /**
     * LivingHurtEvent handler.
     * <p>
     * Check if the entity that is getting hurt is a player,
     * players that get hurt by other players are handled elsewhere,
     * this here is handling players getting hurt by citizens.
     * @param event
     */
    @SubscribeEvent
    public void on(final LivingHurtEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer
              && event.getSource().getEntity() instanceof EntityCitizen
              && ((EntityCitizen) event.getSource().getEntity()).getCitizenColonyHandler().getColonyId() == colony.getID()
              && colony.getRaiderManager().isRaided()
              && !colony.getPermissions().hasPermission((Player) event.getEntity(), Action.GUARDS_ATTACK))
        {
            event.setCanceled(true);
        }
    }

    /**
     * AttackEntityEvent handler.
     * <p>
     * Check, if a player tries to attack an entity.. Deny if: - If the attacking happens in the colony - Player is less than officer to the colony.
     *
     * @param event ItemEntityPickupEvent
     */
    @SubscribeEvent
    public void on(final AttackEntityEvent event)
    {
        if (event.getTarget() instanceof Monster)
        {
            return;
        }

        @NotNull final Player player = EntityUtils.getPlayerOfFakePlayer(event.getEntity(), event.getEntity().getCommandSenderWorld());

        if (MineColonies.getConfig().getServer().enableColonyProtection.get()
              && colony.isCoordInColony(player.getCommandSenderWorld(), player.blockPosition()))
        {
            final Permissions perms = colony.getPermissions();
            if (event.getTarget() instanceof EntityCitizen)
            {
                final AbstractEntityCitizen citizen = (AbstractEntityCitizen) event.getTarget();
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
    }
}
