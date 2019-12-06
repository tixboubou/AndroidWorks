package com.bumptech.glide.request.transition;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.Transition.ViewAdapter;

public abstract class BitmapContainerTransitionFactory<R> implements TransitionFactory<R> {
    private final TransitionFactory<Drawable> realFactory;

    private final class BitmapGlideAnimation implements Transition<R> {
        private final Transition<Drawable> transition;

        BitmapGlideAnimation(Transition<Drawable> transition2) {
            this.transition = transition2;
        }

        public boolean transition(R current, ViewAdapter adapter2) {
            return this.transition.transition(new BitmapDrawable(adapter2.getView().getResources(), BitmapContainerTransitionFactory.this.getBitmap(current)), adapter2);
        }
    }

    /* access modifiers changed from: protected */
    public abstract Bitmap getBitmap(R r);

    public BitmapContainerTransitionFactory(TransitionFactory<Drawable> realFactory2) {
        this.realFactory = realFactory2;
    }

    public Transition<R> build(DataSource dataSource, boolean isFirstResource) {
        return new BitmapGlideAnimation(this.realFactory.build(dataSource, isFirstResource));
    }
}
