package dpphong.ntu.appqlcv.ck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Biểu đồ cột đơn giản (không phụ thuộc thư viện ngoài) cho thống kê 7 ngày.
 */
public class BarChartView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private String[] labels = new String[0];
    private int[] completed = new int[0];
    private int[] totals = new int[0];
    private int maxValue = 1;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorSurfaceMuted));
        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextSecondary));
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(String[] labels, int[] completed, int[] totals) {
        this.labels = labels != null ? labels : new String[0];
        this.completed = completed != null ? completed : new int[0];
        this.totals = totals != null ? totals : new int[0];

        maxValue = 1;
        for (int value : this.totals) maxValue = Math.max(maxValue, value);
        for (int value : this.completed) maxValue = Math.max(maxValue, value);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (labels.length == 0) return;

        float labelHeight = dp(22);
        float topPadding = dp(18);
        float bottom = getHeight() - labelHeight;
        float chartHeight = bottom - topPadding;
        float slot = (float) getWidth() / labels.length;
        float barWidth = Math.min(dp(26), slot * 0.48f);
        float radius = barWidth / 2f;

        for (int i = 0; i < labels.length; i++) {
            float centerX = slot * i + slot / 2f;
            int total = i < totals.length ? totals[i] : 0;
            int done = i < completed.length ? completed[i] : 0;

            float trackTop = bottom - chartHeight * (total / (float) maxValue);
            rect.set(centerX - barWidth / 2f, trackTop, centerX + barWidth / 2f, bottom);
            canvas.drawRoundRect(rect, radius, radius, trackPaint);

            if (done > 0) {
                float doneTop = bottom - chartHeight * (done / (float) maxValue);
                rect.set(centerX - barWidth / 2f, doneTop, centerX + barWidth / 2f, bottom);
                canvas.drawRoundRect(rect, radius, radius, barPaint);
            }

            if (done > 0) {
                canvas.drawText(String.valueOf(done), centerX, trackTop - dp(5), textPaint);
            }

            canvas.drawText(labels[i], centerX, getHeight() - dp(4), textPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
