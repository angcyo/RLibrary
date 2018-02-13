package com.angcyo.uiview.helper.sound;

import android.media.MediaPlayer;

public class Music extends BaseAudioEntity {
    // ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private MediaPlayer mMediaPlayer;

	// ===========================================================
	// Constructors
	// ===========================================================

	Music(final MusicManager pMusicManager, final MediaPlayer pMediaPlayer) {
		super(pMusicManager);

		this.mMediaPlayer = pMediaPlayer;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean isPlaying() {
		this.assertNotReleased();

		return this.mMediaPlayer.isPlaying();
	}

	public MediaPlayer getMediaPlayer() {
		this.assertNotReleased();

		return this.mMediaPlayer;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected MusicManager getAudioManager() {
		return (MusicManager) super.getAudioManager();
	}

	@Override
	protected void throwOnReleased() {
		// throw new MusicReleasedException();
	}

	@Override
	public void play() {
		super.play();

		this.mMediaPlayer.start();
	}

	@Override
	public void stop() {
		super.stop();

		this.mMediaPlayer.stop();
	}

	@Override
	public void resume() {
		super.resume();

		this.mMediaPlayer.start();
	}

	@Override
	public void pause() {
		super.pause();

		this.mMediaPlayer.pause();
	}

	@Override
	public void setLooping(final boolean pLooping) {
		super.setLooping(pLooping);

		this.mMediaPlayer.setLooping(pLooping);
	}

	@Override
	public void setVolume(final float pLeftVolume, final float pRightVolume) {
		super.setVolume(pLeftVolume, pRightVolume);

		final float masterVolume = this.getAudioManager().getMasterVolume();
		final float actualLeftVolume = pLeftVolume * masterVolume;
		final float actualRightVolume = pRightVolume * masterVolume;

		this.mMediaPlayer.setVolume(actualLeftVolume, actualRightVolume);
	}

	@Override
	public void onMasterVolumeChanged(final float pMasterVolume) {
		this.setVolume(this.mLeftVolume, this.mRightVolume);
	}

	@Override
	public void release() {
		this.assertNotReleased();

		this.mMediaPlayer.release();
		this.mMediaPlayer = null;

		this.getAudioManager().remove(this);

		super.release();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void seekTo(final int pMilliseconds) {
		this.assertNotReleased();

		this.mMediaPlayer.seekTo(pMilliseconds);
	}

	public void setOnCompletionListener(
			final MediaPlayer.OnCompletionListener pOnCompletionListener) {
		this.assertNotReleased();

		this.mMediaPlayer.setOnCompletionListener(pOnCompletionListener);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}