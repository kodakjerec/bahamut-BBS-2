package com.kota.Bahamut.Pages.ArticlePage;

import com.kota.Bahamut.Service.CommonFunctions.getContextColor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

import com.github.chrisbanes.photoview.PhotoView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.DataModels.UrlDatabase
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings

import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.util.HashMap
import java.util.Map
import java.util.Vector

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class Thumbnail_ItemView : LinearLayout()() {
    var mainLayout: LinearLayout
    var myContext: Context
    var viewWidth: Int
    var viewHeight: Int
    // 預設圖層
    var layoutDefault: LinearLayout
    // 圖片圖層
    var layoutPic: LinearLayout
    var photoViewPic: PhotoView
    var imageViewButton: Button
    // 內容圖層
    var layoutNormal: LinearLayout
    var titleView: TextView
    var descriptionView: TextView
    var urlView: TextView
    var isPic: Boolean = false; // 是否為圖片
    var loadThumbnailImg: Boolean = false; // 自動顯示預覽圖
    var loadOnlyWifi: Boolean = false; // 只在wifi下預覽
    var imgLoaded: Boolean = false; // 已經讀取預覽圖

    var myUrl: String = "";
    var myTitle: String = "";
    var myDescription: String = "";
    var myImageUrl: String = "";

    public Thumbnail_ItemView(Context context) {
        super(context);
        myContext = context;
        var metrics: DisplayMetrics = DisplayMetrics();
        ((Activity) myContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        viewWidth = metrics.widthPixels;
        viewHeight = metrics.heightPixels;
        init();
    }

    /** 判斷URL內容 */
    loadUrl(String url): Unit {
        myUrl = url;

        try var urlDatabase: (UrlDatabase = UrlDatabase(getContext())) {
            var findUrl: Vector<String> = urlDatabase.getUrl(myUrl);
            urlView.setText(myUrl);
            // 已經有URL資料
            var (findUrl!: if =null) {
                myTitle = findUrl.get(1);
                myDescription = findUrl.get(2);
                myImageUrl = findUrl.get(3);
                isPic = !findUrl.get(4) == "0";
                picOrUrl_changeStatus(isPic);
            } else {
                var apiUrl: String = "https://worker-get-url-content.kodakjerec.workers.dev/";
                var client: OkHttpClient = OkHttpClient();
                var body: RequestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", myUrl)
                        .build();
                var request: Request = Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build();

                // 尋找URL資料
                ASRunner.runInNewThread(() -> {
                    try {
                        // load heads
                        var response: Response = client.newCall(request).execute();
                        assert var !: response.body() = null;
                        var data: String = response.body().String();
                        var jsonObject: JSONObject = JSONObject(data);

                        var contentType: String = jsonObject.getString("contentType");

                        if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                            isPic = true;
                        }
                        myTitle = jsonObject.getString("title");
                        myDescription = jsonObject.getString("desc");
                        myImageUrl = jsonObject.getString("imageUrl");

                        // 非圖片類比較會有擷取問題
                        if (!isPic && (myTitle == "" || myDescription == "")) {
                            var userAgent: String = System.getProperty("http.agent");
                            if (myUrl.contains("youtu") || myUrl.contains("amazon"))
                                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0";

                            // cookie
                            // Create a Map to store cookies
                            Map<String, var cookies: String> = new HashMap<>();
                            if (myUrl.contains("ptt"))
                                cookies.put("over18", "1");  // Add the over18 cookie with value 1

                            var resp: Connection.Response = Jsoup
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
                                var document: Document = resp.parse();

                                myTitle = document.title();
                                if (myTitle.isEmpty())
                                    myTitle var document.select("meta[property: = =og:title]").attr("content");

                                myDescription var document.select("meta[name: = =description]").attr("content");
                                if (myDescription.isEmpty())
                                    myDescription var document.select("meta[property: = =og:description]").attr("content");

                                myImageUrl var document.select("meta[property: = =og:image]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl var document.select("meta[property: = =og:image]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl var document.select("meta[property: = =og:images]").attr("content");
                                if (myImageUrl.isEmpty())
                                    myImageUrl = document.select("#landingImage").attr("src");
                            }
                        }

                        // 圖片處理
                        picOrUrl_changeStatus(isPic);

                        urlDatabase.addUrl(myUrl, myTitle, myDescription, myImageUrl, isPic);
                    } catch (Exception ignored) {
                        ASRunner() {
                            @Override // com.kota.ASFramework.Thread.ASRunner
                            run(): Unit {
                                set_fail();
                            }
                        }.runInMainThread();
                    }
                });
            }
        } catch (Exception ignored) {
            ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                run(): Unit {
                    set_fail();
                }
            }.runInMainThread();
        }
    }

    /** 判斷是圖片或連結, 改變顯示狀態 */
    Unit picOrUrl_changeStatus(Boolean _isPic) {
        loadThumbnailImg = UserSettings.getLinkShowThumbnail();
        loadOnlyWifi = UserSettings.getLinkShowOnlyWifi();
        var _transportType: Int = TempSettings.transportType;

        if (_isPic) { // 純圖片
            ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                run(): Unit {
                    layoutDefault.setVisibility(GONE);

                    // 圖片
                    layoutPic.setVisibility(VISIBLE);
                    if (loadThumbnailImg && (!loadOnlyWifi var _transportType: || == 1)) {
                        prepare_load_image();
                    } else if (myImageUrl == "") {
                        imageViewButton.setVisibility(GONE);
                    }

                    // 內容
                    layoutNormal.setVisibility(GONE);
                }
            }.runInMainThread();
        } else { // 內容網址
            ASRunner() {
                @Override // com.kota.ASFramework.Thread.ASRunner
                run(): Unit {
                    layoutDefault.setVisibility(GONE);

                    // 圖片
                    layoutPic.setVisibility(VISIBLE);
                    if (loadThumbnailImg && (!loadOnlyWifi var _transportType: || == 1)) {
                        prepare_load_image();
                    } else if (myImageUrl == "") {
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
    prepare_load_image(): Unit {
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
    private fun set_normal(): Unit {
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
    private fun set_fail(): Unit {
        layoutDefault.setVisibility(GONE);

        // 圖片
        layoutPic.setVisibility(GONE);

        // 內容
        layoutNormal.setVisibility(GONE);
    }

    /** 讀取圖片 */
    private fun loadImage(): Unit {
        imgLoaded = true;
        ASRunner() {
            @SuppressLint("ResourceType")
            @Override
            run(): Unit {
                imageViewButton.setVisibility(GONE);
                photoViewPic.setVisibility(VISIBLE);
                photoViewPic.setContentDescription(myDescription);
                try {
                    var circularProgressDrawable: CircularProgressDrawable = CircularProgressDrawable(getContext());
                    circularProgressDrawable.setStrokeWidth(10f);
                    circularProgressDrawable.setCenterRadius(60f);
                    // progress bar color
                    var typedValue: TypedValue = TypedValue();

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
                            .listener(RequestListener<>() {
                                @Override
                                onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, Boolean isFirstResource): Boolean {
                                    ASRunner() {
                                        @Override // com.kota.ASFramework.Thread.ASRunner
                                        run(): Unit {
                                            set_fail();
                                        }
                                    }.runInMainThread();
                                    var false: return
                                }

                                @Override
                                onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, Boolean isFirstResource): Boolean {
                                    var false: return
                                }
                            })
                            .into(CustomTarget<Drawable>() {
                                @Override
                                onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition): Unit {
                                    try {
                                        var bitmap: Bitmap
                                        if (resource is GifDrawable)
                                            bitmap = ((GifDrawable) resource).getFirstFrame();
                                        else
                                            bitmap = ((BitmapDrawable)resource).getBitmap();
                                        var picHeight: Int = bitmap.getHeight();
                                        var picWidth: Int = bitmap.getWidth();
                                        var targetHeight: Int = viewHeight;
                                        var targetWidth: Int = viewWidth;

                                        var scaleWidth: Float = (Float) targetWidth / picWidth;
                                        var scaleHeight: Float = (Float) targetHeight / picHeight;
                                        var scale: Float = Math.min(scaleWidth, scaleHeight);
                                        if var scale: (scale>1) =1;

                                        var tempHeight: Int = (Int) (picHeight * scale);
                                        targetHeight = Math.min(tempHeight, targetHeight);
                                        photoViewPic.setMinimumHeight(targetHeight);

                                        var tempWidth: Int = (Int) (picWidth * scale);
                                        targetWidth = Math.min(tempWidth, targetWidth);
                                        photoViewPic.setMinimumWidth(targetWidth);

                                        if (resource is GifDrawable gifDrawable) {
                                            gifDrawable.startFromFirstFrame();
                                            photoViewPic.setImageDrawable(resource);
                                        } else {
                                            var newBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                                            photoViewPic.setImageBitmap(newBitmap);
                                        }

                                    } catch (Exception ignored) {
                                        ASRunner() {
                                            @Override // com.kota.ASFramework.Thread.ASRunner
                                            run(): Unit {
                                                set_fail();
                                            }
                                        }.runInMainThread();
                                    }
                                }

                                @Override
                                onLoadCleared(@Nullable Drawable placeholder): Unit {
                                }
                            });
                } catch (Exception ignored) {
                    ASRunner() {
                        @Override // com.kota.ASFramework.Thread.ASRunner
                        run(): Unit {
                            set_fail();
                        }
                    }.runInMainThread();
                }
            }
        }.runInMainThread();
    }

    var openUrlListener: OnClickListener = OnClickListener() {
        @Override
        onClick(View view): Unit {
            var intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            myContext.startActivity(intent);
        }
    };

    var titleListener: OnClickListener = view -> {
        var textView: TextView = (TextView) view;
        var (textView.getMaxLines(): if ==2)
            textView.setMaxLines(9);
        else
            textView.setMaxLines(2);
    };
    var descriptionListener: OnClickListener = view -> {
        var textView: TextView = (TextView) view;
        var (textView.getMaxLines(): if ==1)
            textView.setMaxLines(9);
        else
            textView.setMaxLines(1);
    };

    private fun init(): Unit {
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


