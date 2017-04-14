package com.minecolonies.coremod.items;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.network.messages.ChangeFreeToInteractBlockMessage;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterPermission extends AbstractItemMinecolonies
{
    /**
     */
    private static final String TAG_ITEM_MODE = "scepterMode";

    private static final String TAG_VALUE_MODE_BLOCK = "modeBlock";
    private static final String TAG_VALUE_MODE_LOCATION = "modeLocation";

    /**
     */
    public ItemScepterPermission()
    {
        super("scepterPermission");
        this.setMaxDamage(2);

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack scepter, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        if (worldIn.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        ColonyView colonyView = ColonyManager.getClosestColonyView(worldIn, pos);
        if (colonyView == null)
        {
            return EnumActionResult.FAIL;
        }

        String tagItemMode = compound.getString(TAG_ITEM_MODE);

        switch(tagItemMode) {
            case TAG_VALUE_MODE_BLOCK:
                return handleAddBlockType(playerIn, worldIn, pos, colonyView);
            case TAG_VALUE_MODE_LOCATION:
                return handleAddLocation(playerIn, worldIn, pos, colonyView);
            default:
                // TODO Exception?
                Log.getLogger().warn("Invalid tag item mode in ItemScepterPermission");
        }

        return EnumActionResult.FAIL;
    }

    /**
     * handles mid air use.
     *
     * @param scepter
     * @param worldIn
     * @param playerIn
     * @param hand
     * @return
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack scepter, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            return new ActionResult(EnumActionResult.PASS, scepter);
        }

        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        toggleItemMode(playerIn, compound);

        return new ActionResult(EnumActionResult.PASS, scepter);
    }

    private void toggleItemMode(EntityPlayer playerIn, NBTTagCompound compound)
    {
        String itemMode = compound.getString(TAG_ITEM_MODE);

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "location");
                break;
            case TAG_VALUE_MODE_LOCATION:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_BLOCK);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "block");
                break;
            default:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
        }
    }

    static private EnumActionResult handleAddBlockType(EntityPlayer playerIn, World worldIn, BlockPos pos, ColonyView colonyView)
    {
        IBlockState blockState = worldIn.getBlockState(pos);
        Block block = blockState.getBlock();

        ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(colonyView, block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

    static private EnumActionResult handleAddLocation(EntityPlayer playerIn, World worldIn, BlockPos pos, ColonyView colonyView)
    {
        ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(colonyView, pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

}
