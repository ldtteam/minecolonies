package com.minecolonies.coremod.items;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.network.messages.SaveScanMessage;
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
     * Var for first pos string.
     */
    private static final String FIRST_POS_STRING = "pos1";

    /**
     * Var for second pos string.
     */
    private static final String SECOND_POS_STRING = "pos2";

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
    public EnumActionResult onItemUse(
                                       final EntityPlayer playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final EnumFacing facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = stack.getTagCompound();

        if (!compound.hasKey(FIRST_POS_STRING))
        {
            BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, pos);
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.scepterSteel.point");
            }
            return EnumActionResult.SUCCESS;
        }
        else if (!compound.hasKey(SECOND_POS_STRING))
        {
            @NotNull final BlockPos pos1 = BlockPosUtil.readFromNBT(compound, FIRST_POS_STRING);
            @NotNull final BlockPos pos2 = pos;
            if (pos2.distanceSq(pos1) > 0)
            {
                BlockPosUtil.writeToNBT(compound, SECOND_POS_STRING, pos2);
                if (worldIn.isRemote)
                {
                    LanguageHandler.sendPlayerMessage(playerIn, "item.scepterSteel.point2");
                }
                return EnumActionResult.SUCCESS;
            }
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.scepterSteel.samePoint");
            }
            return EnumActionResult.FAIL;
        }
        else
        {
            @NotNull final BlockPos pos1 = BlockPosUtil.readFromNBT(compound, FIRST_POS_STRING);
            @NotNull final BlockPos pos2 = BlockPosUtil.readFromNBT(compound, SECOND_POS_STRING);

            //todo if on client (ssp) -> no need to send message, we can execute it in worldIn.isRemote without a message.
            if (!worldIn.isRemote)
            {
                saveStructure(worldIn, pos1, pos2, playerIn);
            }
            compound.removeTag(FIRST_POS_STRING);
            compound.removeTag(SECOND_POS_STRING);
            return EnumActionResult.SUCCESS;
        }
    }

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world  Current world.
     * @param from   First corner.
     * @param to     Second corner.
     * @param player causing this action.
     */
    private static void saveStructure(@Nullable final World world, @Nullable final BlockPos from, @Nullable final BlockPos to, @NotNull final EntityPlayer player)
    {
        //todo if on clientSide check if isRemote and if it is -> don't seed message, execute right away.
        if (world == null || from == null || to == null)
        {
            throw new IllegalArgumentException("Invalid method call, arguments can't be null. Contact a developer.");
        }

        final BlockPos blockpos =
          new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 =
          new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        final BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);

        final WorldServer worldserver = (WorldServer) world;
        final MinecraftServer minecraftserver = world.getMinecraftServer();
        final TemplateManager templatemanager = worldserver.getStructureTemplateManager();

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        final String fileName = "/minecolonies/scans/" + LanguageHandler.format("item.scepterSteel.scanFormat", "", currentMillisString + ".nbt");

        final Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(fileName));
        template.takeBlocksFromWorld(world, blockpos, size, true, Blocks.STRUCTURE_VOID);
        template.setAuthor(Constants.MOD_ID);

        MineColonies.getNetwork().sendTo(new SaveScanMessage(template.writeToNBT(new NBTTagCompound()), currentMillis), (EntityPlayerMP) player);
    }
}
