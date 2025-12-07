package com.example.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RadarChartView extends View {
    private Paint paintGrid, paintData, paintText;

    // [확인] 6개 데이터 (초기값 0)
    private float[] dataPoints = {0f, 0f, 0f, 0f, 0f, 0f};
    private String[] labels = {"문학/소설", "인문/사회", "경제/경영", "과학/기술", "실용/기타", "그 외"};

    public RadarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintGrid = new Paint();
        paintGrid.setColor(Color.LTGRAY);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(2f);
        paintGrid.setAntiAlias(true);

        paintData = new Paint();
        paintData.setColor(Color.parseColor("#4285F4"));
        paintData.setStyle(Paint.Style.FILL_AND_STROKE);
        paintData.setStrokeWidth(4f);
        paintData.setAlpha(150);
        paintData.setAntiAlias(true);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(24f); // 글자 크기 약간 줄임
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setAntiAlias(true);
    }

    public void setDataPoints(float[] newPoints) {
        // [수정] 들어온 데이터가 6개가 아니면 0으로 채워서 에러 방지
        if (newPoints.length != 6) {
            this.dataPoints = new float[]{0f, 0f, 0f, 0f, 0f, 0f};
        } else {
            this.dataPoints = newPoints;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) * 0.65f;

        // 1. 배경 육각형
        drawPolygon(canvas, centerX, centerY, radius, paintGrid);
        drawPolygon(canvas, centerX, centerY, radius * 0.6f, paintGrid);
        drawPolygon(canvas, centerX, centerY, radius * 0.3f, paintGrid);

        // 2. 데이터 영역
        Path path = new Path();
        int count = 6;
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.toRadians(-90 + (i * (360f / count))));
            float val = Math.min(dataPoints[i], 1.0f) * radius;

            float x = (float) (centerX + val * Math.cos(angle));
            float y = (float) (centerY + val * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);

            // 텍스트 라벨
            float labelRadius = radius * 1.3f; // 간격 조금 더 벌림
            float tx = (float) (centerX + labelRadius * Math.cos(angle));
            float ty = (float) (centerY + labelRadius * Math.sin(angle));
            canvas.drawText(labels[i], tx, ty + 10, paintText);
        }
        path.close();
        canvas.drawPath(path, paintData);
    }

    private void drawPolygon(Canvas canvas, float cx, float cy, float r, Paint paint) {
        Path path = new Path();
        int count = 6;
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.toRadians(-90 + (i * (360f / count))));
            float x = (float) (cx + r * Math.cos(angle));
            float y = (float) (cy + r * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
            canvas.drawLine(cx, cy, x, y, paint);
        }
        path.close();
        canvas.drawPath(path, paint);
    }
}