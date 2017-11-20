package ru.yourok.m3u8loader.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ru.yourok.m3u8loader.R
import java.util.*


class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)


    }

    fun onDonPaypalClick(view: View) {
        val cur = Currency.getInstance(Locale.getDefault())
        val mon = cur.toString()
        val link = "https://www.paypal.me/yourok/0" + mon
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
        finish()
    }

    fun onDonYandClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://money.yandex.ru/to/410013733697114/100"))
        startActivity(browserIntent)
        finish()
    }

    fun onDonationClick(view: View) {
        val cur = Currency.getInstance(Locale.getDefault())
        val mon = cur.toString()
        var count = "1"
        var link = "https://www.paypal.me/yourok/"
        if (mon != "USD" && mon != "EUR")
            count += "00"
        if (mon == "RUB") {
            link = "https://money.yandex.ru/to/410013733697114/100"
        } else
            link = link + count + mon

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
        finish()
    }

    fun onDonUSDClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WT5NZ5AWLXBPY"))
        startActivity(browserIntent)
        finish()
    }

    fun onDonEURClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6MQ5S274L3TEQ"))
        startActivity(browserIntent)
        finish()
    }

    fun onDonRUBClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPYLMA6PJ8F98"))
        startActivity(browserIntent)
        finish()
    }
}
