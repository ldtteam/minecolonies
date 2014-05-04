package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.config.BlockInfo;
import com.github.lunatrius.schematica.config.PlacementData;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.ArrayList;
import java.util.List;

public class SchematicPrinter {
	public static final SchematicPrinter INSTANCE = new SchematicPrinter();

	private final Minecraft minecraft = Minecraft.getMinecraft();
	private final Settings settings = Settings.instance;

	private boolean isEnabled;
	private boolean isPrinting;

	private SchematicWorld schematic = null;
	private byte[][][] timeout = null;

	public boolean isEnabled() {
		return this.isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean togglePrinting() {
		this.isPrinting = !this.isPrinting;
		return this.isPrinting;
	}

	public boolean isPrinting() {
		return this.isPrinting;
	}

	public void setPrinting(boolean isPrinting) {
		this.isPrinting = isPrinting;
	}

	public SchematicWorld getSchematic() {
		return this.schematic;
	}

	public void setSchematic(SchematicWorld schematic) {
		this.isPrinting = false;
		this.schematic = schematic;
		refresh();
	}

	public void refresh() {
		if (this.schematic != null) {
			this.timeout = new byte[this.schematic.getWidth()][this.schematic.getHeight()][this.schematic.getLength()];
		} else {
			this.timeout = null;
		}
	}

	public boolean print() {
		int minX, maxX, minY, maxY, minZ, maxZ, x, y, z, wx, wy, wz, slot;
		boolean isSneaking;
		EntityClientPlayerMP player = this.minecraft.thePlayer;
		World world = this.minecraft.theWorld;

		syncSneaking(player, true);

		minX = Math.max(0, (int) this.settings.getTranslationX() - 3);
		maxX = Math.min(this.schematic.getWidth(), (int) this.settings.getTranslationX() + 3);
		minY = Math.max(0, (int) this.settings.getTranslationY() - 3);
		maxY = Math.min(this.schematic.getHeight(), (int) this.settings.getTranslationY() + 3);
		minZ = Math.max(0, (int) this.settings.getTranslationZ() - 3);
		maxZ = Math.min(this.schematic.getLength(), (int) this.settings.getTranslationZ() + 3);

		slot = player.inventory.currentItem;
		isSneaking = player.isSneaking();

		int renderingLayer = this.schematic.getRenderingLayer();
		for (y = minY; y < maxY; y++) {
			if (renderingLayer >= 0) {
				if (y != renderingLayer) {
					continue;
				}
			}

			for (x = minX; x < maxX; x++) {
				for (z = minZ; z < maxZ; z++) {
					Block block = this.schematic.getBlock(x, y, z);

					if (block == Blocks.air) {
						continue;
					}

					if (this.timeout[x][y][z] > 0) {
						this.timeout[x][y][z] -= Reference.config.placeDelay;
						continue;
					}

					wx = (int) this.settings.offset.x + x;
					wy = (int) this.settings.offset.y + y;
					wz = (int) this.settings.offset.z + z;

					Block realBlock = world.getBlock(wx, wy, wz);
					if (!world.isAirBlock(wx, wy, wz) && realBlock != null && !realBlock.canPlaceBlockAt(world, wx, wy, wz)) {
						continue;
					}

					int metadata = this.schematic.getBlockMetadata(x, y, z);
					if (placeBlock(this.minecraft, world, player, wx, wy, wz, BlockInfo.getItemFromBlock(block), metadata)) {
						this.timeout[x][y][z] = (byte) Reference.config.timeout;
						if (!Reference.config.placeInstantly) {
							player.inventory.currentItem = slot;
							syncSneaking(player, isSneaking);
							return true;
						}
					}
				}
			}
		}

		player.inventory.currentItem = slot;
		syncSneaking(player, isSneaking);
		return true;
	}

	private boolean isSolid(World world, int x, int y, int z, ForgeDirection side) {
		x += side.offsetX;
		y += side.offsetY;
		z += side.offsetZ;

		Block block = world.getBlock(x, y, z);

		if (block == null) {
			return false;
		}

		if (block.isAir(world, x, y, z)) {
			return false;
		}

		if (block instanceof BlockFluidBase) {
			return false;
		}

		if (block.isReplaceable(world, x, y, z)) {
			return false;
		}

		return true;
	}

	private ForgeDirection[] getSolidSides(World world, int x, int y, int z) {
		List<ForgeDirection> list = new ArrayList<ForgeDirection>();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (isSolid(world, x, y, z, side)) {
				list.add(side);
			}
		}

		ForgeDirection[] sides = new ForgeDirection[list.size()];
		return list.toArray(sides);
	}

