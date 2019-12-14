package com.minecolonies.coremod.permissions;

import com.ldtteam.structurize.items.ItemScanTool;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.TOWNHALL_BREAKING_MESSAGE;

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
     * Progress in % of breaking the townHall.
     */
    private int breakProgressOnTownHall = 0;

    /**
     * Ticks at which townhall breaking started.
     */
    private long lastTownHallBreakingTick = 0;

    /**
     * Detect if the town-hall break was valid.
     */
    private boolean validTownHallBreak = false;

    /**
     * The last time the player was notified about not having permission.
     */
    private Map<UUID, Long> lastPlayerNotificationTick = new HashMap<>();

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
        final Action action = event.getPlacedBlock().getBlock() instanceof AbstractBlockHut ? Action.PLACE_HUTS : Action.PLACE_BLOCKS;
        if (Configurations.gameplay.enableColonyProtection && checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getPlacedBlock(), action))
        {
            cancelEvent(event, event.getPlayer(), colony, action, event.getPos());
        }
    }

    /**
     * This method returns TRUE if this event should be denied.
     *
     * @param worldIn    the world to check in
     * @param posIn      the block to check
     * @param playerIn   the player who tries
     * @param blockState the state that block is in
     * @param action     the action that was performed on the position
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

            return !colony.getPermissions().hasPermission(player, action);
        }

        /*
         * - We are not inside the colony
         * - We are in but not denied
         */
        return false;
    }

    /**
     * Cancel an event and record the denial details in the colony's town hall.
     * @param event the event to cancel
     * @param player the player whose action was denied
     * @param colony the colony where the event took place
     * @param action the action which was denied
     * @param pos the location of the action which was denied
     */
    private void cancelEvent(final Event event, @Nullable final EntityPlayer player, final Colony colony, final Action action, final BlockPos pos)
    {
        event.setResult(Event.Result.DENY);
        if (event.isCancelable())
        {
            event.setCanceled(true);
            if (player == null)
            {
                if (colony.hasTownHall())
                {
                    colony.getBuildingManager().getTownHall().addPermissionEvent(new PermissionEvent(null, "-", action, pos));
                }
                return;
            }
            if (colony.hasTownHall())
            {
                colony.getBuildingManager().getTownHall().addPermissionEvent(new PermissionEvent(player.getUniqueID(), player.getName(), action, pos));
            }


            if (player instanceof FakePlayer)
            {
                return;
            }

            final long worldTime = player.world.getWorldTime();
            if (!lastPlayerNotificationTick.containsKey(player.getUniqueID()) || lastPlayerNotificationTick.get(player.getUniqueID()) + (Constants.TICKS_SECOND * Configurations.gameplay.secondsBetweenPermissionMessages ) < worldTime)
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.permission.no");
                lastPlayerNotificationTick.put(player.getUniqueID(), worldTime);
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
        final World world = event.getWorld();
        if (!Configurations.gameplay.enableColonyProtection || world.isRemote)
        {
            return;
        }

        if (event.getState().getBlock() instanceof AbstractBlockHut)
        {
            @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, event.getPos());
            if (building == null)
            {
                return;
            }

            if (event.getState().getBlock() == ModBlocks.blockHutTownHall && !validTownHallBreak && !event.getPlayer().capabilities.isCreativeMode)
            {
                cancelEvent(event, event.getPlayer(), colony, Action.BREAK_HUTS, event.getPos());
                return;
            }

            if (!building.getColony().getPermissions().hasPermission(event.getPlayer(), Action.BREAK_HUTS))
            {
                if (checkEventCancelation(Action.BREAK_HUTS, event.getPlayer(), event.getWorld(), event, event.getPos()))
                {
                    return;
                }
            }

            building.destroy();

            if (Configurations.gameplay.pvp_mode && event.getState().getBlock() == ModBlocks.blockHutTownHall)
            {
                IColonyManager.getInstance().deleteColonyByWorld(building.getColony().getID(), false, event.getWorld());
            }
        }
        else
        {
            checkEventCancelation(Action.BREAK_BLOCKS, event.getPlayer(), event.getWorld(), event, event.getPos());
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
        if (!Configurations.gameplay.turnOffExplosionsInColonies)
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
            cancelEvent(event, null, colony, Action.EXPLODE, new BlockPos(event.getExplosion().getPosition()));
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
            if (event instanceof PlayerInteractEvent.RightClickBlock && block instanceof AbstractBlockHut
                  && !colony.getPermissions().hasPermission(event.getEntityPlayer(), Action.ACCESS_HUTS))
            {
                cancelEvent(event, event.getEntityPlayer(), colony, Action.ACCESS_HUTS, event.getPos());
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
                if (!perms.hasPermission(event.getEntityPlayer(), Action.RIGHTCLICK_BLOCK) && block != Blocks.AIR)
                {
                    checkEventCancelation(Action.RIGHTCLICK_BLOCK, event.getEntityPlayer(), event.getWorld(), event, event.getPos());
                    return;
                }

                if (block instanceof BlockContainer && !perms.hasPermission(event.getEntityPlayer(),
                  Action.OPEN_CONTAINER))
                {
                    cancelEvent(event, event.getEntityPlayer(), colony, Action.OPEN_CONTAINER, event.getPos());
                    return;
                }

                if (event.getWorld().getTileEntity(event.getPos()) != null && !perms.hasPermission(event.getEntityPlayer(), Action.RIGHTCLICK_ENTITY))
                {
                    checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntityPlayer(), event.getWorld(), event, event.getPos());
                    return;
                }

                final ItemStack stack = event.getItemStack();
                if (ItemStackUtils.isEmpty(stack) || stack.getItem() instanceof ItemFood)
                {
                    return;
                }


                if (stack.getItem() instanceof ItemPotion)
                {
                    checkEventCancelation(Action.THROW_POTION, event.getEntityPlayer(), event.getWorld(), event, event.getPos());
                    return;
                }

                if (stack.getItem() instanceof ItemScanTool
                      && !perms.hasPermission(event.getEntityPlayer(), Action.USE_SCAN_TOOL))
                {
                    cancelEvent(event, event.getEntityPlayer(), colony, Action.USE_SCAN_TOOL, event.getPos());
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

        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntityPlayer(), event.getWorld(), event, event.getPos());
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
    public void on(final PlayerEvent.BreakSpeed event)
    {
        if (colony.isCoordInColony(event.getEntity().world, event.getPos()) && Configurations.gameplay.pvp_mode && event.getState().getBlock() == ModBlocks.blockHutTownHall
              && event.getEntityPlayer().world instanceof WorldServer)
        {
            final World world = event.getEntityPlayer().world;
            final double localProgress = breakProgressOnTownHall;
            final double hardness = event.getState().getBlockHardness(world, event.getPos()) * 20.0 * 1.5 / event.getNewSpeed();

            if (localProgress >= hardness / 10.0 * 9.0 && localProgress <= hardness / 10.0 * 9.0 + 1)
            {
                LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), TOWNHALL_BREAKING_MESSAGE, event.getEntityPlayer().getName(), 90);
            }
            if (localProgress >= hardness / 4.0 * 3.0 && localProgress <= hardness / 4.0 * 3.0 + 1)
            {
                LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), TOWNHALL_BREAKING_MESSAGE, event.getEntityPlayer().getName(), 75);
            }
            else if (localProgress >= hardness / 2.0 && localProgress <= hardness / 2.0 + 1)
            {
                LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), TOWNHALL_BREAKING_MESSAGE, event.getEntityPlayer().getName(), 50);
            }
            else if (localProgress >= hardness / 4.0 && localProgress <= hardness / 4.0 + 1)
            {
                LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), TOWNHALL_BREAKING_MESSAGE, event.getEntityPlayer().getName(), 25);
            }

            if (localProgress >= hardness - 1)
            {
                validTownHallBreak = true;
            }

            if (world.getTotalWorldTime() - lastTownHallBreakingTick == 1)
            {
                breakProgressOnTownHall++;
            }
            else
            {
                LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(),
                  "com.minecolonies.coremod.pvp.townhall.break.start",
                  event.getEntityPlayer().getName());
                breakProgressOnTownHall = 0;
                validTownHallBreak = false;
            }
            lastTownHallBreakingTick = world.getTotalWorldTime();
        }
        else if (!Configurations.gameplay.pvp_mode)
        {
            validTownHallBreak = true;
        }
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
    private boolean checkEventCancelation(final Action action, @NotNull final EntityPlayer playerIn, @NotNull final World world, @NotNull final Event event,
            @Nullable final BlockPos pos)
    {
        @NotNull final EntityPlayer player = EntityUtils.getPlayerOfFakePlayer(playerIn, world);

        BlockPos positionToCheck = pos;
        if (null == positionToCheck)
        {
            positionToCheck = player.getPosition();
        }
        if (Configurations.gameplay.enableColonyProtection
              && colony.isCoordInColony(player.getEntityWorld(), positionToCheck)
              && !colony.getPermissions().hasPermission(player, action))
        {
            if (Configurations.gameplay.pvp_mode)
            {
                if (!world.isRemote && colony.isValidAttackingPlayer(playerIn))
                {
                    return false;
                }
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
        checkEventCancelation(Action.RIGHTCLICK_ENTITY, event.getEntityPlayer(), event.getWorld(), event, event.getPos());
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
        if (checkEventCancelation(Action.TOSS_ITEM, event.getPlayer(), event.getPlayer().getEntityWorld(), event, event.getPlayer().getPosition()))
        {
            event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getItem());
        }
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
        checkEventCancelation(Action.PICKUP_ITEM, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event, event.getEntityPlayer().getPosition());
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
        @Nullable BlockPos targetBlockPos = null;
        if (null != event.getTarget())
        {
            targetBlockPos = event.getTarget().getBlockPos();
        }
        checkEventCancelation(Action.FILL_BUCKET, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event, targetBlockPos);
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
        checkEventCancelation(Action.SHOOT_ARROW, event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld(), event, event.getEntity().getPosition());
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
                final AbstractEntityCitizen citizen = (AbstractEntityCitizen) event.getTarget();
                if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && perms.hasPermission(event.getEntityPlayer(), Action.GUARDS_ATTACK))
                {
                    return;
                }

                if (perms.hasPermission(event.getEntityPlayer(), Action.ATTACK_CITIZEN))
                {
                    return;
                }

                cancelEvent(event, event.getEntityPlayer(), colony, Action.ATTACK_CITIZEN, event.getTarget().getPosition());
                return;
            }

            if (!(event.getTarget() instanceof EntityMob) && !perms.hasPermission(event.getEntityPlayer(), Action.ATTACK_ENTITY))
            {
                cancelEvent(event, event.getEntityPlayer(), colony, Action.ATTACK_ENTITY, event.getTarget().getPosition());
            }
        }
    }
}
