package com.minecolonies.coremod.items;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RALLIED_GUARDTOWERS;

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
        super("banner_rally_guards", properties.maxStackSize(1).maxDamage(0).group(ModCreativeTabs.MINECOLONIES));
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final ItemStack banner = context.getPlayer().getHeldItem(context.getHand());

        final CompoundNBT compound = checkForCompound(banner);
        final TileEntity entity = context.getWorld().getTileEntity(context.getPos());

        if (isGuardBuilding(entity))
        {
            if (context.getWorld().isRemote())
            {
                return ActionResultType.SUCCESS;
            }

            final IGuardBuilding building = getGuardBuildingFromTileEntity(entity);

            final ILocation location = building.getLocation();
            if (removeGuardTowerAtLocation(banner, location))
            {
                LanguageHandler.sendPlayerMessage(context.getPlayer(),
                  TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_DESELECTED,
                  building.getSchematicName(), location.toString());
            }
            else
            {
                final ListNBT guardTowers = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);
                guardTowers.add(StandardFactoryController.getInstance().serialize(location));
                LanguageHandler.sendPlayerMessage(context.getPlayer(),
                  TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_SELECTED,
                  building.getSchematicName(), location.toString());
            }
        }
        else
        {
            handleRightClick(banner, context.getPlayer());
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final Hand handIn)
    {
        final ItemStack banner = playerIn.getHeldItem(handIn);
        handleRightClick(banner, playerIn);
        return ActionResult.func_226248_a_(banner);
    }

    private void handleRightClick(final ItemStack banner, final PlayerEntity playerIn)
    {
        if (playerIn.isShiftKeyDown() && !playerIn.getEntityWorld().isRemote())
        {
            toggleBanner(banner, playerIn);
        }
        else if (!playerIn.isShiftKeyDown() && playerIn.getEntityWorld().isRemote())
        {
            if (getGuardTowerLocations(banner).isEmpty())
            {
                LanguageHandler.sendPlayerMessage(playerIn,
                  TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
            }
            else
            {
                MineColonies.proxy.openBannerRallyGuardsWindow(banner, playerIn);
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(final ItemStack item, final PlayerEntity player)
    {
        if (!player.getEntityWorld().isRemote())
        {
            final CompoundNBT compound = checkForCompound(item);
            compound.putBoolean(TAG_IS_ACTIVE, false);
            broadcastPlayerToRally(item, player.getEntityWorld(), null);
        }

        return super.onDroppedByPlayer(item, player);
    }

    /**
     * Toggles the banner. This cannot be done by "the system" but must happen from here by the player.
     * (Note that it will also send chat messages to the player)
     * Thus, this method is private on purpose (for now).
     *
     * @param banner   The banner to toggle
     * @param playerIn The player toggling the banner
     */
    public static void toggleBanner(final ItemStack banner, final PlayerEntity playerIn)
    {
        if (playerIn.getEntityWorld().isRemote())
        {
            Log.getLogger().error("ItemBannerRallyGuards#toggleBanner is not supposed to be run on the client-side! Returning 0.");
            return;
        }
        final CompoundNBT compound = checkForCompound(banner);
        final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
        if (guardTowers == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return;
        }
        if (guardTowers.isEmpty())
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            LanguageHandler.sendPlayerMessage(playerIn,
              TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
        }
        else if (compound.getBoolean(TAG_IS_ACTIVE))
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            broadcastPlayerToRally(banner, playerIn.getEntityWorld(), null);
            LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.deactivated");
        }
        else
        {
            compound.putBoolean(TAG_IS_ACTIVE, true);
            final int numGuards = broadcastPlayerToRally(banner, playerIn.getEntityWorld(), playerIn);

            if (numGuards > 0)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.activated", numGuards);
            }
            else
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.activated.noguards");
            }
        }
    }

    /**
     * Broadcasts the player all the guardtowers rallied by the item are supposed to follow.
     *
     * @param banner   The banner that should broadcast
     * @param playerIn The player to follow. Can be null, if the towers should revert to "normal" mode
     * @return The number of guards rallied
     */

    public static int broadcastPlayerToRally(final ItemStack banner, final World worldIn, @Nullable final PlayerEntity playerIn)
    {
        if (worldIn.isRemote())
        {
            Log.getLogger().error("ItemBannerRallyGuards#broadcastPlayerToRally is not supposed to be run on the client-side! Returning 0.");
            return 0;
        }

        final CompoundNBT compound = checkForCompound(banner);
        @Nullable PlayerEntity rallyTarget = playerIn;
        if (!compound.getBoolean(TAG_IS_ACTIVE))
        {
            rallyTarget = null;
        }

        int numGuards = 0;
        for (final ILocation guardTowerLocation : getGuardTowerLocations(banner))
        {

            final TileEntity entity = worldIn.getTileEntity(guardTowerLocation.getInDimensionLocation());
            // Note that getGuardBuildingFromTileEntity will perform the null-check for entity
            final IGuardBuilding building = getGuardBuildingFromTileEntity(entity);
            if (building != null)
            {
                building.setPlayerToRally(rallyTarget);
                numGuards += building.getAssignedCitizen().size();
            }
            // If the building is null, it means that guardtower has been moved/destroyed since being added.
            // Safely ignore this case, the player must remove the tower from the rallying list manually.
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
        final CompoundNBT compound = checkForCompound(banner);
        final ListNBT guardTowersListNBT = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);
        if (guardTowersListNBT == null)
        {
            // Log this error, but since this should never happen, let's just
            // return an empty list in this case.
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return ImmutableList.of();
        }

        final List<ILocation> resultList = new ArrayList<>(guardTowersListNBT.size());
        for (final INBT guardTowerNBT : guardTowersListNBT)
        {
            resultList.add(StandardFactoryController.getInstance().deserialize((CompoundNBT) guardTowerNBT));
        }
        return ImmutableList.copyOf(resultList);
    }

    /**
     * Checks if the tile entity is a guard building.
     *
     * @param entity The tile entity that's hopefully a guard building
     * @return true if the tile entity is a guard building, false if not.
     */
    public static boolean isGuardBuilding(final TileEntity entity)
    {
        if (entity instanceof TileEntityColonyBuilding)
        {
            final String registryPath = ((TileEntityColonyBuilding) entity).registryName.getPath();
            if (registryPath.equalsIgnoreCase("guardtower") || registryPath.equalsIgnoreCase("barrackstower"))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the guard building from the tile entity.
     * This method is only meaningful on the server-side! Client-side will always return null!
     *
     * @param entity The tile entity that's hopefully a guard building
     * @return The guard building, or null if not found, or null if run on client-side.
     */
    public static IGuardBuilding getGuardBuildingFromTileEntity(final TileEntity entity)
    {
        if (isGuardBuilding(entity))
        {
            return (IGuardBuilding) ((TileEntityColonyBuilding) entity).getBuilding();
        }
        return null;
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

    public static boolean isActive(final ItemStack banner)
    {
        final CompoundNBT compound = checkForCompound(banner);
        return compound.getBoolean(TAG_IS_ACTIVE);
    }

    private static UUID getID(final ItemStack banner)
    {
        final CompoundNBT compound = checkForCompound(banner);
        return compound.getUniqueId(TAG_ID);
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
        final CompoundNBT compound = checkForCompound(banner);
        final ListNBT guardTowers = compound.getList(TAG_RALLIED_GUARDTOWERS, TAG_COMPOUND);

        for (int i = 0; i < guardTowers.size(); i++)
        {
            if (StandardFactoryController.getInstance().deserialize((CompoundNBT) guardTowers.get(i)).equals(guardTowerLocation))
            {
                guardTowers.remove(i);
                banner.setTag(compound);
                return true;
            }
        }

        return false;
    }

    /**
     * Check for the compound and return it.
     * If not available create and return it.
     *
     * @param banner the banner to check for a compound.
     * @return the compound of the item.
     */
    public static CompoundNBT checkForCompound(final ItemStack banner)
    {
        if (!banner.hasTag())
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId(TAG_ID, UUID.randomUUID());
            compound.putBoolean(TAG_IS_ACTIVE, false);

            @NotNull final ListNBT guardTowerList = new ListNBT();
            compound.put(TAG_RALLIED_GUARDTOWERS, guardTowerList);
            banner.setTag(compound);
        }
        return banner.getTag();
    }

    @Override
    public boolean hasEffect(@NotNull final ItemStack stack)
    {
        final CompoundNBT compound = checkForCompound(stack);
        return compound.getBoolean(TAG_IS_ACTIVE);
    }

    @Override
    public void addInformation(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final ITextComponent guiHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_GUI);
        guiHint.setStyle(new Style().setColor(TextFormatting.GRAY));
        tooltip.add(guiHint);

        final ITextComponent rallyHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_RALLY);
        rallyHint.setStyle(new Style().setColor(TextFormatting.GRAY));
        tooltip.add(rallyHint);

        final List<ILocation> guardTowerPositions = getGuardTowerLocations(stack);

        // The isEmpty is in there because the tooltip is sometimes loaded before NBT is loaded.
        // Worst case, the 0-towers-tooltip spams allocations, but compared to the rest of Minecraft, that's negligible.
        if (guardTowerPositions.isEmpty())
        {
            final ITextComponent emptyTooltip = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
            emptyTooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            tooltip.add(emptyTooltip);
        }
        else
        {
            final ITextComponent numGuardTowers = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP, guardTowerPositions.size());
            numGuardTowers.setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_AQUA));
            tooltip.add(numGuardTowers);
        }


        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
