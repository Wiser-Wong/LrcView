package com.wiser.lrc;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import android.widget.Toast;

/**
 * @author Wiser
 * 
 *         歌词View
 */
public class LrcView extends View {

	private static final int	UP_DELAYED				= 111;					// 抬起手指延迟消失时间轴

	private final int			TIME_DELAYED_DISMISS	= 2500;					// 时间轴延时消失的时间

	private List<LrcBean>		lrcBeans;										// 歌词集合

	private Paint				lrcPaint;										// 歌词画笔

	private Paint				timeLinePaint;									// 时间轴画笔

	private Paint				timePaint;										// 时间画笔

	private Bitmap				playBitmap;										// 播放按钮

	private int					dfLrcColor				= Color.GRAY;			// 歌词默认颜色

	private int					playLrcColor			= Color.YELLOW;			// 歌词播放颜色

	private int					timeColor				= Color.WHITE;			// 时间颜色

	private int					timeLineColor			= Color.GRAY;			// 时间线颜色

	private float				lrcTextSize				= 36;					// 歌词默认文字大小

	private float				timeTextSize			= 30;					// 时间默认文字大小

	private float				lrcPadding;										// 歌词间距

	private int					limitLines				= 9;					// 限制显示的行数最小

	private String				emptyLrc;										// 未查询到歌词文案

	private static final int	NORMAL					= 0;					// 默认高亮展示播放中歌词

	private static final int	KARAOKE					= 1;					// 卡拉OK模式

	private int					lrcMode					= NORMAL;				// 歌词模式 默认正常

	private OverScroller		mScroller;										// 滚动组件

	private Paint.FontMetrics	fontMetrics;									// 用于计算歌词高度

	private float				lrcTextH;										// 单行歌词高度

	private boolean				isDragging;										// 是否拖拽

	private boolean				isTimeShow;										// 是否时间轴显示

	private boolean				isAlreadyClickPlay;								// 是否已经点击播放按钮了

	private float				mOffset;										// 偏移量 用于使画布位置发生偏移变化

	private int					lastPosition;									// 用于比较当前位置 不同处理自动滚动动画

	private VelocityTracker		mVelocityTracker;								// 速度跟踪

	private int					mMaximumFlingVelocity, mMinimumFlingVelocity;

	private float				downY;											// 主动触摸滑动时按下的Y轴位置

	private LrcHandler			lrcHandler;

	private SeekListener		seekListener;									// 设置播放进度监听

	private long				maxDuration;									// 总时间

	private long				currentDuration;								// 当前时间

	public LrcView(Context context) {
		super(context);
		init(context, null);
	}

	public LrcView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	// 初始化
	private void init(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
		emptyLrc = typedArray.getString(R.styleable.LrcView_lrcEmptyLrcText);
		playLrcColor = typedArray.getColor(R.styleable.LrcView_lrcPlayColor, getResources().getColor(android.R.color.holo_blue_dark));
		dfLrcColor = typedArray.getColor(R.styleable.LrcView_lrcColor, getResources().getColor(android.R.color.darker_gray));
		timeColor = typedArray.getColor(R.styleable.LrcView_lrcTimeTextColor, getResources().getColor(android.R.color.white));
		timeLineColor = typedArray.getColor(R.styleable.LrcView_lrcTimeLineColor, getResources().getColor(android.R.color.darker_gray));
		limitLines = typedArray.getInt(R.styleable.LrcView_lrcLimitLines, limitLines);
		lrcTextSize = typedArray.getDimension(R.styleable.LrcView_lrcTextSize, lrcTextSize);
		timeTextSize = typedArray.getDimension(R.styleable.LrcView_lrcTimeTextSize, timeTextSize);
		// 播放按钮资源id
		int playSrcId = typedArray.getResourceId(R.styleable.LrcView_lrcPlaySrc, -1);
		lrcMode = typedArray.getInt(R.styleable.LrcView_lrcMode, NORMAL);
		typedArray.recycle();

		if (emptyLrc == null || "".equals(emptyLrc)) emptyLrc = "暂无歌词";

		initPaint();

		if (playSrcId > 0) {
			playBitmap = BitmapFactory.decodeResource(getResources(), playSrcId);
		}

		fontMetrics = lrcPaint.getFontMetrics();

		mScroller = new OverScroller(context);

		mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

		mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

		lrcHandler = new LrcHandler(this);
	}

