package com.angcyo.uiview.helper.sound;

public abstract class BaseAudioEntity implements IAudioEntity {
    // ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final IAudioManager<? extends IAudioEntity> mAudioManager;

	protected float mLeftVolume = 1.0f;
	protected float mRightVolume = 1.0f;

	private boolean mReleased;

	// ===========================================================
	// Constructors
	// ===========================================================

	public BaseAudioEntity(final IAudioManager<? extends IAudioEntity> pAudioManager) {
		this.mAudioManager = pAudioManager;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean isReleased() {
		return this.mReleased;
	}

	protected IAudioManager<? extends IAudioEntity> getAudioManager() {
		this.assertNotReleased();

		return this.mAudioManager;
	}

	public float getActualLeftVolume(){
		this.assertNotReleased();

		return this.mLeftVolume * this.getMasterVolume();
	}

	public float getActualRightVolume(){
		this.assertNotReleased();

		return this.mRightVolume * this.getMasterVolume();
	}

	protected float getMasterVolume(){
		this.assertNotReleased();

		return this.mAudioManager.getMasterVolume();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected abstract void throwOnReleased();

	@Override
	public float getVolume(){
		this.assertNotReleased();

		return (this.mLeftVolume + this.mRightVolume) * 0.5f;
	}

	@Override
	public float getLeftVolume(){
		this.assertNotReleased();

		return this.mLeftVolume;
	}

	@Override
	public float getRightVolume(){
		this.assertNotReleased();

		return this.mRightVolume;
	}

	@Override
	public final void setVolume(final float pVolume){
		this.assertNotReleased();

		this.setVolume(pVolume, pVolume);
	}

	@Override
	public void setVolume(final float pLeftVolume, final float pRightVolume){
		this.assertNotReleased();

		this.mLeftVolume = pLeftVolume;
		this.mRightVolume = pRightVolume;
	}

	@Override
	public void onMasterVolumeChanged(final float pMasterVolume){
		this.assertNotReleased();
	}

	@Override
	public void play(){
		this.assertNotReleased();
	}

	@Override
	public void pause(){
		this.assertNotReleased();
	}

	@Override
	public void resume(){
		this.assertNotReleased();
	}

	@Override
	public void stop(){
		this.assertNotReleased();
	}

	@Override
	public void setLooping(final boolean pLooping){
		this.assertNotReleased();
	}

	@Override
	public void release(){
		this.assertNotReleased();

		this.mReleased = true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected void assertNotReleased(){
		if(this.mReleased) {
			this.throwOnReleased();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}