package com.kota.Bahamut.Pages.ArticlePage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextColor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.DataModels.UrlDatabase;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Thumbnail_ItemView extends LinearLayout {
    LinearLayout mainLayout;
    Context myContext;
    int viewWidth;
    int viewHeight;
    // 預設圖層
    LinearLayout layoutDefault;
    // 圖片圖層
    LinearLayout layoutPic;
    PhotoView photoViewPic;
    Button imageViewButton;
    // 內容圖層
    LinearLayout layoutNormal;
    TextView titleView;
    TextView descriptionView;
    TextView urlView;
    boolean isPic = false; // 是否為圖片
    boolean loadThumbnailImg = false; // 自動顯示預覽圖
    boolean loadOnlyWifi = false; // 只在wifi下預覽
    boolean imgLoaded = false; // 已經讀取預覽圖

    String myUrl = "";
    String myTitle = "";
    String myDescription = "";
    String myImageUrl = "";

    public Thumbnail_ItemView(Context context) {
        super(context);
        myContext = context;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) myContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        viewWidth = metrics.widthPixels;
        viewHeight = metrics.heightPixels;
        init();
    }

    /** 判斷URL內容 */
    public void loadUrl(String url) {
        myUrl = url;

        try (UrlDatabase urlDatabase = new UrlDatabase(getContext())) {
            Vector<String> findUrl = urlDatabase.getUrl(myUrl);
            urlView.setText(myUrl);
            // 已經有URL資料
            if (findUrl!=null) {
                myTitle = findUrl.get(1);
                myDescription = findUrl.get(2);
                myImageUrl = findUrl.get(3);
                isPic = !findUrl.get(4).equals("0");
                picOrUrl_changeStatus(isPic);
            } else {
                String apiUrl = "https://worker-get-url-content.kodakjerec.workers.dev/";
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", myUrl)
                        .build();
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build();

                // 尋找URL資料
                ASRunner.runInNewThread(() -> {
                    try {
                        // load heads
                        Response response = client.newCall(request).execute();
                        assert response.body() != null;
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);

                        String contentType = jsonObject.getString("contentType");

                        if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                            isPic = true;
                        }
                        myTitle = jsonObject.getString("title");
                        myDescription = jsonObject.getString("desc");
                        myImageUrl = jsonObject.getString("imageUrl");

                        // 非圖片類比較會有擷取問題
                        if (!isPic && (myTitle.equals("") || myDescription.equals(""))) {
                            String userAgent = System.getProperty("http.agent");
                            if (myUrl.contains("youtu") || myUrl.contains("amazon"))
                                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0";

                            // cookie
                            // Create a new Map to store cookies
                            Map<String, String> cookies = new HashMap<>();
                            if (myUrl.contains("ptt"))
                                cookies.put("over18", "1");  // Add the over18 cookie with value 1

                            Connection.Response resp = Jsoup
                                    .connect(myUrl)
                                    .header("User-Agent", userAgent)
                                    .cookies(cookies)
                                    .execute();
                            contentType = resp.contentType();

                            if (contentType.contains("image/") || contentType.contains("video/")) {
                                isPic = true;
                            }
                            // 域名判斷
                            if (url.contains("i.imgur")) {
                                isPic = true;
                            }

                            // 圖片處理
                            if (isPic) {
                                myTitle = myUrl;
                                myDescription = "";
                                myImageUrl = myUrl;
                            } else {
                                // 文字處理
                                Document document = resp.parse();

                                myTitle = document.title();
                                if (myTitle.isEmpty())
                                    myTitle = document.select("meta[property=og:title]").attr("content");

                                myDescription = document.select("meta[name=description]").attr("content");
                                if (myDescription.isEmpty())
                                    myDescription = document.select("meta[property=og:description]").attr("content");

                                myImageUrl = document.select("meta[property=og:image]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl = document.select("meta[property=og:image]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl = document.select("meta[property=og:images]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl = document.select("#landingImage").attr("src");
                            }
                        }

                        // 圖片處理
                        picOrUrl_changeStatus(isPic);

                        urlDatabase.addUrl(myUrl, myTitle, myDescription, myImageUrl, isPic);
                    } catch (Exception ignored) {
                        new ASRunner() {
                            @Override // com.kota.ASFramework.Thread.ASRunner
                            public void run() {
                                set_fail();
                            }
                        }.runInMainThread();
                    }
                });
            }
        } catch (Exception ignored) {
            new ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    set_fail();
                }
            }.runInMainThread();
        }
    }

    /** 判斷是圖片或連結, 改變顯示狀態 */
    void picOrUrl_changeStatus(boolean _isPic) {
        loadThumbnailImg = UserSettings.getLinkShowThumbnail();
        loadOnlyWifi = UserSettings.getLinkShowOnlyWifi();
        int _transportType = TempSettings.getTransportType();

        if (_isPic) { // 純圖片
            new ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    layoutDefault.setVisibility(GONE);

                    // 圖片
                    layoutPic.setVisibility(VISIBLE);
                    if (loadThumbnailImg && (!loadOnlyWifi || _transportType == 1)) {
                        prepare_load_image();
                    } else if (myImageUrl.equals("")) {
                        imageViewButton.setVisibility(GONE);
                    }

                    // 內容
                    layoutNormal.setVisibility(GONE);
                }
            }.runInMainThread();
        } else { // 內容網址
            new ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    layoutDefault.setVisibility(GONE);

                    // 圖片
                    layoutPic.setVisibility(VISIBLE);
                    if (loadThumbnailImg && (!loadOnlyWifi || _transportType == 1)) {
                        prepare_load_image();
                    } else if (myImageUrl.equals("")) {
                        imageViewButton.setVisibility(GONE);
                    }

                    // 內容
                    layoutNormal.setVisibility(VISIBLE);
                    set_normal();
                }
            }.runInMainThread();
        }
    }

    /** 純圖片 */
    public void prepare_load_image() {
        if (imgLoaded) return;

        if (isPic) {
            viewHeight = viewHeight /2;

        } else {
            viewHeight = viewHeight /4;
        }
        photoViewPic.setMinimumHeight(viewHeight);
        loadImage();
        urlView.setText(myImageUrl);
    }

    /** 內容網址 */
    private void set_normal() {
        if (!myTitle.isEmpty()) {
            titleView.setText(myTitle);
            titleView.setVisibility(VISIBLE);
        }
        if (!myDescription.isEmpty()) {
            descriptionView.setText(myDescription);
            descriptionView.setVisibility(VISIBLE);
        }
        urlView.setText(myUrl);
    }

    /** 意外處理 */
    private void set_fail() {
        layoutDefault.setVisibility(GONE);

        // 圖片
        layoutPic.setVisibility(GONE);

        // 內容
        layoutNormal.setVisibility(GONE);
    }

    /** 讀取圖片 */
    private void loadImage() {
        imgLoaded = true;
        new ASRunner() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                imageViewButton.setVisibility(GONE);
                photoViewPic.setVisibility(VISIBLE);
                photoViewPic.setContentDescription(myDescription);
                try {
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getContext());
                    circularProgressDrawable.setStrokeWidth(10f);
                    circularProgressDrawable.setCenterRadius(60f);
                    // progress bar color
                    TypedValue typedValue = new TypedValue();

                    getContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
                    circularProgressDrawable.setColorSchemeColors(getContextColor(typedValue.resourceId));
                    // progress bar start
                    circularProgressDrawable.start();

                    if (myImageUrl.isEmpty()) {
                        return;
                    }

                    photoViewPic.setImageDrawable(circularProgressDrawable);

                    Glide.with(Thumbnail_ItemView.this)
                            .load(myImageUrl)
                            .listener(new RequestListener<>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    new ASRunner() {
                                        @Override // com.kota.ASFramework.Thread.ASRunner
                                        public void run() {
                                            set_fail();
                                        }
                                    }.runInMainThread();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    try {
                                        Bitmap bitmap;
                                        if (resource instanceof GifDrawable)
                                            bitmap = ((GifDrawable) resource).getFirstFrame();
                                        else
                                            bitmap = ((BitmapDrawable)resource).getBitmap();
                                        int picHeight = bitmap.getHeight();
                                        int picWidth = bitmap.getWidth();
                                        int targetHeight = viewHeight;
                                        int targetWidth = viewWidth;

                                        float scaleWidth = (float) targetWidth / picWidth;
                                        float scaleHeight = (float) targetHeight / picHeight;
                                        float scale = Math.min(scaleWidth, scaleHeight);
                                        if (scale>1) scale=1;

                                        int tempHeight = (int) (picHeight * scale);
                                        targetHeight = Math.min(tempHeight, targetHeight);
                                        photoViewPic.setMinimumHeight(targetHeight);

                                        int tempWidth = (int) (picWidth * scale);
                                        targetWidth = Math.min(tempWidth, targetWidth);
                                        photoViewPic.setMinimumWidth(targetWidth);

                                        if (resource instanceof GifDrawable gifDrawable) {
                                            gifDrawable.startFromFirstFrame();
                                            photoViewPic.setImageDrawable(resource);
                                        } else {
                                            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                                            photoViewPic.setImageBitmap(newBitmap);
                                        }

                                    } catch (Exception ignored) {
                                        new ASRunner() {
                                            @Override // com.kota.ASFramework.Thread.ASRunner
                                            public void run() {
                                                set_fail();
                                            }
                                        }.runInMainThread();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                } catch (Exception ignored) {
                    new ASRunner() {
                        @Override // com.kota.ASFramework.Thread.ASRunner
                        public void run() {
                            set_fail();
                        }
                    }.runInMainThread();
                }
            }
        }.runInMainThread();
    }

    OnClickListener openUrlListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            myContext.startActivity(intent);
        }
    };

    OnClickListener titleListener = view -> {
        TextView textView = (TextView) view;
        if (textView.getMaxLines()==2)
            textView.setMaxLines(9);
        else
            textView.setMaxLines(2);
    };
    OnClickListener descriptionListener = view -> {
        TextView textView = (TextView) view;
        if (textView.getMaxLines()==1)
            textView.setMaxLines(9);
        else
            textView.setMaxLines(1);
    };

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.thumbnail, this);
        mainLayout = findViewById(R.id.thumbnail_content_view);
        layoutDefault = mainLayout.findViewById(R.id.thumbnail_default);

        layoutPic = mainLayout.findViewById(R.id.thumbnail_pic);
        photoViewPic = mainLayout.findViewById(R.id.thumbnail_image_pic);
        photoViewPic.setOnClickListener(openUrlListener);
        photoViewPic.setMaximumScale(20.0f);
        photoViewPic.setMediumScale(3.0f);

        imageViewButton = mainLayout.findViewById(R.id.thumbnail_image_button);
        imageViewButton.setOnClickListener(view -> prepare_load_image());

        layoutNormal = mainLayout.findViewById(R.id.thumbnail_normal);
        titleView = mainLayout.findViewById(R.id.thumbnail_title);
        titleView.setOnClickListener(titleListener);
        descriptionView = mainLayout.findViewById(R.id.thumbnail_description);
        descriptionView.setOnClickListener(descriptionListener);
        urlView = mainLayout.findViewById(R.id.thumbnail_url);
    }
}
