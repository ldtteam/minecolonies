package com.minecolonies.core.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blocks.interfaces.IInvisibleBlueprintAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.IOPool;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ldtteam.structurize.api.constants.Constants.INVISIBLE_TAG;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.WindowConstants.LABEL_CONSTRUCTION_NAME;

/**
 * Window to show available styles for a specific building
 */
public class WindowBuildingBrowser extends AbstractWindowSkeleton
{
    /**
     * Number of worker threads to spawn to scan blueprints (each pack on a separate thread); higher reduces total search time.
     */
    private static final int WORKER_THREADS = 4;
    private static final ResourceLocation WINDOW_RESOURCE = new ResourceLocation(MOD_ID, "gui/windowbrowsebuilding.xml");
    @SuppressWarnings("ConstantConditions") private static final int COLOR_NORMAL          = ChatFormatting.BLACK.getColor();
    @SuppressWarnings("ConstantConditions") private static final int COLOR_CHILD           = ChatFormatting.DARK_GREEN.getColor();
    @SuppressWarnings("ConstantConditions") private static final int COLOR_INVISIBLE       = ChatFormatting.DARK_BLUE.getColor();
    @SuppressWarnings("ConstantConditions") private static final int COLOR_INVISIBLE_CHILD = ChatFormatting.BLUE.getColor();

    private static final Map<Block, List<BuildingInfo>> buildingCache = new HashMap<>();

    private final Block block;
    private List<BuildingInfo> buildings;
    private Future<List<BuildingInfo>> futureBuildings;

    /**
     * Construct the window
     * @param block the hut to display styles for
     */
    public WindowBuildingBrowser(@NotNull final Block block)
    {
        super(WINDOW_RESOURCE.toString());
        this.block = block;
    }

    /**
     * Clears the blueprint information cache.  Called on logout.  (So it won't report things
     * scanned this session, but that's probably the least interesting anyway.)
     */
    public static void clearCache()
    {
        buildingCache.clear();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (block instanceof AbstractBlockHut<?> hutBlock)
        {
            findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(hutBlock.getBlueprintDisplayName());
        }
        else
        {
            findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(block.getName());
        }

        futureBuildings = IOPool.submit(this::discoverBuildings);
    }

