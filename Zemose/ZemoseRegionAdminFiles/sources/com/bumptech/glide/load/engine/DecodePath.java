package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import android.support.v4.util.Pools.Pool;
import android.util.Log;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.util.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DecodePath<DataType, ResourceType, Transcode> {
    private static final String TAG = "DecodePath";
    private final Class<DataType> dataClass;
    private final List<? extends ResourceDecoder<DataType, ResourceType>> decoders;
    private final String failureMessage;
    private final Pool<List<Throwable>> listPool;
    private final ResourceTranscoder<ResourceType, Transcode> transcoder;

    interface DecodeCallback<ResourceType> {
        @NonNull
        Resource<ResourceType> onResourceDecoded(@NonNull Resource<ResourceType> resource);
    }

    public DecodePath(Class<DataType> dataClass2, Class<ResourceType> resourceClass, Class<Transcode> transcodeClass, List<? extends ResourceDecoder<DataType, ResourceType>> decoders2, ResourceTranscoder<ResourceType, Transcode> transcoder2, Pool<List<Throwable>> listPool2) {
        this.dataClass = dataClass2;
        this.decoders = decoders2;
        this.transcoder = transcoder2;
        this.listPool = listPool2;
        StringBuilder sb = new StringBuilder();
        sb.append("Failed DecodePath{");
        sb.append(dataClass2.getSimpleName());
        sb.append("->");
        sb.append(resourceClass.getSimpleName());
        sb.append("->");
        sb.append(transcodeClass.getSimpleName());
        sb.append("}");
        this.failureMessage = sb.toString();
    }

    public Resource<Transcode> decode(DataRewinder<DataType> rewinder, int width, int height, @NonNull Options options, DecodeCallback<ResourceType> callback) throws GlideException {
        return this.transcoder.transcode(callback.onResourceDecoded(decodeResource(rewinder, width, height, options)), options);
    }

    @NonNull
    private Resource<ResourceType> decodeResource(DataRewinder<DataType> rewinder, int width, int height, @NonNull Options options) throws GlideException {
        List<Throwable> exceptions = (List) Preconditions.checkNotNull(this.listPool.acquire());
        try {
            return decodeResourceWithList(rewinder, width, height, options, exceptions);
        } finally {
            this.listPool.release(exceptions);
        }
    }

    @NonNull
    private Resource<ResourceType> decodeResourceWithList(DataRewinder<DataType> rewinder, int width, int height, @NonNull Options options, List<Throwable> exceptions) throws GlideException {
        Resource<ResourceType> result = null;
        int size = this.decoders.size();
        for (int i = 0; i < size; i++) {
            ResourceDecoder<DataType, ResourceType> decoder = (ResourceDecoder) this.decoders.get(i);
            try {
                if (decoder.handles(rewinder.rewindAndGet(), options)) {
                    result = decoder.decode(rewinder.rewindAndGet(), width, height, options);
                }
            } catch (IOException | OutOfMemoryError | RuntimeException e) {
                if (Log.isLoggable(TAG, 2)) {
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed to decode data for ");
                    sb.append(decoder);
                    Log.v(str, sb.toString(), e);
                }
                exceptions.add(e);
            }
            if (result != null) {
                break;
            }
        }
        if (result != null) {
            return result;
        }
        throw new GlideException(this.failureMessage, (List<Throwable>) new ArrayList<Throwable>(exceptions));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DecodePath{ dataClass=");
        sb.append(this.dataClass);
        sb.append(", decoders=");
        sb.append(this.decoders);
        sb.append(", transcoder=");
        sb.append(this.transcoder);
        sb.append('}');
        return sb.toString();
    }
}