package ru.yourok.loader;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by yourok on 15.12.16.
 */

public class LoaderServiceHandler {
    public static ArrayList<Loader> loadersList = new ArrayList<>();
    public static LinkedBlockingDeque<Integer> loadersQueue = new LinkedBlockingDeque<>();

    public static void AddLoader(Loader l) {
        loadersList.add(l);
    }

    public static Loader GetLoader(int i) {
        if (i < 0 || i >= loadersList.size())
            return null;
        return loadersList.get(i);
    }

    public static void RemoveLoader(int i) {
        if (i < 0 || i >= loadersList.size())
            return;
        loadersList.get(i).Stop();
        loadersList.remove(i);
    }

    public static int SizeLoaders() {
        return loadersList.size();
    }

    public static void AddQueue(int id) {
        if (loadersQueue.contains(id)) return;
        loadersQueue.add(id);
    }

    public void RemoveQueue(int id) {
        loadersQueue.remove(id);
    }
}
