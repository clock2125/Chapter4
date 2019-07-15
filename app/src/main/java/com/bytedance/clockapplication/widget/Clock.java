package com.bytedance.clockapplication.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Delayed;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    private TimerHandler mHandler = new TimerHandler();

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    private static class TimerHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
        }

    }

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;

        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    postInvalidate();
                    mHandler.postDelayed(this,1000);
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

//        postInvalidateDelayed(1000);

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(80);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);





        int rEnd = mCenterX - (int) (mWidth * 0.11f);

        int step=1;

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;

        for (int i = 30; i <= 360; i += 30 /* Step */) {

            String time = String.format("%s",
                    String.format(Locale.getDefault(), "%02d", step++));

            int startX = (int) (mCenterX + rEnd * Math.sin(Math.toRadians(i)));
            int startY = (int) (mCenterX - rEnd * Math.cos(Math.toRadians(i)));

            int baseLineY = (int)(startY-top/2-bottom/2);


            canvas.drawText(time,startX,baseLineY,textPaint);
        }


    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor

        int hourlength = (int)(0.3*mRadius);
        int minuteslength = (int)(0.4*mRadius);
        int secondslength = (int)(0.5*mRadius);

        Paint painth = new Paint(Paint.ANTI_ALIAS_FLAG);
        painth.setStyle(Paint.Style.FILL_AND_STROKE);
        painth.setStrokeCap(Paint.Cap.ROUND);
        painth.setStrokeWidth(2*mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        painth.setColor(hoursNeedleColor);

        Paint paintm = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintm.setStyle(Paint.Style.FILL_AND_STROKE);
        paintm.setStrokeCap(Paint.Cap.ROUND);
        paintm.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintm.setColor(minutesNeedleColor);

        Paint paints = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints.setStyle(Paint.Style.FILL_AND_STROKE);
        paints.setStrokeCap(Paint.Cap.ROUND);
        paints.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paints.setColor(secondsNeedleColor);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);


        int seconddegree = second*6;
        int minutedegree = (int) (minute*6+seconddegree*6/360.0);
        int hourdegree = (int) (hour*30+minutedegree*30/360.0);

        int startX = (mCenterX);

        int startY = (mCenterY);



        int hourendX = (int)(mCenterX + hourlength*Math.sin(Math.toRadians(hourdegree)));

        int hourendY = (int)(mCenterY-hourlength*Math.cos(Math.toRadians(hourdegree)));

        int minuteendX=(int)(mCenterX+ minuteslength*Math.sin(Math.toRadians(minutedegree)));

        int minuteendY=(int)(mCenterY-minuteslength*Math.cos(Math.toRadians(minutedegree)));

        int secondendX=(int)(mCenterX+ secondslength*Math.sin(Math.toRadians(seconddegree)));

        int secondendY=(int)(mCenterY-secondslength*Math.cos(Math.toRadians(seconddegree)));

        canvas.drawLine(startX, startY, hourendX, hourendY, painth);

        canvas.drawLine(startX,startY,minuteendX,minuteendY,paintm);

        canvas.drawLine(startX,startY,secondendX,secondendY,paints);

    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paintic = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintic.setStyle(Paint.Style.FILL_AND_STROKE);
        paintic.setStrokeCap(Paint.Cap.ROUND);
        paintic.setStrokeWidth((int)(3*mWidth * DEFAULT_DEGREE_STROKE_WIDTH));
        paintic.setColor(centerInnerColor);


        Paint paintoc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintoc.setStyle(Paint.Style.FILL_AND_STROKE);
        paintoc.setStrokeCap(Paint.Cap.ROUND);
        paintoc.setStrokeWidth((int)(4*mWidth * DEFAULT_DEGREE_STROKE_WIDTH));
        paintoc.setColor(centerOuterColor);

        canvas.drawPoint(mCenterX,mCenterY,paintoc);
        canvas.drawPoint(mCenterX,mCenterY,paintic);


    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}