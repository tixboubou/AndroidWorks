package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.cache.DiskCache.Writer;
import java.io.File;

class DataCacheWriter<DataType> implements Writer {

    /* renamed from: data reason: collision with root package name */
    private final DataType f7data;
    private final Encoder<DataType> encoder;
    private final Options options;

    DataCacheWriter(Encoder<DataType> encoder2, DataType data2, Options options2) {
        this.encoder = encoder2;
        this.f7data = data2;
        this.options = options2;
    }

    public boolean write(@NonNull File file) {
        return this.encoder.encode(this.f7data, file, this.options);
    }
}
