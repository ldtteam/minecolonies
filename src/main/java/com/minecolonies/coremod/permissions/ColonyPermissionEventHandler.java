package com.minecolonies.coremod.permissions;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.items.ItemScanTool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
public class ColonyPermissionEventHandler
{

    private final Colony colony;

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
    public void on(final BlockEvent.PlaceEvent event)
    {
        if (Configurations.gameplay.enableColonyProtection && checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getPlacedBlock(),
          event.getPlacedBlock().getBlock() instanceof AbstractBlockHut ? Action.PLACE_HUTS : Action.PLACE_BLOCKS))
        {
            cancelEvent(event, event.getPlayer());
        }
    }

    /**
     * This method returns TRUE if this event should be denied.
     *
     * @param worldIn    the world to check in
     * @param posIn      the block to check
     * @param playerIn   the player who tries
     * @param blockState the state that block is in
     * @return true if canceled
     */
    private boolean checkBlockEventDenied(
                                           final World worldIn, final BlockPos posIn, final EntityPlayer playerIn, final IBlockState blockState,
                                           final Action action)
    {
        @NotNull final EntityPlayer player = EntityUtils.getPlayerOfFakePlayer(playerIn, worldIn);

        if (colony.isCoordInColony(worldIn, posIn))
        {
            if (!colony.getPermissions().isColonyMember(player))
            {
                return true;
            }

            if (blockState.getBlock() instanceof AbstractBlockHut
                  && colony.getPermissions().hasPermission(player, action))
            {
                return false;
            }

            if (colony.getPermissions().hasPermission(player, action))
            {
                return false;
            }

            return true;
        }

        /*
         * - We are not inside the colony
         * - We are in but not denied
         */
        return false;
    }

    private static void cancelEvent(final Event event, @Nullable final EntityPlayer player)
    {
        event.setResult(Event.Result.DENY);
        if (event.isCancelable())
        {
            event.setCanceled(true);

            if (player == null || player instanceof FakePlayer)
            {
                return;
            }
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.permission.no");
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
        if (Configurations.gameplay.enableColonyProtection && checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(),
          event.getWorld().getBlockState(event.getPos()),
          event.getWorld().getBlockState(event.getPos()).getBlock() instanceof AbstractBlockHut ? Action.BREAK_HUTS : Action.BREAK_BLOCKS))
        {
            cancelEvent(event, event.getPlayer());
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
        if (!Configurations.gameplay.enableColonyProtection || !Configurations.gameplay.turnOffExplosionsInColonies)
        {
            return;
        }

        final World eventWorld = event.getWorld();
        final Predicate<BlockPos> getBlocksInColony = pos -> colony.isCoordInColony(eventWorld, pos);
        final Predicate<Entity> getEntitiesInColony = entity -> colony.isCoordInColony(entity.getEntityWorld(), entity.getPosition());
        // if block is in colony -> remove from list
        final List<BlockPos> blocksToRemove = event.getAffectedBlocks().stream()
                                                .filter(getBlocksInColony)
                                                .collect(Collectors.toList());

        // if entity is in colony -> remove from list
        final List<Entity> entitiesToRemove = event.getAffectedEntities().stream()
                                                .filter(getEntitiesInColony)
                                                .collect(Collectors.toList());
        event.getAffectedBlocks().removeAll(blocksToRemove);
        event.getAffectedEntities().removeAll(entitiesToRemove);
    }

    /**
     * ExplosionEvent.Start handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(final ExplosionEvent.Start event)
    {
        if (Configurations.gameplay.enableColonyProtection
              && Configurations.gameplay.turnOffExplosionsInColonies
              && colony.isCoordInColony(event.getWorld(), new BlockPos(event.getExplosion().getPosition())))
        {
            cancelEvent(event, null);
        }
    }

    /**
     * PlayerInteractEvent handler.
     * <p>
     * Check, if a player right clicked a block.
     * Deny if:
     * - If the block is in colony
     * - block is AbstractBlockHut
     * - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent event)
    {
        if (colony.isCoordInColony(event.getWorld(), event.getPos())
              && !(event instanceof PlayerInteractEvent.EntityInteract || event instanceof PlayerInteractEvent.EntityInteractSpecific))
        {
            final Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            // Huts
            if (block instanceof AbstractBlockHut
                  && !colony.getPermissions().hasPermission(event.getEntityPlayer(), Action.ACCESS_HUTS))
            {
                cancelEvent(event, event.getEntityPlayer());
                return;
            }

            final Permissions perms = colony.getPermissions();

            if (isFreeToInteractWith(block, event.getPos())
                  && perms.hasPermission(event.getEntityPlayer(), Action.ACCESS_FREE_BLOCKS))
            {
                return;
            }

            if (Configurations.gameplay.enableColonyProtection)
            {
                if (!perms.hasPermission(event.getEntityPlayer(), Action.RIGHTCLICK_BLOCK) && block != null && block != Blocks.AIR)
                {
                    cancelEvent(event, event.getEntityPlayer());
                    return;
                }

                if (block instanceof BlockContainer && !perms.hasPermission(event.getEntityPlayer(),
                  Action.OPEN_CONTAINER))
                {
                    cancelEvent(event, event.getEntityPlayer());
                    return;
                }

                if (event.getWorld().getTileEntity(event.getPos()) != null && !perms.hasPermission(event.getEntityPlayer(), Action.RIGHTCLICK_ENTITY))
                {
                    cancelEvent(event, event.getEntityPlayer());
                    return;
                }

                final ItemStack stack = event.getItemStack();
                if(ItemStackUtils.isEmpty(stack) || stack.getItem() instanceof ItemFood)
                {
                    return;
                }

                if (stack.getItem() instanceof ItemPotion && !perms.hasPermission(event.getEntityPlayer(),
                  Action.THROW_POTION))
                {
                    cancelEvent(event, event.getEntityPlayer());
                    return;
                }

                if (stack.getItem() instanceof ItemScanTool
                      && !perms.hasPermission(event.getEntityPlayer(), Action.USE_SCAN_TOOL))
                {
                    cancelEvent(event, event.getEntityPlayer());
                    return;
                }
            }
        }
    }

    /**
     * Check in the config if that block can be interacted with freely.
     *
     * @param block the block to check.
     * @return true if so.
     */
    private boolean isFreeToInteractWith(@Nullable final Block block, final BlockPos pos)
    {
        return (block != null && colony.getFreeBlocks().stream().anyMatch(b -> b.equals(block))) || colony.getFreePositions().stream().anyMatch(position -> position.equals(pos));
    }

    /**
     * PlayerInteractEvent.EntityInteract handler.
     * <p>
     * Check, if a player right clicked an entity.
     * Deny if:
     * - If the entity is in colony
     * - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent.EntityInteract event)
    {
        if (isFreeToInteractWith(null, event.getPos())
              && colony.getPermissions().hasPermission(event.getEntityPlayer(), Action.ACCESS_FREE_BLOCKS))
        {
            return;
        }

        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntityPlayer(), event.getWorld(), event);
    }

    /**
     * Check if the event should be canceled for a given player and minimum rank.
     *
     * @param rankIn   the minimum rank.
     * @param playerIn the player.
     * @param world    the world.
     * @param event    the event.
     */
    private void checkEventCancelation(final Action action, @NotNull final EntityPlayer playerIn, @NotNull final World world, @NotNull final Event event)
    {
        @NotNull final EntityPlayer player = EntityUtils.getPlayerOfFakePlayer(playerIn, world);

        if (Configurations.gameplay.enableColonyProtection
              && colony.isCoordInColony(player.getEntityWorld(), player.getPosition())
              && !colony.getPermissions().hasPermission(player, action))
        {
            cancelEvent(event, player);
        }
    }

    /**
     * PlayerInteractEvent.EntityInteractSpecific handler.
     * <p>
     * Check, if a player right clicked a entity.
     * Deny if:
     * - If the entity is in colony
     * - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent.EntityInteractSpecific event)
    {
        if (isFreeToInteractWith(null, event.getPos())
              && colony.getPermissions().hasPermission(event.getEntityPlayer(), Action.ACCESS_FREE_BLOCKS))
        {
            return;
        }
        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntityPlayer(), event.getWorld(), event);
    }

    /**
     * ItemTossEvent handler.
     * <p>
     * Check, if a player tossed a block.
     * Deny if:
     * - If the tossing happens in the colony
     * - player is hostile to colony
     *
     * @param event ItemTossEvent
     */
    @SubscribeEvent
    public void on(final ItemTossEvent event)
    {
        checkEventCancelation(Action.TOSS_ITEM, event.getPlayer(), event.getPlayer().getEntityWorld(), event);
    }

    /**
     * EntityItemPickupEvent handler.
     * <p>
     * Check, if a player tries to pickup a block.
     * Deny if:
     * - If the pickUp happens in the colony
     * - player is neutral or hostile to colony
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final EntityItemPickupEvent event)
    {
        checkEventCancelation(Action.PICKUP_ITEM, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event);
    }

    /**
     * FillBucketEvent handler.
     * <p>
     * Check, if a player tries to fill a bucket.
     * Deny if:
     * - If the fill happens in the colony
     * - player is neutral or hostile to colony
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final FillBucketEvent event)
    {
        checkEventCancelation(Action.FILL_BUCKET, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event);
    }

    /**
     * ArrowLooseEvent handler.
     * <p>
     * Check, if a player tries to shoot an arrow.
     * Deny if:
     * - If the shooting happens in the colony
     * - player is neutral or hostile to colony
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final ArrowLooseEvent event)
    {
        checkEventCancelation(Action.SHOOT_ARROW, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event);
    }

    /**
     * AttackEntityEvent handler.
     * <p>
     * Check, if a player tries to attack an entity..
     * Deny if:
     * - If the attacking happens in the colony
     * - Player is less than officer to the colony.
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final AttackEntityEvent event)
    {
        if (event.getTarget() instanceof EntityMob)
        {
            return;
        }

        @NotNull final EntityPlayer player = EntityUtils.getPlayerOfFakePlayer(event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld());

        if (Configurations.gameplay.enableColonyProtection
              && colony.isCoordInColony(player.getEntityWorld(), player.getPosition()))
        {
            final Permissions perms = colony.getPermissions();
            if (event.getTarget() instanceof EntityCitizen)
            {
                final EntityCitizen citizen = (EntityCitizen) event.getTarget();
                if (citizen.getColonyJob() instanceof JobGuard && perms.hasPermission(event.getEntityPlayer(), Action.GUARDS_ATTACK))
                {
                    return;
                }

                if (perms.hasPermission(event.getEntityPlayer(), Action.ATTACK_CITIZEN))
                {
                    return;
                }

                cancelEvent(event, player);
                return;
            }

            if (!(event.getTarget() instanceof  EntityMob) && !perms.hasPermission(event.getEntityPlayer(), Action.ATTACK_ENTITY))
            {
                cancelEvent(event, player);
            }
        }
    }
}
