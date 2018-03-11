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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command entry point to make minecraft inheritance happy.
 */
public class CommandEntryPointNew extends CommandBase
{
    private static class ParsingResult
    {
        @NotNull private final List<String> tabCompletions;
        @NotNull private final TreeNode<Menu> executionTreeNode;
        @Nullable private final List<ActionArgument> executionActionArgumentList;
        @Nullable private String badArgument;

        ParsingResult(@NotNull final List<String> tabCompletions, @NotNull final TreeNode<Menu> executionTreeNode,
                @Nullable final List<ActionArgument> executionActionArgumentList,
                @Nullable final String badArgument)
        {
            super();
            this.tabCompletions = tabCompletions;
            this.executionTreeNode = executionTreeNode;
            this.executionActionArgumentList = executionActionArgumentList;
            this.badArgument = badArgument;
        }

        public List<String> getTabCompletions()
        {
            return tabCompletions;
        }

        public TreeNode<Menu> getExecutionTreeNode()
        {
            return executionTreeNode;
        }

        public List<ActionArgument> getExecutionActionArgumentList()
        {
            return executionActionArgumentList;
        }

        public String getBadArgument()
        {
            return badArgument;
        }

        public void setBadArgument(final String badArgument)
        {
            this.badArgument = badArgument;
        }

    }

    public static final class ActionMenuHolder
    {
        private TreeNode<Menu> treeNode;
        private ActionArgument actionArgument;
        private Object value;

        ActionMenuHolder(@NotNull final TreeNode<Menu> treeNode, @NotNull final ActionArgument actionArgument)
        {
            super();
            this.treeNode = treeNode;
            this.actionArgument = actionArgument;
        }
        public TreeNode<Menu> getTreeNode()
        {
            return treeNode;
        }
        public ActionArgument getActionArgument()
        {
            return actionArgument;
        }
        public Object getValue()
        {
            return value;
        }
        public void setValue(final Object value)
        {
            this.value = value;
        }
    }

    /**
     * The level required to execute /mc commands.
     * private static final int OP_PERMISSION_LEVEL = 3;
     */

    @NotNull
    private final TreeNode<Menu> root;

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

    private TreeNode<Menu> buildMenu(@NotNull final MenuType menuType)
    {
        return buildMenu(menuType, new HashSet<>());
    }

    private TreeNode<Menu> buildMenu(@NotNull final MenuType menuType, @NotNull final Set<MenuType> menuTypesSoFar)
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

        final Menu menu = menuType.getMenu();
        final TreeNode<Menu> treeNode = new TreeNode<Menu>(menu);

