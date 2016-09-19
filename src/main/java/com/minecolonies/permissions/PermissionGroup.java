package com.minecolonies.permissions;

import com.minecolonies.api.permission.IPermissionGroup;
import com.sun.tools.javac.util.List;

/**
 */
public class PermissionGroup implements IPermissionGroup
{

    private List<PermissionKey> keys;

    @Override
    public String serialize()
    {
        // TODO

        return null;
    }

    @Override
    public void deserialize(final String data)
    {

    }

}
