package com.pseudoblue.greentree;

import java.util.Random;

public abstract class Event {
	
	protected long mWaitPeriod = 0;
	protected long mEventLength = 0;
	
	protected long mLastOccurrence = 0;
	protected long mEventStart = 0;
	
	protected boolean mIsRunning = false;
	
	protected long mTimeLeft = 0;
	
	protected abstract int getOccurrenceRangeMax();
	protected abstract int getOccurrenceRangeMin();
	protected abstract int getEventLengthMax();
	protected abstract int getEventLengthMin();
	
	Event(TreeView treeView, TreeGame treeGame){
		
	}
	
	public boolean isRunning() {
		return mIsRunning;
	}
	
	public void updatePhysics (long elapsed) {
		long timePassed;
		long effectiveElapsed;
		long now = System.currentTimeMillis();

		if (mTimeLeft > 0) {
			// We are restoring an event that was happenning offline.			
			if (elapsed >= mTimeLeft) {
				// [====|====]---|
				effectiveElapsed = elapsed - mTimeLeft;
				affect(effectiveElapsed);
				mLastOccurrence = now - elapsed;
			}
			else {
				// [====|====|==]
				affect(elapsed);
				mEventLength = mTimeLeft - elapsed;
				// Event is in progress.
				startEvent(now);
			}
			mTimeLeft = 0;
		}
		
		else {
			if (!mIsRunning) {
				if (mLastOccurrence == 0) {
					mLastOccurrence = now;
					calcWaitPeriod();
					calcEventLength();
					return;
				}
				
				timePassed = now - mLastOccurrence;
				if (timePassed > mWaitPeriod) {
					if (timePassed > (mWaitPeriod + mEventLength)){
						// Event has completely passed. Update values based on mEventLength.
						affect(mEventLength);
						mLastOccurrence += mWaitPeriod + mEventLength;
						finishEvent();
					}
					else {
						// Event is in progress.
						if (!mIsRunning) {
							startEvent(now);
						}
					}
				}
			}
			else {
				timePassed = now - mEventStart;
				affect(elapsed);
				if (timePassed > mEventLength) {
					// Event is over
					finishEvent();
				}
			}
		}
	}
	
	public void setLastOccurrence(long lo) {
		setLastOccurrence(lo, false);
	}
	
	public void setLastOccurrence(long lo, boolean waking) {
		mLastOccurrence = lo;
		calcWaitPeriod();
		calcEventLength();
		
		if (waking) {
			long timePassed = System.currentTimeMillis() - mLastOccurrence;
			if (mWaitPeriod < timePassed) {
				int extension = new Random().nextInt(10000) + 5000;
				mWaitPeriod = timePassed + extension;
			}
		}
	}
	
	public void setTimeLeft(long tl) {
		mTimeLeft = tl;
	}
	
	protected void affect(long timePeriod){
		
	}
	
	protected void calcWaitPeriod() {
		int maxvalue = getOccurrenceRangeMax() - getOccurrenceRangeMin();
		mWaitPeriod = new Random().nextInt(maxvalue);
		mWaitPeriod += getOccurrenceRangeMin();
	}
	
	protected void calcEventLength() {
		int maxvalue = getEventLengthMax() - getEventLengthMin();
		mEventLength = new Random().nextInt(maxvalue);
		mEventLength += getEventLengthMin();
	}
	
	public void startEvent(long now) {
		mLastOccurrence = now;
		mIsRunning = true;
		mEventStart = now;
	}
	
	public void finishEvent() {
		mIsRunning = false; 
		calcEventLength();
		calcWaitPeriod();
	}
	
	public void trigger(){
		if (!mIsRunning) {
			long now = System.currentTimeMillis();
			startEvent(now);
		}
	}
	
	public long getTimeLeft() {
		long now = System.currentTimeMillis();
		if (mIsRunning) {
			return mEventLength - (now - mLastOccurrence);
		}
		else {
			return 0;
		}
	}
	
	public long getLastOccurrence(){
		return mLastOccurrence;
	}
	
	public void init() {
		mLastOccurrence = 0;
		mIsRunning = false;
		mTimeLeft = 0;
		mEventStart = 0;
	}
	
	public long getEventStart() {
		return mEventStart;
	}
	
	public void setEventStart(long es) {
		mEventStart = es;
	}
	
}


