package com.android.volley.toolbox;

import android.content.Context;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class NetworkImageView extends ImageView {
    /* access modifiers changed from: private */
    public int mDefaultImageId;
    /* access modifiers changed from: private */
    public int mErrorImageId;
    private ImageContainer mImageContainer;
    private ImageLoader mImageLoader;
    private String mUrl;

    public NetworkImageView(Context context) {
        this(context, null);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @MainThread
    public void setImageUrl(String url, ImageLoader imageLoader) {
        Threads.throwIfNotOnMainThread();
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        loadImageIfNecessary(false);
    }

    public void setDefaultImageResId(int defaultImage) {
        this.mDefaultImageId = defaultImage;
    }

    public void setErrorImageResId(int errorImage) {
        this.mErrorImageId = errorImage;
    }

    /* access modifiers changed from: 0000 */
    public void loadImageIfNecessary(final boolean isInLayoutPass) {
        boolean wrapHeight;
        boolean wrapWidth;
        int width = getWidth();
        int height = getHeight();
        ScaleType scaleType = getScaleType();
        boolean z = true;
        int maxHeight = 0;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == -2;
            wrapHeight = getLayoutParams().height == -2;
        } else {
            wrapWidth = false;
            wrapHeight = false;
        }
        if (!wrapWidth || !wrapHeight) {
            z = false;
        }
        boolean isFullyWrapContent = z;
        if (width != 0 || height != 0 || isFullyWrapContent) {
            if (TextUtils.isEmpty(this.mUrl)) {
                ImageContainer imageContainer = this.mImageContainer;
                if (imageContainer != null) {
                    imageContainer.cancelRequest();
                    this.mImageContainer = null;
                }
                setDefaultImageOrNull();
                return;
            }
            ImageContainer imageContainer2 = this.mImageContainer;
            if (!(imageContainer2 == null || imageContainer2.getRequestUrl() == null)) {
                if (!this.mImageContainer.getRequestUrl().equals(this.mUrl)) {
                    this.mImageContainer.cancelRequest();
                    setDefaultImageOrNull();
                } else {
                    return;
                }
            }
            int maxWidth = wrapWidth ? 0 : width;
            if (!wrapHeight) {
                maxHeight = height;
            }
            this.mImageContainer = this.mImageLoader.get(this.mUrl, new ImageListener() {
                public void onErrorResponse(VolleyError error) {
                    if (NetworkImageView.this.mErrorImageId != 0) {
                        NetworkImageView networkImageView = NetworkImageView.this;
                        networkImageView.setImageResource(networkImageView.mErrorImageId);
                    }
                }

                public void onResponse(final ImageContainer response, boolean isImmediate) {
                    if (!isImmediate || !isInLayoutPass) {
                        if (response.getBitmap() != null) {
                            NetworkImageView.this.setImageBitmap(response.getBitmap());
                        } else if (NetworkImageView.this.mDefaultImageId != 0) {
                            NetworkImageView networkImageView = NetworkImageView.this;
                            networkImageView.setImageResource(networkImageView.mDefaultImageId);
                        }
                        return;
                    }
                    NetworkImageView.this.post(new Runnable() {
                        public void run() {
                            AnonymousClass1.this.onResponse(response, false);
                        }
                    });
                }
            }, maxWidth, maxHeight, scaleType);
        }
    }

    private void setDefaultImageOrNull() {
        int i = this.mDefaultImageId;
        if (i != 0) {
            setImageResource(i);
        } else {
            setImageBitmap(null);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ImageContainer imageContainer = this.mImageContainer;
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            setImageBitmap(null);
            this.mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}