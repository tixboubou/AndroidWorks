package com.bumptech.glide;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.load.data.InputStreamRewinder;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.prefill.BitmapPreFiller;
import com.bumptech.glide.load.engine.prefill.PreFillType.Builder;
import com.bumptech.glide.load.model.AssetUriLoader;
import com.bumptech.glide.load.model.ByteArrayLoader;
import com.bumptech.glide.load.model.ByteArrayLoader.ByteBufferFactory;
import com.bumptech.glide.load.model.ByteBufferEncoder;
import com.bumptech.glide.load.model.ByteBufferFileLoader;
import com.bumptech.glide.load.model.DataUrlLoader;
import com.bumptech.glide.load.model.FileLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.MediaStoreFileLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.ResourceLoader.AssetFileDescriptorFactory;
import com.bumptech.glide.load.model.ResourceLoader.FileDescriptorFactory;
import com.bumptech.glide.load.model.ResourceLoader.StreamFactory;
import com.bumptech.glide.load.model.ResourceLoader.UriFactory;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.model.StringLoader;
import com.bumptech.glide.load.model.UnitModelLoader.Factory;
import com.bumptech.glide.load.model.UriLoader;
import com.bumptech.glide.load.model.UrlUriLoader;
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader;
import com.bumptech.glide.load.model.stream.HttpUriLoader;
import com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader;
import com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader;
import com.bumptech.glide.load.model.stream.UrlLoader;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.ResourceBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.UnitBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.VideoDecoder;
import com.bumptech.glide.load.resource.bytes.ByteBufferRewinder;
import com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder;
import com.bumptech.glide.load.resource.drawable.UnitDrawableDecoder;
import com.bumptech.glide.load.resource.file.FileDecoder;
import com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawableEncoder;
import com.bumptech.glide.load.resource.gif.GifFrameResourceDecoder;
import com.bumptech.glide.load.resource.gif.StreamGifDecoder;
import com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder;
import com.bumptech.glide.load.resource.transcode.BitmapDrawableTranscoder;
import com.bumptech.glide.load.resource.transcode.DrawableBytesTranscoder;
import com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.RequestManagerRetriever;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.module.ManifestParser;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Glide implements ComponentCallbacks2 {
    private static final String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
    private static final String TAG = "Glide";
    private static volatile Glide glide;
    private static volatile boolean isInitializing;
    private final ArrayPool arrayPool;
    private final BitmapPool bitmapPool;
    private final BitmapPreFiller bitmapPreFiller;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;
    private final Engine engine;
    private final GlideContext glideContext;
    private final List<RequestManager> managers = new ArrayList();
    private final MemoryCache memoryCache;
    private MemoryCategory memoryCategory = MemoryCategory.NORMAL;
    private final Registry registry;
    private final RequestManagerRetriever requestManagerRetriever;

    @Nullable
    public static File getPhotoCacheDir(@NonNull Context context) {
        return getPhotoCacheDir(context, "image_manager_disk_cache");
    }

    @Nullable
    public static File getPhotoCacheDir(@NonNull Context context, @NonNull String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (result.mkdirs() || (result.exists() && result.isDirectory())) {
                return result;
            }
            return null;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    @NonNull
    public static Glide get(@NonNull Context context) {
        if (glide == null) {
            synchronized (Glide.class) {
                if (glide == null) {
                    checkAndInitializeGlide(context);
                }
            }
        }
        return glide;
    }

    private static void checkAndInitializeGlide(@NonNull Context context) {
        if (!isInitializing) {
            isInitializing = true;
            initializeGlide(context);
            isInitializing = false;
            return;
        }
        throw new IllegalStateException("You cannot call Glide.get() in registerComponents(), use the provided Glide instance instead");
    }

    @VisibleForTesting
    @Deprecated
    public static synchronized void init(Glide glide2) {
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            glide = glide2;
        }
    }

    @VisibleForTesting
    public static synchronized void init(@NonNull Context context, @NonNull GlideBuilder builder) {
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            initializeGlide(context, builder);
        }
    }

    @VisibleForTesting
    public static synchronized void tearDown() {
        synchronized (Glide.class) {
            if (glide != null) {
                glide.getContext().getApplicationContext().unregisterComponentCallbacks(glide);
                glide.engine.shutdown();
            }
            glide = null;
        }
    }

    private static void initializeGlide(@NonNull Context context) {
        initializeGlide(context, new GlideBuilder());
    }

    private static void initializeGlide(@NonNull Context context, @NonNull GlideBuilder builder) {
        Context applicationContext = context.getApplicationContext();
        GeneratedAppGlideModule annotationGeneratedModule = getAnnotationGeneratedGlideModules();
        List<GlideModule> manifestModules = Collections.emptyList();
        if (annotationGeneratedModule == null || annotationGeneratedModule.isManifestParsingEnabled()) {
            manifestModules = new ManifestParser(applicationContext).parse();
        }
        if (annotationGeneratedModule != null && !annotationGeneratedModule.getExcludedModuleClasses().isEmpty()) {
            Set<Class<?>> excludedModuleClasses = annotationGeneratedModule.getExcludedModuleClasses();
            Iterator<GlideModule> iterator = manifestModules.iterator();
            while (iterator.hasNext()) {
                GlideModule current = (GlideModule) iterator.next();
                if (excludedModuleClasses.contains(current.getClass())) {
                    if (Log.isLoggable(TAG, 3)) {
                        String str = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("AppGlideModule excludes manifest GlideModule: ");
                        sb.append(current);
                        Log.d(str, sb.toString());
                    }
                    iterator.remove();
                }
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            for (GlideModule glideModule : manifestModules) {
                String str2 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Discovered GlideModule from manifest: ");
                sb2.append(glideModule.getClass());
                Log.d(str2, sb2.toString());
            }
        }
        builder.setRequestManagerFactory(annotationGeneratedModule != null ? annotationGeneratedModule.getRequestManagerFactory() : null);
        for (GlideModule module : manifestModules) {
            module.applyOptions(applicationContext, builder);
        }
        if (annotationGeneratedModule != null) {
            annotationGeneratedModule.applyOptions(applicationContext, builder);
        }
        Glide glide2 = builder.build(applicationContext);
        for (GlideModule module2 : manifestModules) {
            module2.registerComponents(applicationContext, glide2, glide2.registry);
        }
        if (annotationGeneratedModule != null) {
            annotationGeneratedModule.registerComponents(applicationContext, glide2, glide2.registry);
        }
        applicationContext.registerComponentCallbacks(glide2);
        glide = glide2;
    }

    @Nullable
    private static GeneratedAppGlideModule getAnnotationGeneratedGlideModules() {
        try {
            return (GeneratedAppGlideModule) Class.forName("com.bumptech.glide.GeneratedAppGlideModuleImpl").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            if (!Log.isLoggable(TAG, 5)) {
                return null;
            }
            Log.w(TAG, "Failed to find GeneratedAppGlideModule. You should include an annotationProcessor compile dependency on com.github.bumptech.glide:compiler in your application and a @GlideModule annotated AppGlideModule implementation or LibraryGlideModules will be silently ignored");
            return null;
        } catch (InstantiationException e2) {
            throwIncorrectGlideModule(e2);
            return null;
        } catch (IllegalAccessException e3) {
            throwIncorrectGlideModule(e3);
            return null;
        } catch (NoSuchMethodException e4) {
            throwIncorrectGlideModule(e4);
            return null;
        } catch (InvocationTargetException e5) {
            throwIncorrectGlideModule(e5);
            return null;
        }
    }

    private static void throwIncorrectGlideModule(Exception e) {
        throw new IllegalStateException("GeneratedAppGlideModuleImpl is implemented incorrectly. If you've manually implemented this class, remove your implementation. The Annotation processor will generate a correct implementation.", e);
    }

    Glide(@NonNull Context context, @NonNull Engine engine2, @NonNull MemoryCache memoryCache2, @NonNull BitmapPool bitmapPool2, @NonNull ArrayPool arrayPool2, @NonNull RequestManagerRetriever requestManagerRetriever2, @NonNull ConnectivityMonitorFactory connectivityMonitorFactory2, int logLevel, @NonNull RequestOptions defaultRequestOptions, @NonNull Map<Class<?>, TransitionOptions<?, ?>> defaultTransitionOptions) {
        Context context2 = context;
        MemoryCache memoryCache3 = memoryCache2;
        BitmapPool bitmapPool3 = bitmapPool2;
        ArrayPool arrayPool3 = arrayPool2;
        this.engine = engine2;
        this.bitmapPool = bitmapPool3;
        this.arrayPool = arrayPool3;
        this.memoryCache = memoryCache3;
        this.requestManagerRetriever = requestManagerRetriever2;
        this.connectivityMonitorFactory = connectivityMonitorFactory2;
        DecodeFormat decodeFormat = (DecodeFormat) defaultRequestOptions.getOptions().get(Downsampler.DECODE_FORMAT);
        this.bitmapPreFiller = new BitmapPreFiller(memoryCache3, bitmapPool3, decodeFormat);
        Resources resources = context.getResources();
        this.registry = new Registry();
        this.registry.register((ImageHeaderParser) new DefaultImageHeaderParser());
        Downsampler downsampler = new Downsampler(this.registry.getImageHeaderParsers(), resources.getDisplayMetrics(), bitmapPool3, arrayPool3);
        ByteBufferGifDecoder byteBufferGifDecoder = new ByteBufferGifDecoder(context2, this.registry.getImageHeaderParsers(), bitmapPool3, arrayPool3);
        ResourceDecoder<ParcelFileDescriptor, Bitmap> parcelFileDescriptorVideoDecoder = VideoDecoder.parcel(bitmapPool2);
        ByteBufferBitmapDecoder byteBufferBitmapDecoder = new ByteBufferBitmapDecoder(downsampler);
        StreamBitmapDecoder streamBitmapDecoder = new StreamBitmapDecoder(downsampler, arrayPool3);
        ResourceDrawableDecoder resourceDrawableDecoder = new ResourceDrawableDecoder(context2);
        Downsampler downsampler2 = downsampler;
        StreamFactory resourceLoaderStreamFactory = new StreamFactory(resources);
        DecodeFormat decodeFormat2 = decodeFormat;
        UriFactory resourceLoaderUriFactory = new UriFactory(resources);
        FileDescriptorFactory resourceLoaderFileDescriptorFactory = new FileDescriptorFactory(resources);
        AssetFileDescriptorFactory resourceLoaderAssetFileDescriptorFactory = new AssetFileDescriptorFactory(resources);
        BitmapEncoder bitmapEncoder = new BitmapEncoder(arrayPool3);
        BitmapBytesTranscoder bitmapBytesTranscoder = new BitmapBytesTranscoder();
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder = new GifDrawableBytesTranscoder();
        AssetFileDescriptorFactory resourceLoaderAssetFileDescriptorFactory2 = resourceLoaderAssetFileDescriptorFactory;
        StreamBitmapDecoder streamBitmapDecoder2 = streamBitmapDecoder;
        FileDescriptorFactory resourceLoaderFileDescriptorFactory2 = resourceLoaderFileDescriptorFactory;
        UriFactory resourceLoaderUriFactory2 = resourceLoaderUriFactory;
        AssetFileDescriptorFactory resourceLoaderAssetFileDescriptorFactory3 = resourceLoaderAssetFileDescriptorFactory2;
        ByteBufferBitmapDecoder byteBufferBitmapDecoder2 = byteBufferBitmapDecoder;
        ResourceDecoder resourceDecoder = parcelFileDescriptorVideoDecoder;
        Context context3 = context;
        ContentResolver contentResolver = context.getContentResolver();
        BitmapBytesTranscoder bitmapBytesTranscoder2 = bitmapBytesTranscoder;
        ByteBufferGifDecoder byteBufferGifDecoder2 = byteBufferGifDecoder;
        ContentResolver contentResolver2 = contentResolver;
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder2 = gifDrawableBytesTranscoder;
        this.registry.append(ByteBuffer.class, (Encoder<Data>) new ByteBufferEncoder<Data>()).append(InputStream.class, (Encoder<Data>) new StreamEncoder<Data>(arrayPool3)).append(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class, byteBufferBitmapDecoder).append(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class, streamBitmapDecoder).append(Registry.BUCKET_BITMAP, ParcelFileDescriptor.class, Bitmap.class, parcelFileDescriptorVideoDecoder).append(Registry.BUCKET_BITMAP, AssetFileDescriptor.class, Bitmap.class, VideoDecoder.asset(bitmapPool2)).append(Bitmap.class, Bitmap.class, (ModelLoaderFactory<Model, Data>) Factory.getInstance()).append(Registry.BUCKET_BITMAP, Bitmap.class, Bitmap.class, new UnitBitmapDecoder()).append(Bitmap.class, (ResourceEncoder<TResource>) bitmapEncoder).append(Registry.BUCKET_BITMAP_DRAWABLE, ByteBuffer.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, (ResourceDecoder<DataType, Bitmap>) byteBufferBitmapDecoder)).append(Registry.BUCKET_BITMAP_DRAWABLE, InputStream.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, (ResourceDecoder<DataType, Bitmap>) streamBitmapDecoder)).append(Registry.BUCKET_BITMAP_DRAWABLE, ParcelFileDescriptor.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, parcelFileDescriptorVideoDecoder)).append(BitmapDrawable.class, (ResourceEncoder<TResource>) new BitmapDrawableEncoder<TResource>(bitmapPool3, bitmapEncoder)).append(Registry.BUCKET_GIF, InputStream.class, GifDrawable.class, new StreamGifDecoder(this.registry.getImageHeaderParsers(), byteBufferGifDecoder, arrayPool3)).append(Registry.BUCKET_GIF, ByteBuffer.class, GifDrawable.class, byteBufferGifDecoder).append(GifDrawable.class, (ResourceEncoder<TResource>) new GifDrawableEncoder<TResource>()).append(GifDecoder.class, GifDecoder.class, (ModelLoaderFactory<Model, Data>) Factory.getInstance()).append(Registry.BUCKET_BITMAP, GifDecoder.class, Bitmap.class, new GifFrameResourceDecoder(bitmapPool3)).append(Uri.class, Drawable.class, (ResourceDecoder<Data, TResource>) resourceDrawableDecoder).append(Uri.class, Bitmap.class, (ResourceDecoder<Data, TResource>) new ResourceBitmapDecoder<Data,TResource>(resourceDrawableDecoder, bitmapPool3)).register((DataRewinder.Factory<?>) new ByteBufferRewinder.Factory<Object>()).append(File.class, ByteBuffer.class, (ModelLoaderFactory<Model, Data>) new ByteBufferFileLoader.Factory<Model,Data>()).append(File.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new FileLoader.StreamFactory<Model,Data>()).append(File.class, File.class, (ResourceDecoder<Data, TResource>) new FileDecoder<Data,TResource>()).append(File.class, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new FileLoader.FileDescriptorFactory<Model,Data>()).append(File.class, File.class, (ModelLoaderFactory<Model, Data>) Factory.getInstance()).register((DataRewinder.Factory<?>) new InputStreamRewinder.Factory<Object>(arrayPool3)).append(Integer.TYPE, InputStream.class, (ModelLoaderFactory<Model, Data>) resourceLoaderStreamFactory).append(Integer.TYPE, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) resourceLoaderFileDescriptorFactory2).append(Integer.class, InputStream.class, (ModelLoaderFactory<Model, Data>) resourceLoaderStreamFactory).append(Integer.class, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) resourceLoaderFileDescriptorFactory2).append(Integer.class, Uri.class, (ModelLoaderFactory<Model, Data>) resourceLoaderUriFactory2).append(Integer.TYPE, AssetFileDescriptor.class, (ModelLoaderFactory<Model, Data>) resourceLoaderAssetFileDescriptorFactory3).append(Integer.class, AssetFileDescriptor.class, (ModelLoaderFactory<Model, Data>) resourceLoaderAssetFileDescriptorFactory3).append(Integer.TYPE, Uri.class, (ModelLoaderFactory<Model, Data>) resourceLoaderUriFactory2).append(String.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new DataUrlLoader.StreamFactory<Model,Data>()).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new DataUrlLoader.StreamFactory<Model,Data>()).append(String.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new StringLoader.StreamFactory<Model,Data>()).append(String.class, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new StringLoader.FileDescriptorFactory<Model,Data>()).append(String.class, AssetFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new StringLoader.AssetFileDescriptorFactory<Model,Data>()).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new HttpUriLoader.Factory<Model,Data>()).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new AssetUriLoader.StreamFactory<Model,Data>(context.getAssets())).append(Uri.class, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new AssetUriLoader.FileDescriptorFactory<Model,Data>(context.getAssets())).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new MediaStoreImageThumbLoader.Factory<Model,Data>(context3)).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new MediaStoreVideoThumbLoader.Factory<Model,Data>(context3)).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new UriLoader.StreamFactory<Model,Data>(contentResolver)).append(Uri.class, ParcelFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new UriLoader.FileDescriptorFactory<Model,Data>(contentResolver)).append(Uri.class, AssetFileDescriptor.class, (ModelLoaderFactory<Model, Data>) new UriLoader.AssetFileDescriptorFactory<Model,Data>(contentResolver)).append(Uri.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new UrlUriLoader.StreamFactory<Model,Data>()).append(URL.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new UrlLoader.StreamFactory<Model,Data>()).append(Uri.class, File.class, (ModelLoaderFactory<Model, Data>) new MediaStoreFileLoader.Factory<Model,Data>(context3)).append(GlideUrl.class, InputStream.class, (ModelLoaderFactory<Model, Data>) new HttpGlideUrlLoader.Factory<Model,Data>()).append(byte[].class, ByteBuffer.class, (ModelLoaderFactory<Model, Data>) new ByteBufferFactory<Model,Data>()).append(byte[].class, InputStream.class, (ModelLoaderFactory<Model, Data>) new ByteArrayLoader.StreamFactory<Model,Data>()).append(Uri.class, Uri.class, (ModelLoaderFactory<Model, Data>) Factory.getInstance()).append(Drawable.class, Drawable.class, (ModelLoaderFactory<Model, Data>) Factory.getInstance()).append(Drawable.class, Drawable.class, (ResourceDecoder<Data, TResource>) new UnitDrawableDecoder<Data,TResource>()).register(Bitmap.class, BitmapDrawable.class, new BitmapDrawableTranscoder(resources)).register(Bitmap.class, byte[].class, bitmapBytesTranscoder2).register(Drawable.class, byte[].class, new DrawableBytesTranscoder(bitmapPool3, bitmapBytesTranscoder2, gifDrawableBytesTranscoder2)).register(GifDrawable.class, byte[].class, gifDrawableBytesTranscoder2);
        ResourceDrawableDecoder resourceDrawableDecoder2 = resourceDrawableDecoder;
        ImageViewTargetFactory imageViewTargetFactory = new ImageViewTargetFactory();
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder3 = gifDrawableBytesTranscoder2;
        GlideContext glideContext2 = r1;
        Registry registry2 = this.registry;
        StreamFactory streamFactory = resourceLoaderStreamFactory;
        Resources resources2 = resources;
        BitmapBytesTranscoder bitmapBytesTranscoder3 = bitmapBytesTranscoder2;
        DecodeFormat decodeFormat3 = decodeFormat2;
        UriFactory uriFactory = resourceLoaderUriFactory2;
        DecodeFormat decodeFormat4 = decodeFormat3;
        GlideContext glideContext3 = new GlideContext(context, arrayPool2, registry2, imageViewTargetFactory, defaultRequestOptions, defaultTransitionOptions, engine2, logLevel);
        this.glideContext = glideContext2;
    }

    @NonNull
    public BitmapPool getBitmapPool() {
        return this.bitmapPool;
    }

    @NonNull
    public ArrayPool getArrayPool() {
        return this.arrayPool;
    }

    @NonNull
    public Context getContext() {
        return this.glideContext.getBaseContext();
    }

    /* access modifiers changed from: 0000 */
    public ConnectivityMonitorFactory getConnectivityMonitorFactory() {
        return this.connectivityMonitorFactory;
    }

    /* access modifiers changed from: 0000 */
    @NonNull
    public GlideContext getGlideContext() {
        return this.glideContext;
    }

    public void preFillBitmapPool(@NonNull Builder... bitmapAttributeBuilders) {
        this.bitmapPreFiller.preFill(bitmapAttributeBuilders);
    }

    public void clearMemory() {
        Util.assertMainThread();
        this.memoryCache.clearMemory();
        this.bitmapPool.clearMemory();
        this.arrayPool.clearMemory();
    }

    public void trimMemory(int level) {
        Util.assertMainThread();
        this.memoryCache.trimMemory(level);
        this.bitmapPool.trimMemory(level);
        this.arrayPool.trimMemory(level);
    }

    public void clearDiskCache() {
        Util.assertBackgroundThread();
        this.engine.clearDiskCache();
    }

    @NonNull
    public RequestManagerRetriever getRequestManagerRetriever() {
        return this.requestManagerRetriever;
    }

    @NonNull
    public MemoryCategory setMemoryCategory(@NonNull MemoryCategory memoryCategory2) {
        Util.assertMainThread();
        this.memoryCache.setSizeMultiplier(memoryCategory2.getMultiplier());
        this.bitmapPool.setSizeMultiplier(memoryCategory2.getMultiplier());
        MemoryCategory oldCategory = this.memoryCategory;
        this.memoryCategory = memoryCategory2;
        return oldCategory;
    }

    @NonNull
    private static RequestManagerRetriever getRetriever(@Nullable Context context) {
        Preconditions.checkNotNull(context, "You cannot start a load on a not yet attached View or a Fragment where getActivity() returns null (which usually occurs when getActivity() is called before the Fragment is attached or after the Fragment is destroyed).");
        return get(context).getRequestManagerRetriever();
    }

    @NonNull
    public static RequestManager with(@NonNull Context context) {
        return getRetriever(context).get(context);
    }

    @NonNull
    public static RequestManager with(@NonNull Activity activity) {
        return getRetriever(activity).get(activity);
    }

    @NonNull
    public static RequestManager with(@NonNull FragmentActivity activity) {
        return getRetriever(activity).get(activity);
    }

    @NonNull
    public static RequestManager with(@NonNull Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @Deprecated
    @NonNull
    public static RequestManager with(@NonNull android.app.Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @NonNull
    public static RequestManager with(@NonNull View view) {
        return getRetriever(view.getContext()).get(view);
    }

    @NonNull
    public Registry getRegistry() {
        return this.registry;
    }

    /* access modifiers changed from: 0000 */
    public boolean removeFromManagers(@NonNull Target<?> target) {
        synchronized (this.managers) {
            for (RequestManager requestManager : this.managers) {
                if (requestManager.untrack(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: 0000 */
    public void registerRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (!this.managers.contains(requestManager)) {
                this.managers.add(requestManager);
            } else {
                throw new IllegalStateException("Cannot register already registered manager");
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void unregisterRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (this.managers.contains(requestManager)) {
                this.managers.remove(requestManager);
            } else {
                throw new IllegalStateException("Cannot unregister not yet registered manager");
            }
        }
    }

    public void onTrimMemory(int level) {
        trimMemory(level);
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
        clearMemory();
    }
}
