package com.minecolonies.coremod.util;

public class ModifyableLambdaWrapper<T>
{

    private T value;

    public ModifyableLambdaWrapper(final T value)
    {
        this.value = value;
    }

    public ModifyableLambdaWrapper()
    {
    }

    public T get()
    {
        return value;
    }

    public void setValue(final T value)
    {
        this.value = value;
    }
}
