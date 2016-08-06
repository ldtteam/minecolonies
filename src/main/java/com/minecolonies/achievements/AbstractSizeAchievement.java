package com.minecolonies.achievements;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

public abstract class AbstractSizeAchievement extends AbstractAchievement {
	
	public final int size;

	public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Item itemIcon,
			Achievement parent, int size) {
		super(id, name, offsetX, offsetY, itemIcon, parent);
		
		this.size = size;
	}

	public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Block blockIcon,
			Achievement parent, int size) {
		super(id, name, offsetX, offsetY, blockIcon, parent);
		
		this.size = size;
	}

	public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, ItemStack itemStackIcon,
			Achievement parent, int size) {
		super(id, name, offsetX, offsetY, itemStackIcon, parent);
		
		this.size = size;
	}
	
	public abstract void triggerAchievement(EntityPlayer player);
	
	protected boolean compare(int compare) {
	    return compare >= this.size;
	}
	
}
