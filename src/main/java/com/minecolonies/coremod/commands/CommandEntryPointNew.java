package com.minecolonies.coremod.commands;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command entry point to make minecraft inheritance happy.
 */
public class CommandEntryPointNew extends CommandBase
{
    @NotNull
    private final TreeNode<IMenu> root;

    private static class ParsingResult
    {
        @NotNull private final List<String> tabCompletions;
        @NotNull private final TreeNode<IMenu> executionTreeNode;
        @NotNull private final List<ActionArgument> executionActionArgumentList;
        @Nullable private final ActionMenuState actionMenuState;
        @Nullable private final String badArgument;

        ParsingResult(@NotNull final List<String> tabCompletions, @NotNull final TreeNode<IMenu> executionTreeNode,
                @NotNull final List<ActionArgument> executionActionArgumentList,
                @Nullable final ActionMenuState actionMenuState,
                @Nullable final String badArgument)
        {
            super();
            this.tabCompletions = new ArrayList<>(tabCompletions);
            this.executionTreeNode = executionTreeNode;
            this.executionActionArgumentList = new ArrayList<>(executionActionArgumentList);
            this.actionMenuState = actionMenuState;
            this.badArgument = badArgument;
        }

        public List<String> getTabCompletions()
        {
            return Collections.unmodifiableList(tabCompletions);
        }

        public TreeNode<IMenu> getExecutionTreeNode()
        {
            return executionTreeNode;
        }

        public List<ActionArgument> getExecutionActionArgumentList()
        {
            return Collections.unmodifiableList(executionActionArgumentList);
        }

        public String getBadArgument()
        {
            return badArgument;
        }

        @Nullable
        public ActionMenuState getActionMenuState()
        {
            return actionMenuState;
        }
    }

    /**
     * The level required to execute /mc commands.
     * private static final int OP_PERMISSION_LEVEL = 3;
     */

    /**
     * Create our entry point once.
     */
    public CommandEntryPointNew()
    {
        super();
        root = buildMenu(NavigationMenuType.MINECOLONIES);
    }

    /**
     * Removed so we can control this at the config
     * Only allow OP's execute the commands.
     *
     * @return the int permission level (3 for OP).
     * public int getRequiredPermissionLevel(){return OP_PERMISSION_LEVEL;}
     */
    @NotNull
    @Override
    public List<String> getAliases()
    {
        return Arrays.asList("mc", "col", "mcol", "mcolonies", "minecol", "minecolonies");
    }

    private TreeNode<IMenu> buildMenu(@NotNull final IMenuType menuType)
    {
        return buildMenu(menuType, new HashSet<>());
    }

    private TreeNode<IMenu> buildMenu(@NotNull final IMenuType menuType, @NotNull final Set<IMenuType> menuTypesSoFar)
    {
        // Prevent recursion
        if (menuTypesSoFar.contains(menuType))
        {
            return null;
        }
        else
        {
            menuTypesSoFar.add(menuType);
        }

        final IMenu menu = menuType.getMenu();
        final TreeNode<IMenu> treeNode = new TreeNode<>(menu);

        if (menuType.isNavigationMenu())
        {
            final NavigationMenu navigationMenu = (NavigationMenu) menu;
            for (final IMenuType childMenuType : navigationMenu.getChildrenMenuList())
            {
                final TreeNode<IMenu> childTreeNode = buildMenu(childMenuType, menuTypesSoFar);
                treeNode.addChild(childTreeNode);
            }
        }

        return treeNode;
    }

    @NotNull
    @Override
    public String getName()
    {
        return root.getData().getMenuItemName();
    }

    @NotNull
    @Override
    public String getUsage(final ICommandSender sender)
    {
        return getCommandUsage(sender, root);
    }

