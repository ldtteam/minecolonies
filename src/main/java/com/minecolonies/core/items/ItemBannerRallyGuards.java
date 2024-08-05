package com.minecolonies.core.items;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.STANDARD;
import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RALLIED_GUARDTOWERS;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Rally Guards Banner Item class. Used to give tasks to guards.
 */
public class ItemBannerRallyGuards extends AbstractItemMinecolonies
{
    /**
     * The compound tag for the activity status of the banner
     */
    private static final String TAG_IS_ACTIVE = "isActive";

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

        final CompoundTag compound = checkForCompound(banner);

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

                final ILocation location = building.getLocation();
                if (removeGuardTowerAtLocation(banner, location))
                {
                    MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_DESELECTED, building.getSchematicName(), location.toString()).sendTo(player);
                }
                else
                {
                    final ListTag guardTowers = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);
                    guardTowers.add(StandardFactoryController.getInstance().serialize(location));
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

            final IColony colony = getColony(compound, context.getLevel());
            if (colony != null && colony.getPermissions().hasPermission(player, Action.RALLY_GUARDS))
            {
                if (colony.getResearchManager().getResearchEffects().getEffectStrength(STANDARD) <= 0)
                {
                    MessageUtils.format(TOOL_RALLY_BANNER_NEEDS_RESEARCH).sendTo(context.getPlayer());
                    return InteractionResult.FAIL;
                }
                compound.putBoolean(TAG_IS_ACTIVE, true);

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
     * Get the colony from the compound data.
     * @param compound the compound to get it from.
     * @return the colony or null if not found.
     */
    @Nullable
    private static IColony getColony(final CompoundTag compound, final Level world)
    {
        final ListTag guardTowersListNBT = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);
        if (guardTowersListNBT == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return null;
        }

        final List<ILocation> resultList = new ArrayList<>(guardTowersListNBT.size());
        for (final Tag guardTowerNBT : guardTowersListNBT)
        {
            ILocation location = StandardFactoryController.getInstance().deserializeTag((CompoundTag) guardTowerNBT);
            if (location.getDimension().equals(world.dimension()))
            {
                final IBuilding building = getGuardBuilding(world, location.getInDimensionLocation());
                if (building != null)
                {
                    return building.getColony();
                }
            }
        }
        return null;
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
            final CompoundTag compound = checkForCompound(item);
            compound.putBoolean(TAG_IS_ACTIVE, false);
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
        final CompoundTag compound = checkForCompound(banner);
        final ListTag guardTowers = (ListTag) compound.get(TAG_RALLIED_GUARDTOWERS);
        if (guardTowers == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return;
        }
        if (guardTowers.isEmpty())
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY).sendTo(playerIn);
        }
        else if (compound.getBoolean(TAG_IS_ACTIVE))
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            broadcastPlayerToRally(banner, playerIn.getCommandSenderWorld(), null);
            MessageUtils.format(TOOL_RALLY_BANNER_DEACTIVATED).sendTo(playerIn);
        }
        else
        {
            compound.putBoolean(TAG_IS_ACTIVE, true);

           final IColony colony = getColony(compound, playerIn.level());
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
        for (final ILocation guardTowerLocation : getGuardTowerLocations(banner))
        {
            // Note: getCurrentServer().getWorld() must be used here because MineColonies.proxy.getWorld() fails on single player worlds
            // We are sure we are on the server-side in this function though, so it's fine.
            final IGuardBuilding building =
              getGuardBuilding(ServerLifecycleHooks.getCurrentServer().getLevel(guardTowerLocation.getDimension()),
                guardTowerLocation.getInDimensionLocation());

            // If the building is null, it means that guardtower has been moved/destroyed since being added.
            // Safely ignore this case, the player must remove the tower from the rallying list manually.
            if (building != null)
            {
                building.setRallyLocation(rallyTarget);
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
     */
    public static ImmutableList<ILocation> getGuardTowerLocations(final ItemStack banner)
    {
        final CompoundTag compound = checkForCompound(banner);
        final ListTag guardTowersListNBT = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);
        if (guardTowersListNBT == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return ImmutableList.of();
        }

        final List<ILocation> resultList = new ArrayList<>(guardTowersListNBT.size());
        for (final Tag guardTowerNBT : guardTowersListNBT)
        {
            resultList.add(StandardFactoryController.getInstance().deserializeTag((CompoundTag) guardTowerNBT));
        }
        return ImmutableList.copyOf(resultList);
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
    public static List<Pair<ILocation, AbstractBuildingGuards.View>> getGuardTowerViews(final ItemStack banner, final Level level)
    {
        final LinkedList<Pair<ILocation, AbstractBuildingGuards.View>> result = new LinkedList<>();
        for (final ILocation guardTowerLocation : getGuardTowerLocations(banner))
        {
            result.add(new Pair<>(guardTowerLocation,
              getGuardBuildingView(level, guardTowerLocation.getInDimensionLocation())));
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

        for (final ILocation existingTower : getGuardTowerLocations(banner))
        {
            if (existingTower.equals(guardTower.getLocation()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the banner is active
     *
     * @param banner The banner that should be checked
     * @return true if the banner is active
     */
    public static boolean isActive(final ItemStack banner)
    {
        final CompoundTag compound = checkForCompound(banner);
        return compound.getBoolean(TAG_IS_ACTIVE);
    }

    /**
     * Removes the guard tower from the rallying list based on its position
     *
     * @param banner             The banner to remove the guard tower from
     * @param guardTowerLocation The location of the guard tower
     * @return true if a tower has been removed
     */
    public static boolean removeGuardTowerAtLocation(final ItemStack banner, final ILocation guardTowerLocation)
    {
        final CompoundTag compound = checkForCompound(banner);
        final ListTag guardTowers = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);

        for (int i = 0; i < guardTowers.size(); i++)
        {
            if (StandardFactoryController.getInstance().deserializeTag((CompoundTag) guardTowers.get(i)).equals(guardTowerLocation))
            {
                guardTowers.remove(i);
                banner.setTag(compound);
                return true;
            }
        }

        return false;
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param banner the banner to check for a compound.
     * @return the compound of the item.
     */
    public static CompoundTag checkForCompound(final ItemStack banner)
    {
        if (!banner.hasTag())
        {
            final CompoundTag compound = new CompoundTag();
            banner.setTag(compound);
        }

        final CompoundTag compound = banner.getTag();
        if (!compound.contains(TAG_RALLIED_GUARDTOWERS))
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            @NotNull final ListTag guardTowerList = new ListTag();
            compound.put(TAG_RALLIED_GUARDTOWERS, guardTowerList);
        }
        else if (compound.contains(TAG_ID))
        {
            compound.remove(TAG_ID);
        }
        return compound;
    }

    @Override
    public boolean isFoil(@NotNull final ItemStack stack)
    {
        final CompoundTag compound = checkForCompound(stack);
        return compound.getBoolean(TAG_IS_ACTIVE);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        final MutableComponent rallyHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_RALLY);
        rallyHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(rallyHint);

        final List<ILocation> guardTowerPositions = getGuardTowerLocations(stack);

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


        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
