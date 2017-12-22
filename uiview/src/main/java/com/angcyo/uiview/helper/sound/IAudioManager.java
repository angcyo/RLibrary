package com.angcyo.uiview.helper.sound;

public interface IAudioManager<T extends IAudioEntity> {
    // ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public float getMasterVolume();
	public void setMasterVolume(final float pMasterVolume);

	public void add(final T pAudioEntity);
	public boolean remove(final T pAudioEntity);

	public void releaseAll();
}