    @NotNull
    private static String getCommandUsage(final ICommandSender sender, final TreeNode<IMenu> currentMenuTreeNode)
    {
        final IMenuType currentMenuType = currentMenuTreeNode.getData().getMenuType();
        if (currentMenuTreeNode.hasChildren())
        {
            final StringBuilder sb = new StringBuilder();
            buildParentPathString(sb, currentMenuTreeNode);
            sb.append(" <");
            boolean first = true;
            for (final TreeNode<IMenu> childTreeNode : currentMenuTreeNode.getChildren())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    sb.append('|');
                }
                sb.append(childTreeNode.getData().getMenuItemName());
            }
            sb.append('>');
            return sb.toString();
        }
        else
        {
            final StringBuilder sb = new StringBuilder();
            buildParentPathString(sb, currentMenuTreeNode);
            final IMenu menu = currentMenuType.getMenu();
            if (!currentMenuType.isNavigationMenu())
            {
                final ActionMenu actionMenu = (ActionMenu) menu;
                for (final ActionArgument actionArgument : actionMenu.getActionArgumentList())
                {
                    sb.append(' ').append(actionArgument.getUsage());
                }
            }
            return sb.toString();
        }
    }

    private static void buildParentPathString(final StringBuilder sb, final TreeNode<IMenu> menuTreeNode)
    {
        final List<TreeNode<IMenu>> treeNodeMenuListMinusRoot = new ArrayList<>();
        TreeNode<IMenu> currentMenuTreeNode = menuTreeNode;
        while (null != currentMenuTreeNode.getParent())
        {
            treeNodeMenuListMinusRoot.add(currentMenuTreeNode);
            currentMenuTreeNode = currentMenuTreeNode.getParent();
        }
        sb.append('/').append(currentMenuTreeNode.getData().getMenuItemName());

        Collections.reverse(treeNodeMenuListMinusRoot);
        for (final TreeNode<IMenu> treeNode : treeNodeMenuListMinusRoot)
        {
            sb.append(' ').append(treeNode.getData().getMenuItemName());
        }
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args) throws CommandException
    {
        final BlockPos pos = null;
        final ParsingResult parsingResult = getTabCompletionsAndParsingHolders(root, server, sender, args, pos);
        final TreeNode<IMenu> executionTreeNode = parsingResult.getExecutionTreeNode();
        if (null == executionTreeNode)
        {
            throw new CommandException(getCommandUsage(sender, root));
        }

        final IMenu executionMenu = executionTreeNode.getData();
        if (executionMenu.getMenuType().isNavigationMenu())
        {
            throw new CommandException(getCommandUsage(sender, executionTreeNode));
        }

        @NotNull final List<ActionArgument> executionActionArgumentList = parsingResult.getExecutionActionArgumentList();
        // actionMenuState will be non-null at this point
        @NotNull final ActionMenuState actionMenuState = parsingResult.getActionMenuState();
        final ActionMenu actionMenu = actionMenuState.getActionMenu();

        final String badArgument = parsingResult.getBadArgument();
        throwCommandUsageExceptionIfRequiredArgumentsAreNotProvided(executionTreeNode, actionMenu, executionActionArgumentList, actionMenuState, badArgument, sender);

        if (sender instanceof EntityPlayer)
        {
            final ForgePermissionNodes forgePermissionNode = actionMenu.getForgePermissionNode();

            final EntityPlayer player = (EntityPlayer) sender;
            if (!PermissionAPI.hasPermission(player.getGameProfile(), forgePermissionNode.getNodeName(), new PlayerContext(player)))
            {
                // TODO: Do something if permission check fails.
                // But we don't have permissions set up yet.
            }
        }

        for (final ActionArgument executionActionArgument : executionActionArgumentList)
        {
            if (!actionMenuState.isValueSet(executionActionArgument))
            {
                throw new CommandException(getCommandUsage(sender, executionTreeNode));
            }
        }

        final Class<? extends IActionCommand> clazz = actionMenu.getActionCommandClass();
        try
        {
            createInstanceAndExecute(server, sender, actionMenuState, clazz);
        }
        catch (final InstantiationException | IllegalAccessException e)
        {
            final Logger log = LogManager.getLogger();
            log.error("Unable to instantiate class %s for command %s ", clazz.getName(), actionMenu.getDescription(), e);
            throw new CommandException("Unable to instantiate class " + clazz.getName() + " for command " + actionMenu.getDescription(), e);
        }
    }

    protected void createInstanceAndExecute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState,
            @NotNull final Class<? extends IActionCommand> clazz) throws InstantiationException, IllegalAccessException, CommandException
    {
        final IActionCommand actionCommand = clazz.newInstance();
        actionCommand.execute(server, sender, actionMenuState);
    }

    private static void throwCommandUsageExceptionIfRequiredArgumentsAreNotProvided(@NotNull final TreeNode<IMenu> executionTreeNode, @NotNull final ActionMenu actionMenu,
            @NotNull final List<ActionArgument> executionActionArgumentList, @NotNull final ActionMenuState actionMenuState, final String badArgument,
            @NotNull final ICommandSender sender) throws CommandException
    {
        final List<ActionArgument> actionArgumentListForActionMenu = actionMenu.getActionArgumentList();
        for (final ActionArgument actionArgument : actionArgumentListForActionMenu)
        {
            if (actionArgument.isRequired())
            {
                boolean foundArgument = false;
                for (final ActionArgument executionActionArgument : executionActionArgumentList)
                {
                    if ((null != executionActionArgument) && actionArgument.getName().equals(executionActionArgument.getName()))
                    {
                        if (!actionMenuState.isValueSet(executionActionArgument))
                        {
                            if (null == badArgument)
                            {
                                throw new CommandException(
                                        getCommandUsage(sender, executionTreeNode)
                                            + ": no value specified for required argument " + actionArgument.getName());
                            }
                            else
                            {
                                throw new CommandException(
                                        getCommandUsage(sender, executionTreeNode)
                                            + ": invalid value '" + badArgument + "' for required argument " + actionArgument.getName());
                            }
                        }
                        else
                        {
                            foundArgument = true;
                        }
                    }
                }
                if (!foundArgument)
                {
                    throw new CommandException(getCommandUsage(sender, executionTreeNode)
                            + ": missing required parameter " + actionArgument.getName());
                }
            }
        }

        if (null != executionActionArgumentList)
        {
            for (final ActionArgument executionActionArgument : executionActionArgumentList)
            {
                if ((null != executionActionArgument) && !actionMenuState.isValueSet(executionActionArgument))
                {
                    throw new CommandException(
                            getCommandUsage(sender, executionTreeNode)
                                + ": invalid value '" + badArgument + "' for required argument " + executionActionArgument.getName());
                }
            }
        }
    }

    /**
     * Check if the player has the permission to use commands.
     *
     * @param server the server to check for.
     * @param sender the sender of the command.
     * @return true if so.
     */
    @Override
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender)
    {
        if (sender instanceof EntityPlayer)
        {
            return AbstractSingleCommand.isPlayerOpped(sender) || Configurations.gameplay.opLevelForServer <= 0;
        }
        return true;
    }

    private Map<String, TreeNode<IMenu>> getNavigationCommands(@NotNull final TreeNode<IMenu> parentTreeNode)
    {
        final Map<String, TreeNode<IMenu>> map = new HashMap<>();
        final List<TreeNode<IMenu>> children = parentTreeNode.getChildren();
        for (final TreeNode<IMenu> treeNode : children)
        {
            map.put(treeNode.getData().getMenuItemName().toLowerCase(Locale.ROOT), treeNode);
        }
        return map;
    }

    private Map<String, ActionMenuHolder> getActionCommands(@NotNull final TreeNode<IMenu> treeNode, @NotNull final List<ActionMenuHolder> parsedHolders)
    {
        final Map<String, ActionMenuHolder> map = new HashMap<>();
        final IMenu menu = treeNode.getData();
        if (!menu.getMenuType().isNavigationMenu())
        {
            final ActionMenu actionMenu = (ActionMenu) menu;
            for (final ActionArgument actionArgument : actionMenu.getActionArgumentList())
            {
                if (usedThisActionArgumentName(parsedHolders, actionArgument))
                {
                    continue;
                }
                map.put(actionArgument.getName().toLowerCase(Locale.ROOT) + ":", new ActionMenuHolder(treeNode, actionArgument));
            }
        }
        return map;
    }

    private boolean usedThisActionArgumentName(@NotNull final List<ActionMenuHolder> parsedHolders, @NotNull final ActionArgument actionArgument)
    {
        for (final ActionMenuHolder actionMenuHolder : parsedHolders)
        {
            if (actionMenuHolder.getActionArgument().getName().equals(actionArgument.getName()))
            {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(
                                           @NotNull final MinecraftServer server,
                                           @NotNull final ICommandSender sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        final ParsingResult parsingResult = getTabCompletionsAndParsingHolders(root, server, sender, args, pos);
        return parsingResult.getTabCompletions();
    }

    @NotNull
    private ParsingResult getTabCompletionsAndParsingHolders(
                                           @NotNull final TreeNode<IMenu> treeNode,
                                           @NotNull final MinecraftServer server,
                                           @NotNull final ICommandSender sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        if (treeNode.getData().getMenuType().isNavigationMenu())
        {
            final Map<String, TreeNode<IMenu>> childs = getNavigationCommands(treeNode);
            String lowerCaseArg0;
            if (args.length != 0)
            {
                lowerCaseArg0 = args[0].toLowerCase(Locale.ROOT);
            }
            else
            {
                lowerCaseArg0 = root.toString();
            }
            if (args.length <= 1
                  || !childs.containsKey(lowerCaseArg0))
            {
                final List<String> tabCompletions = childs.keySet().stream().filter(k -> k.startsWith(lowerCaseArg0)).collect(Collectors.toList());
                if (childs.containsKey(lowerCaseArg0))
                {
                    final TreeNode<IMenu> childTreeNode = childs.get(lowerCaseArg0);
                    final IMenu childMenu = childTreeNode.getData();
                    final ActionMenuState actionMenuState;
                    if (!childMenu.getMenuType().isNavigationMenu())
                    {
                        final ActionMenu actionMenu = (ActionMenu) childMenu;
                        actionMenuState = new ActionMenuState(actionMenu);
                    }
                    else
                    {
                        actionMenuState = null;
                    }
                    return new ParsingResult(tabCompletions, childTreeNode, Collections.emptyList(), actionMenuState, (String) null);
                }
                else
                {
                    return new ParsingResult(tabCompletions, treeNode, Collections.emptyList(), (ActionMenuState) null, lowerCaseArg0);
                }
            }
            final TreeNode<IMenu> child = childs.get(lowerCaseArg0);
            if (null == child)
            {
                final List<String> tabCompletions = Collections.emptyList();
                return new ParsingResult(tabCompletions, treeNode, Collections.emptyList(), (ActionMenuState) null, lowerCaseArg0);
            }
            final String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            return getTabCompletionsAndParsingHolders(child, server, sender, newArgs, pos);
        }
        else
        {
            final List<ActionMenuHolder> parsedHolders = new ArrayList<>();
            final Map<String, ActionMenuHolder> possibleActionCommands = getActionCommands(treeNode, parsedHolders);
            @NotNull final List<ActionArgument> parsedActionArgumentList = new ArrayList<>();
            final ActionMenu actionMenu = (ActionMenu) treeNode.getData();
            @NotNull final ActionMenuState actionMenuState = new ActionMenuState(actionMenu);
            return getTabCompletionsAndParsingHoldersForActionMenuTreeNode(treeNode, actionMenuState, parsedHolders, parsedActionArgumentList, possibleActionCommands, server,
                    sender, args, pos);
        }
    }

    @NotNull
    private ParsingResult getTabCompletionsAndParsingHoldersForActionMenuTreeNode(
                                           @NotNull final TreeNode<IMenu> actionMenuTreeNode,
           // TODO: can parsedHolders, parsedActionArgumentList, and possibleActionCommands be merged into actionMenuState?
                                           @NotNull final ActionMenuState actionMenuState,
                                           @NotNull final List<ActionMenuHolder> parsedHolders,
                                           @NotNull final List<ActionArgument> parsedActionArgumentList,
                                           @NotNull final Map<String, ActionMenuHolder> possibleActionCommands,
                                           @NotNull final MinecraftServer server,
                                           @NotNull final ICommandSender sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        final String lowerCaseArg0 = args[0].toLowerCase(Locale.ROOT);
        if (args.length <= 1 || !possibleActionCommands.containsKey(lowerCaseArg0))
        {
            final List<String> tabCompletions = possibleActionCommands.keySet().stream().filter(k -> k.startsWith(lowerCaseArg0)).collect(Collectors.toList());
            if (possibleActionCommands.containsKey(lowerCaseArg0))
            {
                final ActionMenuHolder actionMenuHolder = possibleActionCommands.get(lowerCaseArg0);
                final ActionArgument actionArgument = actionMenuHolder.getActionArgument();
                parsedActionArgumentList.add(actionArgument);
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, actionMenuState, (String) null);
            }
            else
            {
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, actionMenuState, lowerCaseArg0);
            }
        }
        final ActionMenuHolder holder = possibleActionCommands.get(lowerCaseArg0);
        if (null == holder)
        {
            final List<String> tabCompletions = Collections.emptyList();
            return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, actionMenuState, lowerCaseArg0);
        }
        else
        {
            possibleActionCommands.remove(lowerCaseArg0);
            final ActionArgument actionArgument = holder.getActionArgument();
            parsedActionArgumentList.add(actionArgument);
        }

        int newArgsStartPos = 1;
        if (2 <= args.length)
        {
            final ActionArgumentType actionArgumentType = holder.getActionArgument().getType();
            final int allowedSpaceCount = actionArgumentType.allowedSpaceCount();
            int wordCount = 1;
            final StringBuilder sb = new StringBuilder(args[1]);
            while ((wordCount <= allowedSpaceCount) && ((wordCount + 1) < args.length))
            {
                final String nextPotentialWord = args[1 + wordCount];
                if (null == possibleActionCommands.get(nextPotentialWord.toLowerCase(Locale.ROOT)))
                {
                    sb.append(' ').append(nextPotentialWord);
                    ++wordCount;
                }
                else
                {
                    break;
                }
            }
            final String potentialArgumentValue = sb.toString();
            newArgsStartPos = 1 + wordCount;
            final Object parsedObject = actionArgumentType.parse(server, sender, pos, parsedHolders, potentialArgumentValue);
            if (null == parsedObject)
            {
                final List<String> tabCompletions = actionArgumentType.getTabCompletions(server, pos, actionMenuState, potentialArgumentValue);
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, actionMenuState, potentialArgumentValue);
            }
            else
            {
                final ActionArgument actionArgument = holder.getActionArgument();
                actionMenuState.setValue(actionArgument, parsedObject);
                parsedHolders.add(holder);

                // add any subArguments
                final TreeNode<IMenu> treeNode = holder.getTreeNode();
                final List<ActionArgument> subActionArgumentList = actionArgument.getActionArgumentList();
                for (final ActionArgument subActionArgument : subActionArgumentList)
                {
                    possibleActionCommands.put(subActionArgument.getName().toLowerCase(Locale.ROOT) + ":", new ActionMenuHolder(treeNode, subActionArgument));
                }


                if (newArgsStartPos == args.length)
                {
                    final List<String> tabCompletions = Collections.emptyList();
                    return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, actionMenuState, (String) null);
                }
            }
        }

        final String[] newArgs = new String[args.length - newArgsStartPos];
        System.arraycopy(args, newArgsStartPos, newArgs, 0, newArgs.length);
        return getTabCompletionsAndParsingHoldersForActionMenuTreeNode(actionMenuTreeNode, actionMenuState, parsedHolders, parsedActionArgumentList, possibleActionCommands, server,
                sender, newArgs, pos);
    }
}
