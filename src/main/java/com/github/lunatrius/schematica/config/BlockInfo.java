package com.github.lunatrius.schematica.config;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.lunatrius.schematica.config.PlacementData.PlacementType;

public class BlockInfo {
    public static final List<Block>               BLOCK_LIST_IGNORE_BLOCK    = new ArrayList<Block>();
    public static final List<Block>               BLOCK_LIST_IGNORE_METADATA = new ArrayList<Block>();
    public static final Map<Block, Item>          BLOCK_ITEM_MAP             = new HashMap<Block, Item>();
    public static final Map<Class, PlacementData> CLASS_PLACEMENT_MAP        = new HashMap<Class, PlacementData>();
    public static final Map<Item, PlacementData>  ITEM_PLACEMENT_MAP         = new HashMap<Item, PlacementData>();

    private static final String MINECRAFT = "minecraft";

    private static String modId = MINECRAFT;

    public static void setModId(String modId)
    {
        BlockInfo.modId = modId;
    }

    public static void populateIgnoredBlocks()
    {
        BLOCK_LIST_IGNORE_BLOCK.clear();

        /**
         * minecraft
         */
        addIgnoredBlock(Blocks.piston_head);
        addIgnoredBlock(Blocks.piston_extension);
        addIgnoredBlock(Blocks.portal);
        addIgnoredBlock(Blocks.end_portal);
    }

    private static boolean addIgnoredBlock(Block block)
    {
        if(block == null)
        {
            return false;
        }

        return BLOCK_LIST_IGNORE_BLOCK.add(block);
    }

    private static boolean addIgnoredBlock(String blockName)
    {
        if(!MINECRAFT.equals(modId) && !Loader.isModLoaded(modId))
        {
            return false;
        }

        return addIgnoredBlock(GameData.getBlockRegistry().getObject(String.format("%s:%s", modId, blockName)));
    }

    public static void populateIgnoredBlockMetadata()
    {
        BLOCK_LIST_IGNORE_METADATA.clear();

        /**
         * minecraft
         */
        addIgnoredBlockMetadata(Blocks.flowing_water);
        addIgnoredBlockMetadata(Blocks.water);
		addIgnoredBlockMetadata(Blocks.flowing_lava);
		addIgnoredBlockMetadata(Blocks.lava);
		addIgnoredBlockMetadata(Blocks.dispenser);
		addIgnoredBlockMetadata(Blocks.bed);
		addIgnoredBlockMetadata(Blocks.golden_rail);
		addIgnoredBlockMetadata(Blocks.detector_rail);
		addIgnoredBlockMetadata(Blocks.sticky_piston);
		addIgnoredBlockMetadata(Blocks.piston);
		addIgnoredBlockMetadata(Blocks.torch);
		addIgnoredBlockMetadata(Blocks.oak_stairs);
		addIgnoredBlockMetadata(Blocks.chest);
		addIgnoredBlockMetadata(Blocks.redstone_wire);
		addIgnoredBlockMetadata(Blocks.wheat);
		addIgnoredBlockMetadata(Blocks.farmland);
		addIgnoredBlockMetadata(Blocks.furnace);
		addIgnoredBlockMetadata(Blocks.lit_furnace);
		addIgnoredBlockMetadata(Blocks.standing_sign);
		addIgnoredBlockMetadata(Blocks.wooden_door);
		addIgnoredBlockMetadata(Blocks.ladder);
		addIgnoredBlockMetadata(Blocks.rail);
		addIgnoredBlockMetadata(Blocks.stone_stairs);
		addIgnoredBlockMetadata(Blocks.wall_sign);
		addIgnoredBlockMetadata(Blocks.lever);
		addIgnoredBlockMetadata(Blocks.stone_pressure_plate);
		addIgnoredBlockMetadata(Blocks.iron_door);
		addIgnoredBlockMetadata(Blocks.wooden_pressure_plate);
		addIgnoredBlockMetadata(Blocks.unlit_redstone_torch);
		addIgnoredBlockMetadata(Blocks.redstone_torch);
		addIgnoredBlockMetadata(Blocks.stone_button);
		addIgnoredBlockMetadata(Blocks.cactus);
		addIgnoredBlockMetadata(Blocks.reeds);
		addIgnoredBlockMetadata(Blocks.pumpkin);
		addIgnoredBlockMetadata(Blocks.portal);
		addIgnoredBlockMetadata(Blocks.lit_pumpkin);
		addIgnoredBlockMetadata(Blocks.cake);
		addIgnoredBlockMetadata(Blocks.unpowered_repeater);
		addIgnoredBlockMetadata(Blocks.powered_repeater);
		addIgnoredBlockMetadata(Blocks.trapdoor);
		addIgnoredBlockMetadata(Blocks.vine);
		addIgnoredBlockMetadata(Blocks.fence_gate);
		addIgnoredBlockMetadata(Blocks.brick_stairs);
		addIgnoredBlockMetadata(Blocks.stone_brick_stairs);
		addIgnoredBlockMetadata(Blocks.waterlily);
		addIgnoredBlockMetadata(Blocks.nether_brick_stairs);
		addIgnoredBlockMetadata(Blocks.nether_wart);
		addIgnoredBlockMetadata(Blocks.end_portal_frame);
		addIgnoredBlockMetadata(Blocks.redstone_lamp);
		addIgnoredBlockMetadata(Blocks.lit_redstone_lamp);
		addIgnoredBlockMetadata(Blocks.sandstone_stairs);
		addIgnoredBlockMetadata(Blocks.ender_chest);
		addIgnoredBlockMetadata(Blocks.tripwire_hook);
		addIgnoredBlockMetadata(Blocks.tripwire);
		addIgnoredBlockMetadata(Blocks.spruce_stairs);
		addIgnoredBlockMetadata(Blocks.birch_stairs);
		addIgnoredBlockMetadata(Blocks.jungle_stairs);
		addIgnoredBlockMetadata(Blocks.command_block);
		addIgnoredBlockMetadata(Blocks.flower_pot);
		addIgnoredBlockMetadata(Blocks.carrots);
		addIgnoredBlockMetadata(Blocks.potatoes);
		addIgnoredBlockMetadata(Blocks.wooden_button);
		addIgnoredBlockMetadata(Blocks.anvil);
		addIgnoredBlockMetadata(Blocks.trapped_chest);
		addIgnoredBlockMetadata(Blocks.hopper);
		addIgnoredBlockMetadata(Blocks.quartz_stairs);
		addIgnoredBlockMetadata(Blocks.dropper);
	}

