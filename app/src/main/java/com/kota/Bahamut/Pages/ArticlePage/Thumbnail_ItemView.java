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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Vector;

public class Thumbnail_ItemView extends LinearLayout {
    Context _context;
    int _width;
    int _height;
    // 預設圖層
    LinearLayout _layout_default;
    // 圖片圖層
    LinearLayout _layout_pic;
    ImageView _image_view_pic;
    Button _image_view_button;
    // 內容圖層
    LinearLayout _layout_normal;
    TextView _title_view;
    TextView _description_view;
    TextView _url_view;
    boolean _isPic = false; // 是否為圖片
    boolean _load_thumbnail_img = false; // 自動顯示預覽圖
    boolean _load_only_wifi = false; // 只在wifi下預覽
    boolean _img_loaded = false; // 已經讀取預覽圖

    String _url = "";
    String _title = "";
    String _description = "";
    String _imageUrl = "";

    public Thumbnail_ItemView(Context context) {
        super(context);
        _context = context;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)_context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        _width = metrics.widthPixels;
        _height = metrics.heightPixels;
        init();
    }

    // 判斷URL內容
    public void loadUrl(String url) {
        _url = url;
        UrlDatabase urlDatabase = new UrlDatabase(getContext());
        Vector<String> findUrl = urlDatabase.getUrl(_url);
        _url_view.setText(_url);

        // 已經有URL資料
        if (findUrl!=null) {
            _title = findUrl.get(1);
            _description = findUrl.get(2);
            _imageUrl = findUrl.get(3);
            _isPic = !findUrl.get(4).equals("0");
            picOrUrl_changeStatus(_isPic);
        } else {
            // 尋找URL資料
            ASRunner.runInNewThread(() -> {
                // load heads
                try {
                    String userAgent = System.getProperty("http.agent");
                    if (_url.contains("youtu")) {
                        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0";
                    }
                    Connection.Response resp = Jsoup.connect(_url).ignoreContentType(true).header("User-Agent", userAgent).followRedirects(true).execute();
                    String contentType = resp.contentType();

                    if (contentType.contains("image/") || contentType.contains("video/")) {
                        _isPic = true;
                    }
                    // 域名判斷
                    if (url.contains("i.imgur")) {
                        _isPic = true;
                    }

                    // 圖片處理
                    if (_isPic) {
                        _title = _url;
                        _description = "";
                        _imageUrl = _url;
                        picOrUrl_changeStatus(true);
                    } else {
                        // 文字處理
                        Document document = resp.parse();

                        _title = document.title();
                        if (_title.isEmpty())
                            _title = document.select("meta[property=og:title]").attr("content");

                        _description = document.select("meta[name=description]").attr("content");
                        if (_description.isEmpty())
                            _description = document.select("meta[property=og:description]").attr("content");

                        _imageUrl = document.select("meta[property=og:image]").attr("content");
                        if (_imageUrl.isEmpty())
                            _imageUrl = document.select("meta[property=og:image]").attr("content");

                        picOrUrl_changeStatus(false);
                    }

                    urlDatabase.addUrl(_url, _title, _description, _imageUrl,_isPic);
                } catch (Exception e) {
                    new ASRunner() {
                        @Override // com.kota.ASFramework.Thread.ASRunner
                        public void run() {
                            set_fail();
                        }
                    }.runInMainThread();
                }
            });
        }
    }
    // 判斷是圖片或連結, 改變顯示狀態
    void picOrUrl_changeStatus(boolean _isPic) {
        _load_thumbnail_img = UserSettings.getLinkShowThumbnail();
        _load_only_wifi = UserSettings.getLinkShowOnlyWifi();
        int _transportType = TempSettings.getTransportType();

        if (_isPic) { // 純圖片
            new ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    _layout_default.setVisibility(GONE);

                    // 圖片
                    _layout_pic.setVisibility(VISIBLE);
                    if (_load_thumbnail_img && (!_load_only_wifi || _transportType == 1)) {
                        prepare_load_image();
                    } else if (_imageUrl.equals("")) {
                        _image_view_button.setVisibility(GONE);
                    }

                    // 內容
                    _layout_normal.setVisibility(GONE);
                }
            }.runInMainThread();
        } else { // 內容網址
            new ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                public void run() {
                    _layout_default.setVisibility(GONE);

                    // 圖片
                    _layout_pic.setVisibility(VISIBLE);
                    if (_load_thumbnail_img && (!_load_only_wifi || _transportType == 1)) {
                        prepare_load_image();
                    } else if (_imageUrl.equals("")) {
                        _image_view_button.setVisibility(GONE);
                    }

                    // 內容
                    _layout_normal.setVisibility(VISIBLE);
                    set_normal();
                }
            }.runInMainThread();
        }
    }

    // 純圖片
    public void prepare_load_image() {
        if (_img_loaded) return;
        if (_isPic) {
            _height = _height/2;

        } else {
            _height = _height/4;
        }
        _image_view_pic.setMinimumHeight(_height);
        loadImage();
        _url_view.setText(_imageUrl);
    }

    // 內容網址
    private void set_normal() {
        if (!_title.isEmpty()) {
            _title_view.setText(_title);
            _title_view.setVisibility(VISIBLE);
        }
        if (!_description.isEmpty()) {
            _description_view.setText(_description);
            _description_view.setVisibility(VISIBLE);
        }
        _url_view.setText(_url);
    }

    // 意外處理
    private void set_fail() {
        _layout_default.setVisibility(GONE);

        // 圖片
        _layout_pic.setVisibility(GONE);

        // 內容
        _layout_normal.setVisibility(GONE);

        Log.v("Thumbnail_Fail", _imageUrl.isEmpty()?_url:_imageUrl);
    }

    // 讀取圖片
    private void loadImage() {
        _img_loaded = true;
        new ASRunner() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                _image_view_button.setVisibility(GONE);
                _image_view_pic.setVisibility(VISIBLE);
                _image_view_pic.setContentDescription(_description);
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

                    if (_imageUrl.isEmpty()) {
                        throw new Exception(_url);
                    }

                    _image_view_pic.setImageDrawable(circularProgressDrawable);

                    Glide.with(Thumbnail_ItemView.this)
                            .load(_imageUrl)
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
                                        int targetHeight = _height;
                                        int targetWidth = _width;

                                        float scaleWidth = (float) targetWidth / picWidth;
                                        float scaleHeight = (float) targetHeight / picHeight;
                                        float scale = Math.min(scaleWidth, scaleHeight);
                                        if (scale>1) scale=1;

                                        int tempHeight = (int) (picHeight * scale);
                                        targetHeight = Math.min(tempHeight, targetHeight);
                                        _image_view_pic.setMinimumHeight(targetHeight);

                                        int tempWidth = (int) (picWidth * scale);
                                        targetWidth = Math.min(tempWidth, targetWidth);
                                        _image_view_pic.setMinimumWidth(targetWidth);

                                        if (resource instanceof GifDrawable) {
                                            GifDrawable gifDrawable = (GifDrawable) resource;
                                            gifDrawable.startFromFirstFrame();
                                            _image_view_pic.setImageDrawable(resource);
                                        } else {
                                            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                                            _image_view_pic.setImageBitmap(newBitmap);
                                        }

                                    } catch (Exception e) {
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
                } catch (Exception e) {
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

    OnClickListener _open_url_listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(_url));
            _context.startActivity(intent);
        }
    };

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.thumbnail, this);
        _layout_default = findViewById(R.id.thumbnail_default);

        _layout_pic = findViewById(R.id.thumbnail_pic);
        _image_view_pic = findViewById(R.id.thumbnail_image_pic);
        _image_view_pic.setOnClickListener(_open_url_listener);
        _image_view_button = findViewById(R.id.thumbnail_image_button);
        _image_view_button.setOnClickListener(view -> prepare_load_image());

        _layout_normal = findViewById(R.id.thumbnail_normal);
        _title_view = findViewById(R.id.thumbnail_title);
        _description_view = findViewById(R.id.thumbnail_description);
        _url_view = findViewById(R.id.thumbnail_url);
    }
}
