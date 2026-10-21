package com.example.chronodino.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Procedural 8-bit retro sound synthesizer using AudioTrack.
 * Generates classic arcade square-wave tones without external audio files.
 */
public class SoundManager {

    private static final int SAMPLE_RATE = 22050;
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isMuted = false;

    public SoundManager() {}

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void toggleMute() {
        this.isMuted = !this.isMuted;
    }

    /**
     * Retro chomp / munch sound (two quick playful chirps)
     */
    public void playChompSound() {
        if (isMuted) return;
        audioExecutor.execute(() -> {
            byte[] note1 = generateSquareWave(480, 0.08f, 0.7f);
            byte[] silence = new byte[SAMPLE_RATE / 50 * 2]; // 20ms
            byte[] note2 = generateSquareWave(720, 0.10f, 0.8f);
            playPcm(concat(note1, silence, note2));
        });
    }

    /**
     * Joyful level-up / happy cheer arpeggio
     */
    public void playHappySound() {
        if (isMuted) return;
        audioExecutor.execute(() -> {
            byte[] c5 = generateSquareWave(523, 0.07f, 0.6f);
            byte[] e5 = generateSquareWave(659, 0.07f, 0.7f);
            byte[] g5 = generateSquareWave(784, 0.12f, 0.8f);
            playPcm(concat(c5, e5, g5));
        });
    }

    /**
     * Craving bonus fanfare
     */
    public void playBonusSound() {
        if (isMuted) return;
        audioExecutor.execute(() -> {
            byte[] c5 = generateSquareWave(523, 0.05f, 0.6f);
            byte[] e5 = generateSquareWave(659, 0.05f, 0.6f);
            byte[] g5 = generateSquareWave(784, 0.05f, 0.7f);
            byte[] c6 = generateSquareWave(1046, 0.15f, 0.8f);
            playPcm(concat(c5, e5, g5, c6));
        });
    }

    /**
     * Warning countdown tick
     */
    public void playWarningTick() {
        if (isMuted) return;
        audioExecutor.execute(() -> {
            byte[] tick = generateSquareWave(980, 0.03f, 0.5f);
            playPcm(tick);
        });
    }

    /**
     * 8-bit Game Over descending sorrow tone
     */
    public void playGameOverSound() {
        if (isMuted) return;
        audioExecutor.execute(() -> {
            byte[] n1 = generateSquareWave(440, 0.15f, 0.8f);
            byte[] n2 = generateSquareWave(392, 0.15f, 0.8f);
            byte[] n3 = generateSquareWave(349, 0.15f, 0.8f);
            byte[] n4 = generateSquareWave(261, 0.35f, 0.9f);
            playPcm(concat(n1, n2, n3, n4));
        });
    }

    private byte[] generateSquareWave(int freqHz, float durationSec, float volume) {
        int numSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[numSamples * 2]; // 16-bit mono
        double periodSamples = (double) SAMPLE_RATE / freqHz;

        for (int i = 0; i < numSamples; i++) {
            // Envelope decay to prevent harsh clipping
            float env = 1.0f - ((float) i / numSamples);
            double phase = i % periodSamples;
            short sampleValue = (short) ((phase < periodSamples / 2.0 ? 1 : -1) * 32767 * volume * env);

            buffer[2 * i] = (byte) (sampleValue & 0xFF);
            buffer[2 * i + 1] = (byte) ((sampleValue >> 8) & 0xFF);
        }
        return buffer;
    }

    private byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] b : arrays) total += b.length;
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] b : arrays) {
            System.arraycopy(b, 0, result, offset, b.length);
            offset += b.length;
        }
        return result;
    }

    private void playPcm(byte[] pcmData) {
        try {
            AudioTrack track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(pcmData.length)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();

            track.write(pcmData, 0, pcmData.length);
            track.play();

            // Release after playback finishes
            int durationMs = (int) ((pcmData.length / 2.0 / SAMPLE_RATE) * 1000) + 150;
            mainHandler.postDelayed(() -> {
                try {
                    track.stop();
                    track.release();
                } catch (Exception ignored) {}
            }, durationMs);

        } catch (Exception ignored) {}
    }

    public void release() {
        audioExecutor.shutdown();
    }
}
