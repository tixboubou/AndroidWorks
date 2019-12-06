package com.bumptech.glide.signature;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;
import java.security.MessageDigest;

public final class ObjectKey implements Key {
    private final Object object;

    public ObjectKey(@NonNull Object object2) {
        this.object = Preconditions.checkNotNull(object2);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObjectKey{object=");
        sb.append(this.object);
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ObjectKey)) {
            return false;
        }
        return this.object.equals(((ObjectKey) o).object);
    }

    public int hashCode() {
        return this.object.hashCode();
    }

    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(this.object.toString().getBytes(CHARSET));
    }
}
