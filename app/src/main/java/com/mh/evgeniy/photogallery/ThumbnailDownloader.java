package com.mh.evgeniy.photogallery;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by evgeniy on 27.07.2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread{
    private static final String TAG="ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD=0;
    private static final int MESSAGE_DOWNLOAD_TO_CACHE=1;

    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap=new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache mLruCache;


    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener=listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler=responseHandler;
    }

    public void setLruCache(ActivityManager manager){
        int availMemInBytes=manager.getMemoryClass()*1024*1024;
        mLruCache=new LruCache<String,Bitmap>(availMemInBytes/8);

        Log.d(TAG,"mLruCache "+mLruCache.maxSize());
    }

    @Override
    protected void onLooperPrepared(){
        mRequestHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==MESSAGE_DOWNLOAD){
                    T target=(T) msg.obj;
                    Log.d(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
                if(msg.what==MESSAGE_DOWNLOAD_TO_CACHE){
                    String url=(String) msg.obj;
                    handleRequestPreloadToCache(url);
                }
            }
        };
    }

    public void queueThumbnail(T target,String url){
        Log.d(TAG, "Got a URL: " + url);

        if(url==null){
            mRequestMap.remove(target);
        }else {
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget(); //Message object’s target field to mRequestHandler
        }
    }

    public void queueThumbnailToCache(String url){
        Log.d(TAG, "Got a URL to cache: " + url);

        if(url!=null){
            if(mLruCache.get(url)==null)
                mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD_TO_CACHE,url).sendToTarget(); //Message object’s target field to mRequestHandler
        }
    }

    private void handleRequest(final T target){
        try{
            final  String url=mRequestMap.get(target);

            if(url==null) return;

            final Bitmap bitmap;

            if(mLruCache.get(url)==null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                mLruCache.put(url,bitmap);
                Log.d(TAG, "Bitmap created");
            }else {
                bitmap=(Bitmap) mLruCache.get(url);
            }

            mResponseHandler.post(new Runnable() { ///это выполняет main thread
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url) return;//By the time ThumbnailDownloader finishes downloading the Bitmap, RecyclerView may have recycled the PhotoHolder and requested a different URL for it.

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });

        }catch (IOException ioe){
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    private void handleRequestPreloadToCache(String url){


        try {
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            mLruCache.put(url, bitmap);
            Log.d(TAG, "Bitmap created to cache");
        }catch (IOException ioe){
            Log.e(TAG, "Error downloading image to cache", ioe);
        }
    }

    public void clearQueue(){
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
    }


}
