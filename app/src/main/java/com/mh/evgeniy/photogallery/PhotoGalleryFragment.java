package com.mh.evgeniy.photogallery;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evgeniy on 25.07.2016.
 */
public class PhotoGalleryFragment  extends Fragment{
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems=new ArrayList<>();
    private boolean isLoading;
    private int currentFlickrPage=1;
    private PhotoAdapter mPhotoAdapter;
    private int lastVisibleItem; //номер последнего показаного айтема
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;


    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        isLoading =true;
        new FetchItemsTask().execute();

        Handler responseHandler=new Handler();
        mThumbnailDownloader=new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                photoHolder.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.setLruCache((ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE));
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mRecyclerView=(RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        ViewTreeObserver vto=mRecyclerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int width=0;
            int oneColumnWidth;
            int columnCount;
            @Override
            public void onGlobalLayout() {
                if(width!=mRecyclerView.getMeasuredWidth()) {
                    width = mRecyclerView.getMeasuredWidth();
                    oneColumnWidth = 200;
                    columnCount = (int) Math.floor(width / oneColumnWidth);

                    GridLayoutManager layoutManager= (GridLayoutManager)mRecyclerView.getLayoutManager();
                    layoutManager.setSpanCount(columnCount);

                    Log.d("listnr",String.valueOf(width));
                    Log.d("listnr",String.valueOf(columnCount));
                }
            }
        });
        setupAdapter();
        return v;
    }

    private void setupAdapter(){
        if(isAdded()){
            mPhotoAdapter=new PhotoAdapter(mItems);
            mRecyclerView.setAdapter(mPhotoAdapter);

            GridLayoutManager manager=(GridLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new ScrollListener(manager));
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        int totalItemsCount; //всего итемов в ресайклере
        int threshold=5; //порог итемов, короче смотри в код
        GridLayoutManager mManager;

        public ScrollListener(GridLayoutManager manager){
            mManager=manager;
            Log.d("childCount",mManager.toString());

        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            lastVisibleItem=mManager.findLastVisibleItemPosition();
            totalItemsCount=mManager.getItemCount();

            //Log.d("childCount",String.valueOf(lastVisibleItem));

            if(!isLoading && (lastVisibleItem+threshold)>=totalItemsCount){
                isLoading=true;
                        currentFlickrPage++;
                        //new FetchItemsTask().execute();

                        Log.d("childCount","end of list");
                        Log.d("childCount",String.valueOf(lastVisibleItem));
                        Log.d("childCount",String.valueOf(threshold));
                        Log.d("childCount",String.valueOf(totalItemsCount));
                    }
                }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>>{
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems();
            //return new FlickrFetchr().fetchItems(currentFlickrPage);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            //mItems=items;

            List<GalleryItem> buff=new ArrayList<GalleryItem>();
            buff.addAll(items);
            buff.addAll(mItems);

            mItems=buff;
            mPhotoAdapter.notifyDataSetChanged();
            mRecyclerView.getLayoutManager().scrollToPosition(lastVisibleItem);

            isLoading =false;
            setupAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView mItemImageView;
        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View view=inflater.inflate(R.layout.gallery_item,viewGroup,false);
            return new PhotoHolder(view);
        }
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder=getResources().getDrawable(R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);

            mThumbnailDownloader.queueThumbnail(photoHolder,galleryItem.getUrl());

            //предзагрузка
            //найти соседние +10 и -10 галери айтемов и загрузить в кеш
            for(int i=-10;i<=10;i++){

                Log.d("предзагрузка","mGalleryItems.size()="+mGalleryItems.size());
                Log.d("предзагрузка",""+(i+position));

                if(!((i+position)<=0 || (i+position)>mGalleryItems.size())){
                    mThumbnailDownloader.queueThumbnailToCache(mGalleryItems.get(position+i).getUrl());
                }
                else Log.d("предзагрузка","(mGalleryItems.get(position+i)==null");
            }


        }
        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

    }
    }
