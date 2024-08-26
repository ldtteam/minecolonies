package com.minecolonies.core.items;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.api.items.component.RallyData;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.client.gui.WindowBannerRallyGuards;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.core.colony.requestsystem.locations.StaticLocation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.STANDARD;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Rally Guards Banner Item class. Used to give tasks to guards.
 */
public class ItemBannerRallyGuards extends AbstractItemMinecolonies
{
    /**
     * Rally Guards Banner constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemBannerRallyGuards(final Properties properties)
    {
        super("banner_rally_guards", properties.stacksTo(1).durability(0));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final Player player = context.getPlayer();

        if (player == null)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack banner = context.getPlayer().getItemInHand(context.getHand());
        final RallyData rallyData = RallyData.readFromItemStack(banner);
        if (isGuardBuilding(context.getLevel(), context.getClickedPos()))
        {
            if (context.getLevel().isClientSide())
            {
                return InteractionResult.SUCCESS;
            }
            else
            {
                final IGuardBuilding building = getGuardBuilding(context.getLevel(), context.getClickedPos());
                if (!building.getColony().getPermissions().hasPermission(player, Action.RALLY_GUARDS))
                {
                    MessageUtils.format(PERMISSION_DENIED).sendTo(player);
                    return InteractionResult.FAIL;
                }

                building.getColony().writeToItemStack(banner);
                final ILocation location = building.getLocation();
                if (removeGuardTowerAtLocation(banner, location.getInDimensionLocation()))
                {
                    MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_DESELECTED, building.getSchematicName(), location.toString()).sendTo(player);
                }
                else
                {
                    rallyData.withPosAddition(location.getInDimensionLocation()).writeToItemStack(banner);
                    MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_SELECTED, building.getSchematicName(), location.toString()).sendTo(player);
                }
            }
        }
        else if (context.getLevel().getBlockState(context.getClickedPos()).getBlock().equals(ModBlocks.blockColonyBanner))
        {
            if (context.getLevel().isClientSide())
            {
                return InteractionResult.SUCCESS;
            }

            final IColony colony = getColony(banner, context.getLevel());
            if (colony != null && colony.getPermissions().hasPermission(player, Action.RALLY_GUARDS))
            {
                if (colony.getResearchManager().getResearchEffects().getEffectStrength(STANDARD) <= 0)
                {
                    MessageUtils.format(TOOL_RALLY_BANNER_NEEDS_RESEARCH).sendTo(context.getPlayer());
                    return InteractionResult.FAIL;
                }
                rallyData.withActive(true).writeToItemStack(banner);
                final int numGuards =
                  broadcastPlayerToRally(banner, context.getPlayer().getCommandSenderWorld(), new StaticLocation(context.getClickedPos(), context.getLevel().dimension()));
                if (numGuards > 0)
                {
                    MessageUtils.format(TOOL_RALLY_BANNER_ACTIVATED, numGuards).sendTo(context.getPlayer());
                }
                else
                {
                    MessageUtils.format(TOOL_RALLY_BANNER_NO_GUARDS).sendTo(context.getPlayer());
                }
            }
        }
        else
        {
            handleRightClick(banner, context.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Get the colony from the stack data.
     * @param stack the stack to get it from.
     * @return the colony or null if not found.
     * @deprecated use inline
     */
    @Deprecated(forRemoval = true, since = "1.21")
    @Nullable
    private static IColony getColony(final ItemStack stack, final Level world)
    {
        return ColonyId.readColonyFromItemStack(stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        final ItemStack banner = playerIn.getItemInHand(handIn);
        handleRightClick(banner, playerIn);
        return InteractionResultHolder.success(banner);
    }

    @Override
    public boolean onDroppedByPlayer(final ItemStack item, final Player player)
    {
        if (!player.getCommandSenderWorld().isClientSide())
        {
            RallyData.updateItemStack(item, rally -> rally.withActive(false));
            broadcastPlayerToRally(item, player.getCommandSenderWorld(), null);
        }

        return super.onDroppedByPlayer(item, player);
    }

    /**
     * Handles a rightclick or rightclick-while-sneaking that's *not* adding/removing a guard tower from the list
     *
     * @param banner   The banner with which the player rightclicked.
     * @param playerIn The player that rightclicked.
     */
    private void handleRightClick(final ItemStack banner, final Player playerIn)
    {
        if (playerIn.isShiftKeyDown() && !playerIn.getCommandSenderWorld().isClientSide())
        {
            toggleBanner(banner, playerIn);
        }
        else if (!playerIn.isShiftKeyDown() && playerIn.getCommandSenderWorld().isClientSide())
        {
            if (getGuardTowerLocations(banner).isEmpty())
            {
                MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY).sendTo(playerIn);
            }
            else
            {
                new WindowBannerRallyGuards(banner).open();
            }
        }
    }