	// 初始化画笔
	private void initPaint() {
		lrcPaint = new Paint();
		lrcPaint.setStyle(Paint.Style.FILL);
		lrcPaint.setAntiAlias(true);
		lrcPaint.setColor(dfLrcColor);
		lrcPaint.setTextSize(lrcTextSize);
		lrcPaint.setTextAlign(Paint.Align.CENTER);

		timeLinePaint = new Paint();
		timeLinePaint.setStyle(Paint.Style.FILL);
		timeLinePaint.setAntiAlias(true);
		timeLinePaint.setColor(timeLineColor);
		timeLinePaint.setTextSize(timeTextSize);
		timeLinePaint.setTextAlign(Paint.Align.CENTER);
		timeLinePaint.setStrokeWidth(1);

		timePaint = new Paint();
		timePaint.setStyle(Paint.Style.FILL);
		timePaint.setAntiAlias(true);
		timePaint.setColor(timeColor);
		timePaint.setTextSize(timeTextSize);
		timePaint.setTextAlign(Paint.Align.CENTER);
	}

	// 设置歌词
	public void setLrc(String lrc) {
		lrcBeans = LrcParseTool.parseLrc(lrc);
		System.out.println("--------->>" + lrcBeans.get(lrcBeans.size() - 1).endTime);
	}

	// 设置总时间
	public void setMaxDuration(long maxDuration) {
		this.maxDuration = maxDuration;
	}

	// 设置当前时间
	public void setCurrentDuration(long currentDuration) {
		this.currentDuration = currentDuration;
	}

	// 判断歌词是否为空
	private boolean isEmptyLrc() {
		return lrcBeans == null || lrcBeans.size() == 0;
	}

	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();

		// 画歌词
		canvasLrc(canvas);

		if (isDragging) {
			// 画线
			canvasTimeLine(canvas);
		}

		canvas.restore();

