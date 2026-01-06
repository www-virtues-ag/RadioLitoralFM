package br.com.fivecom.litoralfm.ui.views.progress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.R;

public class RainbowProgressBar extends View {

    private float progress;
    private Paint paint;
    private Paint rainbowPaint;

    private int[] colors;
    private LinearGradient shader;

    private float progressHeight = dp2px(5);
    private int startColor = Color.parseColor("#EE2525");
    private int centerColor = Color.parseColor("#E3CE4D");
    private int endColor = Color.parseColor("#00BA00");
    private int unreachedColor = Color.parseColor("#CDCDCD");
    private int max = 100;
    private float radius = dp2px(35);
    private int type;

    private int[] colorsRainbow;
    private int[] colorsRainbow1;
    private LinearGradient firstShader;
    private LinearGradient secondShader;
    private Paint firstPaint;
    private Paint secondPaint;
    private Matrix firstMatrix;
    private Matrix secondMatrix;
    private int mViewWidth = 0;
    private int mTranslate = 0;
    private boolean stopRainbow;
    
    // Variáveis para animação do círculo
    private float circleRotationAngle = 0f;
    private boolean isCircleAnimating = false;
    private boolean indeterminate = false;
    private Runnable animationRunnable;

    private static final int CIRCLE_TYPE = 0;
    private static final int LINE_TYPE = 1;
    private static final int RAINBOW_TYPE = 2;

    public RainbowProgressBar(Context context) {
        super(context);
        init();
    }