    /**
     * Toggles the banner. This cannot be done by "the system" but must happen from here by the player. (Note that it will also send chat messages to the player) Thus, this method
     * is private on purpose (for now).
     *
     * @param banner   The banner to toggle
     * @param playerIn The player toggling the banner
     */
    public static void toggleBanner(final ItemStack banner, final Player playerIn)
    {
        if (playerIn.getCommandSenderWorld().isClientSide())
        {
            Log.getLogger().error("Tried to run server-side function #toggleBanner() on the client-side!");
            return;
        }
        final RallyData rallyData = RallyData.readFromItemStack(banner);
        if (rallyData == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return;
        }

        boolean activeRaid = false;
        if (rallyData.towers().isEmpty())
        {
            MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY).sendTo(playerIn);
        }
        else if (rallyData.active())
        {
            broadcastPlayerToRally(banner, playerIn.getCommandSenderWorld(), null);
            MessageUtils.format(TOOL_RALLY_BANNER_DEACTIVATED).sendTo(playerIn);
        }
        else
        {
            activeRaid = true;
           final IColony colony = getColony(banner, playerIn.level());
           if (colony != null && colony.getPermissions().hasPermission(playerIn, Action.RALLY_GUARDS))
           {
               final int numGuards = broadcastPlayerToRally(banner, playerIn.getCommandSenderWorld(), playerIn == null ? null : new EntityLocation(playerIn.getUUID()));

               if (numGuards > 0)
               {
                   MessageUtils.format(TOOL_RALLY_BANNER_ACTIVATED, numGuards).sendTo(playerIn);
               }
               else
               {
                   MessageUtils.format(TOOL_RALLY_BANNER_NO_GUARDS).sendTo(playerIn);
               }
           }
        }
        rallyData.withActive(activeRaid).writeToItemStack(banner);

    }

    /**
     * Broadcasts the player all the guardtowers rallied by the item are supposed to follow.
     *
     * @param banner   The banner that should broadcast
     * @return The number of guards rallied
     */
    public static int broadcastPlayerToRally(final ItemStack banner, final Level worldIn, @Nullable final ILocation rallyLocation)
    {
        if (worldIn.isClientSide())
        {
            Log.getLogger().error("Tried to run server-side function #broadcastPlayerToRally() on the client-side!");
            return 0;
        }

        @Nullable ILocation rallyTarget = null;
        if (!isActive(banner) || rallyLocation == null)
        {
            rallyTarget = null;
        }
        else
        {
            rallyTarget = rallyLocation;
        }

        int numGuards = 0;
        for (final BlockPos guardTowerLocation : getGuardTowerLocations(banner))
        {
            // Note: getCurrentServer().getWorld() must be used here because MineColonies.proxy.getWorld() fails on single player worlds
            // We are sure we are on the server-side in this function though, so it's fine.
            final IBuilding building = getColony(banner, worldIn).getBuildingManager().getBuilding(guardTowerLocation);

            // If the building is null, it means that guardtower has been moved/destroyed since being added.
            // Safely ignore this case, the player must remove the tower from the rallying list manually.
            if (building instanceof IGuardBuilding iGuardBuilding)
            {
                iGuardBuilding.setRallyLocation(rallyTarget);
                numGuards += building.getAllAssignedCitizen().size();
            }
        }
        return numGuards;
    }

    /**
     * Returns the guard tower positions of towers rallied by the given banner.
     *
     * @param banner The banner of which the guard towers should be retrieved
     * @return The list of guardtower positions, or an empty list if anything goes wrong during retrieval.
     * @deprecated use inline
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static List<BlockPos> getGuardTowerLocations(final ItemStack banner)
    {
        return RallyData.readFromItemStack(banner).towers();
    }

    /**
     * Checks if the position is a guard building
     *
     * @param worldIn  The world in which to check
     * @param position The position to check
     * @return true if there is a guard building at the position
     */
    public static boolean isGuardBuilding(final Level worldIn, final BlockPos position)
    {
        if (worldIn.isClientSide())
        {
            return IColonyManager.getInstance().getBuildingView(worldIn.dimension(), position) instanceof AbstractBuildingGuards.View;
        }
        else
        {
            return IColonyManager.getInstance().getBuilding(worldIn, position) instanceof IGuardBuilding;
        }
    }

    /**
     * Fetches the (client-side) View for the guard tower at a specific position.
     *
     * @param worldIn  The world in which to search for the guard tower.
     * @param position The position of the guard tower.
     * @return The Guard tower View, or null if no guard tower present at the location.
     */
    @Nullable
    public static AbstractBuildingGuards.View getGuardBuildingView(final Level worldIn, final BlockPos position)
    {
        if (!worldIn.isClientSide())
        {
            Log.getLogger().error("Tried to run client-side function #getGuardBuildingView() on the server-side!");
            return null;
        }

        return isGuardBuilding(worldIn, position)
                 ? (AbstractBuildingGuards.View) IColonyManager.getInstance().getBuildingView(worldIn.dimension(), position)
                 : null;
    }

    /**
     * Fetches the (server-side) buildings for the guard tower at a specific position.
     *
     * @param worldIn  The world in which to search for the guard tower.
     * @param position The position of the guard tower.
     * @return The building, or null if no guard tower present at the location.
     */
    @Nullable
    public static IGuardBuilding getGuardBuilding(final Level worldIn, final BlockPos position)
    {
        if (worldIn.isClientSide())
        {
            Log.getLogger().error("Tried to run server-side function #getGuardBuilding() on the client-side!");
            return null;
        }

        return isGuardBuilding(worldIn, position) ? (IGuardBuilding) IColonyManager.getInstance().getBuilding(worldIn, position) : null;
    }

    /**
     * Fetches the (client-side) Views of the guard towers rallied by the banner. If a rallied position is not a guard tower anymore (tower was moved or destroyed), the
     * corresponding entry will be null.
     *
     * @return A list of maps. Map's key is the position, Map's value is a guard tower or null.
     */
    public static List<Pair<BlockPos, AbstractBuildingGuards.View>> getGuardTowerViews(final ItemStack banner, final Level level)
    {
        final LinkedList<Pair<BlockPos, AbstractBuildingGuards.View>> result = new LinkedList<>();
        for (final BlockPos guardTowerLocation : getGuardTowerLocations(banner))
        {
            result.add(new Pair<>(guardTowerLocation,
              getGuardBuildingView(level, guardTowerLocation)));
        }
        return ImmutableList.copyOf(result);
    }

    /**
     * Checks if the given banner is active and valid for the given guardtower.
     *
     * @param banner     The banner to check
     * @param guardTower The guardtower to check
     * @return true if the banner is active and has guardTower in the list.
     */
    public boolean isActiveForGuardTower(final ItemStack banner, final IGuardBuilding guardTower)
    {
        if (!isActive(banner))
        {
            return false;
        }

        for (final BlockPos existingTower : getGuardTowerLocations(banner))
        {
            if (existingTower.equals(guardTower.getLocation().getInDimensionLocation()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the banner is active
     *
     * @param stack The banner that should be checked
     * @return true if the banner is active
     * @deprecated use inline
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static boolean isActive(final ItemStack stack)
    {
        return RallyData.readFromItemStack(stack).active();
    }

    /**
     * Removes the guard tower from the rallying list based on its position
     *
     * @param banner             The banner to remove the guard tower from
     * @param guardTowerLocation The location of the guard tower
     * @return true if a tower has been removed
     */
    public static boolean removeGuardTowerAtLocation(final ItemStack banner, final BlockPos guardTowerLocation)
    {
        final RallyData old = RallyData.readFromItemStack(banner);
        final RallyData modified = old.withPosRemoval(guardTowerLocation);
        if (old != modified) modified.writeToItemStack(banner);
        return old != modified;
    }


    @Override
    public boolean isFoil(@NotNull final ItemStack stack)
    {
        return isActive(stack);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        final MutableComponent rallyHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_RALLY);
        rallyHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(rallyHint);

        final List<BlockPos> guardTowerPositions = getGuardTowerLocations(stack);

        if (guardTowerPositions.isEmpty())
        {
            final MutableComponent emptyTooltip = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
            emptyTooltip.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            tooltip.add(emptyTooltip);
        }
        else
        {
            final MutableComponent numGuardTowers = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP, guardTowerPositions.size());
            numGuardTowers.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
            tooltip.add(numGuardTowers);
        }


        super.appendHoverText(stack, ctx, tooltip, flagIn);
    }
}
