package com.minecolonies.items;

import com.minecolonies.MineColonies;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.SaveScanMessage;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemMinecolonies
{
    /**
     * Creates instance of item.
     */
    public ItemScanTool()
    {
        super("scepterSteel");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();

        if (!compound.hasKey("pos1"))
        {
            BlockPosUtil.writeToNBT(compound, "pos1", pos);
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.point");
            }
            return EnumActionResult.SUCCESS;
        }
        else if (!compound.hasKey("pos2"))
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = pos;
            if (pos2.distanceSq(pos1) > 0)
            {
                BlockPosUtil.writeToNBT(compound, "pos2", pos2);
                if (worldIn.isRemote)
                {
                    LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.point2");
                }
                return EnumActionResult.SUCCESS;
            }
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.samePoint");
            }
            return EnumActionResult.FAIL;
        }
        else
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = BlockPosUtil.readFromNBT(compound, "pos2");
            if (!worldIn.isRemote)
            {
                saveStructure(worldIn, pos1, pos2, playerIn);
            }
            compound.removeTag("pos1");
            compound.removeTag("pos2");
            return EnumActionResult.SUCCESS;
        }
    }

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world Current world.
     * @param from  First corner.
     * @param to    Second corner.
     * @param player causing this action.
     */
    private static void saveStructure(@Nullable World world, @Nullable BlockPos from, @Nullable BlockPos to, @NotNull EntityPlayer player)
    {
        if (world == null || from == null || to == null)
        {
            throw new IllegalArgumentException("Invalid method call, arguments can't be null. Contact a developer.");
        }

        BlockPos blockpos =
                new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos blockpos1 =
                new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);

        WorldServer worldserver = (WorldServer) world;
        MinecraftServer minecraftserver = world.getMinecraftServer();
        TemplateManager templatemanager = worldserver.getStructureTemplateManager();

        String currentMillis = Long.toString(System.currentTimeMillis());
        String fileName = "/minecolonies/scans/" + LanguageHandler.format("item.scepterSteel.scanFormat", "", currentMillis + ".nbt");

        Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(fileName));
        template.takeBlocksFromWorld(world, blockpos, size, true, Blocks.STRUCTURE_VOID);
        template.setAuthor(Constants.MOD_ID);

        MineColonies.getNetwork().sendTo(new SaveScanMessage(template.writeToNBT(new NBTTagCompound()), fileName), (EntityPlayerMP) player);
    }
}
