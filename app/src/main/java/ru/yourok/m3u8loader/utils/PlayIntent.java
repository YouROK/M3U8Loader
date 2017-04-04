package ru.yourok.m3u8loader.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.util.List;

import ru.yourok.loader.Store;

/**
 * Created by yourok on 03.01.17.
 */

public class PlayIntent {
    public static void start(Context context, String filename, String title) {
        String player = Store.getPlayer(context);
        Intent intent;
        switch (player) {
            //Chooser
            case "0":
                intent = getChooser(filename, title);
                break;
            //Default
            default:
            case "1":
                intent = getDefaultPlayer(filename, title);
                break;
            //MX Player
            case "2":
                intent = getMXPlayer(context, filename, title);
                break;
            case "3":
                intent = getKodiPlayer(context, filename, title);
                break;
        }
        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent);
    }

    private static Intent getChooser(String filename, String title) {
        Intent chooser = Intent.createChooser(getDefaultPlayer(filename, title), "   ");
        return chooser;
    }

    private static Intent getDefaultPlayer(String filename, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filename)), "video/mp4");
        intent.putExtra("title", title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static Intent getMXPlayer(Context context, String filename, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String pkg = "";

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.pro")) {
                pkg = "com.mxtech.videoplayer.pro";
                break;
            }

        if (pkg.isEmpty())
            for (ApplicationInfo packageInfo : packages)
                if (packageInfo.packageName.equals("com.mxtech.videoplayer.ad")) {
                    pkg = "com.mxtech.videoplayer.ad";
                    break;
                }

        if (pkg.isEmpty())
            return getChooser(filename, title);

        intent.setDataAndType(Uri.fromFile(new File(filename)), "video/*");
        intent.putExtra("title", title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(pkg);
        return intent;
    }

    private static Intent getKodiPlayer(Context context, String filename, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String pkg = "";

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
            if (packageInfo.packageName.equals("org.xbmc.kodi")) {
                pkg = "org.xbmc.kodi";
                break;
            }

        if (pkg.isEmpty())
            return getChooser(filename, title);

        intent.setDataAndType(Uri.fromFile(new File(filename)), "video/*");
        intent.putExtra("title", title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(pkg);
        return intent;
    }
}
