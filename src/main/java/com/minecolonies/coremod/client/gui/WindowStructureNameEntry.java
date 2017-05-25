package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.structures.helpers.Settings;
import org.jetbrains.annotations.NotNull;

/**
 * Window for a town hall name entry.
 */
public class WindowStructureNameEntry extends Window implements ButtonHandler
{
    private static final String BUTTON_DONE                    = "done";
    private static final String BUTTON_CANCEL                  = "cancel";
    private static final String INPUT_NAME                     = "name";
    private static final String STRUCTURE_NAME_RESOURCE_SUFFIX = ":gui/windowstructurenameentry.xml";

    private final Structures.StructureName structureName;
    private final TextField                inputName;

    /**
     * Constructor for a structure rename entry window.
     *
     * @param s {@link Structures.StructureName}
     */
    public WindowStructureNameEntry(final Structures.StructureName s)
    {
        super(Constants.MOD_ID + STRUCTURE_NAME_RESOURCE_SUFFIX);
        this.structureName = s;
        inputName = findPaneOfTypeByID(INPUT_NAME, TextField.class);
    }

    @Override
    public void onOpened()
    {
        inputName.setText(structureName.getStyle() + '/' + structureName.getSchematic());
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String name = inputName.getText();
            if (!name.isEmpty())
            {
                final Structures.StructureName newStructureName = Structures.renameScannedStructure(structureName, name);
                if (newStructureName != null)
                {
                    Settings.instance.setStructureName(newStructureName.toString());
                }
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        close();
        MineColonies.proxy.openBuildToolWindow(null);
    }
}
