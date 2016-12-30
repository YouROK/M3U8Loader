package ru.yourok.loader;

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

    public static int FindLoader(Loader loader) {
        for (int i = 0; i < loadersList.size(); i++)
            if (loadersList.get(i) == loader)
                return i;
        return -1;
    }

    public static int SizeLoaders() {
        return loadersList.size();
    }

    public static void AddQueue(int id) {
        if (loadersQueue.contains(id)) return;
        loadersQueue.add(id);
    }

    public static int PollQueue() {
        if (loadersQueue.size() > 0)
            return loadersQueue.poll();
        return -1;
    }

    public static void RemoveQueue(int id) {
        loadersQueue.remove(id);
    }

    public static int SizeQueue() {
        return loadersQueue.size();
    }
}
