package br.com.fivecom.litoralfm.ui.views.switchs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import br.com.fivecom.litoralfm.R;

/**
 * SwitchView Totalmente Customizável
 *
 * Características:
 * - Cores customizáveis para estados ON/OFF (thumb e background)
 * - Suporte a degradês com ângulo personalizável
 * - Corner radius customizável para thumb e background
 * - Tamanho da thumb customizável
 * - Thumb inset: negativo (estilo Android) ou positivo (estilo iOS)
 * - Animações suaves de cores e posição
 * - Totalmente configurável via XML
 */
public class SwitchView extends View {

    private static final long ANIMATION_DURATION = 300L;
    private static final int DEFAULT_COLOR_ANIMATION_DURATION = 300;

    // Animadores
    private ValueAnimator thumbPositionAnimator;
    private ValueAnimator thumbExpandAnimator;
    private ValueAnimator colorAnimator;

    // Gesture Detector
    private GestureDetector gestureDetector;

    // Dimensões
    private int width;
    private int height;
    private int centerX;
    private int centerY;

    // Bounds
    private RectF backgroundBound;
    private RectF thumbBound;
    private RectF tempRect;

    // Estados
    private boolean isOn;
    private boolean knobState;
    private boolean preIsOn;
    private float thumbPosition = 0.0f; // 0.0 = OFF, 1.0 = ON
    private float thumbExpandRate = 0.0f;
    private float colorProgress = 0.0f; // 0.0 = OFF colors, 1.0 = ON colors

    // Cores do Background
    private int backgroundColorOff;
    private int backgroundColorOn;
    private boolean backgroundUseGradient;
    private int backgroundGradientStartOff;
    private int backgroundGradientCenterOff;
    private int backgroundGradientEndOff;
    private int backgroundGradientStartOn;
    private int backgroundGradientCenterOn;
    private int backgroundGradientEndOn;
    private float backgroundGradientAngle;
    private int currentBackgroundColor;
    private int currentBackgroundGradientStart;
    private int currentBackgroundGradientCenter;
    private int currentBackgroundGradientEnd;
    private boolean backgroundUseCenterColor;

    // Cores do Thumb
    private int thumbColorOff;
    private int thumbColorOn;
    private int thumbDisabledColor;
    private boolean thumbUseGradient;
    private int thumbGradientStartOff;
    private int thumbGradientEndOff;
    private int thumbGradientStartOn;
    private int thumbGradientEndOn;
    private float thumbGradientAngle;
    private int currentThumbColor;
    private int currentThumbGradientStart;
    private int currentThumbGradientEnd;

    // Corner Radius
    private float backgroundCornerRadius;
    private float thumbCornerRadius;
    private boolean autoBackgroundRadius = true;
    private boolean autoThumbRadius = true;

    // Dimensões customizáveis
    private float thumbWidth;
    private float thumbHeight;
    private float thumbInset; // Negativo = para fora (Android), Positivo = para dentro (iOS)
    private float intrinsicThumbWidth;
    private float intrinsicThumbHeight;
    private boolean autoThumbSize = true;

    // Dimensões do switch (background)
    private int switchWidth = 0;
    private int switchHeight = 0;

    // Borda do Thumb
    private float thumbBorderWidth = 0;
    private int thumbBorderColor;
    private int thumbBorderColorOn;
    private int currentThumbBorderColor;

    // Ícones do Thumb
    private Drawable thumbIconOff;
    private Drawable thumbIconOn;
    private int thumbIconTint = 0;
    private int thumbIconTintOn = 0;
    private float thumbIconSize = 0;
    private boolean autoThumbIconSize = true;

    // Animação
    private int colorAnimationDuration;

    // Shadow
    private int shadowSpace;
    private int outerStrokeWidth;

    // Paint
    private Paint paint;
    private ArgbEvaluator argbEvaluator;

    // Estado de anexação
    private boolean isAttachedToWindow = false;
    private boolean dirtyAnimation = false;

    public interface OnSwitchStateChangeListener {
        void onSwitchStateChange(boolean isOn);
    }

    private OnSwitchStateChangeListener onSwitchStateChangeListener;

