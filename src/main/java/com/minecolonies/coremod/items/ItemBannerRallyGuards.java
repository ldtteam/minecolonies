package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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

        final IGuardBuilding building = getGuardBuildingFromTileEntity(entity);
        if (building != null)
        {
            final BlockPos position = building.getPosition();
            final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
            if (guardTowers == null)
            {
                Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
                return ActionResultType.FAIL;
            }
            int indexToRemove = -1;
            for (int i = 0; i < guardTowers.size(); i++)
            {
                if (NBTUtil.readBlockPos(guardTowers.getCompound(i)).equals(position))
                {
                    indexToRemove = i;
                    break;
                }
            }

            String message = null;
            if (indexToRemove > -1)
            {
                guardTowers.remove(indexToRemove);
                message = TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_DESELECTED;
            }
            else
            {
                guardTowers.add(NBTUtil.writeBlockPos(position));
                message = TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_SELECTED;
            }

            if (!context.getWorld().isRemote)
            {
                LanguageHandler.sendPlayerMessage(context.getPlayer(),
                  message,
                  building.getSchematicName(), BlockPosUtil.getString(position));
            }
        }
        else
        {
            if (!context.getWorld().isRemote)
            {
                toggleBanner(banner, context.getPlayer(), context.getWorld());
            }
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, @NotNull final Hand handIn)
    {
        final ItemStack banner = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote)
        {
            if (playerIn.isShiftKeyDown())
            {
                toggleBanner(banner, playerIn, worldIn);
            }
            else
            {
                // TODO: Open configuration window here.
                Log.getLogger().info("Item configuration opened");
            }
        }

        return ActionResult.func_226248_a_(banner);
    }

    @Override
    public boolean onDroppedByPlayer(final ItemStack item, final PlayerEntity player)
    {
        final CompoundNBT compound = checkForCompound(item);
        compound.putBoolean(TAG_IS_ACTIVE, false);
        broadcastPlayerToRally(item, player.getEntityWorld(), null);
        return super.onDroppedByPlayer(item, player);
    }

    /**
     * Toggles the banner. This cannot be done by "the system" but must happen from here by the player.
     * (Note that it will also send chat messages to the player)
     * Thus, this method is private on purpose (for now).
     *
     * @param banner   The banner to toggle
     * @param playerIn The player toggling the banner
     * @param worldIn  The world in which to toggle
     */
    private void toggleBanner(final ItemStack banner, final PlayerEntity playerIn, final World worldIn)
    {
        final CompoundNBT compound = checkForCompound(banner);
        final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
        if (guardTowers == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return;
        }
        if (guardTowers.isEmpty())
        {
            LanguageHandler.sendPlayerMessage(playerIn,
              TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
        }
        else if (compound.getBoolean(TAG_IS_ACTIVE))
        {
            compound.putBoolean(TAG_IS_ACTIVE, false);
            broadcastPlayerToRally(banner, worldIn, null);
            LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.deactivated");
        }
        else
        {
            compound.putBoolean(TAG_IS_ACTIVE, true);
            final int numGuards = broadcastPlayerToRally(banner, worldIn, playerIn);

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
     * @param worldIn  The world in which to broadcast
     * @param playerIn The player to follow. Can be null, if the towers should revert to "normal" mode
     * @return The number of guards rallied
     */
    public static int broadcastPlayerToRally(final ItemStack banner, final World worldIn, @Nullable final PlayerEntity playerIn)
    {
        final CompoundNBT compound = checkForCompound(banner);
        @Nullable PlayerEntity rallyTarget = playerIn;
        if (!compound.getBoolean(TAG_IS_ACTIVE))
        {
            rallyTarget = null;
        }

        final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
        if (guardTowers == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return 0;
        }

        int numGuards = 0;
        for (final INBT guardTower : guardTowers)
        {
            final TileEntity entity = worldIn.getTileEntity(NBTUtil.readBlockPos((CompoundNBT) guardTower));
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

    private static IGuardBuilding getGuardBuildingFromTileEntity(final TileEntity entity)
    {
        if (entity instanceof TileEntityColonyBuilding)
        {
            final String registryPath = ((TileEntityColonyBuilding) entity).registryName.getPath();
            if (registryPath.equalsIgnoreCase("guardtower") || registryPath.equalsIgnoreCase("barrackstower"))
            {
                return (IGuardBuilding) ((TileEntityColonyBuilding) entity).getBuilding();
            }
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
        final CompoundNBT compound = checkForCompound(banner);
        final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
        if (guardTowers == null)
        {
            Log.getLogger().error("Compound corrupt, missing TAG_RALLIED_GUARDTOWERS");
            return false;
        }
        for (final INBT tower : guardTowers)
        {
            if (NBTUtil.readBlockPos((CompoundNBT) tower).equals(guardTower.getPosition()))
            {
                if (compound.getBoolean(TAG_IS_ACTIVE))
                {
                    return true;
                }
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
    private static CompoundNBT checkForCompound(final ItemStack banner)
    {
        if (!banner.hasTag() || banner.getTag().get(TAG_IS_ACTIVE) == null || banner.getTag().get(TAG_RALLIED_GUARDTOWERS) == null)
        {
            final CompoundNBT compound = new CompoundNBT();

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
        final CompoundNBT compound = checkForCompound(stack);
        final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);

        // The isEmpty is in there because the tooltip is sometimes loaded before NBT is loaded.
        // Worst case, the 0-towers-tooltip spams allocations, but compared to the rest of Minecraft, that's negligible.
        if (guardTowers.isEmpty())
        {
            final ITextComponent emptyTooltip = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP_EMPTY);
            emptyTooltip.setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_AQUA));
            tooltip.add(emptyTooltip);
        }
        else
        {
            final ITextComponent numGuardTowers = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP, guardTowers.size());
            numGuardTowers.setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_AQUA));
            tooltip.add(numGuardTowers);
        }


        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
