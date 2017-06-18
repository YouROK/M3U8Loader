package ru.yourok.m3u8loader;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

import ru.yourok.loader.Store;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class DonationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_donation);

        ((TextView) findViewById(R.id.textViewDonMessage)).setText(getString(R.string.donation_msg) + " " + giveMe() + " :)");
    }

    //Если это приложение делает вас счастливее, можете угостить меня
    private static final String[] thing_rus = {
            "чашкой кофе",
            "чашкой чая",
            "кружкой пива",
            "шоколадкой",
            "печенькой",
            "жареным мясом",
            "хлебом и солью"
    };
    private static final String[] thing_eng = {
            "coffee cup",
            "teacup",
            "mug of beer",
            "chocolate bar",
            "cookie",
            "fried meat",
            "bread and salt"
    };

    private String giveMe() {
        Random rnd = new Random();
        int pos;
        if (getString(R.string.donation).equals("Пожертвование")) {
            pos = rnd.nextInt(thing_rus.length);
            return thing_rus[pos];
        } else {
            pos = rnd.nextInt(thing_eng.length);
            return thing_eng[pos];
        }
    }

    public void onDonEURClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6MQ5S274L3TEQ"));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
    }

    public void onDonRUBClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPYLMA6PJ8F98"));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
    }
}

//RUB
//https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPYLMA6PJ8F98
//
//EUR
//https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6MQ5S274L3TEQ