	private static boolean addIgnoredBlockMetadata(Block block) {
		if (block == null) {
			return false;
		}

		return BLOCK_LIST_IGNORE_METADATA.add(block);
	}

	private static boolean addIgnoredBlockMetadata(String blockName) {
		if (!MINECRAFT.equals(modId) && !Loader.isModLoaded(modId)) {
			return false;
		}

		return addIgnoredBlockMetadata(GameData.getBlockRegistry().getObject(String.format("%s:%s", modId, blockName)));
	}

	public static void populateBlockItemMap() {
		BLOCK_ITEM_MAP.clear();

		/**
		 * minecraft
		 */
		addBlockItemMapping(Blocks.flowing_water, Items.water_bucket);
		addBlockItemMapping(Blocks.water, Items.water_bucket);
		addBlockItemMapping(Blocks.flowing_lava, Items.lava_bucket);
		addBlockItemMapping(Blocks.lava, Items.lava_bucket);
		addBlockItemMapping(Blocks.bed, Items.bed);
		addBlockItemMapping(Blocks.redstone_wire, Items.redstone);
		addBlockItemMapping(Blocks.wheat, Items.wheat_seeds);
		addBlockItemMapping(Blocks.lit_furnace, Blocks.furnace);
		addBlockItemMapping(Blocks.standing_sign, Items.sign);
		addBlockItemMapping(Blocks.wooden_door, Items.wooden_door);
		addBlockItemMapping(Blocks.iron_door, Items.iron_door);
		addBlockItemMapping(Blocks.wall_sign, Items.sign);
		addBlockItemMapping(Blocks.unlit_redstone_torch, Blocks.redstone_torch);
		addBlockItemMapping(Blocks.reeds, Items.reeds);
		addBlockItemMapping(Blocks.unpowered_repeater, Items.repeater);
		addBlockItemMapping(Blocks.powered_repeater, Items.repeater);
		addBlockItemMapping(Blocks.pumpkin_stem, Items.pumpkin_seeds);
		addBlockItemMapping(Blocks.melon_stem, Items.melon_seeds);
		addBlockItemMapping(Blocks.nether_wart, Items.nether_wart);
		addBlockItemMapping(Blocks.brewing_stand, Items.brewing_stand);
		addBlockItemMapping(Blocks.cauldron, Items.cauldron);
		addBlockItemMapping(Blocks.lit_redstone_lamp, Blocks.redstone_lamp);
		addBlockItemMapping(Blocks.cocoa, Items.dye);
		addBlockItemMapping(Blocks.tripwire, Items.string);
		addBlockItemMapping(Blocks.flower_pot, Items.flower_pot);
		addBlockItemMapping(Blocks.carrots, Items.carrot);
		addBlockItemMapping(Blocks.potatoes, Items.potato);
		addBlockItemMapping(Blocks.skull, Items.skull);
	}

	private static Item addBlockItemMapping(Block block, Item item) {
		if (block == null || item == null) {
			return null;
		}

		return BLOCK_ITEM_MAP.put(block, item);
	}

	private static Item addBlockItemMapping(Block block, Block item) {
		return addBlockItemMapping(block, Item.getItemFromBlock(item));
	}

	private static Item addBlockItemMapping(Object blockObj, Object itemObj) {
		if (!MINECRAFT.equals(modId) && !Loader.isModLoaded(modId)) {
			return null;
		}

		Block block = null;
		Item item = null;

		if (blockObj instanceof Block) {
			block = (Block) blockObj;
		} else if (blockObj instanceof String) {
			block = GameData.getBlockRegistry().getObject(String.format("%s:%s", modId, blockObj));
		}

		if (itemObj instanceof Item) {
			item = (Item) itemObj;
		} else if (itemObj instanceof Block) {
			item = Item.getItemFromBlock((Block) itemObj);
		} else if (itemObj instanceof String) {
			String formattedName = String.format("%s:%s", modId, itemObj);
			item = GameData.getItemRegistry().getObject(formattedName);
			if (item == null) {
				item = Item.getItemFromBlock(GameData.getBlockRegistry().getObject(formattedName));
			}
		}

		return addBlockItemMapping(block, item);
	}