		// 判断没有歌词不重新绘制
		if (!isEmptyLrc()) postInvalidate();
	}

	// 画歌词
	private void canvasLrc(Canvas canvas) {
		float width = getMeasuredWidth();
		float height = getMeasuredHeight();
		lrcTextH = fontMetrics.bottom - fontMetrics.top;
		float lrcBaselineY = height / 2 + fontMetrics.bottom;

		// 计算距离
		calculatePadding(height, lrcTextH);

		// 画无歌词情况
		if (isEmptyLrc()) {
			canvas.drawText(emptyLrc, width / 2, lrcBaselineY, lrcPaint);
			return;
		}

		// 获取当前播放的歌词条目
		int currentPosition = getCurrentPosition();

		// 计算画布偏移量
		canvas.translate(0, -mOffset);

		// 播放中歌词显示模式
		if (lrcMode == KARAOKE) {// 播放歌词卡拉OK模式
			// 默认歌词颜色
			lrcPaint.setColor(dfLrcColor);
			for (int i = 0; i < lrcBeans.size(); i++) {
				LrcBean lrcBean = lrcBeans.get(i);
				if (lrcBean == null || lrcBean.lrc == null || "".equals(lrcBean.lrc)) break;
				canvas.drawText(lrcBean.lrc, width / 2, calculateOffset(i) + lrcBaselineY, lrcPaint);
			}
			// 播放中歌词颜色
			lrcPaint.setColor(playLrcColor);
			LrcBean lrcBean = lrcBeans.get(currentPosition);
			// 歌词宽度
			float lrcTextW = lrcPaint.measureText(lrcBean.lrc);
			// 计算一行歌词播放中执行的宽度 来创建歌词Bitmap
			int goWidth;
			if (currentPosition == lrcBeans.size() - 1) {
				goWidth = (int) ((currentDuration - lrcBean.startTime) * 1.0 / (maxDuration - lrcBean.startTime) * lrcTextW);
			} else {
				goWidth = (int) ((currentDuration - lrcBean.startTime) * 1.0 / (lrcBean.endTime - lrcBean.startTime) * lrcTextW);
			}
			if (goWidth > 0) {
				Bitmap textBitmap = Bitmap.createBitmap(goWidth, (int) (lrcTextH + lrcPadding / 2), Bitmap.Config.ARGB_8888);
				Canvas textCanvas = new Canvas(textBitmap);
				textCanvas.drawText(lrcBean.lrc, lrcTextW / 2, lrcTextH, lrcPaint);
				canvas.drawBitmap(textBitmap, (width - lrcTextW) / 2, lrcBaselineY + lrcTextH * (currentPosition - 1) + lrcPadding * currentPosition, null);
				textBitmap.recycle();
			}
		} else {// 默认模式 直接展示高亮播放着歌词
			// 画歌词
			for (int i = 0; i < lrcBeans.size(); i++) {
				LrcBean lrcBean = lrcBeans.get(i);
				if (lrcBean == null || lrcBean.lrc == null || "".equals(lrcBean.lrc)) break;
				float y = lrcTextH * i + lrcBaselineY;
				if (i == currentPosition) {
					lrcPaint.setColor(playLrcColor);
				} else {
					lrcPaint.setColor(dfLrcColor);
				}
				canvas.drawText(lrcBean.lrc, width / 2, y + lrcPadding * i, lrcPaint);
			}
		}

		// 判断滚动时机 当前播放歌词位置跟上次不同时 并且 没有手动拖动 时候进行自动滚动
		if (lastPosition != currentPosition && !isDragging) {
			scrollToPosition(currentPosition);
		}
		lastPosition = currentPosition;
	}

	/**
	 * 画时间轴
	 *
	 * @param canvas
	 */
	private void canvasTimeLine(Canvas canvas) {
		if (isEmptyLrc()) return;
		// 计算拖拽到歌词位置
		int draggingPosition = calculateDraggingPosition();
		int playWidth = 0;
		if (playBitmap != null) playWidth = playBitmap.getWidth();
		// 时间文本
		String time = getMinuteSecondStrForLong(lrcBeans.get(draggingPosition).startTime);
		// 时间文本宽度
		float timeWidth = timePaint.measureText(time);
		// 画时间
		canvas.drawText(time, timeWidth / 2 + 20, (float) getMeasuredHeight() / 2 + (float) getTextValue(timePaint, time)[1] / 2 + mOffset, timePaint);
		// 画左线
		canvas.drawLine(timeWidth * 3 / 2, (float) getMeasuredHeight() / 2 + mOffset, timeWidth * 3 / 2 + 200, (float) getMeasuredHeight() / 2 + mOffset, timeLinePaint);
		// 画右线
		canvas.drawLine(getMeasuredWidth() - Math.max(playWidth, timeWidth) - 240, (float) getMeasuredHeight() / 2 + mOffset, getMeasuredWidth() - Math.max(playWidth, timeWidth) - 40,
				(float) getMeasuredHeight() / 2 + mOffset, timeLinePaint);

		// 画播放按钮
		if (playBitmap != null) canvas.drawBitmap(playBitmap, getMeasuredWidth() - playBitmap.getWidth() - 20, (float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2) + mOffset, null);
	}

	// 获取文字宽高
	private int[] getTextValue(Paint paint, String text) {
		int[] values = new int[2];
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		values[0] = rect.width();
		values[1] = rect.height();
		return values;
	}

	/**
	 * 根据long类型转分秒类型字符串
	 *
	 * @param mill
	 * @return
	 */
	public static String getMinuteSecondStrForLong(long mill) {
		Date date = new Date(mill);
		String dateStr = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.CHINA);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			// 进行格式化
			dateStr = sdf.format(date);
			return dateStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateStr;
	}

	@Override public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mOffset = mScroller.getCurrY();
			invalidate();
		}
	}

	// 自动滚动到当前播放位置
	private void scrollToPosition(int linePosition) {
		float scrollY = calculateOffset(linePosition);// 将要滚动的一行的偏移量
		final ValueAnimator animator = ValueAnimator.ofFloat(mOffset, scrollY);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override public void onAnimationUpdate(ValueAnimator animation) {
				mOffset = (float) animation.getAnimatedValue();
				invalidate();
			}
		});
		animator.setDuration(300);
		animator.start();
	}

	// 计算Padding
	private void calculatePadding(float height, float lrcHeight) {
		int maxLines = (int) (height / lrcHeight);
		if (limitLines > maxLines) {
			limitLines = maxLines;
		}
		lrcPadding = (height - lrcHeight * limitLines) / (limitLines + 1);
	}

	// 计算偏移量
	private float calculateOffset(int position) {
		return position * (lrcTextH + lrcPadding);
	}

	// 计算拖拽到歌词位置
	private int calculateDraggingPosition() {
		int draggingPosition = (int) (mOffset / (lrcPadding + lrcTextH));
		if (draggingPosition < 0) {
			draggingPosition = 0;
		}
		if (draggingPosition > lrcBeans.size() - 1) {
			draggingPosition = lrcBeans.size() - 1;
		}
		return draggingPosition;
	}

	// 获取当前播放的歌词条目
	private int getCurrentPosition() {
		int currentPosition = 0;
		if (lrcBeans == null || lrcBeans.size() == 0) return currentPosition;
		long currentMillis = currentDuration;
		if (currentMillis < lrcBeans.get(0).startTime) {
			return currentPosition;
		}
		if (currentMillis > lrcBeans.get(lrcBeans.size() - 1).startTime) {
			return lrcBeans.size() - 1;
		}
		for (int i = 0; i < lrcBeans.size(); i++) {
			if (currentMillis >= lrcBeans.get(i).startTime && currentMillis < lrcBeans.get(i).endTime) {
				return i;
			}
		}
		return currentPosition;
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		if (isEmptyLrc()) {
			return super.onTouchEvent(event);
		}
		// 速度跟踪
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lrcHandler.removeMessages(UP_DELAYED);
				isDragging = true;
				downY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float moveY = event.getY() - downY;
				if (isDragging) {
					if (lrcBeans != null && lrcBeans.size() > 0) {
						float maxHeight = calculateOffset(lrcBeans.size() - 1);
						if (mOffset < 0 || mOffset > maxHeight) {
							moveY /= 3.5f;
						}
					}
					mOffset -= moveY;
					downY = event.getY();
					invalidate();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:

				// 只有时间轴显示的时候才可以点击播放
				if (isTimeShow) play(event.getX(), event.getY());

				handleActionUp();
				break;
		}
		return true;
	}

	// 处理按下播放按钮控件逻辑
	private void play(float upX, float upY) {
		if (playBitmap == null) return;
		if (upX >= (getMeasuredWidth() - playBitmap.getWidth() - 20) && upX <= (getMeasuredWidth() - 20) && upY >= ((float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2))
				&& upY <= ((float) (getMeasuredHeight() / 2 + playBitmap.getHeight() / 2))) {
			Toast.makeText(getContext(), "播放", Toast.LENGTH_SHORT).show();
			// 点击播放后立即消失时间轴 先移除自动消失时间轴Handler 然后将时间轴显示设置false 再讲是否拖动设置false 为了快速消失时间轴
			lrcHandler.removeMessages(UP_DELAYED);
			isAlreadyClickPlay = true;
			isTimeShow = false;
			isDragging = false;
			// 播放到具体位置音乐
			int draggingPosition = calculateDraggingPosition();
			if (lrcBeans != null && draggingPosition < lrcBeans.size()) {
				if (seekListener != null) {
					seekListener.seekToDuration(this, lrcBeans.get(draggingPosition).startTime);
				}
			}
		}
	}

	// 处理抬起触摸事件
	private void handleActionUp() {

		// 处理播放按钮点击判断
		if (!isAlreadyClickPlay) isTimeShow = true;
		else isAlreadyClickPlay = false;
		// 延时处理时间轴消失
		lrcHandler.sendEmptyMessageDelayed(UP_DELAYED, TIME_DELAYED_DISMISS);

		// 上越界
		if (mOffset < 0) {
			scrollToPosition(0);
			return;
		}

		// 下越界
		if (mOffset > calculateOffset(lrcBeans.size() - 1)) {
			scrollToPosition(lrcBeans.size() - 1);
			return;
		}

		// 处理速度
		mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
		float YVelocity = mVelocityTracker.getYVelocity();
		float absYVelocity = Math.abs(YVelocity);
		if (absYVelocity > mMinimumFlingVelocity) {
			mScroller.fling(0, (int) mOffset, 0, (int) (-YVelocity), 0, 0, 0, (int) calculateOffset(lrcBeans.size() - 1), 0, (int) lrcTextH);
			postInvalidate();
		}
	}

	private static class LrcHandler extends Handler {

		WeakReference<LrcView> reference;

		LrcHandler(LrcView lrcView) {
			reference = new WeakReference<>(lrcView);
		}

		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (reference != null && reference.get() != null) {
				// 延迟更新时间轴
				if (msg.what == UP_DELAYED) {
					removeMessages(UP_DELAYED);
					reference.get().isTimeShow = false;
					reference.get().isDragging = false;
					reference.get().postInvalidate();
				}
			}
		}
	}

	@Override protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		detach();
	}

	private void detach() {
		if (lrcBeans != null) lrcBeans.clear();
		lrcBeans = null;
		if (lrcHandler != null) {
			lrcHandler.removeMessages(UP_DELAYED);
			if (lrcHandler.reference != null) {
				lrcHandler.reference.clear();
				lrcHandler.reference = null;
			}
			lrcHandler = null;
		}
		if (playBitmap != null) {
			playBitmap.recycle();
			playBitmap = null;
		}
		lrcPaint = null;
		timeLinePaint = null;
		timePaint = null;
		mScroller = null;
		fontMetrics = null;
		mVelocityTracker = null;
	}

	public void setSeekListener(SeekListener seekListener) {
		this.seekListener = seekListener;
	}

	public interface SeekListener {

		void seekToDuration(LrcView lrcView, long duration);

	}
}
