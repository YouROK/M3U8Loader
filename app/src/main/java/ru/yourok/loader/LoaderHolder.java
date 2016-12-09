package ru.yourok.loader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yourok on 07.12.16.
 */
public class LoaderHolder {
    private static LoaderHolder ourInstance = new LoaderHolder();
    private static LoaderManager loaderManager;

    public static LoaderHolder getInstance() {
        return ourInstance;
    }

    private LoaderHolder() {
        list = new ArrayList<>();
    }

    private List<Loader> list;

    public void AddLoader(Loader l) {
        list.add(l);
    }

    public Loader GetLoader(int i) {
        if (i < 0 || i >= list.size())
            return null;
        return list.get(i);
    }

    public void Remove(int i) {
        if (i < 0 || i >= list.size())
            return;
        list.get(i).Stop();
        list.remove(i);
    }

    public int Size() {
        return list.size();
    }
}
