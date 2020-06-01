package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
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

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemBannerRallyGuards extends AbstractItemMinecolonies
{
    /**
     * The compound tag for the activity status of the banner
     */
    private static final String TAG_IS_ACTIVE = "isActive";

    @Nullable
    private ITextComponent cachedTooltip = null;

    /**
     * Guard Scepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemBannerRallyGuards(final Properties properties)
    {
        super("banner_rally_guards", properties.maxStackSize(1).maxDamage(0).group(ModCreativeTabs.MINECOLONIES));
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(final ItemUseContext ctx)
    {
        final ItemStack scepter = ctx.getPlayer().getHeldItem(ctx.getHand());

        final CompoundNBT compound = checkForCompound(scepter);
        final TileEntity entity = ctx.getWorld().getTileEntity(ctx.getPos());

        // TODO: Check barracks tower
        if (entity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) entity).registryName.getPath().equalsIgnoreCase("guardtower"))
        {
            final BlockPos position = ((AbstractTileEntityColonyBuilding) entity).getPosition();
            final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
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

            if (!ctx.getWorld().isRemote)
            {
                LanguageHandler.sendPlayerMessage(ctx.getPlayer(),
                  message,
                  ((AbstractTileEntityColonyBuilding) entity).getDisplayName().getUnformattedComponentText(), BlockPosUtil.getString(position));
            }
            cachedTooltip = null; // Reset the tooltip here, it will be regenerated during the next tooltip update.
        }
        else
        {
            // TODO: Toggle banner activation state
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos);
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, @NotNull final Hand hand)
    {
        final ItemStack banner = playerIn.getHeldItem(hand);
        final CompoundNBT compound = checkForCompound(banner);


        if (!worldIn.isRemote && compound != null)
        {
            if (playerIn.isShiftKeyDown())
            {
                final ListNBT guardTowers = (ListNBT) compound.get(TAG_RALLIED_GUARDTOWERS);
                if (guardTowers.size() == 0)
                {
                    // TODO: Open management window
                    return ActionResult.func_226248_a_(banner);
                }

                if (compound.getBoolean(TAG_IS_ACTIVE))
                {
                    compound.putBoolean(TAG_IS_ACTIVE, false);
                    for (int i = 0; i < guardTowers.size(); i++)
                    {
                        final TileEntity entity = worldIn.getTileEntity(NBTUtil.readBlockPos((CompoundNBT) guardTowers.get(i)));
                        if (entity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) entity).registryName.getPath().equalsIgnoreCase("guardtower"))
                        {
                            final IGuardBuilding building = (IGuardBuilding) ((TileEntityColonyBuilding) entity).getBuilding();
                            if (building == null)
                            {
                                Log.getLogger().info("Building not found!");
                                continue;
                            }
                            building.setPlayerToRally(null);
                        }
                    }
                    LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.deactivated");
                }
                else
                {
                    compound.putBoolean(TAG_IS_ACTIVE, true);
                    int numGuards = 0;
                    for (int i = 0; i < guardTowers.size(); i++)
                    {
                        final TileEntity entity = worldIn.getTileEntity(NBTUtil.readBlockPos((CompoundNBT) guardTowers.get(i)));
                        if (entity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) entity).registryName.getPath().equalsIgnoreCase("guardtower"))
                        {
                            final IGuardBuilding building = (IGuardBuilding) ((TileEntityColonyBuilding) entity).getBuilding();
                            if (building == null)
                            {
                                Log.getLogger().info("Building not found!");
                                continue;
                            }
                            building.setPlayerToRally(playerIn);
                            numGuards += building.getAssignedCitizen().size();
                        }
                    }
                    if (numGuards > 0)
                    {
                        LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.activated", numGuards);
                    }
                    else
                    {
                        LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.banner_rally_guards.activated.noguards");
                    }

                    // TODO
                    /*
                            if (this.getColony().getWorld() != null)
        {
            if (player != null)
            {
                this.getColony()
                  .getWorld()
                  .getScoreboard()
                  .addPlayerToTeam(player.getScoreboardName(), new ScorePlayerTeam(this.getColony().getWorld().getScoreboard(), TEAM_COLONY_NAME + getColony().getID()));
                player.addPotionEffect(new EffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION_TEAM, GLOW_EFFECT_MULTIPLIER, false, false));//no reason for particales
            }

            if (rallyPlayer != null)
            {
                try
                {
                    this.getColony()
                      .getWorld()
                      .getScoreboard()
                      .removePlayerFromTeam(rallyPlayer.getScoreboardName(), this.getColony().getWorld().getScoreboard().getTeam(TEAM_COLONY_NAME + getColony().getID()));
                    player.removePotionEffect(GLOW_EFFECT);
                }
                catch (final Exception e)
                {
                    Log.getLogger().warn("Unable to remove player " + rallyPlayer.getName().getFormattedText() + " from team " + TEAM_COLONY_NAME + getColony().getID());
                }
            }
        }
                     */


                }
            }
            else
            {
                // TODO: Open configuration window here.
                Log.getLogger().info("Item configuration opened");
            }
        }

        return ActionResult.func_226248_a_(banner);
    }

    /**
     * Check for the compound and return it.
     * If not available create and return it.
     *
     * @param item the item to check in for.
     * @return the compound of the item.
     */
    private static CompoundNBT checkForCompound(final ItemStack item)
    {
        if (!item.hasTag() || item.getTag().get(TAG_IS_ACTIVE) == null || item.getTag().get(TAG_RALLIED_GUARDTOWERS) == null)
        {
            final CompoundNBT compound = new CompoundNBT();

            compound.putBoolean(TAG_IS_ACTIVE, false);

            @NotNull final ListNBT guardTowerList = new ListNBT();
            compound.put(TAG_RALLIED_GUARDTOWERS, guardTowerList);
            item.setTag(compound);
        }
        return item.getTag();
    }

    @Override
    public void addInformation(
      final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
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
            if (cachedTooltip == null)
            {
                cachedTooltip = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_TOOLTIP, guardTowers.size());
                cachedTooltip.setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_AQUA));
            }
            tooltip.add(cachedTooltip);
        }


        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