    public RainbowProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.RainbowProgressBar);
        max = attributes.getInteger(R.styleable.RainbowProgressBar_progress_max, 100);
        progress = attributes.getInteger(R.styleable.RainbowProgressBar_progress_current, 0);
        startColor = attributes.getColor(R.styleable.RainbowProgressBar_progress_start_color, startColor);
        centerColor = attributes.getColor(R.styleable.RainbowProgressBar_progress_center_color, centerColor);
        endColor = attributes.getColor(R.styleable.RainbowProgressBar_progress_end_color, endColor);
        radius = attributes.getDimension(R.styleable.RainbowProgressBar_progress_radius, dp2px(35));
        progressHeight = attributes.getDimension(R.styleable.RainbowProgressBar_progress_height, dp2px(5));
        unreachedColor = attributes.getColor(R.styleable.RainbowProgressBar_progress_unreached_color, unreachedColor);
        type = attributes.getInteger(R.styleable.RainbowProgressBar_progress_type, 1);
        indeterminate = attributes.getBoolean(R.styleable.RainbowProgressBar_progress_indeterminate, false);

        attributes.recycle();
        init();
    }

    public RainbowProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(unreachedColor);
        paint.setStrokeWidth(progressHeight);
        paint.setStrokeCap(Paint.Cap.ROUND);

        rainbowPaint = new Paint();
        rainbowPaint.setAntiAlias(true);

        rainbowPaint.setStyle(Paint.Style.STROKE);
        rainbowPaint.setAntiAlias(true);
        rainbowPaint.setColor(startColor);
        rainbowPaint.setStrokeWidth(progressHeight);
        rainbowPaint.setStrokeCap(Paint.Cap.ROUND);

        if (max > 0) {
            colors = new int[max];
            initColors();
        }
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        int width = 0;
        if (type == CIRCLE_TYPE) {
            width = ((int) (radius * 2 + progressHeight * 2));
        } else {
            // Para LINE_TYPE e RAINBOW_TYPE, usa a altura mínima padrão
            width = (int) progressHeight;
        }
        return width;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (mViewWidth > 0) {
            // Array de 3 cores para o gradiente: start, center, end
            int[] gradientColors = new int[]{startColor, centerColor, endColor};
            // Posições: 0.0 (start), 0.5 (center), 1.0 (end)
            float[] positions = new float[]{0.0f, 0.5f, 1.0f};
            
            int drawableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int drawableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            
            if (firstPaint == null) {
                firstPaint = getPaint();
            }
            firstShader = new LinearGradient(0, 0, drawableWidth, drawableHeight,
                    gradientColors,
                    positions, Shader.TileMode.CLAMP);
            firstPaint.setShader(firstShader);
            if (firstMatrix == null) {
                firstMatrix = new Matrix();
            }

            if (secondPaint == null) {
                secondPaint = getPaint();
            }
            secondShader = new LinearGradient(-drawableWidth, 0, 0, drawableHeight,
                    gradientColors,
                    positions, Shader.TileMode.CLAMP);
            secondPaint.setShader(secondShader);
            if (secondMatrix == null) {
                secondMatrix = new Matrix();
            }
            
            // Para o modo círculo, força a recriação do shader quando o tamanho mudar
            if (type == CIRCLE_TYPE) {
                shader = null;
            }
        }
    }



    public Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        int height = 0;
        if (type == CIRCLE_TYPE) {
            height = ((int) (radius * 2 + progressHeight * 2));
        } else {
            // Para LINE_TYPE e RAINBOW_TYPE, usa a altura mínima baseada no progressHeight
            height = (int) progressHeight;
        }
        return height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measure(widthMeasureSpec, true);
        int height = measure(heightMeasureSpec, false);
        setMeasuredDimension(width, height);
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (type == CIRCLE_TYPE) {
            drawCircle(canvas);
            // Inicia animação automática se estiver em modo indeterminado ou se o progresso for 0
            if ((indeterminate || progress == 0) && !isCircleAnimating && getVisibility() == VISIBLE) {
                startCircleAnimation();
            }
        } else if (type == LINE_TYPE) {
            drawLine(canvas);
        } else  if(type == RAINBOW_TYPE){
            drawRainbow(canvas);
        }
    }
    
    private void startCircleAnimation() {
        if (isCircleAnimating) {
            return; // Já está animando
        }
        isCircleAnimating = true;
        animateCircle();
    }
    
    private void stopCircleAnimation() {
        isCircleAnimating = false;
        // Remove qualquer Runnable pendente
        if (animationRunnable != null) {
            removeCallbacks(animationRunnable);
            animationRunnable = null;
        }
    }
    
    private void animateCircle() {
        // Verifica se deve continuar animando
        if (!isCircleAnimating || type != CIRCLE_TYPE || getVisibility() != VISIBLE) {
            isCircleAnimating = false;
            return;
        }
        
        // Incrementa o ângulo de rotação
        circleRotationAngle += 5f; // Velocidade da rotação
        if (circleRotationAngle >= 360f) {
            circleRotationAngle = 0f;
        }
        
        postInvalidate();
        
        // Continua a animação
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                animateCircle();
            }
        };
        postDelayed(animationRunnable, 16); // ~60 FPS
    }
    
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        // Para a animação se a visibilidade não for VISIBLE (GONE ou INVISIBLE)
        if (visibility != VISIBLE) {
            stopCircleAnimation();
        } else {
            // Se voltou para VISIBLE e deve estar animando, reinicia a animação
            if (type == CIRCLE_TYPE && (indeterminate || progress == 0)) {
                startCircleAnimation();
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Garante que a animação pare quando a view for removida
        stopCircleAnimation();
    }

    public void setStopRainbow(boolean stop){
        this.stopRainbow = stop;
    }

    private void drawRainbow(Canvas canvas){
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        if (width <= 0 || height <= 0 || max <= 0) {
            return;
        }
        
        // Calcula a área desenhável
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = left + width;
        float bottom = top + height;
        
        // Calcula o progresso
        float progressWidth = width * (progress / max);
        
        // Desenha o fundo (não preenchido)
        Paint bgPaint = new Paint();
        bgPaint.setColor(unreachedColor);
        canvas.drawRect(new RectF(left, top, right, bottom), bgPaint);
        
        // Desenha apenas a parte do progresso
        if (progressWidth > 0) {
            float progressRight = left + progressWidth;
            
            // Se os paints não estiverem inicializados, cria um gradiente simples
            if (firstPaint == null || firstShader == null) {
                // Cria um paint temporário com gradiente
                Paint tempPaint = new Paint();
                tempPaint.setAntiAlias(true);
                int[] gradientColors = new int[]{startColor, centerColor, endColor};
                float[] positions = new float[]{0.0f, 0.5f, 1.0f};
                LinearGradient tempShader = new LinearGradient(left, top, right, bottom,
                        gradientColors, positions, Shader.TileMode.CLAMP);
                tempPaint.setShader(tempShader);
                canvas.drawRect(new RectF(left, top, progressRight, bottom), tempPaint);
            } else {
                // Anima o gradiente apenas se não estiver parado
                if (!stopRainbow) {
                    mTranslate += width / 20;
                    if (mTranslate >= width) {
                        mTranslate = 0;
                    }
                    if (firstMatrix != null) {
                        firstMatrix.setTranslate(mTranslate, 0);
                        firstShader.setLocalMatrix(firstMatrix);
                    }
                    if (secondMatrix != null && secondShader != null) {
                        secondMatrix.setTranslate(mTranslate, 0);
                        secondShader.setLocalMatrix(secondMatrix);
                    }
                }
                
                // Desenha o progresso com gradiente
                canvas.drawRect(new RectF(left, top, progressRight, bottom), firstPaint);
                
                // Desenha o segundo shader para efeito de animação
                if (!stopRainbow && secondPaint != null && secondShader != null && 
                    mTranslate > 0 && mTranslate < progressWidth) {
                    canvas.drawRect(new RectF(left, top, left + mTranslate, bottom), secondPaint);
                }
                
                // Continua a animação
                if (!stopRainbow) {
                    postInvalidateDelayed(20);
                }
            }
        }
    }


    private void drawLine(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        if (width <= 0 || height <= 0 || max <= 0) {
            return;
        }
        
        // Garante que o array de cores está inicializado
        if (colors == null || colors.length != max) {
            colors = new int[max];
            initColors();
        }
        
        // Calcula a posição Y centralizada verticalmente
        float centerY = getPaddingTop() + height / 2.0f;
        
        // Calcula as posições X considerando o padding
        float startX = getPaddingLeft();
        float endX = getPaddingLeft() + width;
        
        // Desenha a linha de fundo (não preenchida)
        canvas.drawLine(startX, centerY, endX, centerY, paint);
        
        // Calcula o progresso
        float progressWidth = width * (progress / max);
        float progressEndX = startX + progressWidth;
        
        // Cria o gradiente horizontal usando as cores do array ou cores diretas
        if (colors != null && colors.length > 0) {
            shader = new LinearGradient(startX, centerY, endX, centerY, colors, null,
                    Shader.TileMode.CLAMP);
            rainbowPaint.setShader(shader);
        } else {
            // Fallback: usa as cores diretas se o array não estiver pronto
            int[] gradientColors = new int[]{startColor, centerColor, endColor};
            float[] positions = new float[]{0.0f, 0.5f, 1.0f};
            shader = new LinearGradient(startX, centerY, endX, centerY, 
                    gradientColors, positions, Shader.TileMode.CLAMP);
            rainbowPaint.setShader(shader);
        }
        
        // Desenha a linha de progresso
        if (progressWidth > 0) {
            canvas.drawLine(startX, centerY, progressEndX, centerY, rainbowPaint);
        }
    }

    private void drawCircle(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        if (width <= 0 || height <= 0 || max <= 0) {
            return;
        }
        
        // Garante que o array de cores está inicializado
        if (colors == null || colors.length != max) {
            colors = new int[max];
            initColors();
        }
        
        // Calcula o centro considerando padding
        float centerX = getPaddingLeft() + width / 2.0f;
        float centerY = getPaddingTop() + height / 2.0f;
        
        // Ajusta o raio para caber no espaço disponível
        float availableRadius = Math.min(width, height) / 2.0f - progressHeight / 2.0f;
        float actualRadius = Math.min(radius, availableRadius);
        
        if (actualRadius <= 0) {
            return;
        }
        
        // Se estiver em modo indeterminado ou o progresso for 0, usa um arco fixo de 270 graus
        // Caso contrário, usa o progresso real
        float sweepAngle;
        if (indeterminate || progress == 0) {
            // Indicador de carregamento: arco de 270 graus
            sweepAngle = 270f;
        } else {
            sweepAngle = 360f / max * progress;
        }

        // Desenha o círculo de fundo
        canvas.drawCircle(centerX, centerY, actualRadius, paint);
        
        // Garante que o array de cores está inicializado
        if (colors == null || colors.length != max) {
            colors = new int[max];
            initColors();
        }
        
        // Cria o gradiente para o arco
        if (colors != null && colors.length > 0) {
            shader = new LinearGradient(
                    centerX - actualRadius, centerY - actualRadius,
                    centerX + actualRadius, centerY + actualRadius, 
                    colors, null, Shader.TileMode.CLAMP);
            rainbowPaint.setShader(shader);
        } else {
            // Fallback: usa as cores diretas se o array não estiver pronto
            int[] gradientColors = new int[]{startColor, centerColor, endColor};
            float[] positions = new float[]{0.0f, 0.5f, 1.0f};
            shader = new LinearGradient(
                    centerX - actualRadius, centerY - actualRadius,
                    centerX + actualRadius, centerY + actualRadius,
                    gradientColors, positions, Shader.TileMode.CLAMP);
            rainbowPaint.setShader(shader);
        }
        
        // Define o retângulo para o arco
        RectF rect = new RectF(
                centerX - actualRadius,
                centerY - actualRadius,
                centerX + actualRadius,
                centerY + actualRadius);
        
        // Desenha o arco de progresso
        // Para indicador de carregamento (indeterminate ou progress == 0), sempre desenha o arco
        // Para progresso real, só desenha se houver progresso
        if (indeterminate || progress == 0 || sweepAngle > 0) {
            // Para indicador de carregamento, inicia do ângulo atual de rotação
            float startAngle = (indeterminate || progress == 0) ? (-90 + circleRotationAngle) : -90;
            canvas.drawArc(rect, startAngle, sweepAngle, false, rainbowPaint);
        }
    }

    public void setProgress(int currentProgress) {
        // Garante que o progresso está dentro dos limites
        float oldProgress = this.progress;
        this.progress = Math.max(0, Math.min(currentProgress, max));
        
        // Se estiver em modo indeterminado, não para a animação
        if (indeterminate) {
            postInvalidate();
            return;
        }
        
        // Se o progresso mudou de 0 para outro valor, para a animação
        if (oldProgress == 0 && this.progress > 0) {
            stopCircleAnimation();
        }
        // Se o progresso voltou para 0, reinicia a animação
        else if (oldProgress > 0 && this.progress == 0 && type == CIRCLE_TYPE) {
            startCircleAnimation();
        }
        
        postInvalidate();
    }

    public void setMax(int max) {
        if (max > 0) {
            this.max = max;
            colors = new int[max];
            initColors();
            // Garante que o progresso não exceda o novo máximo
            if (progress > max) {
                progress = max;
            }
            postInvalidate();
        }
    }

    public void setStartColor(int color) {
        this.startColor = color;
        initColors();
        updateShaders();
        postInvalidate();
    }

    public void setCenterColor(int color) {
        this.centerColor = color;
        initColors();
        updateShaders();
        postInvalidate();
    }

    public void setEndColor(int color) {
        this.endColor = color;
        initColors();
        updateShaders();
        postInvalidate();
    }

    /**
     * Define se o progresso está em modo indeterminado (loop infinito)
     * @param indeterminate true para modo indeterminado, false para modo normal
     */
    public void setIndeterminate(boolean indeterminate) {
        if (this.indeterminate != indeterminate) {
            this.indeterminate = indeterminate;
            
            if (indeterminate && type == CIRCLE_TYPE && getVisibility() == VISIBLE) {
                // Inicia a animação se estiver em modo indeterminado e visível
                startCircleAnimation();
            } else if (!indeterminate && type == CIRCLE_TYPE) {
                // Para a animação se sair do modo indeterminado e não estiver em progresso 0
                if (progress > 0) {
                    stopCircleAnimation();
                } else if (progress == 0 && getVisibility() == VISIBLE) {
                    // Se progresso for 0 e estiver visível, mantém a animação
                    // (não precisa fazer nada, já está animando)
                } else {
                    // Se não estiver visível, para a animação
                    stopCircleAnimation();
                }
            }
            
            postInvalidate();
        }
    }

    /**
     * Retorna se o progresso está em modo indeterminado
     * @return true se estiver em modo indeterminado, false caso contrário
     */
    public boolean isIndeterminate() {
        return indeterminate;
    }

    private void updateShaders() {
        if (mViewWidth > 0 && firstPaint != null && secondPaint != null) {
            // Array de 3 cores para o gradiente: start, center, end
            int[] gradientColors = new int[]{startColor, centerColor, endColor};
            // Posições: 0.0 (start), 0.5 (center), 1.0 (end)
            float[] positions = new float[]{0.0f, 0.5f, 1.0f};
            
            int drawableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int drawableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            
            firstShader = new LinearGradient(0, 0, drawableWidth, drawableHeight,
                    gradientColors,
                    positions, Shader.TileMode.CLAMP);
            firstPaint.setShader(firstShader);

            secondShader = new LinearGradient(-drawableWidth, 0, 0, drawableHeight,
                    gradientColors,
                    positions, Shader.TileMode.CLAMP);
            secondPaint.setShader(secondShader);
        }
    }

    private void initColors() {
        if (max <= 0 || colors == null || colors.length != max) {
            return;
        }
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            int currentColor = getCurrentColor(i);
            colors[i] = currentColor;
            list.add(currentColor);
        }
        int size = colorsMap.size();
        colorsMap.put(size, list);
        index++;

        colorsRainbow = new int[5];
        colorsRainbow[0] = startColor;
        colorsRainbow[1] = centerColor;
        colorsRainbow[2] = endColor;
        colorsRainbow[3] = endColor;
        colorsRainbow[4] = startColor;


        colorsRainbow1 = new int[colorsRainbow.length];
        for (int i = 0; i < colorsRainbow.length; i++) {
            colorsRainbow1[i] = colorsRainbow[colorsRainbow.length-1-i];
        }


    }

    private int index;
    private Map<Integer, List<Integer>> colorsMap = new HashMap<>();

    private int[] list2Array(List<Integer> integers) {
        int[] color = new int[integers.size()];
        for (int i = 0; i < integers.size(); i++) {
            color[i] = integers.get(i);
        }
        return color;
    }

    private int getCurrentColor(int progress) {
        if (progress == 0) {
            return startColor;
        }
        if (progress == max) {
            return endColor;
        }

        int[] startARGB = convertColor(startColor);
        int[] centerARGB = convertColor(centerColor);
        int[] endARGB = convertColor(endColor);

        int alpha, red, green, blue;
        float ratio = (float) progress / max;

        // Interpolação entre start -> center -> end
        if (ratio <= 0.5f) {
            // Primeira metade: start -> center
            float localRatio = ratio * 2.0f; // 0.0 a 1.0 na primeira metade
            alpha = (int) (startARGB[0] + (centerARGB[0] - startARGB[0]) * localRatio);
            red = (int) (startARGB[1] + (centerARGB[1] - startARGB[1]) * localRatio);
            green = (int) (startARGB[2] + (centerARGB[2] - startARGB[2]) * localRatio);
            blue = (int) (startARGB[3] + (centerARGB[3] - startARGB[3]) * localRatio);
        } else {
            // Segunda metade: center -> end
            float localRatio = (ratio - 0.5f) * 2.0f; // 0.0 a 1.0 na segunda metade
            alpha = (int) (centerARGB[0] + (endARGB[0] - centerARGB[0]) * localRatio);
            red = (int) (centerARGB[1] + (endARGB[1] - centerARGB[1]) * localRatio);
            green = (int) (centerARGB[2] + (endARGB[2] - centerARGB[2]) * localRatio);
            blue = (int) (centerARGB[3] + (endARGB[3] - centerARGB[3]) * localRatio);
        }

        return Color.argb(alpha, red, green, blue);
    }

    private int[] convertColor(int color) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = (color & 0x000000ff);
        return new int[]{alpha, red, green, blue};
    }

    private int dp2px(int value) {
        float v = getContext().getResources().getDisplayMetrics().density;
        return (int) (v * value + 0.5f);
    }

    private int sp2px(int value) {
        float v = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (v * value + 0.5f);
    }
}