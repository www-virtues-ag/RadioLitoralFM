package br.com.fivecom.litoralfm.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import br.com.fivecom.litoralfm.R;

/**
 * Controle circular de volume com:
 * - Arco de fundo (track) cinza
 * - Arco de progresso vermelho
 * - Thumb (bolinha) vermelha
 *
 * Agora com um espaço vazio na parte de baixo (gap).
 */
public class CircularVolumeSeekBar extends View {

    public interface OnVolumeChangedListener {
        void onVolumeChanged(int volume, boolean fromUser);
    }

    // Configuração de desenho
    private Paint trackPaint;
    private Paint progressPaint;
    private Paint thumbPaint;
    private RectF arcBounds;

    // Valores de volume
    private int maxValue = 100;
    private int currentValue = 50;

    // Cores padrão
    private int trackColor = Color.parseColor("#E0E0E0"); // Cinza
    private int progressColor = Color.parseColor("#E53935"); // Vermelho
    private int thumbColor = Color.parseColor("#E53935"); // Vermelho

    // Dimensões padrão
    private float strokeWidthPx;
    private float thumbRadiusPx;

    // Listener
    private OnVolumeChangedListener volumeListener;

    /**
     * ANGULAÇÃO
     *
     * Canvas usa:
     *  - 0° na direita (3h)
     *  - 90° embaixo (6h)
     *  - 180° esquerda (9h)
     *  - 270° em cima (12h)
     *
     * Vamos deixar um "gap" embaixo, centrado em 90°.
     */
    private static final float GAP_ANGLE_DEG = 100f; // tamanho do espaço vazio (em graus)
    private static final float ACTIVE_SWEEP_ANGLE = 360f - GAP_ANGLE_DEG; // quanto do círculo é usável (300°)
    private static final float START_ANGLE = 90f + GAP_ANGLE_DEG / 2f;    // início do arco (em graus no canvas)

    public CircularVolumeSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public CircularVolumeSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularVolumeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;
        strokeWidthPx = 6f * density;
        thumbRadiusPx = 10f * density;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularVolumeSeekBar);

            maxValue      = a.getInt(R.styleable.CircularVolumeSeekBar_cv_max, maxValue);
            currentValue  = a.getInt(R.styleable.CircularVolumeSeekBar_cv_progress, currentValue);
            trackColor    = a.getColor(R.styleable.CircularVolumeSeekBar_cv_trackColor, trackColor);
            progressColor = a.getColor(R.styleable.CircularVolumeSeekBar_cv_progressColor, progressColor);
            thumbColor    = a.getColor(R.styleable.CircularVolumeSeekBar_cv_thumbColor, thumbColor);
            strokeWidthPx = a.getDimension(R.styleable.CircularVolumeSeekBar_cv_strokeWidth, strokeWidthPx);
            thumbRadiusPx = a.getDimension(R.styleable.CircularVolumeSeekBar_cv_thumbRadius, thumbRadiusPx);

            a.recycle();
        }

        // Pintura do arco de fundo
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Pintura do arco de progresso
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Thumb
        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbColor);

        arcBounds = new RectF();

        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size   = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width  = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        float halfStroke = strokeWidthPx / 2f;
        float padding = halfStroke + thumbRadiusPx + 4f;

        arcBounds.set(
                padding,
                padding,
                width - padding,
                height - padding
        );

        // Desenha arco de fundo (apenas arco ativo, não o círculo completo)
        canvas.drawArc(arcBounds, START_ANGLE, ACTIVE_SWEEP_ANGLE, false, trackPaint);

        // Progresso de 0 até ACTIVE_SWEEP_ANGLE
        float sweep = (currentValue / (float) maxValue) * ACTIVE_SWEEP_ANGLE;

        if (sweep > 0f) {
            canvas.drawArc(arcBounds, START_ANGLE, sweep, false, progressPaint);
        }

        // Ângulo atual do thumb no sistema do canvas (0° direita, 90° baixo, etc)
        float angleCanvas = START_ANGLE + sweep;
        double angleRad = Math.toRadians(angleCanvas);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius  = arcBounds.width() / 2f;

        float thumbX = centerX + (float) (radius * Math.cos(angleRad));
        float thumbY = centerY + (float) (radius * Math.sin(angleRad));

        canvas.drawCircle(thumbX, thumbY, thumbRadiusPx, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                updateFromTouch(x, y, true);
                return true;

            case MotionEvent.ACTION_UP:
                updateFromTouch(x, y, true);
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Converte o toque em ângulo, respeita o gap de baixo
     * e transforma em valor de volume.
     */
    private void updateFromTouch(float x, float y, boolean fromUser) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float dx = x - centerX;
        float dy = y - centerY;

        // Ângulo em graus no sistema "0° em cima, horário"
        float angleTop = (float) Math.toDegrees(Math.atan2(dy, dx));
        angleTop += 90f;
        if (angleTop < 0f) {
            angleTop += 360f;
        }
        if (angleTop >= 360f) {
            angleTop -= 360f;
        }

        // Definição do gap na parte de baixo (180° neste sistema)
        float gapCenter = 180f;
        float halfGap   = GAP_ANGLE_DEG / 2f;
        float gapStart  = gapCenter - halfGap; // exemplo: 150°
        float gapEnd    = gapCenter + halfGap; // exemplo: 210°

        // Se o toque cair dentro do gap, aproxima para a borda mais próxima
        if (angleTop >= gapStart && angleTop <= gapEnd) {
            if (angleTop - gapStart < gapEnd - angleTop) {
                angleTop = gapStart;
            } else {
                angleTop = gapEnd;
            }
        }

        // Agora mapeamos o ângulo para um intervalo contínuo [0, ACTIVE_SWEEP_ANGLE]
        float effectiveAngle;
        if (angleTop > gapEnd) {
            // Depois do fim do gap
            effectiveAngle = angleTop - gapEnd;
        } else {
            // Antes do início do gap (parte que "passou" pelo 360°)
            effectiveAngle = angleTop + (360f - gapEnd);
        }

        int newValue = Math.round((effectiveAngle / ACTIVE_SWEEP_ANGLE) * maxValue);
        setVolumeInternal(newValue, fromUser);
    }

    // API pública

    public void setVolume(int volume) {
        setVolumeInternal(volume, false);
    }
    
    /**
     * Define o volume sem disparar o listener (usado para atualizações do sistema)
     */
    public void setVolumeSilent(int volume) {
        int newValue = Math.max(0, Math.min(volume, maxValue));
        if (newValue != currentValue) {
            currentValue = newValue;
            invalidate();
            // Não dispara o listener quando é uma atualização silenciosa
        }
    }

    public int getVolume() {
        return currentValue;
    }

    public void setMaxVolume(int max) {
        if (max <= 0) return;
        this.maxValue = max;
        if (currentValue > maxValue) {
            currentValue = maxValue;
        }
        invalidate();
    }

    public int getMaxVolume() {
        return maxValue;
    }

    public void setTrackColor(int color) {
        this.trackColor = color;
        trackPaint.setColor(color);
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setThumbColor(int color) {
        this.thumbColor = color;
        thumbPaint.setColor(color);
        invalidate();
    }

    public void setOnVolumeChangedListener(OnVolumeChangedListener listener) {
        this.volumeListener = listener;
    }

    private void setVolumeInternal(int volume, boolean fromUser) {
        int newValue = Math.max(0, Math.min(volume, maxValue));
        if (newValue != currentValue) {
            currentValue = newValue;
            invalidate();

            if (volumeListener != null) {
                volumeListener.onVolumeChanged(currentValue, fromUser);
            }
        }
    }
}