    @Override
    public void onClosed()
    {
        super.onClosed();

        if (futureBuildings != null)
        {
            futureBuildings.cancel(false);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (futureBuildings != null && futureBuildings.isDone())
        {
            try
            {
                buildings = futureBuildings.get();
                displayBuildings();
            }
            catch (InterruptedException | ExecutionException e)
            {
                // ignore
            }
            futureBuildings = null;
        }
    }

    private void displayBuildings()
    {
        findPaneOfTypeByID("loading", Text.class).hide();

        final List<BuildingInfo> visibleBuildings = mc.player.isCreative() ? buildings
                : buildings.stream().filter(b -> !b.isInvisible()).toList();

        final ScrollingList buildingList = findPaneOfTypeByID("buildings", ScrollingList.class);
        buildingList.enable();
        buildingList.show();
        buildingList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return visibleBuildings.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final BuildingInfo building = visibleBuildings.get(index);
                final Text packLabel = rowPane.findPaneOfTypeByID("packName", Text.class);
                final Text nameLabel = rowPane.findPaneOfTypeByID("buildingName", Text.class);
                final Text sizeLabel = rowPane.findPaneOfTypeByID("buildingSize", Text.class);
                final Text levelLabel = rowPane.findPaneOfTypeByID("buildingLevel", Text.class);

                packLabel.setText(Component.literal(building.pack().getName()));
                nameLabel.setText(Component.literal(building.path()));
                nameLabel.setColors(building.isParent() ? building.isInvisible() ? COLOR_INVISIBLE_CHILD : COLOR_CHILD
                        : building.isInvisible() ? COLOR_INVISIBLE : COLOR_NORMAL);
                sizeLabel.setText(Component.literal(String.format("%d x %d x %d",
                        building.size().getX(), building.size().getY(), building.size().getZ())));
                if (building.levels().isEmpty())
                {
                    levelLabel.hide();
                }
                else
                {
                    levelLabel.show();
                    levelLabel.setText(formatLevels(building.levels()));
                }
            }
        });
    }

    @NotNull
    private Component formatLevels(@NotNull final Set<Integer> levels)
    {
        final List<Integer> list = new ArrayList<>(levels);
        if (list.size() == 1)
        {
            return Component.literal(Integer.toString(list.get(0)));
        }
        final int minLevel = list.get(0);
        final int maxLevel = list.get(list.size() - 1);
        if (list.size() == maxLevel - minLevel + 1)
        {
            return Component.translatableEscape("%s-%s", Integer.toString(minLevel), Integer.toString(maxLevel));
        }
        return Component.literal(String.join(",", list.stream().map(i -> Integer.toString(i)).toList()));
    }

    @NotNull
    private List<BuildingInfo> discoverBuildings()
    {
        List<BuildingInfo> cached = buildingCache.get(block);
        if (cached != null)
        {
            return cached;
        }

        rebuildCache();

        return buildingCache.computeIfAbsent(block, k -> new ArrayList<>());
    }

    private void rebuildCache()
    {
        if (!StructurePacks.waitUntilFinishedLoading())
        {
            return;
        }

        final List<Block> browsableBlocks = findBrowsableBlocks();

        // to reduce total search time, kick work off to several worker threads (currently 4 threads ~= 5s on a decent CPU and slow disk)
        final ExecutorService packPool = Executors.newFixedThreadPool(WORKER_THREADS, runnable ->
        {
            final Thread thread = new Thread(runnable, "Minecolonies Building Browser Worker");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((thread1, throwable) -> Log.getLogger().error("Minecolonies Building Browser errored! ", throwable));
            return thread;
        });
        final Map<StructurePackMeta, Future<Map<Block, List<BuildingInfo>>>> packFutures = StructurePacks.getPackMetas().stream()
                .collect(Collectors.toMap(pack -> pack, pack -> packPool.submit(() -> discoverBuildings(pack, browsableBlocks))));
        while (!futureBuildings.isCancelled() && packFutures.values().stream().anyMatch(f -> !f.isDone()))
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                packPool.shutdown();
                return;
            }
        }
        packPool.shutdown();

        if (futureBuildings.isCancelled())
        {
            return;
        }

        buildingCache.clear();
        for (final Future<Map<Block, List<BuildingInfo>>> futureBuildings : packFutures.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .map(Map.Entry::getValue).toList())
        {
            try
            {
                for (final Map.Entry<Block, List<BuildingInfo>> entry : futureBuildings.get().entrySet())
                {
                    buildingCache.merge(entry.getKey(), entry.getValue(), (prev, next) ->
                            ImmutableList.<BuildingInfo>builder().addAll(prev).addAll(next).build());
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                // ignore
            }
        }
    }

    @NotNull
    private Map<Block, List<BuildingInfo>> discoverBuildings(@NotNull final StructurePackMeta pack,
                                                             @NotNull final List<Block> browsableBlocks)
    {
        final Map<Block, List<BuildingInfo>> buildings = new HashMap<>();
        try
        {
            try (final Stream<Path> paths = Files.walk(pack.getPath()))
            {
                paths.forEach(file ->
                {
                    if (futureBuildings.isCancelled()) { return; }
                    if (!Files.isDirectory(file) && file.toString().endsWith(".blueprint"))
                    {
                        final Blueprint blueprint = StructurePacks.getBlueprint(pack.getName(), file, true);
                        if (blueprint != null)
                        {
                            final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                            for (final Block block : browsableBlocks)
                            {
                                classifyBlueprint(pack, buildings, blueprint, anchor, block);
                            }
                        }
                    }
                });
            }
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprints for {}: ", pack.getName(), e);
        }

        buildings.replaceAll((k, v) -> BuildingInfo.flattenLevels(v));
        return buildings;
    }

    @NotNull
    private static List<Block> findBrowsableBlocks()
    {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof IBuildingBrowsableBlock)
                .toList();
    }

    private void classifyBlueprint(@NotNull final StructurePackMeta pack,
                                   @NotNull final Map<Block, List<BuildingInfo>> buildings,
                                   @NotNull final Blueprint blueprint,
                                   @NotNull final BlockState anchor,
                                   @NotNull final Block block)
    {
        if (anchor.is(block))
        {
            buildings.computeIfAbsent(block, k -> new ArrayList<>())
                    .add(BuildingInfo.create(pack, blueprint, false));
        }
        else if (Arrays.stream(blueprint.getPalette()).anyMatch(p -> p.is(block)))
        {
            buildings.computeIfAbsent(block, k -> new ArrayList<>())
                    .add(BuildingInfo.create(pack, blueprint, true));
        }
    }

    private record BuildingInfo(StructurePackMeta pack, String path, Set<Integer> levels, BlockPos size, boolean isParent, boolean isInvisible)
    {
        /**
         * Creates building info for the specified blueprint.
         * @param pack      the pack.
         * @param blueprint the blueprint.
         * @param isParent  true if this is a parent building/deco (the search block is not the anchor)
         * @return the created info.
         */
        @NotNull
        public static BuildingInfo create(@NotNull final StructurePackMeta pack,
                                          @NotNull final Blueprint blueprint,
                                          final boolean isParent)
        {
            String path = pack.getSubPath(blueprint.getFilePath()).replace('\\', '/');
            String name = blueprint.getFileName().replace(".blueprint", "");
            Set<Integer> levels = new TreeSet<>();
            if (name.length() > 0 && Character.isDigit(name.charAt(name.length() - 1)))
            {
                levels.add(Integer.parseInt(name.substring(name.length() - 1)));
                name = name.substring(0, name.length() - 1);
            }
            path = path + '/' + name;
            final BlockPos size = new BlockPos(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());

            return new BuildingInfo(pack, path, levels, size, isParent, isInvisible(blueprint));
        }

        /**
         * For a single pack, merges single-level entries to multi-level entries.
         * @param input the list of info for a single pack.
         * @return the merged list of info.
         */
        public static List<BuildingInfo> flattenLevels(@NotNull final List<BuildingInfo> input)
        {
            return input.stream()
                    .collect(Collectors.groupingBy(BuildingInfo::path))
                    .values().stream()
                    .map(BuildingInfo::getFlattened)
                    .toList();
        }

        private static BuildingInfo getFlattened(@NotNull final List<BuildingInfo> input)
        {
            final BuildingInfo first = input.get(0);
            final Set<Integer> levels = input.stream()
                    .flatMap(info -> info.levels.stream())
                    .collect(Collectors.toCollection(TreeSet::new));
            final BlockPos size = input.stream()
                    .map(BuildingInfo::size)
                    .max(Comparator.naturalOrder())
                    .get();     // sizes *should* be all the same, but technically don't have to be...
            return new BuildingInfo(first.pack, first.path, levels, size, first.isParent, first.isInvisible);
        }

        // copied from WindowExtendedBuildTool -- todo move to utility class in Structurize
        private static boolean isInvisible(@NotNull final Blueprint blueprint)
        {
            final BlockInfo anchor = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset());
            if (anchor.getState().getBlock() instanceof IInvisibleBlueprintAnchorBlock invis &&
                    !invis.isVisible(anchor.getTileEntityData()))
            {
                return true;
            }

            assert !anchor.hasTileEntityData() || anchor.getTileEntityData() != null;   // quiet warnings
            if (anchor.hasTileEntityData() && anchor.getTileEntityData().contains(TAG_BLUEPRINTDATA))
            {
                final Map<BlockPos, List<String>> tagMap = IBlueprintDataProviderBE.readTagPosMapFrom(anchor.getTileEntityData().getCompound(TAG_BLUEPRINTDATA));
                final List<String> anchorTags = tagMap.computeIfAbsent(BlockPos.ZERO, k -> new ArrayList<>());
                if (anchorTags.contains(INVISIBLE_TAG))
                {
                    return true;
                }
            }

            return false;
        }
    }
}
