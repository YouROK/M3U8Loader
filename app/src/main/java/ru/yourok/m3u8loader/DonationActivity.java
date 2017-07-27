package ru.yourok.m3u8loader;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Currency;
import java.util.Locale;
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

    public void onDonPaypalClick(View view) {
        Currency cur = Currency.getInstance(Locale.getDefault());
        String mon = cur.toString();
        String link = "https://www.paypal.me/yourok/0"+mon;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
    }

    public void onDonYandClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://money.yandex.ru/to/410013733697114/100"));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
    }

    public void onDonationClick(View view) {
        Currency cur = Currency.getInstance(Locale.getDefault());
        String mon = cur.toString();
        String count = "1";
        String link = "https://www.paypal.me/yourok/";
        if (!mon.equals("USD") && !mon.equals("EUR"))
            count += "00";
        if (mon.equals("RUB")) {
            link = "https://money.yandex.ru/to/410013733697114/100";
        } else
            link = link + count + mon;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
    }

    public void onDonUSDClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WT5NZ5AWLXBPY"));
        startActivity(browserIntent);
        Store.setLastDonationView(this, -1);
        finish();
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


///DONATE Paypal
//https://www.paypal.me/yourok/
//
///DANOATE Yandex
//https://money.yandex.ru/to/410013733697114
//
///USD
//https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WT5NZ5AWLXBPY
//
///RUB
//https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPYLMA6PJ8F98
//
///EUR
//https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6MQ5S274L3TEQ