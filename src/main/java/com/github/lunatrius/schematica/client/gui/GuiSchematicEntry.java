package com.github.lunatrius.schematica.client.gui;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GuiSchematicEntry {
	private final String name;
	private final ItemStack itemStack;
	private final boolean isDirectory;

	public GuiSchematicEntry(String name, ItemStack itemStack, boolean isDirectory) {
		this(name, itemStack.getItem(), itemStack.getItemDamage(), isDirectory);
	}

	public GuiSchematicEntry(String name, Item item, int itemDamage, boolean isDirectory) {
		this.name = name;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(item, 1, itemDamage);
	}

	public GuiSchematicEntry(String name, Block block, int itemDamage, boolean isDirectory) {
		this.name = name;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(block, 1, itemDamage);
	}

	public String getName() {
		return this.name;
	}

	public Item getItem() {
		return this.itemStack.getItem();
	}

	public int getItemDamage() {
		return this.itemStack.getItemDamage();
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}
}
