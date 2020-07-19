package com.example.tovisit_prakash_c0773839_android.database;

import android.content.Context;
import android.util.Log;


import com.example.tovisit_prakash_c0773839_android.interfaces.AddPlaceViewInterface;
import com.example.tovisit_prakash_c0773839_android.interfaces.DeletePlaceInterface;
import com.example.tovisit_prakash_c0773839_android.interfaces.EditPlaceViewInterface;
import com.example.tovisit_prakash_c0773839_android.interfaces.MainInterface;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocalCacheManager {
    private Context context;
    private static LocalCacheManager _instance;
    private AppDatabase db;

    public static LocalCacheManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new LocalCacheManager(context);
        }
        return _instance;
    }

    public LocalCacheManager(Context context) {
        this.context = context;
        db = AppDatabase.getAppDatabase(context);
    }

    public void getPlaces(final MainInterface mainViewInterface) {
        db.placeDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Place>>() {
                        @Override
                        public void accept(List<Place> notes) throws Exception {
                            mainViewInterface.onPlacesLoaded(notes);
                        }
                    });
    }





    public void addPlaces(final AddPlaceViewInterface addPlaceViewInterface, final Place place) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                db.placeDao().insertAll(place);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                addPlaceViewInterface.onPlaceAdded();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("ERROR", "onError: " + e);
                addPlaceViewInterface.onDataNotAvailable();
            }
        });
    }


    public void updateNote(final EditPlaceViewInterface editPlaceViewInterface, final Place place) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                db.placeDao().update(place);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                editPlaceViewInterface.onPlaceUpdated();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("ERROR", "onError: " + e);
        }
        });
    }


    public void deleteNote(final DeletePlaceInterface deletePlaceInterface, final Place place) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                db.placeDao().delete(place);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                deletePlaceInterface.onPlaceDeleted();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("ERROR", "onError: " + e);
            }
        });
    }



}
