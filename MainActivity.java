package com.example.chronodino;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chronodino.audio.SoundManager;
import com.example.chronodino.model.FoodItem;
import com.example.chronodino.view.PixelDinoView;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final long MAX_TIME_MS = 60000L; // 60 seconds
    private static final long TICK_INTERVAL_MS = 50L;
    private static final String PREFS_NAME = "chronodino_prefs";
    private static final String KEY_HIGH_SCORE = "high_score";

    // UI elements
    private TextView tvScore;
    private TextView tvHighScore;
    private TextView tvFeedCount;
    private TextView tvTimerText;
    private TextView tvTimerStatus;
    private ProgressBar pbTimer;
    private PixelDinoView pixelDinoView;
    private ImageButton btnSoundToggle;

    private MaterialCardView cardApple;
    private MaterialCardView cardMeat;
    private MaterialCardView cardCarrot;

    // Game Over Overlay
    private View layoutGameOver;
    private TextView tvGameOverScore;
    private TextView tvGameOverHighScore;
    private TextView tvGameOverFeeds;
    private TextView tvNewRecordBadge;
    private Button btnRestart;

    // Game State
    private CountDownTimer countDownTimer;
    private long remainingTimeMs = MAX_TIME_MS;
    private int score = 0;
    private int highScore = 0;
    private int feedCount = 0;
    private boolean isGameOver = false;
    private int lastWarningSec = -1;

    private SoundManager soundManager;
    private SharedPreferences prefs;
    private final Random random = new Random();
    private FoodItem currentCraving = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initSoundAndPrefs();
        setupListeners();
        startNewGame();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvFeedCount = findViewById(R.id.tvFeedCount);
        tvTimerText = findViewById(R.id.tvTimerText);
        tvTimerStatus = findViewById(R.id.tvTimerStatus);
        pbTimer = findViewById(R.id.pbTimer);
        pixelDinoView = findViewById(R.id.pixelDinoView);
        btnSoundToggle = findViewById(R.id.btnSoundToggle);

        cardApple = findViewById(R.id.cardApple);
        cardMeat = findViewById(R.id.cardMeat);
        cardCarrot = findViewById(R.id.cardCarrot);

        layoutGameOver = findViewById(R.id.layoutGameOver);
        tvGameOverScore = findViewById(R.id.tvGameOverScore);
        tvGameOverHighScore = findViewById(R.id.tvGameOverHighScore);
        tvGameOverFeeds = findViewById(R.id.tvGameOverFeeds);
        tvNewRecordBadge = findViewById(R.id.tvNewRecordBadge);
        btnRestart = findViewById(R.id.btnRestart);
    }

    private void initSoundAndPrefs() {
        soundManager = new SoundManager();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvHighScore.setText(String.valueOf(highScore));
    }

    private void setupListeners() {
        cardApple.setOnClickListener(v -> feedFood(cardApple, FoodItem.APPLE));
        cardMeat.setOnClickListener(v -> feedFood(cardMeat, FoodItem.MEAT));
        cardCarrot.setOnClickListener(v -> feedFood(cardCarrot, FoodItem.CARROT));

        btnRestart.setOnClickListener(v -> startNewGame());

        btnSoundToggle.setOnClickListener(v -> {
            soundManager.toggleMute();
            if (soundManager.isMuted()) {
                btnSoundToggle.setImageResource(android.R.drawable.ic_lock_silent_mode);
                btnSoundToggle.setColorFilter(0xFF94A3B8);
            } else {
                btnSoundToggle.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                btnSoundToggle.setColorFilter(0xFF10B981);
                soundManager.playHappySound();
            }
        });
    }

    private void startNewGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isGameOver = false;
        score = 0;
        feedCount = 0;
        lastWarningSec = -1;
        remainingTimeMs = MAX_TIME_MS;

        updateHUD();
        hideGameOver();

        pixelDinoView.setState(PixelDinoView.DinoState.IDLE);
        rollNextCraving();
        startCountdown(MAX_TIME_MS);
    }

    private void startCountdown(long durationMs) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(durationMs, TICK_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isGameOver) return;
                remainingTimeMs = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                if (!isGameOver) {
                    remainingTimeMs = 0;
                    updateTimerDisplay(0);
                    onGameOver();
                }
            }
        };
        countDownTimer.start();
    }

    private void updateTimerDisplay(long millisUntilFinished) {
        float seconds = millisUntilFinished / 1000.0f;
        tvTimerText.setText(String.format(Locale.US, "%.1fs", seconds));

        int progress = (int) (millisUntilFinished / 100L); // 0 to 600
        pbTimer.setProgress(progress);

        if (millisUntilFinished > 25000L) {
            // Safe zone (26s - 60s)
            int green = 0xFF10B981;
            tvTimerText.setTextColor(green);
            pbTimer.setProgressTintList(ColorStateList.valueOf(green));
            tvTimerStatus.setText("Dino is full and energetic!");
            tvTimerStatus.setTextColor(0xFF059669);
            if (pixelDinoView.getCurrentState() == PixelDinoView.DinoState.HUNGRY) {
                pixelDinoView.setState(PixelDinoView.DinoState.IDLE);
            }
        } else if (millisUntilFinished > 10000L) {
            // Warning zone (11s - 25s)
            int amber = 0xFFF59E0B;
            tvTimerText.setTextColor(amber);
            pbTimer.setProgressTintList(ColorStateList.valueOf(amber));
            tvTimerStatus.setText("Dino is getting hungry!");
            tvTimerStatus.setTextColor(0xFFD97706);
            if (pixelDinoView.getCurrentState() == PixelDinoView.DinoState.HUNGRY) {
                pixelDinoView.setState(PixelDinoView.DinoState.IDLE);
            }
        } else {
            // Danger zone (0s - 10s)
            int danger = 0xFFEF4444;
            tvTimerText.setTextColor(danger);
            pbTimer.setProgressTintList(ColorStateList.valueOf(danger));
            tvTimerStatus.setText("⚠️ STARVING! FEED BEFORE TIMER RUNS OUT!");
            tvTimerStatus.setTextColor(0xFFDC2626);

            if (pixelDinoView.getCurrentState() == PixelDinoView.DinoState.IDLE) {
                pixelDinoView.setState(PixelDinoView.DinoState.HUNGRY);
            }

            int currentSec = (int) (millisUntilFinished / 1000L);
            if (currentSec != lastWarningSec && currentSec > 0) {
                lastWarningSec = currentSec;
                soundManager.playWarningTick();
                vibrate(40);
            }
        }
    }

    private void feedFood(View cardView, FoodItem food) {
        if (isGameOver) return;

        vibrate(30);

        // Tactile button bounce
        cardView.animate()
                .scaleX(0.92f).scaleY(0.92f)
                .setDuration(90)
                .withEndAction(() -> cardView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(90).start())
                .start();

        // Calculate card position relative to PixelDinoView
        int[] cardLocation = new int[2];
        cardView.getLocationOnScreen(cardLocation);
        int[] dinoLocation = new int[2];
        pixelDinoView.getLocationOnScreen(dinoLocation);

        float startX = (cardLocation[0] + cardView.getWidth() * 0.5f) - dinoLocation[0];
        float startY = (cardLocation[1] + cardView.getHeight() * 0.5f) - dinoLocation[1];

        // Launch food projectile into dino's mouth
        pixelDinoView.launchFoodProjectile(startX, startY, food, () -> {
            if (isGameOver) return;

            boolean isCravingMatch = (currentCraving != null && currentCraving == food);
            int pointsEarned = isCravingMatch ? food.getPoints() * 2 : food.getPoints();

            // Sound
            if (isCravingMatch) {
                soundManager.playBonusSound();
            } else {
                soundManager.playChompSound();
            }

            // Dino reaction & crumbs
            pixelDinoView.feedFood(food, pointsEarned, isCravingMatch, null);

            // Update stats
            score += pointsEarned;
            feedCount++;

            if (score > highScore) {
                highScore = score;
                prefs.edit().putInt(KEY_HIGH_SCORE, highScore).apply();
            }

            updateHUD();

            // Reset survival timer to 60 seconds as per game rule
            startCountdown(MAX_TIME_MS);

            // Roll next craving
            rollNextCraving();
        });
    }

    private void rollNextCraving() {
        // 65% chance dino craves a specific food item
        if (random.nextFloat() < 0.65f) {
            FoodItem[] foods = FoodItem.values();
            currentCraving = foods[random.nextInt(foods.length)];
        } else {
            currentCraving = null;
        }
        pixelDinoView.setCraving(currentCraving);
    }

    private void updateHUD() {
        tvScore.setText(String.valueOf(score));
        tvHighScore.setText(String.valueOf(highScore));
        tvFeedCount.setText(String.valueOf(feedCount));
    }

    private void onGameOver() {
        isGameOver = true;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        vibrate(350);
        soundManager.playGameOverSound();

        pixelDinoView.setState(PixelDinoView.DinoState.DEAD);

        // Update Game Over Dialog stats
        tvGameOverScore.setText(String.valueOf(score));
        tvGameOverHighScore.setText(String.valueOf(highScore));
        tvGameOverFeeds.setText(String.valueOf(feedCount));

        if (score >= highScore && score > 0) {
            tvNewRecordBadge.setVisibility(View.VISIBLE);
        } else {
            tvNewRecordBadge.setVisibility(View.GONE);
        }

        // Show Game Over overlay with smooth fade
        layoutGameOver.setAlpha(0f);
        layoutGameOver.setVisibility(View.VISIBLE);
        layoutGameOver.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void hideGameOver() {
        if (layoutGameOver.getVisibility() == View.VISIBLE) {
            layoutGameOver.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            layoutGameOver.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void vibrate(long durationMs) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(durationMs);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null && !isGameOver) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameOver && remainingTimeMs > 0) {
            startCountdown(remainingTimeMs);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (soundManager != null) {
            soundManager.release();
        }
    }
}