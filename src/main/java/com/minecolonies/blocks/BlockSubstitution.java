package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockSubstitution extends Block {

	private final String BLOCK_NAME = "blockSubstitution";
	
	/**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
	public BlockSubstitution() {
		super(Material.wood);
		initBlock();
	}

	private void initBlock() {
		setRegistryName(BLOCK_NAME);
		setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + BLOCK_NAME);
		setCreativeTab(ModCreativeTabs.MINECOLONIES);
		GameRegistry.registerBlock(this, BLOCK_NAME);
		setHardness(5f);

	}

	
	/**
	 * 
	 * @return true
	 */
	@Override
	public boolean isOpaqueCube() {
		return true;
	}
	/**
	 *Constructor for Registering the Substitution Block
	 * 
	 * {@link #registerRender(Block)}
	 */
	public static void registerRenders() {
		registerRender(ModBlocks.blockSubstitution);
	}
	
	/**
	 * Register a block
	 * @param block
	 */
	public static void registerRender(Block block) {
		Item item = Item.getItemFromBlock(block);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Constants.MOD_ID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
	
	
}