        if (menuType.isNavigationMenu())
        {
            final NavigationMenu navigationMenu = (NavigationMenu) menu;
            for (final MenuType childMenuType : navigationMenu.getChildrenMenuList())
            {
                final TreeNode<Menu> childTreeNode = buildMenu(childMenuType, menuTypesSoFar);
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
    public String getCommandUsage(final ICommandSender sender, final TreeNode<Menu> currentMenuTreeNode)
    {
        final MenuType currentMenuType = currentMenuTreeNode.getData().getMenuType();
        if (currentMenuTreeNode.hasChildren())
        {
            final StringBuilder sb = new StringBuilder();
            buildParentPathString(sb, currentMenuTreeNode);
            sb.append(' ').append('<');
            boolean first = true;
            for (final TreeNode<Menu> childTreeNode : currentMenuTreeNode.getChildren())
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
            final Menu menu = currentMenuType.getMenu();
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

    private void buildParentPathString(final StringBuilder sb, final TreeNode<Menu> menuTreeNode)
    {
        final List<TreeNode<Menu>> treeNodeMenuListMinusRoot = new ArrayList<>();
        TreeNode<Menu> currentMenuTreeNode = menuTreeNode;
        while (null != currentMenuTreeNode.getParent())
        {
            treeNodeMenuListMinusRoot.add(currentMenuTreeNode);
            currentMenuTreeNode = currentMenuTreeNode.getParent();
        }
        sb.append('/').append(currentMenuTreeNode.getData().getMenuItemName());

        Collections.reverse(treeNodeMenuListMinusRoot);
        for (final TreeNode<Menu> treeNode : treeNodeMenuListMinusRoot)
        {
            sb.append(' ').append(treeNode.getData().getMenuItemName());
        }
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args) throws CommandException
    {
        final BlockPos pos = null;
        final ParsingResult parsingResult = getTabCompletionsAndParsingHolders(root, server, sender, args, pos);
        final TreeNode<Menu> executionTreeNode = parsingResult.getExecutionTreeNode();
        final List<ActionArgument> executionActionArgumentList = parsingResult.getExecutionActionArgumentList();
        final String badArgument = parsingResult.getBadArgument();
        if (null == executionTreeNode)
        {
            throw new CommandException(getCommandUsage(sender, root));
        }

        final Menu executionMenu = executionTreeNode.getData();
        if (executionMenu.getMenuType().isNavigationMenu())
        {
            throw new CommandException(getCommandUsage(sender, executionTreeNode));
        }

        final ActionMenu actionMenu = (ActionMenu) executionMenu;

        // Check that required arguments are provided with a valid value
        final List<ActionArgument> actionArgumentListForActionMenu = actionMenu.getActionArgumentList();
        for (final ActionArgument actionArgument : actionArgumentListForActionMenu)
        {
            if (actionArgument.isRequired())
            {
                boolean foundIt = false;
                if (null != executionActionArgumentList)
                {
                    for (final ActionArgument executionActionArgument : executionActionArgumentList)
                    {
                        if (null != executionActionArgument)
                        {
                            if (actionArgument.getName().equals(executionActionArgument.getName()))
                            {
                                if (!executionActionArgument.isValueSet())
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
                                    foundIt = true;
                                }
                            }
                        }
                    }
                }
                if (!foundIt)
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
                if (null != executionActionArgument)
                {
                    if (!executionActionArgument.isValueSet())
                    {
                        throw new CommandException(
                                getCommandUsage(sender, executionTreeNode)
                                    + ": invalid value '" + badArgument + "' for required argument " + executionActionArgument.getName());
                    }
                }
            }
        }

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

        final Map<String, Object> argumentValueByActionArgumentNameMap = new HashMap<>();
        final List<ActionArgument> actionArgumentList = new ArrayList<>();
        if (null != executionActionArgumentList)
        {
            for (final ActionArgument executionActionArgument : executionActionArgumentList)
            {
                if (!executionActionArgument.isValueSet())
                {
                    throw new CommandException(getCommandUsage(sender, executionTreeNode));
                }
                actionArgumentList.add(executionActionArgument);
                argumentValueByActionArgumentNameMap.put(executionActionArgument.getName(), executionActionArgument.getValue());
            }
        }

        final Class<? extends IActionCommand> clazz = actionMenu.getActionCommandClass();
        try
        {
            createInstanceAndExecute(server, sender, argumentValueByActionArgumentNameMap, actionArgumentList, clazz);
        }
        catch (final InstantiationException | IllegalAccessException e)
        {
            final Logger log = LogManager.getLogger();
            log.error("Unable to instantiate class %s for command %s ", clazz.getName(), actionMenu.getDescription(), e);
            throw new CommandException("Unable to instantiate class " + clazz.getName() + " for command " + actionMenu.getDescription(), e);
        }
    }

    protected void createInstanceAndExecute(final MinecraftServer server, final ICommandSender sender, final Map<String, Object> argumentValueByActionArgumentNameMap,
            final List<ActionArgument> actionArgumentList, final Class<? extends IActionCommand> clazz) throws InstantiationException, IllegalAccessException, CommandException
    {
        final IActionCommand actionCommand = clazz.newInstance();
        actionCommand.execute(server, sender, actionArgumentList, argumentValueByActionArgumentNameMap);
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

    private Map<String, TreeNode<Menu>> getNavigationCommands(@NotNull final TreeNode<Menu> parentTreeNode)
    {
        final Map<String, TreeNode<Menu>> map = new HashMap<>();
        final List<TreeNode<Menu>> children = parentTreeNode.getChildren();
        for (final TreeNode<Menu> treeNode : children)
        {
            map.put(treeNode.getData().getMenuItemName().toLowerCase(), treeNode);
        }
        return map;
    }

    private Map<String, ActionMenuHolder> getActionCommands(@NotNull final TreeNode<Menu> treeNode, @NotNull final List<ActionMenuHolder> parsedHolders)
    {
        final Map<String, ActionMenuHolder> map = new HashMap<>();
        final Menu menu = treeNode.getData();
        if (!menu.getMenuType().isNavigationMenu())
        {
            final ActionMenu actionMenu = (ActionMenu) menu;
            for (final ActionArgument actionArgument : actionMenu.getActionArgumentList())
            {
                if (usedThisActionArgumentName(parsedHolders, actionArgument))
                {
                    continue;
                }
                map.put(actionArgument.getName() + ":", new ActionMenuHolder(treeNode, actionArgument));
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
                                           @NotNull final TreeNode<Menu> treeNode,
                                           @NotNull final MinecraftServer server,
                                           @NotNull final ICommandSender sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        if (treeNode.getData().getMenuType().isNavigationMenu())
        {
            final Map<String, TreeNode<Menu>> childs = getNavigationCommands(treeNode);
            final String lowerCaseArg0 = args[0].toLowerCase();
            if (args.length <= 1
                  || !childs.containsKey(lowerCaseArg0))
            {
                final List<String> tabCompletions = childs.keySet().stream().filter(k -> k.startsWith(lowerCaseArg0)).collect(Collectors.toList());
                if (childs.containsKey(lowerCaseArg0))
                {
                    final TreeNode<Menu> childTreeNode = childs.get(lowerCaseArg0);
                    return new ParsingResult(tabCompletions, childTreeNode, (List<ActionArgument>) null, (String) null);
                }
                else
                {
                    return new ParsingResult(tabCompletions, treeNode, (List<ActionArgument>) null, lowerCaseArg0);
                }
            }
            final TreeNode<Menu> child = childs.get(lowerCaseArg0);
            if (null == child)
            {
                final List<String> tabCompletions = Collections.emptyList();
                return new ParsingResult(tabCompletions, treeNode, (List<ActionArgument>) null, lowerCaseArg0);
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
            return getTabCompletionsAndParsingHolders(parsedHolders, treeNode, parsedActionArgumentList, possibleActionCommands, server, sender, args, pos);
        }
    }

    @NotNull
    private ParsingResult getTabCompletionsAndParsingHolders(
                                           @NotNull final List<ActionMenuHolder> parsedHolders,
                                           @NotNull final TreeNode<Menu> actionMenuTreeNode,
                                           @NotNull final List<ActionArgument> parsedActionArgumentList,
                                           @NotNull final Map<String, ActionMenuHolder> possibleActionCommands,
                                           @NotNull final MinecraftServer server,
                                           @NotNull final ICommandSender sender,
                                           @NotNull final String[] args,
                                           @Nullable final BlockPos pos)
    {
        final String lowerCaseArg0 = args[0].toLowerCase();
        if (args.length <= 1 || !possibleActionCommands.containsKey(lowerCaseArg0))
        {
            final List<String> tabCompletions = possibleActionCommands.keySet().stream().filter(k -> k.startsWith(lowerCaseArg0)).collect(Collectors.toList());
            if (possibleActionCommands.containsKey(lowerCaseArg0))
            {
                final ActionMenuHolder actionMenuHolder = possibleActionCommands.get(lowerCaseArg0);
                final ActionArgument actionArgument = actionMenuHolder.getActionArgument();
                parsedActionArgumentList.add(actionArgument);
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, (String) null);
            }
            else
            {
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, lowerCaseArg0);
            }
        }
        final ActionMenuHolder holder = possibleActionCommands.get(lowerCaseArg0);
        if (null == holder)
        {
            final List<String> tabCompletions = Collections.emptyList();
            return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, lowerCaseArg0);
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
                if (null == possibleActionCommands.get(nextPotentialWord))
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
                final List<String> tabCompletions = actionArgumentType.getTabCompletions(server, pos, potentialArgumentValue);
                return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, potentialArgumentValue);
            }
            else
            {
                final ActionArgument actionArgument = holder.getActionArgument();
                actionArgument.setValue(parsedObject);
                parsedHolders.add(holder);

                // add any subArguments
                final TreeNode<Menu> treeNode = holder.getTreeNode();
                final List<ActionArgument> subActionArgumentList = actionArgument.getActionArgumentList();
                for (final ActionArgument subActionArgument : subActionArgumentList)
                {
                    possibleActionCommands.put(subActionArgument.getName() + ":", new ActionMenuHolder(treeNode, subActionArgument));
                }


                if (newArgsStartPos == args.length)
                {
                    final List<String> tabCompletions = Collections.emptyList();
                    return new ParsingResult(tabCompletions, actionMenuTreeNode, parsedActionArgumentList, (String) null);
                }
            }
        }

        final String[] newArgs = new String[args.length - newArgsStartPos];
        System.arraycopy(args, newArgsStartPos, newArgs, 0, newArgs.length);
        return getTabCompletionsAndParsingHolders(parsedHolders, actionMenuTreeNode, parsedActionArgumentList, possibleActionCommands, server, sender, newArgs, pos);
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        super.isUsernameIndex(args, index);
        // TODO: implement
        return false;

        // return root.isUsernameIndex(args, index);
    }
}
