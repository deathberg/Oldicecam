package com.kusu.loadingbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.potplayer.music.R;
import okhttp3.HttpUrl;
import p056c1.AbstractC1033d;
import p056c1.C1032c;
import p081l.C1789q;

/* loaded from: classes.dex */
public class LoadingButton extends C1789q implements View.OnTouchListener {

    /* renamed from: d */
    public boolean f2563d;

    /* renamed from: e */
    public boolean f2564e;

    /* renamed from: f */
    public final boolean f2565f;

    /* renamed from: g */
    public int f2566g;

    /* renamed from: h */
    public final int f2567h;

    /* renamed from: i */
    public int f2568i;

    /* renamed from: j */
    public int f2569j;

    /* renamed from: k */
    public int f2570k;

    /* renamed from: l */
    public boolean f2571l;

    /* renamed from: m */
    public final int f2572m;

    /* renamed from: n */
    public final int f2573n;

    /* renamed from: o */
    public final int f2574o;

    /* renamed from: p */
    public final int f2575p;

    /* renamed from: q */
    public LayerDrawable f2576q;

    /* renamed from: r */
    public LayerDrawable f2577r;

    /* renamed from: s */
    public C1032c f2578s;

    /* renamed from: t */
    public final int f2579t;

    /* renamed from: u */
    public final int f2580u;

    /* renamed from: v */
    public int f2581v;

    /* renamed from: w */
    public String f2582w;

    /* renamed from: x */
    public Canvas f2583x;

