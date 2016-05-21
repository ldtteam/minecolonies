package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockSubstitution extends Block implements ITileEntityProvider {

	private final String blockName = "blockSubstitution";

	public BlockSubstitution() {
		super(Material.wood);
		initBlock();
	}

	private void initBlock() {
		setRegistryName(blockName);
		setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + blockName);
		System.out.println(Constants.MOD_ID.toLowerCase() + "." + blockName);
		setCreativeTab(ModCreativeTabs.MINECOLONIES);
		GameRegistry.registerBlock(this, blockName);
		setHardness(5f);
		
	}

	@Override
	public boolean isOpaqueCube() {
		return true;
	}

	public static void registerRenders() {
		registerRender(ModBlocks.blockSubstitution);
	}

	public static void registerRender(Block block) {
		Item item = Item.getItemFromBlock(block);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(
				Constants.MOD_ID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	@Override
	public TileEntity createNewTileEntity(World arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}
}
