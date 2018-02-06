package ru.yourok.m3u8loader.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ru.yourok.dwl.settings.Preferences
import ru.yourok.m3u8loader.R
import java.util.*


class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        Preferences.set("LastViewDonate", System.currentTimeMillis() + 518400000L)//Через неделю
    }

    fun onDonPaypalClick(view: View) {
        val cur = Currency.getInstance(Locale.getDefault())
        val mon = cur.toString()
        val link = "https://www.paypal.me/yourok/0" + mon
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
        finish()
        Preferences.set("LastViewDonate", -1L)
    }

    fun onDonYandClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://money.yandex.ru/to/410013733697114/100"))
        startActivity(browserIntent)
        finish()
        Preferences.set("LastViewDonate", -1L)
    }

    fun onDonUSDClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WT5NZ5AWLXBPY"))
        startActivity(browserIntent)
        finish()
        Preferences.set("LastViewDonate", -1L)
    }

    fun onDonEURClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6MQ5S274L3TEQ"))
        startActivity(browserIntent)
        finish()
        Preferences.set("LastViewDonate", -1L)
    }

    fun onDonRUBClick(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPYLMA6PJ8F98"))
        startActivity(browserIntent)
        finish()
        Preferences.set("LastViewDonate", -1L)
    }
}