    @SuppressLint({"ClickableViewAccessibility"})
    public LoadingButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.f2563d = false;
        this.f2565f = false;
        this.f2571l = false;
        this.f2579t = 15;
        this.f2580u = 10;
        this.f2582w = HttpUrl.FRAGMENT_ENCODE_SET;
        this.f2564e = true;
        Resources resources = getResources();
        if (resources != null) {
            this.f2567h = resources.getColor(R.color.white);
            this.f2566g = resources.getColor(R.color.fbutton_default_color);
            this.f2568i = resources.getColor(R.color.fbutton_default_shadow_color);
            this.f2569j = resources.getDimensionPixelSize(R.dimen.fbutton_default_shadow_height);
            this.f2570k = resources.getDimensionPixelSize(R.dimen.fbutton_default_conner_radius);
            this.f2582w = getText().toString();
        }
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, AbstractC1033d.f2106a);
        if (obtainStyledAttributes != null) {
            for (int i = 0; i < obtainStyledAttributes.getIndexCount(); i++) {
                int index = obtainStyledAttributes.getIndex(i);
                if (index == 4) {
                    this.f2564e = obtainStyledAttributes.getBoolean(index, true);
                } else if (index == 0) {
                    this.f2566g = obtainStyledAttributes.getColor(index, getResources().getColor(R.color.unpressed_color));
                } else if (index == 5) {
                    this.f2567h = obtainStyledAttributes.getColor(index, getResources().getColor(R.color.white));
                } else if (index == 8) {
                    this.f2568i = obtainStyledAttributes.getColor(index, getResources().getColor(R.color.pressed_color));
                    this.f2563d = true;
                } else if (index == 9) {
                    this.f2569j = obtainStyledAttributes.getDimensionPixelSize(index, R.dimen.fbutton_default_shadow_height);
                } else if (index == 1) {
                    this.f2570k = obtainStyledAttributes.getDimensionPixelSize(index, R.dimen.fbutton_default_conner_radius);
                } else if (index == 2) {
                    this.f2565f = obtainStyledAttributes.getBoolean(index, false);
                } else if (index == 3) {
                    this.f2571l = obtainStyledAttributes.getBoolean(index, false);
                } else if (index == 6) {
                    int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(index, R.dimen.fbutton_default_progress_margin);
                    this.f2570k = dimensionPixelSize;
                    this.f2579t = dimensionPixelSize;
                } else if (index == 7) {
                    int dimensionPixelSize2 = obtainStyledAttributes.getDimensionPixelSize(index, R.dimen.fbutton_default_progress_width);
                    this.f2570k = dimensionPixelSize2;
                    this.f2580u = dimensionPixelSize2;
                }
            }
            obtainStyledAttributes.recycle();
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, new int[]{android.R.attr.paddingLeft, android.R.attr.paddingRight});
            if (obtainStyledAttributes2 != null) {
                this.f2572m = obtainStyledAttributes2.getDimensionPixelSize(0, 0);
                this.f2573n = obtainStyledAttributes2.getDimensionPixelSize(1, 0);
                obtainStyledAttributes2.recycle();
                TypedArray obtainStyledAttributes3 = context.obtainStyledAttributes(attributeSet, new int[]{android.R.attr.paddingTop, android.R.attr.paddingBottom});
                if (obtainStyledAttributes3 != null) {
                    this.f2574o = obtainStyledAttributes3.getDimensionPixelSize(0, 0);
                    this.f2575p = obtainStyledAttributes3.getDimensionPixelSize(1, 0);
                    obtainStyledAttributes3.recycle();
                }
            }
        }
        setOnTouchListener(this);
    }

    private void setLoading(boolean z2) {
        this.f2571l = z2;
        if (z2) {
            m1770b(this.f2583x);
            setText(HttpUrl.FRAGMENT_ENCODE_SET);
        } else if (this.f2582w.length() != 0) {
            setText(this.f2582w);
        }
    }

    /* renamed from: a */
    public final LayerDrawable m1769a(int i, int i2, int i3) {
        float f2 = i;
        float[] fArr = {f2, f2, f2, f2, f2, f2, f2, f2};
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(fArr, null, null));
        shapeDrawable.getPaint().setColor(i2);
        ShapeDrawable shapeDrawable2 = new ShapeDrawable(new RoundRectShape(fArr, null, null));
        shapeDrawable2.getPaint().setColor(i3);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{shapeDrawable2, shapeDrawable});
        if (!this.f2564e || i2 == 0) {
            layerDrawable.setLayerInset(0, 0, this.f2569j, 0, 0);
        } else {
            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
        }
        layerDrawable.setLayerInset(1, 0, 0, 0, this.f2569j);
        return layerDrawable;
    }

    /* renamed from: b */
    public final void m1770b(Canvas canvas) {
        C1032c c1032c = this.f2578s;
        if (c1032c != null) {
            c1032c.draw(canvas);
            return;
        }
        int width = (getWidth() - getHeight()) / 2;
        this.f2581v = this.f2567h;
        this.f2578s = new C1032c(this.f2581v, this.f2580u);
        int i = this.f2579t + width;
        int width2 = (getWidth() - width) - this.f2579t;
        int height = getHeight();
        int i2 = this.f2579t;
        this.f2578s.setBounds(i, i2, width2, height - i2);
        this.f2578s.setCallback(this);
        this.f2578s.start();
    }

    /* renamed from: c */
    public final void m1771c() {
        setLoading(false);
    }

    /* renamed from: d */
    public final void m1772d() {
        int alpha = Color.alpha(this.f2566g);
        float[] fArr = new float[3];
        Color.colorToHSV(this.f2566g, fArr);
        fArr[2] = fArr[2] * 0.8f;
        if (!this.f2563d) {
            this.f2568i = Color.HSVToColor(alpha, fArr);
        }
        if (!isEnabled()) {
            Color.colorToHSV(this.f2566g, fArr);
            fArr[1] = fArr[1] * 0.6f;
            int HSVToColor = Color.HSVToColor(alpha, fArr);
            this.f2568i = HSVToColor;
            this.f2576q = m1769a(this.f2570k, HSVToColor, 0);
            this.f2577r = m1769a(this.f2570k, HSVToColor, 0);
        } else if (this.f2564e) {
            this.f2576q = m1769a(this.f2570k, 0, this.f2566g);
            this.f2577r = m1769a(this.f2570k, this.f2566g, this.f2568i);
        } else {
            this.f2569j = 0;
            this.f2576q = m1769a(this.f2570k, this.f2568i, 0);
            this.f2577r = m1769a(this.f2570k, this.f2566g, 0);
        }
        LayerDrawable layerDrawable = this.f2577r;
        if (layerDrawable != null) {
            setBackground(layerDrawable);
        }
        int i = this.f2572m;
        int i2 = this.f2574o;
        int i3 = this.f2569j;
        setPadding(i, i2 + i3, this.f2573n, this.f2575p + i3);
    }

    /* renamed from: e */
    public final void m1773e() {
        setLoading(true);
    }

    public int getButtonColor() {
        return this.f2566g;
    }

    public String getButtonText() {
        return this.f2582w;
    }

    @Override // android.widget.TextView
    public int getShadowColor() {
        return this.f2568i;
    }

    public int getShadowHeight() {
        return this.f2569j;
    }

    @Override // android.widget.TextView, android.view.View
    public final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.f2583x = canvas;
        if (this.f2571l) {
            m1770b(canvas);
            setText(HttpUrl.FRAGMENT_ENCODE_SET);
        } else if (this.f2582w.length() != 0) {
            setText(this.f2582w);
        }
    }

    @Override // android.view.View
    public final void onFinishInflate() {
        super.onFinishInflate();
        m1772d();
    }

    @Override // android.widget.TextView, android.view.View
    public final void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int size = View.MeasureSpec.getSize(i);
        if (this.f2565f) {
            this.f2570k = size / 2;
            m1772d();
        }
    }

    @Override // android.view.View.OnTouchListener
    public final boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            LayerDrawable layerDrawable = this.f2576q;
            if (layerDrawable != null) {
                setBackground(layerDrawable);
            }
            setPadding(this.f2572m, this.f2574o + this.f2569j, this.f2573n, this.f2575p);
            return false;
        }
        if (action != 1) {
            if (action == 2) {
                Rect rect = new Rect();
                view.getLocalVisibleRect(rect);
                if (rect.contains((int) motionEvent.getX(), (this.f2569j * 3) + ((int) motionEvent.getY())) || rect.contains((int) motionEvent.getX(), ((int) motionEvent.getY()) - (this.f2569j * 3))) {
                    return false;
                }
                LayerDrawable layerDrawable2 = this.f2577r;
                if (layerDrawable2 != null) {
                    setBackground(layerDrawable2);
                }
                int i = this.f2572m;
                int i2 = this.f2574o;
                int i3 = this.f2569j;
                setPadding(i, i2 + i3, this.f2573n, this.f2575p + i3);
                return false;
            }
            if (action != 3 && action != 4) {
                return false;
            }
        }
        LayerDrawable layerDrawable3 = this.f2577r;
        if (layerDrawable3 != null) {
            setBackground(layerDrawable3);
        }
        int i4 = this.f2572m;
        int i5 = this.f2574o;
        int i6 = this.f2569j;
        setPadding(i4, i5 + i6, this.f2573n, this.f2575p + i6);
        return false;
    }

    public void setButtonColor(int i) {
        this.f2566g = i;
        m1772d();
    }

    public void setButtonText(String str) {
        this.f2582w = str;
    }

    public void setCornerRadius(int i) {
        this.f2570k = i;
        m1772d();
    }

    @Override // android.widget.TextView, android.view.View
    public void setEnabled(boolean z2) {
        super.setEnabled(z2);
        m1772d();
    }

    public void setShadowColor(int i) {
        this.f2568i = i;
        this.f2563d = true;
        m1772d();
    }

    public void setShadowEnabled(boolean z2) {
        this.f2564e = z2;
        m1772d();
    }

    public void setShadowHeight(int i) {
        this.f2569j = i;
        m1772d();
    }
}
