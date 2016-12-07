package ru.yourok.loader;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by yourok on 07.12.16.
 */

public class LoaderManager extends IntentService {

    public LoaderManager() {
        super("LoaderManager");
    }

    private boolean isLoading;
    private Loader loader;

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra("LoaderID", -1);
//        if (id==-1)
        //TODO err
        loader = LoaderHolder.getInstance().GetLoader(id);
        if (loader != null) {
            isLoading = true;
            checkState();
            loader.Load();
            isLoading = false;
        }
    }

    private void checkState(){
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLoading){
                    loader.PollState();
                }
            }
        });
        th.start();
    }

}
