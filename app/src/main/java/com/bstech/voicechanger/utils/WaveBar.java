package com.bstech.voicechanger.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.bstech.voicechanger.R;


public class WaveBar extends View {
    private static final int[] COLUMN_1;
    private static final int[] COLUMN_2;
    private static final int[] COLUMN_3;
    private static final int[] COLUMN_4;

    static {
        COLUMN_1 = new int[]{14, 15, 16, 17, 18, 19, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 19, 18, 17, 16, 15, 14, 15, 16, 17, 18, 19, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        COLUMN_2 = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 9, 8, 7, 6, 5, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 10, 9, 8, 7, 6, 5, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        COLUMN_3 = new int[]{7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 15, 14, 13, 12, 11, 10, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 6, 7, 8, 9, 10, 11, 10, 9, 8, 7, 6, 5, 4, 3, 4, 5, 6, 7, 8, 9, 10, 11, 10, 9, 8, 7, 6};
        COLUMN_4 = new int[]{20, 19, 18, 17, 16, 15, 14, 12, 11, 10, 9, 8, 9, 10, 11, 12, 13, 14, 15, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 11, 10, 9, 8, 7, 6, 5, 4, 5, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    }

    private Paint paint;
    private float radius;
    private RectF[] rectFs;
    private int width;
    private int height;
    private int j;
    private int k;
    private float l;
    private float m;
    private int count;
    private boolean isPlaying;

    public WaveBar(Context context) {
        this(context, null, 0);
    }

    public WaveBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WaveBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        a(attributeSet, i);
    }

    private void a(AttributeSet attributeSet, int i) {
        paint = new Paint();
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.WaveBar, 0, i);
        try {
            paint.setColor(obtainStyledAttributes.getColor(R.styleable.WaveBar_wb_color, Color.GREEN));
            radius = obtainStyledAttributes.getDimension(R.styleable.WaveBar_wb_radius, 0.0f);
            paint.setStyle(Style.FILL);
            paint.setAntiAlias(true);
            rectFs = new RectF[4];
            for (int i2 = 0; i2 < 4; i2++) {
                rectFs[i2] = new RectF();
            }
            width = 0;
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    protected void onDraw(Canvas canvas) {
        int i = 0;
        super.onDraw(canvas);
        if (width <= 0) {
            width = getWidth();
            height = getHeight();
            l = ((float) width) / 9.0f;
            float f = l * 7.0f;
            m = f / 20.0f;
            k = (int) ((((float) height) - f) / 2.0f);
        }
        if (isPlaying) {
            count++;
            if (count > 69) {
                count = 0;
            }
            for (int k = 0; k < 4; k++) {
                float f2 = m;
                i = count;
                switch (k) {
                    case 0:
                        i = COLUMN_1[i];
                        break;
                    case 1:
                        i = COLUMN_2[i];
                        break;
                    case 2:
                        i = COLUMN_3[i];
                        break;
                    default:
                        i = COLUMN_4[i];
                        break;
                }
                j = (int) (((float) i) * f2);
                rectFs[k].set(l + (((float) (k * 2)) * l), (float) ((height - j) - this.k), (l * 2.0f) + (((float) (k * 2)) * l), (float) (height - this.k));
                canvas.drawRoundRect(rectFs[k], radius, radius, paint);
            }
            postInvalidateDelayed(10);
            return;
        }
        while (i < 4) {
            rectFs[i].set(l + (((float) (i * 2)) * l), (((float) height) - (l * 2.0f)) - ((float) k), (l * 2.0f) + (((float) (i * 2)) * l), (float) (height - k));
            canvas.drawRoundRect(rectFs[i], radius, radius, paint);
            i++;
        }
    }

    public void setPlaying(boolean playing) {
        if (isPlaying != playing) {
            isPlaying = playing;
            invalidate();
        }
    }
}