package com.mike.loctest;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    private static Context ctx;
    private static MySingleton instance;
    private ImageLoader imageLoader = new ImageLoader(this.requestQueue, new ImageCache() {
        private final LruCache<String, Bitmap> cache = new LruCache<>(20);

        public Bitmap getBitmap(String url) {
            return (Bitmap) this.cache.get(url);
        }

        public void putBitmap(String url, Bitmap bitmap) {
            this.cache.put(url, bitmap);
        }
    });
    private RequestQueue requestQueue = getRequestQueue();

    private MySingleton(Context context) {
        ctx = context;
    }

    public static synchronized MySingleton getInstance(Context context) {
        MySingleton mySingleton;
        synchronized (MySingleton.class) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            mySingleton = instance;
        }
        return mySingleton;
    }

    public RequestQueue getRequestQueue() {
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return this.requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return this.imageLoader;
    }
}
