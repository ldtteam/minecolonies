package com.minecolonies.coremod.items;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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

    @Override
    public EnumActionResult onItemUse(
            final ItemStack stack,
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        final ItemStack clipboard = playerIn.getHeldItem(hand);

        final NBTTagCompound compound = checkForCompound(clipboard);
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.setInteger(TAG_COLONY, ((TileEntityColonyBuilding) entity).getColonyId());
            if (!worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET, ((TileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.hasKey(TAG_COLONY))
        {
            if (!worldIn.isRemote)
            {
                final int colonyId = compound.getInteger(TAG_COLONY);
                MineColonies.proxy.openClipBoardWindow(colonyId);
            }
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack itemStackIn, final World worldIn, final EntityPlayer playerIn, final EnumHand hand)
    {
        final ItemStack cllipboard = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
        }

        final NBTTagCompound compound = checkForCompound(cllipboard);

        if (compound.hasKey(TAG_COLONY))
        {
            final int colonyId = compound.getInteger(TAG_COLONY);
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
    private static NBTTagCompound checkForCompound(final ItemStack scepter)
    {
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        return scepter.getTagCompound();
    }
}
