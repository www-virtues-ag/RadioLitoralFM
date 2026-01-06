 package br.com.fivecom.litoralfm.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import br.com.fivecom.litoralfm.R;

/**
 * Custom TextView que suporta gradiente de texto configurável.
 *
 * Uso via XML:
 * <pre>
 * &lt;br.com.fivecom.litoralfm.ui.views.GradientTextView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="Texto com Gradiente"
 *     android:textSize="24sp"
 *     app:gradientStartColor="#FF0000"
 *     app:gradientEndColor="#0000FF"
 *     app:gradientAngle="45"
 *     app:gradientEnabled="true" /&gt;
 * </pre>
 *
 * Uso via código:
 * <pre>
 * GradientTextView textView = findViewById(R.id.gradientText);
 * textView.setGradientColors(Color.RED, Color.BLUE);
 * textView.setGradientAngle(45f);
 * textView.setGradientEnabled(true);
 * </pre>
 */
public class GradientTextView extends AppCompatTextView {

    // Tipos de gradiente
    public static final int GRADIENT_LINEAR = 0;
    public static final int GRADIENT_RADIAL = 1;
    public static final int GRADIENT_SWEEP = 2;

    // Atributos do gradiente
    private int gradientStartColor = 0xFF000000; // Preto padrão
    private int gradientEndColor = 0xFFFFFFFF;   // Branco padrão
    private int gradientMiddleColor = -1;        // -1 significa sem cor do meio
    private float gradientAngle = 0f;            // Ângulo em graus
    private int gradientType = GRADIENT_LINEAR;
    private boolean gradientEnabled = false;

    // Shader do gradiente
    private Shader gradientShader;
    private boolean needsShaderUpdate = true;

