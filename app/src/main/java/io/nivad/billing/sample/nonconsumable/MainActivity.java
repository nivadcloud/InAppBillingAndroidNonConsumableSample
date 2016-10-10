package io.nivad.billing.sample.nonconsumable;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import io.nivad.iab.BillingProcessor;
import io.nivad.iab.TransactionDetails;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BillingProcessor.IBillingHandler {

    // این مقدار را از پنل بازار دریافت می‌کنید
    private static final String BAZAAR_RSA_KEY = "";
    // این دو مقدار به صورت اختیاری می‌توانید از پنل نیواد دریافت کنید. برای فعالسازی قابلیت ضدهک این کار لازم است
    private static final String NIVAD_APPLICATION_ID = null;
    private static final String NIVAD_APPLICATION_SECRET = null;

    // تمام کارهایی که مربوط به خرید و چک کردن محصولات است را با این کلاس انجام می‌دهیم
    private BillingProcessor mBillingProcessor;

    private Button btnBuy;

    // شناسه‌ی محصول در بازار
    private static final String FULL_VERSION_SKU = "full.version";

    public boolean isFullVersion() {
        // از shared preferences چک می‌کند که کاربر نسخه‌ی کامل را خریده یا نه
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_full_version", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBuy = (Button) findViewById(R.id.btnBuy);
        // تا زمانی که امکان خرید آماده نشده دکمه را غیرفعال می‌کنیم تا ارور ایجاد نشود
        btnBuy.setEnabled(false);
        btnBuy.setOnClickListener(this);

        mBillingProcessor = new BillingProcessor(this, BAZAAR_RSA_KEY, NIVAD_APPLICATION_ID, NIVAD_APPLICATION_SECRET, this);

        updateText();
        setupButtons();
    }

    // کلیک روی دکمه‌ی خرید
    @Override
    public void onClick(View v) {
        mBillingProcessor.purchase(this, FULL_VERSION_SKU);
    }

    // ---------------------------------------------------------------
    // ۴ تابع اصلی برنامه که خرید‌ها را هندل می‌کنند

    // وقتی برنامه به بازار متصل و آماده‌ی خرید می‌شود
    @Override
    public void onBillingInitialized() {
        // از الان به بعد خرید قابل انجام است پس دکمه را فعال می‌کنیم
        btnBuy.setEnabled(true);
    }

    // وقتی خرید با موفقیت انجام می‌شود
    @Override
    public void onProductPurchased(String sku, TransactionDetails transactionDetails) {
        if (FULL_VERSION_SKU.equals(sku)) {
            // خرید با موفقیت انجام شده. در shared preferences ذخیره می‌کنیم تا در بخش‌های دیگر برنامه
            // بتوانیم چک کنیم که کاربر نسخه‌ی کامل را خریده یا نه.
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit().putBoolean("is_full_version", true).apply();
        }
        updateText();
    }

    // وقتی خرید‌های قبلی از بازار دوباره load می‌شوند
    @Override
    public void onPurchaseHistoryRestored() {
        if (mBillingProcessor.isPurchased(FULL_VERSION_SKU)) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit().putBoolean("is_full_version", true).apply();
        } else {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit().putBoolean("is_full_version", false).apply();
        }
        updateText();
    }

    // وقتی در فرایند خرید مشکلی ایجاد شود یا کاربر سعی کند که برنامه‌را هک کند
    @Override
    public void onBillingError(int errorCode, Throwable throwable) {
        if (errorCode == 205) {
            Toast.makeText(MainActivity.this, R.string.nivad_protects_this_application, Toast.LENGTH_LONG).show();
        }
    }

    // -----------------------------------------------------------------------------------

    // آزاد کردن حافظه
    @Override
    protected void onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
        }
        super.onDestroy();
    }

    // پاس دادن پارامترهای onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    // ------------------- کدهای زیر به پرداخت درون برنامه‌ای ارتباطی ندارند -----------------------
    private void updateText() {
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        if (isFullVersion()) {
            tvStatus.setText(R.string.you_have_full_version);
            tvStatus.setTextColor(0xff11ff11);
        } else {
            tvStatus.setText(R.string.you_have_demo_version);
            tvStatus.setTextColor(0xffff1111);
        }
    }

    private void setupButtons() {
        TextView tvIntro = (TextView) findViewById(R.id.tvIntro);
        tvIntro.setClickable(true);
        tvIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://nivad.io/billing/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        Button btnSourceCode = (Button) findViewById(R.id.btnSourceCode);
        btnSourceCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/nivadcloud/InAppBillingAndroidNonConsumableSample";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
