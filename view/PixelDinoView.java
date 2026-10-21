package com.example.chronodino.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.chronodino.model.FoodItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PixelDinoView extends View {

    public enum DinoState {
        IDLE,
        EATING,
        HAPPY,
        HUNGRY,
        DEAD
    }

    private static final int GRID_SIZE = 24;

    // Sprite frame definitions (24x24 characters)
    // . = empty, G = body green, D = dark green shadow/outline, L = light mint belly,
    // O = orange spine spike, W = white, E = black eye pupil, P = pink mouth,
    // R = pink cheek blush, X = eye X (dead), T = teeth
    private static final String[] SPRITE_IDLE_1 = {
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGGGGGDD....",
        "........DGGGGWWEGGGD....",
        "........DGGGGWWEGGGD....",
        ".......ODGGGRGGGGGGGDD..",
        "......O.DGGGGGGDDDDDDD..",
        ".....O..DGGGGDD.........",
        "....O...DGGGGGGDD.......",
        "...O.....DGGGGGGGGDD....",
        "..O......DGGGGLLLLLDD...",
        ".O.DDD...DGGGGLLLLLDD...",
        "DGGGGDD.DGGGGGLLLLLDD...",
        "DGGGGGDDDGGGGGLLLLLDD...",
        ".DGGGGGGGGGGGGLLLLLDD...",
        "..DGGGGGGGGGGGLLLLDD....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_IDLE_2 = {
        "........................",
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGWWEGGDD...",
        "........ODGGGGWWEGGGD...",
        ".......O.DGGGRGGGGGGGD..",
        "......O..DGGGGGGDDDDDDD.",
        ".....O...DGGGGDD........",
        "....O....DGGGGGGDD......",
        "...O......DGGGGGGGDD....",
        "..O.......DGGGGLLLLDD...",
        ".O.DDD....DGGGGLLLLDD...",
        "DGGGGDD..DGGGGGLLLLDD...",
        "DGGGGGDDDGGGGGGLLLLDD...",
        ".DGGGGGGGGGGGGGLLLLDD...",
        "..DGGGGGGGGGGGGLLLDD....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_EAT_OPEN = {
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGGGGGDD....",
        "........DGGGGWWEEGGGD...",
        "........DGGGRWWEEGGGD...",
        ".......ODGGGGTTTTTTDD...",
        "......O.DGGGPPPPPPPDD...",
        ".....O..DGGGPPPPPPPDD...",
        "....O...DGGGGTTTTTTDD...",
        "...O.....DGGGGGGGGDD....",
        "..O......DGGGGLLLLLDD...",
        ".O.DDD...DGGGGLLLLLDD...",
        "DGGGGDD.DGGGGGLLLLLDD...",
        "DGGGGGDDDGGGGGLLLLLDD...",
        ".DGGGGGGGGGGGGLLLLLDD...",
        "..DGGGGGGGGGGGLLLLDD....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_EAT_CHOMP = {
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGGGGGDD....",
        "........DGGGGRWWREGGD...",
        "........DGGGRRWWREGGD...",
        ".......ODGGGGGGGGGGGDD..",
        "......O.DGGGGDDDDDDDDD..",
        ".....O..DGGGGDD.........",
        "....O...DGGGGGGDD.......",
        "...O.....DGGGGGGGGDD....",
        "..O......DGGGGLLLLLDD...",
        ".O.DDD...DGGGGLLLLLDD...",
        "DGGGGDD.DGGGGGLLLLLDD...",
        "DGGGGGDDDGGGGGLLLLLDD...",
        ".DGGGGGGGGGGGGLLLLLDD...",
        "..DGGGGGGGGGGGLLLLDD....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_HAPPY = {
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGGGGGDD....",
        "........DGGGGWWWGGGD....",
        "........DGGGRWWWGGGD....",
        ".......ODGGGGWWWGGD.....",
        "......O.DGGGGDDDDDD.....",
        ".....O..DGGGPPPPPDD.....",
        "....O...DGGGGGGDDD......",
        "...O...DDGGGGGGGGDD.....",
        "..O...DGGGGGGLLLLLDD....",
        ".O.DD.DGGGGGGLLLLLDD....",
        "DGGGGDDDGGGGGLLLLLDD....",
        "DGGGGGDDGGGGGLLLLLDD....",
        ".DGGGGGGGGGGGLLLLLDD....",
        "..DGGGGGGGGGGLLLLDD.....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_HUNGRY = {
        "........................",
        "........................",
        "...........OOOO.........",
        "..........DGGGGDDD......",
        ".........DGGGGWWEEGD....",
        "........ODGGGGWWEEGD....",
        ".......O.DGGGGGGGGGD....",
        "......O..DGGGGDDDDDD....",
        ".....O...DGGDD..........",
        "....O....DGGGGDD........",
        "...O......DGGGGGDD......",
        "..O.......DGGGGLLLDD....",
        ".O.DDD....DGGGGLLLDD....",
        "DGGGGDD..DGGGGGLLLDD....",
        "DGGGGGDDDGGGGGGLLLDD....",
        ".DGGGGGGGGGGGGGLLLDD....",
        "..DGGGGGGGGGGGGLLDD.....",
        "...DDGGGGGGGGGGDDD......",
        ".....DDDDGGDDGGD........",
        "........DGGDDGGD........",
        "........DGGDDGGD........",
        ".......DDDD.DDDD........",
        "........................",
        "........................"
    };

    private static final String[] SPRITE_DEAD = {
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        ".......OOOO.............",
        "......DGGGGDDD..........",
        ".....DGGGGGGGGDD........",
        "....DGGGGXXGGGGDD.......",
        "....DGGGGXXGGGGDD.......",
        "...DGGGGGGGGDDDDD.......",
        "..DGGGGGGGGLLLLLDDD.....",
        ".DGGGGGGGGGLLLLLDDGD....",
        "DDDDDDDDDDDDDDDDDDDDD...",
        "........................",
        "........................"
    };

    // Color definitions
    private final Paint paintPixel = new Paint();
    private final Paint paintBackground = new Paint();
    private final Paint paintGround = new Paint();
    private final Paint paintShadow = new Paint();
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintParticle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCloud = new Paint();

    private final int COLOR_BODY = 0xFF22C55E;       // G
    private final int COLOR_DARK = 0xFF15803D;       // D
    private final int COLOR_BELLY = 0xFF86EFAC;      // L
    private final int COLOR_SPIKE = 0xFFF97316;      // O
    private final int COLOR_WHITE = 0xFFFFFFFF;      // W
    private final int COLOR_EYE = 0xFF0F172A;        // E
    private final int COLOR_MOUTH = 0xFFE11D48;      // P
    private final int COLOR_BLUSH = 0xFFFB7185;      // R
    private final int COLOR_TEETH = 0xFFF8FAFC;      // T
    private final int COLOR_XX = 0xFF334155;         // X

    // State & Animation
    private DinoState currentState = DinoState.IDLE;
    private long stateStartTime = 0;
    private long lastFrameTime = 0;
    private int idleFrame = 0;
    private int eatFrame = 0;

    // Bounce / Hop offset
    private float hopOffsetY = 0f;
    private float shakeOffsetX = 0f;

    // Craving thought
    private FoodItem currentCraving = null;

    // Flying food animation
    private static class FlyingFood {
        FoodItem item;
        float startX, startY;
        float currentX, currentY;
        float targetX, targetY;
        float progress = 0f; // 0 to 1
        Runnable onFinish;
    }
    private final List<FlyingFood> flyingFoods = new ArrayList<>();

    // Floating text items (e.g. "+10 PTS", "YUM!")
    private static class FloatingText {
        String text;
        float x, y;
        int color;
        float alpha = 1.0f;
        long createTime;
    }
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // Floating particles (hearts, crumbs, sweat, Zzz)
    private static class Particle {
        String symbol; // "♥", "Z", "💧", or null for square crumb
        float x, y;
        float vx, vy;
        int color;
        float size;
        float alpha = 1.0f;
        long createTime;
    }
    private final List<Particle> particles = new ArrayList<>();

    // Decorative clouds
    private static class Cloud {
        float x, y;
        float speed;
        float width, height;
    }
    private final List<Cloud> clouds = new ArrayList<>();
    private final Random random = new Random();

    public PixelDinoView(Context context) {
        super(context);
        init();
    }

    public PixelDinoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelDinoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintPixel.setStyle(Paint.Style.FILL);
        paintShadow.setStyle(Paint.Style.FILL);
        paintShadow.setColor(0x33000000);

        paintCloud.setStyle(Paint.Style.FILL);
        paintCloud.setColor(0x88FFFFFF);

        paintText.setTypeface(Typeface.DEFAULT_BOLD);
        paintText.setTextAlign(Paint.Align.CENTER);

        // Initialize clouds
        clouds.add(createCloud(0.15f, 0.12f, 0.4f));
        clouds.add(createCloud(0.65f, 0.22f, 0.3f));
        clouds.add(createCloud(0.85f, 0.08f, 0.5f));

        stateStartTime = SystemClock.uptimeMillis();
        lastFrameTime = SystemClock.uptimeMillis();
    }

    private Cloud createCloud(float xRatio, float yRatio, float speed) {
        Cloud c = new Cloud();
        c.x = xRatio;
        c.y = yRatio;
        c.speed = speed;
        c.width = 70f + random.nextInt(40);
        c.height = 24f + random.nextInt(12);
        return c;
    }

    public void setState(DinoState state) {
        this.currentState = state;
        this.stateStartTime = SystemClock.uptimeMillis();
        invalidate();
    }

    public DinoState getCurrentState() {
        return currentState;
    }

    public void setCraving(@Nullable FoodItem foodItem) {
        this.currentCraving = foodItem;
        invalidate();
    }

    @Nullable
    public FoodItem getCraving() {
        return currentCraving;
    }

    /**
     * Triggers eating animation, particles, and floating score
     */
    public void feedFood(FoodItem food, int points, boolean isCravingBonus, Runnable onChomp) {
        setState(DinoState.EATING);

        // Spawn crumb particles
        float dinoCenterX = getWidth() * 0.5f;
        float dinoCenterY = getHeight() * 0.55f;
        for (int i = 0; i < 10; i++) {
            Particle p = new Particle();
            p.x = dinoCenterX + (random.nextFloat() - 0.2f) * 60f;
            p.y = dinoCenterY + (random.nextFloat() - 0.5f) * 40f;
            p.vx = (random.nextFloat() - 0.4f) * 12f;
            p.vy = (random.nextFloat() - 0.7f) * 12f;
            p.color = food.getAccentColor();
            p.size = 10f + random.nextFloat() * 8f;
            p.createTime = SystemClock.uptimeMillis();
            particles.add(p);
        }

        // Floating score text
        FloatingText ft = new FloatingText();
        ft.text = isCravingBonus ? "+" + points + " CRAVING 2X!" : "+" + points;
        ft.x = dinoCenterX;
        ft.y = dinoCenterY - 100f;
        ft.color = isCravingBonus ? 0xFFFFD700 : 0xFF10B981;
        ft.createTime = SystemClock.uptimeMillis();
        floatingTexts.add(ft);

        if (onChomp != null) {
            onChomp.run();
        }

        // Hop animation
        ValueAnimator hopAnim = ValueAnimator.ofFloat(0, -50f, 0);
        hopAnim.setDuration(400);
        hopAnim.setInterpolator(new DecelerateInterpolator());
        hopAnim.addUpdateListener(anim -> {
            hopOffsetY = (float) anim.getAnimatedValue();
            invalidate();
        });
        hopAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Transition to Happy for 1 second, then back to Idle
                setState(DinoState.HAPPY);
                // Spawn hearts
                for (int i = 0; i < 3; i++) {
                    postDelayed(() -> spawnHeart(), i * 200);
                }
                postDelayed(() -> {
                    if (currentState == DinoState.HAPPY) {
                        setState(DinoState.IDLE);
                    }
                }, 1000);
            }
        });
        hopAnim.start();
    }

    private void spawnHeart() {
        Particle p = new Particle();
        p.symbol = "♥";
        p.x = getWidth() * 0.5f + (random.nextFloat() - 0.5f) * 80f;
        p.y = getHeight() * 0.45f;
        p.vx = (random.nextFloat() - 0.5f) * 3f;
        p.vy = -4f - random.nextFloat() * 3f;
        p.color = 0xFFF43F5E;
        p.size = 32f;
        p.createTime = SystemClock.uptimeMillis();
        particles.add(p);
        invalidate();
    }

    private void spawnSweat() {
        Particle p = new Particle();
        p.symbol = "💧";
        p.x = getWidth() * 0.55f;
        p.y = getHeight() * 0.42f;
        p.vx = 1f;
        p.vy = 4f;
        p.color = 0xFF38BDF8;
        p.size = 28f;
        p.createTime = SystemClock.uptimeMillis();
        particles.add(p);
    }

    private void spawnZzz() {
        Particle p = new Particle();
        p.symbol = "Z";
        p.x = getWidth() * 0.55f + (random.nextFloat() * 20f);
        p.y = getHeight() * 0.52f;
        p.vx = 2f;
        p.vy = -3f;
        p.color = 0xFF94A3B8;
        p.size = 24f + (random.nextInt(3) * 6);
        p.createTime = SystemClock.uptimeMillis();
        particles.add(p);
    }

    /**
     * Animate food projectile from tap coordinates to dino mouth
     */
    public void launchFoodProjectile(float startX, float startY, FoodItem food, Runnable onArrive) {
        FlyingFood ff = new FlyingFood();
        ff.item = food;
        ff.startX = startX;
        ff.startY = startY;
        ff.currentX = startX;
        ff.currentY = startY;
        ff.targetX = getWidth() * 0.52f;
        ff.targetY = getHeight() * 0.52f;
        ff.onFinish = onArrive;

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(350);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addUpdateListener(animation -> {
            float t = (float) animation.getAnimatedValue();
            ff.progress = t;
            // Parabolic curve
            ff.currentX = ff.startX + (ff.targetX - ff.startX) * t;
            float arc = (float) Math.sin(t * Math.PI) * -120f;
            ff.currentY = ff.startY + (ff.targetY - ff.startY) * t + arc;
            invalidate();
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flyingFoods.remove(ff);
                if (ff.onFinish != null) {
                    ff.onFinish.run();
                }
                invalidate();
            }
        });
        flyingFoods.add(ff);
        anim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        long now = SystemClock.uptimeMillis();

        // 1. Draw Sky background gradient
        paintBackground.setShader(new LinearGradient(
                0, 0, 0, h,
                new int[]{0xFFBAE6FD, 0xFFE0F2FE, 0xFFF0FDF4},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paintBackground);

        // 2. Draw Clouds
        drawClouds(canvas, w, h);

        // 3. Draw Ground
        float groundY = h * 0.76f;
        drawGround(canvas, w, h, groundY);

        // 4. Determine current Dino Sprite & Frame
        String[] sprite = getActiveSprite(now);

        // 5. Dino scale & position calculation
        // Ensure Dino is prominent, centered, and pixel perfect
        float dinoScale = Math.min(w * 0.68f, h * 0.52f) / GRID_SIZE;
        float dinoPixelSize = Math.max(2f, (float) Math.floor(dinoScale));
        float dinoWidth = GRID_SIZE * dinoPixelSize;
        float dinoHeight = GRID_SIZE * dinoPixelSize;

        float dinoLeft = (w - dinoWidth) * 0.5f + shakeOffsetX;
        float dinoTop = groundY - dinoHeight + (GRID_SIZE * 0.12f * dinoPixelSize) + hopOffsetY;

        // Draw shadow under Dino
        if (currentState != DinoState.DEAD) {
            float shadowWidth = dinoWidth * 0.7f;
            float shadowHeight = dinoHeight * 0.12f;
            float shadowLeft = (w - shadowWidth) * 0.5f;
            float shadowTop = groundY - shadowHeight * 0.5f;
            canvas.drawOval(new RectF(shadowLeft, shadowTop, shadowLeft + shadowWidth, shadowTop + shadowHeight), paintShadow);
        }

        // 6. Draw Dino Pixel Grid
        drawPixelSprite(canvas, sprite, dinoLeft, dinoTop, dinoPixelSize);

        // 7. Draw Thought Bubble (Craving or Hungry or Sleeping)
        drawThoughtBubble(canvas, dinoLeft + dinoWidth * 0.5f, dinoTop, dinoPixelSize, now);

        // 8. Draw Flying Foods
        drawFlyingFoods(canvas);

        // 9. Update & Draw Particles (Hearts, Crumbs, Sweat, Zzz)
        updateAndDrawParticles(canvas, now);

        // 10. Update & Draw Floating Score Texts
        updateAndDrawFloatingTexts(canvas, now);

        // Loop animation
        postInvalidateOnAnimation();
    }

    private void drawClouds(Canvas canvas, int w, int h) {
        for (Cloud c : clouds) {
            c.x += c.speed * 0.0003f;
            if (c.x > 1.2f) c.x = -0.2f;

            float cx = c.x * w;
            float cy = c.y * h;
            RectF cloudRect = new RectF(cx - c.width / 2, cy - c.height / 2, cx + c.width / 2, cy + c.height / 2);
            canvas.drawRoundRect(cloudRect, c.height / 2, c.height / 2, paintCloud);
        }
    }

    private void drawGround(Canvas canvas, int w, int h, float groundY) {
        // Grass top band
        paintGround.setColor(0xFF86EFAC);
        canvas.drawRect(0, groundY, w, groundY + 12f, paintGround);

        // Grass blades detail
        paintGround.setColor(0xFF22C55E);
        for (int x = 10; x < w; x += 28) {
            canvas.drawRect(x, groundY - 6f, x + 8f, groundY, paintGround);
        }

        // Earth / dirt bottom
        paintGround.setColor(0xFFBBF7D0);
        canvas.drawRect(0, groundY + 12f, w, h, paintGround);
    }

    private String[] getActiveSprite(long now) {
        switch (currentState) {
            case EATING:
                eatFrame = ((int) ((now - stateStartTime) / 120)) % 2;
                return (eatFrame == 0) ? SPRITE_EAT_OPEN : SPRITE_EAT_CHOMP;

            case HAPPY:
                return SPRITE_HAPPY;

            case HUNGRY:
                // Shiver effect
                shakeOffsetX = ((now / 50) % 2 == 0) ? -3f : 3f;
                if ((now / 800) % 2 == 0 && particles.size() < 15 && random.nextFloat() < 0.15f) {
                    spawnSweat();
                }
                return SPRITE_HUNGRY;

            case DEAD:
                shakeOffsetX = 0f;
                if ((now / 900) % 2 == 0 && particles.size() < 10 && random.nextFloat() < 0.10f) {
                    spawnZzz();
                }
                return SPRITE_DEAD;

            case IDLE:
            default:
                shakeOffsetX = 0f;
                idleFrame = ((int) ((now / 450) % 2));
                return (idleFrame == 0) ? SPRITE_IDLE_1 : SPRITE_IDLE_2;
        }
    }

    private void drawPixelSprite(Canvas canvas, String[] sprite, float startX, float startY, float pixelSize) {
        for (int row = 0; row < sprite.length; row++) {
            String line = sprite[row];
            for (int col = 0; col < line.length(); col++) {
                char ch = line.charAt(col);
                if (ch == '.') continue;

                int color = getColorForChar(ch);
                paintPixel.setColor(color);

                float left = startX + col * pixelSize;
                float top = startY + row * pixelSize;
                canvas.drawRect(left, top, left + pixelSize + 0.5f, top + pixelSize + 0.5f, paintPixel);
            }
        }
    }

    private int getColorForChar(char ch) {
        switch (ch) {
            case 'G': return COLOR_BODY;
            case 'D': return COLOR_DARK;
            case 'L': return COLOR_BELLY;
            case 'O': return COLOR_SPIKE;
            case 'W': return COLOR_WHITE;
            case 'E': return COLOR_EYE;
            case 'P': return COLOR_MOUTH;
            case 'R': return COLOR_BLUSH;
            case 'T': return COLOR_TEETH;
            case 'X': return COLOR_XX;
            default:  return Color.TRANSPARENT;
        }
    }

    private void drawThoughtBubble(Canvas canvas, float centerX, float dinoTop, float pixelSize, long now) {
        if (currentState == DinoState.DEAD) {
            // Draw sleeping Zzz text bubble
            float bubbleX = centerX + 40f;
            float bubbleY = dinoTop + 20f;
            paintText.setTextSize(26f);
            paintText.setColor(0xFF64748B);
            canvas.drawText("Z z z...", bubbleX, bubbleY, paintText);
            return;
        }

        if (currentState == DinoState.HUNGRY) {
            float bubbleX = centerX;
            float bubbleY = dinoTop - 25f;
            paintText.setTextSize(24f);
            paintText.setColor(0xFFDC2626);
            canvas.drawText("I'M STARVING! 💧", bubbleX, bubbleY, paintText);
            return;
        }

        if (currentCraving != null && currentState != DinoState.EATING) {
            // Draw cute retro thought bubble with the craving food
            float bubbleCenterX = centerX + 45f;
            float bubbleCenterY = dinoTop - 35f;

            // Little connector circles
            paintPixel.setColor(0xCCFFFFFF);
            canvas.drawCircle(centerX + 15f, dinoTop - 10f, 6f, paintPixel);
            canvas.drawCircle(centerX + 28f, dinoTop - 20f, 9f, paintPixel);

            // Main bubble card
            RectF bubbleRect = new RectF(bubbleCenterX - 45f, bubbleCenterY - 26f, bubbleCenterX + 45f, bubbleCenterY + 26f);
            paintPixel.setColor(0xFFFFFFFF);
            canvas.drawRoundRect(bubbleRect, 18f, 18f, paintPixel);

            // Bubble border
            Paint borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(3f);
            borderPaint.setColor(currentCraving.getAccentColor());
            canvas.drawRoundRect(bubbleRect, 18f, 18f, borderPaint);

            // Bubble text / emoji
            paintText.setTextSize(26f);
            paintText.setColor(0xFF1E293B);
            canvas.drawText("CRAVE " + currentCraving.getEmoji(), bubbleCenterX, bubbleCenterY + 9f, paintText);
        }
    }

    private void drawFlyingFoods(Canvas canvas) {
        for (FlyingFood ff : flyingFoods) {
            paintText.setTextSize(48f);
            canvas.drawText(ff.item.getEmoji(), ff.currentX, ff.currentY, paintText);
        }
    }

    private void updateAndDrawParticles(Canvas canvas, long now) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            float ageSec = (now - p.createTime) / 1000f;
            if (ageSec > 1.2f) {
                it.remove();
                continue;
            }

            p.x += p.vx;
            p.y += p.vy;
            if (p.symbol == null) {
                // Crumb gravity
                p.vy += 0.5f;
            }
            p.alpha = Math.max(0f, 1.0f - (ageSec / 1.2f));

            if (p.symbol != null) {
                paintText.setTextSize(p.size);
                paintText.setColor(p.color);
                paintText.setAlpha((int) (p.alpha * 255));
                canvas.drawText(p.symbol, p.x, p.y, paintText);
            } else {
                paintParticle.setColor(p.color);
                paintParticle.setAlpha((int) (p.alpha * 255));
                canvas.drawRect(p.x, p.y, p.x + p.size, p.y + p.size, paintParticle);
            }
        }
    }

    private void updateAndDrawFloatingTexts(Canvas canvas, long now) {
        Iterator<FloatingText> it = floatingTexts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            float ageSec = (now - ft.createTime) / 1000f;
            if (ageSec > 1.0f) {
                it.remove();
                continue;
            }

            ft.y -= 2.2f;
            ft.alpha = Math.max(0f, 1.0f - (ageSec / 1.0f));

            paintText.setTextSize(34f);
            paintText.setColor(ft.color);
            paintText.setAlpha((int) (ft.alpha * 255));
            paintText.setShadowLayer(4f, 0, 2f, 0x88000000);
            canvas.drawText(ft.text, ft.x, ft.y, paintText);
            paintText.clearShadowLayer();
        }
    }
}
