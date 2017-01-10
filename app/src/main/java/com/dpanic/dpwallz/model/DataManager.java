package com.dpanic.dpwallz.model;

import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

public class DataManager {
    private static DataManager mDataManager;
    private final BriteDatabase database;
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            mDataManager = new DataManager(context);
        }

        return mDataManager;
    }


    private DataManager(Context context) {
        SqlBrite sqlBrite = SqlBrite.create(new SqlBrite.Logger() {
            @Override
            public void log(String s) {
                Log.d("thanh.dao", "db log: " + s);
            }
        });

        DBOpenHelper dbHelper = DBOpenHelper.getInstance(context);

        database = sqlBrite.wrapDatabaseHelper(dbHelper, Schedulers.io());
    }

    /* Image function */
    private Observable<Integer> addImage(final Image image) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext((int) database.insert(Image.TABLE_NAME, Image.toContentValues(image)));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });

    }

    @SuppressWarnings("unused")
    public Observable<Integer> addImages(final List<Image> imageList) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    int count = 0;
                    for (Image image : imageList) {
                        if (database.insert(Image.TABLE_NAME, Image.toContentValues(image)) >= 0) {
//                            subscriber.onNext(image);
                            count++;
                        }
                    }
                    subscriber.onNext(count);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> deleteImage(final Image image) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
//                    int result = database.delete(Image.TABLE_NAME, Image.FIELD_PEXELS_ID + " = ?", image.getPexelId());
//                    if (result > 0) {
                    subscriber.onNext(database.delete(Image.TABLE_NAME, Image.FIELD_PEXELS_ID + " = ?",
                                                      image.getPexelId()));
                    //                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }

    public Observable<List<Image>> getFavoriteImageList() {
        String query = "SELECT * FROM "
                + Image.TABLE_NAME
                + " WHERE "
                + Image.FIELD_IS_FAVORITE
                + " = ? ORDER BY " + Image.FIELD_PKEY + " DESC";
        return database.createQuery(Image.TABLE_NAME, query, String.valueOf(1)).mapToList(Image.MAPPER);
    }

    public Observable<List<Image>> getHistoryImageList() {
        String query = "SELECT * FROM "
                + Image.TABLE_NAME
                + " WHERE "
                + Image.FIELD_LOCAL_LINK
                + " != ? ORDER BY " + Image.FIELD_PKEY + " DESC";
        return database.createQuery(Image.TABLE_NAME, query, String.valueOf("")).mapToList(Image.MAPPER);
    }

    public Observable<List<Image>> getAllImages() {
        String query = "SELECT * FROM "
                + Image.TABLE_NAME;
        return database.createQuery(Image.TABLE_NAME, query, String.valueOf("")).mapToList(Image.MAPPER);
    }

    public Observable<Image> getImage(final Image image) {
        final String query = "SELECT * FROM "
                + Image.TABLE_NAME
                + " WHERE "
                + Image.FIELD_PEXELS_ID
                + " = ? ";

//        return database.createQuery(Image.TABLE_NAME, query, image.getPexelId())
//                .flatMap(new Func1<SqlBrite.Query, Observable<Cursor>>() {
//                    @Override
//                    public Observable<Cursor> call(SqlBrite.Query query) {
//                        return Observable.just(query.run());
//                    }
//                }).map(Image.MAPPER);

        return Observable.create(new Observable.OnSubscribe<Image>() {
            @Override
            public void call(final Subscriber<? super Image> subscriber) {
                compositeSubscription.add(database.createQuery(Image.TABLE_NAME, query, image.getPexelId()).mapToList
                        (Image.MAPPER)
                        .subscribe(new Action1<List<Image>>() {
                            @Override
                            public void call(List<Image> images) {
                                try {
                                    if (images.size() > 0) {
                                        subscriber.onNext(images.get(0));
                                    }
                                    subscriber.onCompleted();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    subscriber.onError(e);
                                }
                            }
                        }));
            }
        });
    }

    @SuppressWarnings("unused")
    public Observable<Boolean> isFavoriteImage(final Image image) {
        final String query = "SELECT * FROM "
            + Image.TABLE_NAME
            + " WHERE "
            + Image.FIELD_PEXELS_ID
            + " = ? AND " + Image.FIELD_IS_FAVORITE + " = " + DB.BOOLEAN_TRUE;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    Cursor cursor = database.query(query, image.getPexelId());
                    Image img = null;
                    if (cursor != null && cursor.moveToFirst()) {
                        img = Image.parseCursor(cursor);
                        cursor.close();
                    }
                    subscriber.onNext(img != null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    /* Category function */
    public Observable<List<Category>> getCategoryList() {
        String query = "SELECT * FROM "
                + Category.TABLE_NAME;
        return database.createQuery(Category.TABLE_NAME, query).mapToList(Category.MAPPER);
    }

    public Observable<Integer> deleteCategoryData() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(database.delete(Category.TABLE_NAME, null, null));
                    Log.e("thanh.dao", "deleteCategoryData: clear categories data");
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Integer> clearThenInsertCategories(final List<Category> categories) {
        return deleteCategoryData().map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                int count = 0;
                for (Category category : categories) {
                    if (database.insert(Category.TABLE_NAME, Category.toContentValues(category)) > 0) {
                        count++;
                    }
                }
                return count;
            }
        });
    }

    public Observable<Integer> addCategories(final List<Category> list) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    int count = 0;
                    for (Category category : list) {
                        if (database.insert(Category.TABLE_NAME, Category.toContentValues(category)) > 0) {
                            count++;
                        }
                    }

                    subscriber.onNext(count);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public void forceToAddImage(final Image image) {
        Log.e("thanh.dao", "forceToAddImage: ");
//        final Subscription[] sub = new Subscription[1];
//        String quey = "INSERT OR REPLACE INTO " +Image.TABLE_NAME + " ("+Image.FIELD_PEXELS_ID, name, role) \n" +
//                "  VALUES (  1, \n" +
//                "            'Susan Bar',\n" +
//                "            COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer')\n" +
//                "          );";
//        Log.d("thanh.dao", "forceToAddImage: ");
        compositeSubscription.add(deleteImage(image).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {

                Log.d("thanh.dao", "call: delete image done = " + integer);
                compositeSubscription.add(addImage(image).subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("thanh.dao", "call: insert image done = " + integer);
                    }
                }));
            }
        }));
    }

    public void destruct() {
//        if (compositeSubscription != null) {
//            compositeSubscription.unsubscribe();
//        }
    }
}