    public SwitchView(Context context) {
        this(context, null);
    }

    public SwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDefaults(context);

        readAttributes(context, attrs);

        backgroundBound = new RectF();
        thumbBound = new RectF();
        tempRect = new RectF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        argbEvaluator = new ArgbEvaluator();

        setupGestureDetector(context);

        initAnimators();

        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        updateCurrentColors();
    }

    private void initDefaults(Context context) {
        float density = context.getResources().getDisplayMetrics().density;

        backgroundColorOff = 0xFFE0E0E0;
        backgroundColorOn = 0xFF4CAF50;
        thumbColorOff = 0xFFFFFFFF;
        thumbColorOn = 0xFFFFFFFF;
        thumbDisabledColor = 0xFFBDBDBD;
        thumbBorderColor = 0xFFCCCCCC;
        thumbBorderColorOn = 0xFFCCCCCC;

        backgroundUseGradient = false;
        thumbUseGradient = false;
        backgroundGradientAngle = 90.0f;
        thumbGradientAngle = 90.0f;
        backgroundUseCenterColor = false;
        backgroundGradientCenterOff = 0;
        backgroundGradientCenterOn = 0;

        shadowSpace = (int) (2 * density);
        outerStrokeWidth = (int) (1 * density);
        thumbInset = (int) (2 * density);
        thumbBorderWidth = 0;

        colorAnimationDuration = DEFAULT_COLOR_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);

        try {
            backgroundColorOff = ta.getColor(R.styleable.SwitchView_backgroundColor, backgroundColorOff);
            backgroundColorOn = ta.getColor(R.styleable.SwitchView_backgroundColorOn, backgroundColorOn);

            if (ta.hasValue(R.styleable.SwitchView_tintColor)) {
                backgroundColorOn = ta.getColor(R.styleable.SwitchView_tintColor, backgroundColorOn);
            }

            backgroundUseGradient = ta.getBoolean(R.styleable.SwitchView_backgroundUseGradient, backgroundUseGradient);
            backgroundGradientStartOff = ta.getColor(R.styleable.SwitchView_backgroundGradientStartOff, backgroundColorOff);
            backgroundGradientEndOff = ta.getColor(R.styleable.SwitchView_backgroundGradientEndOff, backgroundColorOff);
            backgroundGradientStartOn = ta.getColor(R.styleable.SwitchView_backgroundGradientStartOn, backgroundColorOn);
            backgroundGradientEndOn = ta.getColor(R.styleable.SwitchView_backgroundGradientEndOn, backgroundColorOn);
            backgroundGradientAngle = ta.getFloat(R.styleable.SwitchView_backgroundGradientAngle, backgroundGradientAngle);

            if (ta.hasValue(R.styleable.SwitchView_backgroundGradientCenterOff)) {
                backgroundGradientCenterOff = ta.getColor(R.styleable.SwitchView_backgroundGradientCenterOff, 0);
                backgroundUseCenterColor = true;
            } else {
                backgroundGradientCenterOff = 0;
            }

            if (ta.hasValue(R.styleable.SwitchView_backgroundGradientCenterOn)) {
                backgroundGradientCenterOn = ta.getColor(R.styleable.SwitchView_backgroundGradientCenterOn, 0);
                backgroundUseCenterColor = true;
            } else {
                backgroundGradientCenterOn = 0;
            }

            thumbColorOff = ta.getColor(R.styleable.SwitchView_thumbColorOff, thumbColorOff);
            thumbColorOn = ta.getColor(R.styleable.SwitchView_thumbColorOn, thumbColorOn);
            thumbDisabledColor = ta.getColor(R.styleable.SwitchView_thumbDisabledColor, thumbDisabledColor);

            thumbUseGradient = ta.getBoolean(R.styleable.SwitchView_thumbUseGradient, thumbUseGradient);
            thumbGradientStartOff = ta.getColor(R.styleable.SwitchView_thumbGradientStartOff, thumbColorOff);
            thumbGradientEndOff = ta.getColor(R.styleable.SwitchView_thumbGradientEndOff, thumbColorOff);
            thumbGradientStartOn = ta.getColor(R.styleable.SwitchView_thumbGradientStartOn, thumbColorOn);
            thumbGradientEndOn = ta.getColor(R.styleable.SwitchView_thumbGradientEndOn, thumbColorOn);
            thumbGradientAngle = ta.getFloat(R.styleable.SwitchView_thumbGradientAngle, thumbGradientAngle);

            if (ta.hasValue(R.styleable.SwitchView_backgroundCornerRadius)) {
                backgroundCornerRadius = ta.getDimension(R.styleable.SwitchView_backgroundCornerRadius, 0);
                autoBackgroundRadius = false;
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbCornerRadius)) {
                thumbCornerRadius = ta.getDimension(R.styleable.SwitchView_thumbCornerRadius, 0);
                autoThumbRadius = false;
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbSize)) {
                float size = ta.getDimension(R.styleable.SwitchView_thumbSize, 0);
                thumbWidth = size;
                thumbHeight = size;
                autoThumbSize = false;
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbWidth)) {
                thumbWidth = ta.getDimension(R.styleable.SwitchView_thumbWidth, 0);
                autoThumbSize = false;
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbHeight)) {
                thumbHeight = ta.getDimension(R.styleable.SwitchView_thumbHeight, 0);
                autoThumbSize = false;
            }

            thumbInset = ta.getDimension(R.styleable.SwitchView_thumbInset, thumbInset);

            if (ta.hasValue(R.styleable.SwitchView_switchWidth)) {
                switchWidth = ta.getDimensionPixelSize(R.styleable.SwitchView_switchWidth, 0);
            }

            if (ta.hasValue(R.styleable.SwitchView_switchHeight)) {
                switchHeight = ta.getDimensionPixelSize(R.styleable.SwitchView_switchHeight, 0);
            }

            thumbBorderWidth = ta.getDimension(R.styleable.SwitchView_thumbBorderWidth, thumbBorderWidth);
            thumbBorderColor = ta.getColor(R.styleable.SwitchView_thumbBorderColor, thumbBorderColor);
            thumbBorderColorOn = ta.getColor(R.styleable.SwitchView_thumbBorderColorOn, thumbBorderColor);

            if (ta.hasValue(R.styleable.SwitchView_thumbIconOff)) {
                thumbIconOff = ta.getDrawable(R.styleable.SwitchView_thumbIconOff);
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbIconOn)) {
                thumbIconOn = ta.getDrawable(R.styleable.SwitchView_thumbIconOn);
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbIconTint)) {
                thumbIconTint = ta.getColor(R.styleable.SwitchView_thumbIconTint, 0);
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbIconTintOn)) {
                thumbIconTintOn = ta.getColor(R.styleable.SwitchView_thumbIconTintOn, thumbIconTint);
            }

            if (ta.hasValue(R.styleable.SwitchView_thumbIconSize)) {
                thumbIconSize = ta.getDimension(R.styleable.SwitchView_thumbIconSize, 0);
                autoThumbIconSize = false;
            }

            shadowSpace = ta.getDimensionPixelOffset(R.styleable.SwitchView_shadowSpace, shadowSpace);
            outerStrokeWidth = ta.getDimensionPixelOffset(R.styleable.SwitchView_outerStrokeWidth, outerStrokeWidth);

            colorAnimationDuration = ta.getInteger(R.styleable.SwitchView_colorAnimationDuration, colorAnimationDuration);

        } finally {
            ta.recycle();
        }
    }

    private void setupGestureDetector(Context context) {
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                if (!isEnabled()) return false;

                preIsOn = isOn;

                // Anima expansão do thumb
                thumbExpandAnimator.setFloatValues(thumbExpandRate, 1.0F);
                thumbExpandAnimator.start();

                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                toggle();

                thumbExpandAnimator.setFloatValues(thumbExpandRate, 0.0F);
                thumbExpandAnimator.start();

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e2.getX() > centerX) {
                    if (!knobState) {
                        knobState = true;
                        animateToState(true);
                    }
                } else {
                    if (knobState) {
                        knobState = false;
                        animateToState(false);
                    }
                }
                return true;
            }
        };

        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    private void initAnimators() {
        // Animador de posição do thumb
        thumbPositionAnimator = ValueAnimator.ofFloat(0, 1);
        thumbPositionAnimator.setDuration(ANIMATION_DURATION);
        thumbPositionAnimator.setInterpolator(new DecelerateInterpolator());
        thumbPositionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                thumbPosition = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        // Animador de expansão do thumb
        thumbExpandAnimator = ValueAnimator.ofFloat(0, 1);
        thumbExpandAnimator.setDuration(ANIMATION_DURATION);
        thumbExpandAnimator.setInterpolator(new DecelerateInterpolator());
        thumbExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                thumbExpandRate = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        // Animador de cores
        colorAnimator = ValueAnimator.ofFloat(0, 1);
        colorAnimator.setDuration(colorAnimationDuration);
        colorAnimator.setInterpolator(new DecelerateInterpolator());
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                colorProgress = (float) animation.getAnimatedValue();
                updateCurrentColors();
                invalidate();
            }
        });
    }

    private void updateCurrentColors() {
        if (!isEnabled()) {
            currentThumbColor = thumbDisabledColor;
            currentBackgroundColor = backgroundColorOff;
            currentThumbGradientStart = thumbDisabledColor;
            currentThumbGradientEnd = thumbDisabledColor;
            currentBackgroundGradientStart = backgroundGradientStartOff;
            currentBackgroundGradientEnd = backgroundGradientEndOff;
            if (backgroundUseCenterColor) {
                currentBackgroundGradientCenter = backgroundGradientCenterOff;
            }
            currentThumbBorderColor = thumbBorderColor;
            return;
        }

        // Interpola cores do background
        currentBackgroundColor = (Integer) argbEvaluator.evaluate(colorProgress, backgroundColorOff, backgroundColorOn);
        currentBackgroundGradientStart = (Integer) argbEvaluator.evaluate(colorProgress, backgroundGradientStartOff, backgroundGradientStartOn);
        currentBackgroundGradientEnd = (Integer) argbEvaluator.evaluate(colorProgress, backgroundGradientEndOff, backgroundGradientEndOn);
        if (backgroundUseCenterColor) {
            currentBackgroundGradientCenter = (Integer) argbEvaluator.evaluate(colorProgress, backgroundGradientCenterOff, backgroundGradientCenterOn);
        }

        // Interpola cores do thumb
        currentThumbColor = (Integer) argbEvaluator.evaluate(colorProgress, thumbColorOff, thumbColorOn);
        currentThumbGradientStart = (Integer) argbEvaluator.evaluate(colorProgress, thumbGradientStartOff, thumbGradientStartOn);
        currentThumbGradientEnd = (Integer) argbEvaluator.evaluate(colorProgress, thumbGradientEndOff, thumbGradientEndOn);

        // Interpola cor da borda do thumb
        currentThumbBorderColor = (Integer) argbEvaluator.evaluate(colorProgress, thumbBorderColor, thumbBorderColorOn);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Se dimensões customizadas foram definidas, usa elas
        if (switchWidth > 0 && switchHeight > 0) {
            setMeasuredDimension(switchWidth, switchHeight);
            width = switchWidth;
            height = switchHeight;
        } else if (switchWidth > 0) {
            width = switchWidth;
            height = MeasureSpec.getSize(heightMeasureSpec);

            // Mantém proporção mínima
            if ((float) height / (float) width < 0.33333F) {
                height = (int) ((float) width * 0.33333F);
            }

            setMeasuredDimension(width, height);
        } else if (switchHeight > 0) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = switchHeight;

            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            width = MeasureSpec.getSize(widthMeasureSpec);
            height = MeasureSpec.getSize(heightMeasureSpec);

            // Mantém proporção mínima
            if ((float) height / (float) width < 0.33333F) {
                height = (int) ((float) width * 0.33333F);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec));
                super.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
            }
        }

        centerX = width / 2;
        centerY = height / 2;

        // Calcula corner radius automático se necessário
        if (autoBackgroundRadius) {
            backgroundCornerRadius = (height - shadowSpace * 2) / 2f;
        }

        // Calcula tamanho do thumb automático se necessário
        if (autoThumbSize) {
            float availableHeight = height - shadowSpace * 2 - outerStrokeWidth * 2 - thumbInset * 2 - thumbBorderWidth * 2;
            intrinsicThumbWidth = availableHeight;
            intrinsicThumbHeight = availableHeight;
            thumbWidth = intrinsicThumbWidth;
            thumbHeight = intrinsicThumbHeight;
        } else {
            intrinsicThumbWidth = thumbWidth;
            intrinsicThumbHeight = thumbHeight;
        }

        // Calcula corner radius automático do thumb se necessário
        if (autoThumbRadius) {
            thumbCornerRadius = Math.min(thumbWidth, thumbHeight) / 2f;
        }

        // Calcula tamanho do ícone automático se necessário
        if (autoThumbIconSize) {
            thumbIconSize = Math.min(thumbWidth, thumbHeight) * 0.6f;
        }

        // Define bounds do background
        backgroundBound.left = shadowSpace;
        backgroundBound.top = shadowSpace;
        backgroundBound.right = width - shadowSpace;
        backgroundBound.bottom = height - shadowSpace;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Desenha background
        drawBackground(canvas);

        // Desenha thumb
        drawThumb(canvas);
    }

    private void drawBackground(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        // Aplica cor ou gradiente
        if (backgroundUseGradient) {
            float angle = (float) Math.toRadians(backgroundGradientAngle);
            float centerX = backgroundBound.centerX();
            float centerY = backgroundBound.centerY();
            float radius = Math.max(backgroundBound.width(), backgroundBound.height());

            float x1 = centerX - (float) Math.cos(angle) * radius / 2;
            float y1 = centerY - (float) Math.sin(angle) * radius / 2;
            float x2 = centerX + (float) Math.cos(angle) * radius / 2;
            float y2 = centerY + (float) Math.sin(angle) * radius / 2;

            LinearGradient gradient;
            if (backgroundUseCenterColor) {
                // Gradiente com 3 cores (start, center, end)
                int[] colors = {currentBackgroundGradientStart, currentBackgroundGradientCenter, currentBackgroundGradientEnd};
                float[] positions = {0.0f, 0.5f, 1.0f};
                gradient = new LinearGradient(
                        x1, y1, x2, y2,
                        colors,
                        positions,
                        Shader.TileMode.CLAMP
                );
            } else {
                // Gradiente com 2 cores (start, end)
                gradient = new LinearGradient(
                        x1, y1, x2, y2,
                        currentBackgroundGradientStart,
                        currentBackgroundGradientEnd,
                        Shader.TileMode.CLAMP
                );
            }
            paint.setShader(gradient);
        } else {
            paint.setShader(null);
            paint.setColor(currentBackgroundColor);
        }

        canvas.drawRoundRect(backgroundBound, backgroundCornerRadius, backgroundCornerRadius, paint);
        paint.setShader(null);
    }

    private void drawThumb(Canvas canvas) {
        // Calcula posição do thumb
        float trackWidth = backgroundBound.width() - outerStrokeWidth * 2 - thumbInset * 2;
        float currentThumbWidth = thumbWidth;
        float currentThumbHeight = thumbHeight;

        // Adiciona expansão ao thumb
        float expandAmount = currentThumbWidth * 0.3f * thumbExpandRate;
        currentThumbWidth += expandAmount;

        float maxTravel = trackWidth - thumbWidth;
        float thumbX = shadowSpace + outerStrokeWidth + thumbInset + (maxTravel * thumbPosition);
        float thumbY = shadowSpace + outerStrokeWidth + thumbInset + (height - shadowSpace * 2 - outerStrokeWidth * 2 - thumbInset * 2 - thumbHeight) / 2f;

        // Ajusta para manter centralizado durante expansão
        thumbBound.left = thumbX - (expandAmount / 2);
        thumbBound.top = thumbY;
        thumbBound.right = thumbBound.left + currentThumbWidth;
        thumbBound.bottom = thumbY + currentThumbHeight;

        // Aplica sombra
        paint.setShadowLayer(shadowSpace / 2f, 0, shadowSpace / 3f,
                isEnabled() ? 0x40000000 : 0x20000000);

        paint.setStyle(Paint.Style.FILL);

        // Aplica cor ou gradiente
        if (thumbUseGradient) {
            float angle = (float) Math.toRadians(thumbGradientAngle);
            float centerX = thumbBound.centerX();
            float centerY = thumbBound.centerY();
            float radius = Math.max(thumbBound.width(), thumbBound.height());

            float x1 = centerX - (float) Math.cos(angle) * radius / 2;
            float y1 = centerY - (float) Math.sin(angle) * radius / 2;
            float x2 = centerX + (float) Math.cos(angle) * radius / 2;
            float y2 = centerY + (float) Math.sin(angle) * radius / 2;

            LinearGradient gradient = new LinearGradient(
                    x1, y1, x2, y2,
                    currentThumbGradientStart,
                    currentThumbGradientEnd,
                    Shader.TileMode.CLAMP
            );
            paint.setShader(gradient);
        } else {
            paint.setShader(null);
            paint.setColor(currentThumbColor);
        }

        canvas.drawRoundRect(thumbBound, thumbCornerRadius, thumbCornerRadius, paint);

        paint.setShader(null);
        paint.setShadowLayer(0, 0, 0, 0);

        // Desenha borda se configurada
        if (thumbBorderWidth > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(thumbBorderWidth);
            paint.setColor(currentThumbBorderColor);

            // Cria um RectF ajustado para a borda (para não ser cortada)
            RectF borderBound = new RectF(
                    thumbBound.left + thumbBorderWidth / 2,
                    thumbBound.top + thumbBorderWidth / 2,
                    thumbBound.right - thumbBorderWidth / 2,
                    thumbBound.bottom - thumbBorderWidth / 2
            );

            canvas.drawRoundRect(borderBound, thumbCornerRadius, thumbCornerRadius, paint);
        }

        // Desenha ícone se configurado
        drawThumbIcon(canvas);
    }

    private void drawThumbIcon(Canvas canvas) {
        Drawable icon = isOn ? thumbIconOn : thumbIconOff;

        if (icon == null) {
            return;
        }

        // Aplica tint se configurado
        int tintColor = isOn ? thumbIconTintOn : thumbIconTint;
        if (tintColor != 0) {
            icon.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        } else {
            icon.clearColorFilter();
        }

        // Calcula posição centralizada do ícone
        float iconLeft = thumbBound.centerX() - thumbIconSize / 2;
        float iconTop = thumbBound.centerY() - thumbIconSize / 2;

        icon.setBounds(
                (int) iconLeft,
                (int) iconTop,
                (int) (iconLeft + thumbIconSize),
                (int) (iconTop + thumbIconSize)
        );

        icon.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                thumbExpandAnimator.setFloatValues(thumbExpandRate, 0.0F);
                thumbExpandAnimator.start();

                isOn = knobState;

                if (onSwitchStateChangeListener != null)
                    onSwitchStateChangeListener.onSwitchStateChange(isOn);
                break;
        }

        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateCurrentColors();
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;

        if (dirtyAnimation) {
            animateToState(isOn, true);
            dirtyAnimation = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
    }

    // Public API

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        setOn(on, false);
    }

    public void setOn(boolean on, boolean animated) {
        if (this.isOn == on) return;

        if (!isAttachedToWindow && animated) {
            dirtyAnimation = true;
            this.isOn = on;
            return;
        }

        this.isOn = on;
        this.knobState = on;

        animateToState(on, animated);
    }

    public void toggle() {
        setOn(!isOn, true);
    }

    private void animateToState(boolean on) {
        animateToState(on, true);
    }

    private void animateToState(boolean on, boolean animated) {
        float targetPosition = on ? 1.0f : 0.0f;
        float targetColorProgress = on ? 1.0f : 0.0f;

        if (animated) {
            thumbPositionAnimator.setFloatValues(thumbPosition, targetPosition);
            thumbPositionAnimator.start();

            colorAnimator.setFloatValues(colorProgress, targetColorProgress);
            colorAnimator.start();
        } else {
            thumbPosition = targetPosition;
            colorProgress = targetColorProgress;
            updateCurrentColors();
            invalidate();
        }
    }

    public void setOnSwitchStateChangeListener(OnSwitchStateChangeListener listener) {
        this.onSwitchStateChangeListener = listener;
    }

    public OnSwitchStateChangeListener getOnSwitchStateChangeListener() {
        return onSwitchStateChangeListener;
    }

    // Setters para cores

    public void setBackgroundColorOff(int color) {
        this.backgroundColorOff = color;
        if (!backgroundUseGradient) {
            this.backgroundGradientStartOff = color;
            this.backgroundGradientEndOff = color;
        }
        updateCurrentColors();
        invalidate();
    }

    public void setBackgroundColorOn(int color) {
        this.backgroundColorOn = color;
        if (!backgroundUseGradient) {
            this.backgroundGradientStartOn = color;
            this.backgroundGradientEndOn = color;
        }
        updateCurrentColors();
        invalidate();
    }

    public void setThumbColorOff(int color) {
        this.thumbColorOff = color;
        if (!thumbUseGradient) {
            this.thumbGradientStartOff = color;
            this.thumbGradientEndOff = color;
        }
        updateCurrentColors();
        invalidate();
    }

    public void setThumbColorOn(int color) {
        this.thumbColorOn = color;
        if (!thumbUseGradient) {
            this.thumbGradientStartOn = color;
            this.thumbGradientEndOn = color;
        }
        updateCurrentColors();
        invalidate();
    }

    public void setThumbDisabledColor(int color) {
        this.thumbDisabledColor = color;
        updateCurrentColors();
        invalidate();
    }

    // Setters para gradientes

    public void setBackgroundGradient(boolean useGradient, int startColorOff, int endColorOff,
                                      int startColorOn, int endColorOn, float angle) {
        this.backgroundUseGradient = useGradient;
        this.backgroundGradientStartOff = startColorOff;
        this.backgroundGradientEndOff = endColorOff;
        this.backgroundGradientStartOn = startColorOn;
        this.backgroundGradientEndOn = endColorOn;
        this.backgroundGradientAngle = angle;
        this.backgroundUseCenterColor = false;
        updateCurrentColors();
        invalidate();
    }

    public void setBackgroundGradient(boolean useGradient, int startColorOff, int centerColorOff, int endColorOff,
                                      int startColorOn, int centerColorOn, int endColorOn, float angle) {
        this.backgroundUseGradient = useGradient;
        this.backgroundGradientStartOff = startColorOff;
        this.backgroundGradientCenterOff = centerColorOff;
        this.backgroundGradientEndOff = endColorOff;
        this.backgroundGradientStartOn = startColorOn;
        this.backgroundGradientCenterOn = centerColorOn;
        this.backgroundGradientEndOn = endColorOn;
        this.backgroundGradientAngle = angle;
        this.backgroundUseCenterColor = true;
        updateCurrentColors();
        invalidate();
    }

    public void setThumbGradient(boolean useGradient, int startColorOff, int endColorOff,
                                 int startColorOn, int endColorOn, float angle) {
        this.thumbUseGradient = useGradient;
        this.thumbGradientStartOff = startColorOff;
        this.thumbGradientEndOff = endColorOff;
        this.thumbGradientStartOn = startColorOn;
        this.thumbGradientEndOn = endColorOn;
        this.thumbGradientAngle = angle;
        updateCurrentColors();
        invalidate();
    }

    // Setters para dimensões

    public void setBackgroundCornerRadius(float radius) {
        this.backgroundCornerRadius = radius;
        this.autoBackgroundRadius = false;
        invalidate();
    }

    public void setThumbCornerRadius(float radius) {
        this.thumbCornerRadius = radius;
        this.autoThumbRadius = false;
        invalidate();
    }

    public void setThumbSize(float size) {
        this.thumbWidth = size;
        this.thumbHeight = size;
        this.intrinsicThumbWidth = size;
        this.intrinsicThumbHeight = size;
        this.autoThumbSize = false;
        requestLayout();
    }

    public void setThumbWidth(float width) {
        this.thumbWidth = width;
        this.intrinsicThumbWidth = width;
        this.autoThumbSize = false;
        requestLayout();
    }

    public void setThumbHeight(float height) {
        this.thumbHeight = height;
        this.intrinsicThumbHeight = height;
        this.autoThumbSize = false;
        requestLayout();
    }

    public void setThumbInset(float inset) {
        this.thumbInset = inset;
        requestLayout();
    }

    public void setSwitchWidth(int width) {
        this.switchWidth = width;
        requestLayout();
    }

    public void setSwitchHeight(int height) {
        this.switchHeight = height;
        requestLayout();
    }

    public void setSwitchDimensions(int width, int height) {
        this.switchWidth = width;
        this.switchHeight = height;
        requestLayout();
    }

    public void setThumbBorderWidth(float width) {
        this.thumbBorderWidth = width;
        invalidate();
    }

    public void setThumbBorderColor(int color) {
        this.thumbBorderColor = color;
        updateCurrentColors();
        invalidate();
    }

    public void setThumbBorderColorOn(int color) {
        this.thumbBorderColorOn = color;
        updateCurrentColors();
        invalidate();
    }

    public void setThumbBorderColors(int colorOff, int colorOn) {
        this.thumbBorderColor = colorOff;
        this.thumbBorderColorOn = colorOn;
        updateCurrentColors();
        invalidate();
    }

    public void setThumbIconOff(Drawable icon) {
        this.thumbIconOff = icon;
        invalidate();
    }

    public void setThumbIconOn(Drawable icon) {
        this.thumbIconOn = icon;
        invalidate();
    }

    public void setThumbIcons(Drawable iconOff, Drawable iconOn) {
        this.thumbIconOff = iconOff;
        this.thumbIconOn = iconOn;
        invalidate();
    }

    public void setThumbIconTint(int tint) {
        this.thumbIconTint = tint;
        invalidate();
    }

    public void setThumbIconTintOn(int tint) {
        this.thumbIconTintOn = tint;
        invalidate();
    }

    public void setThumbIconTints(int tintOff, int tintOn) {
        this.thumbIconTint = tintOff;
        this.thumbIconTintOn = tintOn;
        invalidate();
    }

    public void setThumbIconSize(float size) {
        this.thumbIconSize = size;
        this.autoThumbIconSize = false;
        invalidate();
    }

    public void setColorAnimationDuration(int duration) {
        this.colorAnimationDuration = duration;
        colorAnimator.setDuration(duration);
    }

    // Getters

    public int getBackgroundColorOff() { return backgroundColorOff; }
    public int getBackgroundColorOn() { return backgroundColorOn; }
    public int getThumbColorOff() { return thumbColorOff; }
    public int getThumbColorOn() { return thumbColorOn; }
    public int getThumbDisabledColor() { return thumbDisabledColor; }
    public float getBackgroundCornerRadius() { return backgroundCornerRadius; }
    public float getThumbCornerRadius() { return thumbCornerRadius; }
    public float getThumbWidth() { return thumbWidth; }
    public float getThumbHeight() { return thumbHeight; }
    public float getThumbInset() { return thumbInset; }
    public int getSwitchWidth() { return switchWidth; }
    public int getSwitchHeight() { return switchHeight; }
    public float getThumbBorderWidth() { return thumbBorderWidth; }
    public int getThumbBorderColor() { return thumbBorderColor; }
    public int getThumbBorderColorOn() { return thumbBorderColorOn; }
    public Drawable getThumbIconOff() { return thumbIconOff; }
    public Drawable getThumbIconOn() { return thumbIconOn; }
    public int getThumbIconTint() { return thumbIconTint; }
    public int getThumbIconTintOn() { return thumbIconTintOn; }
    public float getThumbIconSize() { return thumbIconSize; }
    public boolean isBackgroundUseGradient() { return backgroundUseGradient; }
    public boolean isThumbUseGradient() { return thumbUseGradient; }
}
