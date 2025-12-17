import javax.sound.sampled.*;
import java.io.*;

public class SoundManager {
    private static SoundManager instance;
    private Clip openingMusic;
    private Clip gameplayMusic;
    private Clip clickSound;
    private Clip successSound;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;

    // Paths untuk audio files (taruh file sound kamu di folder sounds/)
    private static final String OPENING_MUSIC_PATH = "sounds/opening.wav";
    private static final String GAMEPLAY_MUSIC_PATH = "sounds/gameplay.wav";
    private static final String CLICK_SOUND_PATH = "sounds/click.wav";
    private static final String SUCCESS_SOUND_PATH = "sounds/success.wav";

    private SoundManager() {
        // Constructor private untuk singleton
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Load sounds from files
    public void loadSounds() {
        try {
            System.out.println("Loading sounds from files...");

            // Load opening music
            openingMusic = loadSound(OPENING_MUSIC_PATH);
            if (openingMusic != null) {
                System.out.println("✓ Opening music loaded");
            } else {
                System.out.println("✗ Opening music not found, using fallback");
                openingMusic = generateFallbackMusic(10, 220);
            }

            // Load gameplay music
            gameplayMusic = loadSound(GAMEPLAY_MUSIC_PATH);
            if (gameplayMusic != null) {
                System.out.println("✓ Gameplay music loaded");
            } else {
                System.out.println("✗ Gameplay music not found, using fallback");
                gameplayMusic = generateFallbackMusic(8, 440);
            }

            // Load click sound
            clickSound = loadSound(CLICK_SOUND_PATH);
            if (clickSound != null) {
                System.out.println("✓ Click sound loaded");
            } else {
                System.out.println("✗ Click sound not found, using fallback");
                clickSound = generateClickSound();
            }

            // Load success sound
            successSound = loadSound(SUCCESS_SOUND_PATH);
            if (successSound != null) {
                System.out.println("✓ Success sound loaded");
            } else {
                System.out.println("✗ Success sound not found, using fallback");
                successSound = generateSuccessSound();
            }

            System.out.println("Sound loading complete!");

        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load a sound file (supports WAV, AU, AIFF)
    private Clip loadSound(String path) {
        try {
            File soundFile = new File(path);

            if (!soundFile.exists()) {
                return null;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioStream.getFormat();

            // Convert to PCM if needed
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat pcmFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        16,
                        format.getChannels(),
                        format.getChannels() * 2,
                        format.getSampleRate(),
                        false
                );
                audioStream = AudioSystem.getAudioInputStream(pcmFormat, audioStream);
                format = pcmFormat;
            }

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);
            setVolume(clip, musicVolume);

            return clip;

        } catch (Exception e) {
            return null;
        }
    }

    // Fallback sounds jika file tidak ditemukan
    private Clip generateFallbackMusic(int durationSeconds, int baseFreq) {
        try {
            int sampleRate = 44100;
            byte[] buffer = new byte[sampleRate * durationSeconds];

            for (int i = 0; i < buffer.length; i++) {
                double time = i / (double) sampleRate;
                double wave = Math.sin(2 * Math.PI * baseFreq * time);
                buffer[i] = (byte) (wave * 50);
            }

            return createClipFromBytes(buffer, sampleRate);
        } catch (Exception e) {
            return null;
        }
    }

    private Clip generateClickSound() {
        try {
            int sampleRate = 44100;
            byte[] buffer = new byte[sampleRate / 10];

            for (int i = 0; i < buffer.length; i++) {
                double time = i / (double) sampleRate;
                double envelope = Math.exp(-30 * time);
                double wave = Math.sin(2 * Math.PI * 800 * time);
                buffer[i] = (byte) (wave * envelope * 100);
            }

            return createClipFromBytes(buffer, sampleRate);
        } catch (Exception e) {
            return null;
        }
    }

    private Clip generateSuccessSound() {
        try {
            int sampleRate = 44100;
            byte[] buffer = new byte[sampleRate];
            double[] notes = {523, 659, 784};

            for (int i = 0; i < buffer.length; i++) {
                double time = i / (double) sampleRate;
                int noteIndex = Math.min((int)(time * 3), 2);
                double freq = notes[noteIndex];
                double envelope = Math.exp(-3 * (time % 0.333));
                double wave = Math.sin(2 * Math.PI * freq * time);
                buffer[i] = (byte) (wave * envelope * 80);
            }

            return createClipFromBytes(buffer, sampleRate);
        } catch (Exception e) {
            return null;
        }
    }

    private Clip createClipFromBytes(byte[] buffer, int sampleRate) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip clip = (Clip) AudioSystem.getLine(info);

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        AudioInputStream ais = new AudioInputStream(bais, format, buffer.length);

        clip.open(ais);
        setVolume(clip, musicVolume);

        return clip;
    }

    private void setVolume(Clip clip, float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10) * 20);
                dB = Math.max(gainControl.getMinimum(), Math.min(dB, gainControl.getMaximum()));
                gainControl.setValue(dB);
            } catch (Exception e) {
                System.err.println("Could not set volume");
            }
        }
    }

    public void playOpeningMusic() {
        if (!musicEnabled || openingMusic == null) return;
        stopAllMusic();
        openingMusic.setFramePosition(0);
        openingMusic.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void playGameplayMusic() {
        if (!musicEnabled || gameplayMusic == null) return;
        stopAllMusic();
        gameplayMusic.setFramePosition(0);
        gameplayMusic.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void playClick() {
        if (!sfxEnabled || clickSound == null) return;
        clickSound.setFramePosition(0);
        clickSound.start();
    }

    public void playSuccess() {
        if (!sfxEnabled || successSound == null) return;
        successSound.setFramePosition(0);
        successSound.start();
    }

    public void stopAllMusic() {
        if (openingMusic != null && openingMusic.isRunning()) {
            openingMusic.stop();
        }
        if (gameplayMusic != null && gameplayMusic.isRunning()) {
            gameplayMusic.stop();
        }
    }

    public void stopAll() {
        stopAllMusic();
        if (clickSound != null && clickSound.isRunning()) {
            clickSound.stop();
        }
        if (successSound != null && successSound.isRunning()) {
            successSound.stop();
        }
    }

    public void fadeOut(int durationMs) {
        new Thread(() -> {
            try {
                Clip currentClip = null;
                if (openingMusic != null && openingMusic.isRunning()) {
                    currentClip = openingMusic;
                } else if (gameplayMusic != null && gameplayMusic.isRunning()) {
                    currentClip = gameplayMusic;
                }

                if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float startVolume = gainControl.getValue();
                    float minVolume = gainControl.getMinimum();

                    int steps = 50;
                    int delay = durationMs / steps;

                    for (int i = 0; i < steps; i++) {
                        float newVolume = startVolume - ((startVolume - minVolume) * i / steps);
                        gainControl.setValue(newVolume);
                        Thread.sleep(delay);
                    }

                    currentClip.stop();
                    gainControl.setValue(startVolume);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!musicEnabled) {
            stopAllMusic();
        }
    }

    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSFXEnabled() {
        return sfxEnabled;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (openingMusic != null) setVolume(openingMusic, musicVolume);
        if (gameplayMusic != null) setVolume(gameplayMusic, musicVolume);
    }

    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
        if (clickSound != null) setVolume(clickSound, sfxVolume);
        if (successSound != null) setVolume(successSound, sfxVolume);
    }
}