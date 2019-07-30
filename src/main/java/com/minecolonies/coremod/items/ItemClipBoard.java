package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.ITileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the clipboard item.
 */
public class ItemClipBoard extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    private static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     */
    public ItemClipBoard()
    {
        super("clipboard");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(STACKSIZE);
    }

    /**
     * Used when clicking on block in world.
     *
     * @param playerIn the player
     * @param worldIn  the world
     * @param pos      the position
     * @param hand     the hand
     * @param facing   the facing hit
     * @param hitX     the x coordinate
     * @param hitY     the y coordinate
     * @param hitZ     the z coordinate
     * @return the result
     */
    @Override
    @NotNull
    public EnumActionResult onItemUse(
                                       final PlayerEntity playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final Direction facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        final ItemStack clipboard = playerIn.getHeldItem(hand);

        final CompoundNBT compound = checkForCompound(clipboard);
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.setInteger(TAG_COLONY, ((ITileEntityColonyBuilding) entity).getColonyId());
            if (!worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET, ((ITileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.keySet().contains(TAG_COLONY))
        {
            if (worldIn.isRemote)
            {
                final int colonyId = compound.getInt(TAG_COLONY);
                MineColonies.proxy.openClipBoardWindow(colonyId);
            }
        }

        return EnumActionResult.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public ActionResult<ItemStack> onItemRightClick(
                                                     final World worldIn,
                                                     final PlayerEntity playerIn,
                                                     final EnumHand hand)
    {
        final ItemStack cllipboard = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
        }

        final CompoundNBT compound = checkForCompound(cllipboard);

        if (compound.keySet().contains(TAG_COLONY))
        {
            final int colonyId = compound.getInt(TAG_COLONY);
            MineColonies.proxy.openClipBoardWindow(colonyId);
        }
        else
        {
            LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_NEED_COLONY);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
    }

    /**
     * Check for the compound and return it.
     * If not available create and return it.
     *
     * @param scepter the scepter to check in for.
     */
    private static CompoundNBT checkForCompound(final ItemStack scepter)
    {
        if (!scepter.hasTagCompound())
        {
            scepter.put(new CompoundNBT());
        }
        return scepter.getTag();
    }
}
