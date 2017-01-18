package ru.yourok.m3u8loader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.m3u8loader.utils.Status;


/**
 * Created by yourok on 08.12.16.
 */

public class AdaptorLoadersList extends BaseAdapter {
    private int selected;
    private Context context;

    public AdaptorLoadersList(Context ctx) {
        this.context = ctx;
        selected = -1;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setSelected(int val) {
        if (val >= 0 && val < LoaderServiceHandler.SizeLoaders())
            ((MainActivity) context).findViewById(R.id.itemLoaderMenu).setVisibility(View.VISIBLE);
        else
            ((MainActivity) context).findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);

        selected = val;
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
        int ret = LoaderServiceHandler.SizeLoaders();
        return ret;
    }

    @Override
    public Object getItem(int i) {
        return LoaderServiceHandler.GetLoader(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.listview_item_loaders, parent, false);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == selected) {
                    setSelected(-1);
                    Animation anim = new AlphaAnimation(1, 0);
                    anim.setRepeatCount(1);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setDuration(200);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            ((ImageButton) ((Activity) context).findViewById(R.id.buttonAdd)).setImageResource(R.drawable.ic_add_black_24dp);
                        }
                    });
                    ((Activity) context).findViewById(R.id.buttonAdd).startAnimation(anim);

                } else {
                    if (selected == -1) {
                        Animation anim = new AlphaAnimation(1, 0);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        anim.setDuration(200);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                ((ImageButton) ((Activity) context).findViewById(R.id.buttonAdd)).setImageResource(R.drawable.ic_mode_edit_black_24dp);
                            }
                        });
                        ((Activity) context).findViewById(R.id.buttonAdd).startAnimation(anim);
                    }
                    setSelected(position);
                }
                AdaptorLoadersList.this.notifyDataSetChanged();
            }
        };

        view.setClickable(true);
        view.setOnClickListener(clickListener);

        Loader loader = LoaderServiceHandler.GetLoader(position);
        if (loader != null) {
            ((TextView) view.findViewById(R.id.itemLoaderName)).setText(loader.GetName());
            ((TextView) view.findViewById(R.id.itemLoaderUrl)).setText(Status.GetUrl(loader));
            ((TextView) view.findViewById(R.id.itemLoaderStatus)).setText(Status.GetStatus(context, loader));
            ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress(Status.GetProgress(loader));
        }

        if (selected == position) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.colorItemSelectMenu, typedValue, true);
            view.setBackgroundColor(typedValue.data);
            view.getBackground().setAlpha(100);
        } else {
            view.setBackgroundResource(android.R.color.transparent);
            view.getBackground().setAlpha(0);
        }

        return view;
    }
}