    public GradientTextView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public GradientTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GradientTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Inicializa a view e processa os atributos XML
     */
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);

            try {
                gradientStartColor = ta.getColor(
                        R.styleable.GradientTextView_gradientStartColor,
                        gradientStartColor
                );

                gradientEndColor = ta.getColor(
                        R.styleable.GradientTextView_gradientEndColor,
                        gradientEndColor
                );

                if (ta.hasValue(R.styleable.GradientTextView_gradientMiddleColor)) {
                    gradientMiddleColor = ta.getColor(
                            R.styleable.GradientTextView_gradientMiddleColor,
                            gradientMiddleColor
                    );
                }

                gradientAngle = ta.getFloat(
                        R.styleable.GradientTextView_gradientAngle,
                        gradientAngle
                );

                gradientType = ta.getInt(
                        R.styleable.GradientTextView_gradientType,
                        gradientType
                );

                gradientEnabled = ta.getBoolean(
                        R.styleable.GradientTextView_gradientEnabled,
                        gradientEnabled
                );

            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        needsShaderUpdate = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (gradientEnabled && needsShaderUpdate) {
            updateShader();
            needsShaderUpdate = false;
        }

        if (gradientEnabled && gradientShader != null) {
            getPaint().setShader(gradientShader);
        } else {
            getPaint().setShader(null);
        }

        super.onDraw(canvas);
    }

    /**
     * Atualiza o shader do gradiente com base nas configurações atuais
     */
    private void updateShader() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Paint paint = getPaint();
        float width = paint.measureText(getText().toString());
        float height = getTextSize();

        int[] colors;
        float[] positions;

        if (gradientMiddleColor != -1) {
            colors = new int[]{gradientStartColor, gradientMiddleColor, gradientEndColor};
            positions = new float[]{0f, 0.5f, 1f};
        } else {
            colors = new int[]{gradientStartColor, gradientEndColor};
            positions = null; // distribuição uniforme
        }

        switch (gradientType) {
            case GRADIENT_LINEAR:
                gradientShader = createLinearGradient(width, height, colors, positions);
                break;

            case GRADIENT_RADIAL:
                gradientShader = createRadialGradient(width, height, colors, positions);
                break;

            case GRADIENT_SWEEP:
                gradientShader = createSweepGradient(width, height, colors, positions);
                break;
        }
    }

    /**
     * Cria um gradiente linear
     */
    private Shader createLinearGradient(float width, float height, int[] colors, float[] positions) {
        // Converte o ângulo em radianos
        double angleRad = Math.toRadians(gradientAngle);

        // Calcula as coordenadas de início e fim com base no ângulo
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        float x0 = centerX - (float) (Math.cos(angleRad) * radius);
        float y0 = centerY - (float) (Math.sin(angleRad) * radius);
        float x1 = centerX + (float) (Math.cos(angleRad) * radius);
        float y1 = centerY + (float) (Math.sin(angleRad) * radius);

        return new LinearGradient(
                x0, y0, x1, y1,
                colors,
                positions,
                Shader.TileMode.CLAMP
        );
    }

    /**
     * Cria um gradiente radial
     */
    private Shader createRadialGradient(float width, float height, int[] colors, float[] positions) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.max(centerX, centerY);

        return new RadialGradient(
                centerX, centerY, radius,
                colors,
                positions,
                Shader.TileMode.CLAMP
        );
    }

    /**
     * Cria um gradiente sweep (cônico)
     */
    private Shader createSweepGradient(float width, float height, int[] colors, float[] positions) {
        float centerX = width / 2f;
        float centerY = height / 2f;

        if (positions != null) {
            return new SweepGradient(centerX, centerY, colors, positions);
        } else {
            return new SweepGradient(centerX, centerY, colors[0], colors[1]);
        }
    }

    // ==================== Métodos Públicos (Setters) ====================

    /**
     * Define as cores do gradiente (duas cores)
     */
    public void setGradientColors(@ColorInt int startColor, @ColorInt int endColor) {
        this.gradientStartColor = startColor;
        this.gradientEndColor = endColor;
        this.gradientMiddleColor = -1;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define as cores do gradiente (três cores)
     */
    public void setGradientColors(@ColorInt int startColor, @ColorInt int middleColor, @ColorInt int endColor) {
        this.gradientStartColor = startColor;
        this.gradientMiddleColor = middleColor;
        this.gradientEndColor = endColor;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define a cor inicial do gradiente
     */
    public void setGradientStartColor(@ColorInt int color) {
        this.gradientStartColor = color;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define a cor final do gradiente
     */
    public void setGradientEndColor(@ColorInt int color) {
        this.gradientEndColor = color;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define a cor do meio do gradiente
     */
    public void setGradientMiddleColor(@ColorInt int color) {
        this.gradientMiddleColor = color;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Remove a cor do meio do gradiente
     */
    public void removeGradientMiddleColor() {
        this.gradientMiddleColor = -1;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define o ângulo do gradiente em graus (0-360)
     */
    public void setGradientAngle(float angle) {
        this.gradientAngle = angle % 360;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Define o tipo de gradiente
     * @param type GRADIENT_LINEAR, GRADIENT_RADIAL ou GRADIENT_SWEEP
     */
    public void setGradientType(int type) {
        if (type < GRADIENT_LINEAR || type > GRADIENT_SWEEP) {
            throw new IllegalArgumentException("Tipo de gradiente inválido: " + type);
        }
        this.gradientType = type;
        needsShaderUpdate = true;
        invalidate();
    }

    /**
     * Habilita ou desabilita o gradiente
     */
    public void setGradientEnabled(boolean enabled) {
        this.gradientEnabled = enabled;
        invalidate();
    }

    // ==================== Métodos Públicos (Getters) ====================

    public int getGradientStartColor() {
        return gradientStartColor;
    }

    public int getGradientEndColor() {
        return gradientEndColor;
    }

    public int getGradientMiddleColor() {
        return gradientMiddleColor;
    }

    public float getGradientAngle() {
        return gradientAngle;
    }

    public int getGradientType() {
        return gradientType;
    }

    public boolean isGradientEnabled() {
        return gradientEnabled;
    }

    // ==================== Métodos Override para Atualização ====================

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        needsShaderUpdate = true;
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        needsShaderUpdate = true;
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        needsShaderUpdate = true;
    }
}