	private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, Item item, int itemDamage) {
		if (item instanceof ItemBucket || item == Items.sign) {
			return false;
		}

		PlacementData data = BlockInfo.getPlacementDataFromItem(item);

		if (!isValidOrientation(player, x, y, z, data, itemDamage)) {
			return false;
		}

		ForgeDirection[] solidSides = getSolidSides(world, x, y, z);
		ForgeDirection direction = ForgeDirection.UNKNOWN;
		float offsetY = 0.0f;

		if (solidSides.length > 0) {
			if (data != null) {
				ForgeDirection[] validDirections = data.getValidDirections(solidSides, itemDamage);
				if (validDirections.length > 0) {
					direction = validDirections[0];
				}

				offsetY = data.getOffsetFromMetadata(itemDamage);

				if (data.maskMetaInHand != -1) {
					if (!swapToItem(player.inventory, item, data.getMetaInHand(itemDamage))) {
						return false;
					}
				} else {
					if (!swapToItem(player.inventory, item)) {
						return false;
					}
				}
			} else {
				direction = solidSides[0];

				if (!swapToItem(player.inventory, item)) {
					return false;
				}
			}
		}

		if (direction != ForgeDirection.UNKNOWN || !Reference.config.placeAdjacent) {
			return placeBlock(minecraft, world, player, x, y, z, direction, 0.0f, offsetY, 0.0f);
		}

		return false;
	}

	private boolean isValidOrientation(EntityPlayer player, int x, int y, int z, PlacementData data, int metadata) {
		ForgeDirection orientation = this.settings.orientation;

		if (data != null) {
			switch (data.type) {
			case BLOCK: {
				return true;
			}

			case PLAYER: {
				Integer integer = data.mapping.get(orientation);
				if (integer != null) {
					return integer == (metadata & data.maskMeta);
				}
				break;
			}

			case PISTON: {
				Integer integer = data.mapping.get(orientation);
				if (integer != null) {
					return BlockPistonBase.determineOrientation(null, x, y, z, player) == BlockPistonBase.getPistonOrientation(metadata);
				}
				break;
			}
			}
			return false;
		}

		return true;
	}

	private boolean placeBlock(Minecraft minecraft, World world, EntityPlayer player, int x, int y, int z, ForgeDirection direction, float offsetX, float offsetY, float offsetZ) {
		ItemStack itemStack = player.getCurrentEquippedItem();
		boolean success = false;

		x += direction.offsetX;
		y += direction.offsetY;
		z += direction.offsetZ;

		int side = direction.getOpposite().ordinal();

		/* copypasted from n.m.client.Minecraft to sooth finicky servers */
		success = !ForgeEventFactory.onPlayerInteract(minecraft.thePlayer, Action.RIGHT_CLICK_BLOCK, x, y, z, side).isCanceled();
		if (success) {
			// still not assured!
			success = minecraft.playerController.onPlayerRightClick(player, world, itemStack, x, y, z, side, Vec3.createVectorHelper(x + offsetX, y + offsetY, z + offsetZ));
			if (success) {
				// yes, some servers actually care about this.
				minecraft.thePlayer.swingItem();
			}
		}

		if (itemStack.stackSize == 0 && success) {
			player.inventory.mainInventory[player.inventory.currentItem] = null;
		}

		return success;
	}

	private void syncSneaking(EntityClientPlayerMP player, boolean isSneaking) {
		player.setSneaking(isSneaking);
		player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, isSneaking ? 1 : 2));
	}

	private boolean swapToItem(InventoryPlayer inventory, Item item, int itemDamage) {
		int slot = getInventorySlotWithItem(inventory, item, itemDamage);
		if (slot > -1 && slot < 9) {
			inventory.currentItem = slot;
			return true;
		}
		return false;
	}

	private boolean swapToItem(InventoryPlayer inventory, Item item) {
		int slot = getInventorySlotWithItem(inventory, item);
		if (slot > -1 && slot < 9) {
			inventory.currentItem = slot;
			return true;
		}
		return false;
	}

	private int getInventorySlotWithItem(InventoryPlayer inventory, Item item, int itemDamage) {
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == item && inventory.mainInventory[i].getItemDamage() == itemDamage) {
				return i;
			}
		}
		return -1;
	}

	private int getInventorySlotWithItem(InventoryPlayer inventory, Item item) {
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == item) {
				return i;
			}
		}
		return -1;
	}
}