	public static Item getItemFromBlock(Block block) {
		if (BLOCK_ITEM_MAP.containsKey(block)) {
			return BLOCK_ITEM_MAP.get(block);
		}

		return Item.getItemFromBlock(block);
	}

	public static void populatePlacementMaps() {
		ITEM_PLACEMENT_MAP.clear();

		/**
		 * minecraft
		 */
		addPlacementMapping(BlockButton.class, new PlacementData(PlacementType.BLOCK, -1, -1, 3, 4, 1, 2).setMaskMeta(0x7));
		addPlacementMapping(BlockChest.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
		addPlacementMapping(BlockDispenser.class, new PlacementData(PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
		addPlacementMapping(BlockEnderChest.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
		addPlacementMapping(BlockFurnace.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
		addPlacementMapping(BlockHopper.class, new PlacementData(PlacementType.BLOCK, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
		addPlacementMapping(BlockLog.class, new PlacementData(PlacementType.BLOCK, 0, 0, 8, 8, 4, 4).setMaskMeta(0xC));
		addPlacementMapping(BlockPistonBase.class, new PlacementData(PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
		addPlacementMapping(BlockPumpkin.class, new PlacementData(PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0xF));
		addPlacementMapping(BlockStairs.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 1, 0).setOffset(0x4, 0.0f, 1.0f).setMaskMeta(0x3));
		addPlacementMapping(BlockTorch.class, new PlacementData(PlacementType.BLOCK, 5, -1, 3, 4, 1, 2).setMaskMeta(0xF));

		addPlacementMapping(Blocks.dirt, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.planks, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.sandstone, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.wool, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.stone_slab, new PlacementData(PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7).setMaskMetaInHand(0x7));
		addPlacementMapping(Blocks.stained_glass, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.trapdoor, new PlacementData(PlacementType.BLOCK, -1, -1, 1, 0, 3, 2).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x3));
		addPlacementMapping(Blocks.monster_egg, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.stonebrick, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.quartz_block, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.fence_gate, new PlacementData(PlacementType.PLAYER, -1, -1, 2, 0, 1, 3).setMaskMeta(0x3));
		addPlacementMapping(Blocks.wooden_slab, new PlacementData(PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7).setMaskMetaInHand(0x7));
		addPlacementMapping(Blocks.anvil, new PlacementData(PlacementType.PLAYER, -1, -1, 1, 3, 0, 2).setMaskMeta(0x3).setMaskMetaInHand(0xC).setBitShiftMetaInHand(2));
		addPlacementMapping(Blocks.stained_hardened_clay, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Blocks.stained_glass_pane, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
		addPlacementMapping(Items.repeater, new PlacementData(PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
	}

	public static PlacementData addPlacementMapping(Class clazz, PlacementData data) {
		if (clazz == null || data == null) {
			return null;
		}

		return CLASS_PLACEMENT_MAP.put(clazz, data);
	}

	public static PlacementData addPlacementMapping(Item item, PlacementData data) {
		if (item == null || data == null) {
			return null;
		}

		return ITEM_PLACEMENT_MAP.put(item, data);
	}

	public static PlacementData addPlacementMapping(Block block, PlacementData data) {
		return addPlacementMapping(Item.getItemFromBlock(block), data);
	}

	public static PlacementData addPlacementMapping(Object itemObj, PlacementData data) {
		if (itemObj == null || data == null) {
			return null;
		}

		Item item = null;

		if (itemObj instanceof Item) {
			item = (Item) itemObj;
		} else if (itemObj instanceof Block) {
			item = Item.getItemFromBlock((Block) itemObj);
		} else if (itemObj instanceof String) {
			String formattedName = String.format("%s:%s", modId, itemObj);
			item = GameData.getItemRegistry().getObject(formattedName);
			if (item == null) {
				item = Item.getItemFromBlock(GameData.getBlockRegistry().getObject(formattedName));
			}
		}

		return addPlacementMapping(item, data);
	}

	public static PlacementData getPlacementDataFromItem(Item item) {
		Block block = Block.getBlockFromItem(item);
		PlacementData data = null;

		for (Class clazz : CLASS_PLACEMENT_MAP.keySet()) {
			if (clazz.isInstance(block)) {
				data = CLASS_PLACEMENT_MAP.get(clazz);
				break;
			}
		}

		for (Item i : ITEM_PLACEMENT_MAP.keySet()) {
			if (i == item) {
				data = ITEM_PLACEMENT_MAP.get(i);
				break;
			}
		}

		return data;
	}

	static {
		populateIgnoredBlocks();
		populateIgnoredBlockMetadata();
		populateBlockItemMap();
		populatePlacementMaps();
	}
}
