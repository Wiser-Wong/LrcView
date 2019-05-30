package com.wiser.lrc;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
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

	private static final int	UP_DELAYED					= 111;						// 抬起手指延迟消失时间轴

	private final int			TIME_DELAYED_DISMISS		= 2000;						// 时间轴延时消失的时间

	private List<LrcBean>		lrcBeans;												// 歌词集合

	private Paint				lrcPaint;												// 歌词画笔

	private Paint				lrcTranslatePaint;										// 音译歌词画笔

	private Paint				timeLeftLinePaint;										// 左时间轴画笔

	private Paint				timeRightLinePaint;										// 右时间轴画笔

	private Paint				timePaint;												// 时间画笔

	private Bitmap				playBitmap;												// 播放按钮

	private int					dfLrcColor					= Color.GRAY;				// 歌词默认颜色

	private int					playLrcColor				= Color.YELLOW;				// 歌词播放颜色

	private int					translateLrcColor			= dfLrcColor;				// 音译歌词颜色

	private int					timeColor					= Color.WHITE;				// 时间颜色

	private int					timeLineColor				= Color.GRAY;				// 时间线颜色

	private float				timeLinePadding				= 20;						// 线与时间距离

	private float				timeLineLength				= 180;						// 线长度

	private float				timeLineHeight				= 1;						// 线高度

	private float				lrcTextSize					= 36;						// 歌词默认文字大小

	private float				timeTextSize				= 30;						// 时间默认文字大小

	private float				lrcPadding;												// 歌词间距

	private int					limitLines					= 9;						// 限制显示的行数最小

	private int					heightLightItems			= 3;						// 限制显示的行数最小

	private String				emptyLrc;												// 未查询到歌词文案

	private static final int	NORMAL						= 0;						// 默认高亮展示播放中歌词

	private static final int	KARAOKE						= 1;						// 卡拉OK模式

	private int					lrcMode						= NORMAL;					// 歌词模式 默认正常

	private final int			LEFT						= 2;						// 歌词左侧显示时间

	private final int			RIGHT						= 3;						// 歌词右侧显示时间

	private int					lrcTimeDirectionMode		= LEFT;						// 时间左侧右侧位置

	private final int			CANVAS_LEFT_LINE_COLOR		= 4;						// 绘制左线Color

	private final int			CANVAS_LEFT_LINE_DRAWABLE	= 5;						// 绘制左线Drawable

	private int					lrcLeftLineMode				= CANVAS_LEFT_LINE_COLOR;

	private final int			CANVAS_RIGHT_LINE_COLOR		= 6;						// 绘制右线Color

	private final int			CANVAS_RIGHT_LINE_DRAWABLE	= 7;						// 绘制右线Drawable

	private int					lrcRightLineMode			= CANVAS_RIGHT_LINE_COLOR;

	private boolean				isTranslateLrcDrawColor;								// 是否音译歌词同步歌词颜色模式

	private OverScroller		mScroller;												// 滚动组件

	private Paint.FontMetrics	fontMetrics;											// 用于计算歌词高度

	private float				lrcTextH;												// 单行歌词高度

	private float				lrcTextRealH;											// 单行歌词高度

	private boolean				isDragging;												// 是否拖拽

	private boolean				isTimeShow;												// 是否时间轴显示

	private boolean				isAlreadyClickPlay;										// 是否已经点击播放按钮了

	private float				mOffset;												// 偏移量 用于使画布位置发生偏移变化

	private int					lastPosition;											// 用于比较当前位置 不同处理自动滚动动画

	private VelocityTracker		mVelocityTracker;										// 速度跟踪

	private int					mMaximumFlingVelocity, mMinimumFlingVelocity;

	private float				downY;													// 主动触摸滑动时按下的Y轴位置

	private LrcHandler			lrcHandler;

	private SeekListener		seekListener;											// 设置播放进度监听

	private long				maxDuration;											// 总时间

	private long				currentDuration;										// 当前时间

	private LinearGradient		linearGradientLeftLine, linearGradientRightLine;		// 时间线渐变组件

	private int					lineStartColor, lineCenterColor, lineEndColor;			// 线渐变颜色值

	private boolean				isDashLine;												// 是否虚线

	private Drawable			leftLineDrawable, rightLineDrawable;					// 左右时间线图片

	private int					lrcMaxLength				= 400;						// 歌词最大长度

	private boolean				isFirstAddData				= true;						// 第一次添加新的Padding

	private boolean				isFirstAddTextLineList		= true;						// 第一次赋值单行歌词折行集合

	private int					currentAlpha;											// 当前播放位置透明度 为了同步卡拉OK模式滑动透明度变化

	private Rect				rect;													// 为了获取歌词宽度矩阵

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
		setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 关闭硬件加速
		this.setWillNotDraw(false); // 调用此方法后，才会执行 onDraw(Canvas) 方法

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
		emptyLrc = typedArray.getString(R.styleable.LrcView_lrcEmptyLrcText);
		playLrcColor = typedArray.getColor(R.styleable.LrcView_lrcPlayColor, getResources().getColor(android.R.color.holo_blue_dark));
		dfLrcColor = typedArray.getColor(R.styleable.LrcView_lrcColor, getResources().getColor(android.R.color.darker_gray));
		timeColor = typedArray.getColor(R.styleable.LrcView_lrcTimeTextColor, getResources().getColor(android.R.color.white));
		timeLineColor = typedArray.getColor(R.styleable.LrcView_lrcTimeLineColor, getResources().getColor(android.R.color.darker_gray));
		limitLines = typedArray.getInt(R.styleable.LrcView_lrcLimitLines, limitLines);
		lrcTextSize = typedArray.getDimension(R.styleable.LrcView_lrcTextSize, lrcTextSize);
		timeTextSize = typedArray.getDimension(R.styleable.LrcView_lrcTimeTextSize, timeTextSize);
		timeLinePadding = typedArray.getDimension(R.styleable.LrcView_lrcTimeLinePadding, timeLinePadding);
		timeLineLength = typedArray.getDimension(R.styleable.LrcView_lrcTimeLineLength, timeLineLength);
		timeLineHeight = typedArray.getDimension(R.styleable.LrcView_lrcTimeLineHeight, timeLineHeight);
		lineStartColor = typedArray.getColor(R.styleable.LrcView_lrcLineStartColor, 0);
		lineCenterColor = typedArray.getColor(R.styleable.LrcView_lrcLineCenterColor, 0);
		lineEndColor = typedArray.getColor(R.styleable.LrcView_lrcLineEndColor, 0);
		// 播放按钮资源id
		int playSrcId = typedArray.getResourceId(R.styleable.LrcView_lrcPlaySrc, -1);
		int leftLineSrcId = typedArray.getResourceId(R.styleable.LrcView_lrcLeftLineSrc, -1);
		int rightLineSrcId = typedArray.getResourceId(R.styleable.LrcView_lrcRightLineSrc, -1);
		lrcMode = typedArray.getInt(R.styleable.LrcView_lrcMode, NORMAL);
		heightLightItems = typedArray.getInt(R.styleable.LrcView_lrcHeightLightItems, heightLightItems);
		isTranslateLrcDrawColor = typedArray.getBoolean(R.styleable.LrcView_lrcTranslateIsDrawColor, isTranslateLrcDrawColor);
		translateLrcColor = typedArray.getColor(R.styleable.LrcView_lrcTranslateColor, translateLrcColor);
		lrcTimeDirectionMode = typedArray.getInt(R.styleable.LrcView_lrcTimeDirectionMode, LEFT);
		isDashLine = typedArray.getBoolean(R.styleable.LrcView_lrcIsDashLine, isDashLine);
		typedArray.recycle();

		if (emptyLrc == null || "".equals(emptyLrc)) emptyLrc = "暂无歌词";

		initPaint();

		initLineMode(leftLineSrcId, rightLineSrcId);

		// 播放按钮id
		if (playSrcId > 0) {
			playBitmap = BitmapFactory.decodeResource(getResources(), playSrcId);
		}

		rect = new Rect();

		// 计算文本高度
		fontMetrics = lrcPaint.getFontMetrics();

		// 滚动
		mScroller = new OverScroller(context);

		mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

		mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

		lrcHandler = new LrcHandler(this);

		// 最大显示5条高亮 否则根据行数计算高亮条数
		if (limitLines / 2 < heightLightItems) heightLightItems = limitLines / 2;
	}

	// 初始化画笔
	private void initPaint() {
		lrcPaint = new Paint();
		lrcPaint.setStyle(Paint.Style.FILL);
		lrcPaint.setAntiAlias(true);
		lrcPaint.setColor(dfLrcColor);
		lrcPaint.setTextSize(lrcTextSize);
		lrcPaint.setTextAlign(Paint.Align.CENTER);

		lrcTranslatePaint = new Paint();
		lrcTranslatePaint.setStyle(Paint.Style.FILL);
		lrcTranslatePaint.setAntiAlias(true);
		lrcTranslatePaint.setColor(translateLrcColor);
		lrcTranslatePaint.setTextSize(lrcTextSize);
		lrcTranslatePaint.setTextAlign(Paint.Align.CENTER);

		timeLeftLinePaint = new Paint();
		timeLeftLinePaint.setStyle(Paint.Style.FILL);
		timeLeftLinePaint.setAntiAlias(true);
		timeLeftLinePaint.setColor(timeLineColor);
		timeLeftLinePaint.setTextSize(timeTextSize);
		timeLeftLinePaint.setTextAlign(Paint.Align.CENTER);
		timeLeftLinePaint.setStrokeWidth(timeLineHeight);
		if (isDashLine) timeLeftLinePaint.setPathEffect(new DashPathEffect(new float[] { 5, 5 }, 0));

		timeRightLinePaint = new Paint();
		timeRightLinePaint.setStyle(Paint.Style.FILL);
		timeRightLinePaint.setAntiAlias(true);
		timeRightLinePaint.setColor(timeLineColor);
		timeRightLinePaint.setTextSize(timeTextSize);
		timeRightLinePaint.setTextAlign(Paint.Align.CENTER);
		timeRightLinePaint.setStrokeWidth(timeLineHeight);
		if (isDashLine) timeRightLinePaint.setPathEffect(new DashPathEffect(new float[] { 5, 5 }, 0));

		timePaint = new Paint();
		timePaint.setStyle(Paint.Style.FILL);
		timePaint.setAntiAlias(true);
		timePaint.setColor(timeColor);
		timePaint.setTextSize(timeTextSize);
		timePaint.setTextAlign(Paint.Align.CENTER);
	}

	// 初始化画线的模式
	private void initLineMode(int leftLineSrc, int rightLineSrc) {
		if (leftLineSrc > 0) {
			leftLineDrawable = getResources().getDrawable(leftLineSrc);
			// 资源Drawable
			if (leftLineDrawable == null) {
				lrcLeftLineMode = CANVAS_LEFT_LINE_COLOR;
			} else {
				lrcLeftLineMode = CANVAS_LEFT_LINE_DRAWABLE;
			}
		} else {
			lrcLeftLineMode = CANVAS_LEFT_LINE_COLOR;
		}
		if (rightLineSrc > 0) {
			rightLineDrawable = getResources().getDrawable(leftLineSrc);
			// 资源Drawable
			if (rightLineDrawable == null) {
				lrcRightLineMode = CANVAS_RIGHT_LINE_COLOR;
			} else {
				lrcRightLineMode = CANVAS_RIGHT_LINE_DRAWABLE;
			}
		} else {
			lrcRightLineMode = CANVAS_RIGHT_LINE_COLOR;
		}
	}

	public List<LrcBean> getLrcBeans() {
		return lrcBeans;
	}

	// 设置歌词
	public void setLrc(String lrc) {
		if (this.lrcBeans != null && this.lrcBeans.size() > 0) this.lrcBeans.clear();
		this.lrcBeans = LrcParseTool.parseLrc(lrc);
	}

	// 设置歌词集合
	public void setLrcBeans(List<LrcBean> lrcBeans) {
		if (this.lrcBeans != null && this.lrcBeans.size() > 0) this.lrcBeans.clear();
		this.lrcBeans = lrcBeans;
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

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setLinearGradient(lrcTimeDirectionMode, 1, lineStartColor, lineCenterColor, lineEndColor);
		setLinearGradient(lrcTimeDirectionMode, 2, lineEndColor, lineCenterColor, lineStartColor);
	}

	// 设置渐变以及计算color以及权重
	private void setLinearGradient(int mode, int type, int startColor, int centerColor, int endColor) {
		int count = 0;
		if (startColor != 0) {
			count++;
		}
		if (centerColor != 0) {
			count++;
		}
		if (endColor != 0) {
			count++;
		}
		if (count == 0) return;
		int[] gradientColors = new int[count];
		float[] weights = new float[count];
		switch (count) {
			case 1:
				gradientColors[0] = startColor != 0 ? startColor : centerColor != 0 ? centerColor : endColor;
				weights[0] = 1;
				break;
			case 2:
				gradientColors[0] = startColor != 0 ? startColor : centerColor;
				gradientColors[1] = endColor != 0 ? endColor : centerColor;
				weights[0] = 0;
				weights[1] = 1;
				break;
			case 3:
				gradientColors[0] = startColor;
				gradientColors[1] = centerColor;
				gradientColors[2] = endColor;
				weights[0] = 0;
				weights[1] = 0.5f;
				weights[2] = 1;
				break;
		}
		float timeWidth = getTextWidth(timePaint, getMinuteSecondStrForLong(currentDuration));
		float playWidth = 0;
		if (playBitmap != null) playWidth = playBitmap.getWidth();
		switch (mode) {
			case LEFT:// 时间在左
				switch (type) {
					case 1:
						linearGradientLeftLine = new LinearGradient(timeWidth + 2 * timeLinePadding, 0, timeWidth + 2 * timeLinePadding + timeLineLength, 0, gradientColors, weights,
								Shader.TileMode.CLAMP);
						timeLeftLinePaint.setShader(linearGradientLeftLine);
						break;
					case 2:
						linearGradientRightLine = new LinearGradient(getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding - timeLineLength, 0,
								getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding, 0, gradientColors, weights, Shader.TileMode.CLAMP);
						timeRightLinePaint.setShader(linearGradientRightLine);
						break;
				}

				break;
			case RIGHT:// 时间在右
				switch (type) {
					case 1:
						linearGradientLeftLine = new LinearGradient(Math.max(playWidth, timeWidth) + 2 * timeLinePadding, 0, Math.max(playWidth, timeWidth) + 2 * timeLinePadding + timeLineLength, 0,
								gradientColors, weights, Shader.TileMode.CLAMP);
						timeLeftLinePaint.setShader(linearGradientLeftLine);
						break;
					case 2:
						linearGradientRightLine = new LinearGradient(getMeasuredWidth() - timeWidth - 2 * timeLinePadding - timeLineLength, 0, getMeasuredWidth() - timeWidth - 2 * timeLinePadding, 0,
								gradientColors, weights, Shader.TileMode.CLAMP);
						timeRightLinePaint.setShader(linearGradientRightLine);
						break;
				}
				break;
		}
	}

	// 添加单行歌词折行集合
	private void addLrcBreakLineList() {
		if (lrcBeans == null || lrcBeans.size() == 0) return;
		for (int i = 0; i < lrcBeans.size(); i++) {
			lrcBeans.get(i).lrcBreakLineList = getStrings(lrcBeans.get(i).lrc, lrcMaxLength);
		}
	}

	// 添加单行翻译歌词折行集合
	private void addLrcTranslateLineList() {
		if (lrcBeans == null || lrcBeans.size() == 0) return;
		for (int i = 0; i < lrcBeans.size(); i++) {
			lrcBeans.get(i).lrcTranslateLineList = getStrings(lrcBeans.get(i).translateLrc, lrcMaxLength);
		}
	}

	@Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (isFirstAddTextLineList) {
			// 最大歌词长度
			lrcMaxLength = getMeasuredWidth() / 2;
			// 添加歌词折行集合
			addLrcBreakLineList();
			// 添加音译歌词折行集合
			addLrcTranslateLineList();
			// 第一次添加判断
			isFirstAddTextLineList = false;
			// 获取歌词高度
			lrcTextH = fontMetrics.bottom - fontMetrics.top;
			// 歌词真实高度
			lrcTextRealH = getTextHeight(lrcPaint, emptyLrc);
			// 计算歌词间距离Padding
			calculatePadding(getMeasuredHeight(), lrcTextH);
		}
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

	// 计算卡拉OK模式播放的宽度
	private float calculateGoWidth(LrcBean lrcBean, float lrcTextW, int currentPosition) {
		if (lrcBean == null) return 0;
		// 计算一行歌词播放中执行的宽度 来创建歌词Bitmap
		float goWidth;
		if (currentPosition == lrcBeans.size() - 1) {
			goWidth = ((currentDuration - lrcBean.startTime) * 1.0f / (maxDuration - lrcBean.startTime) * lrcTextW);
		} else {
			goWidth = ((currentDuration - lrcBean.startTime) * 1.0f / (lrcBean.endTime - lrcBean.startTime) * lrcTextW);
		}
		return goWidth;
	}

	// 画歌词卡拉OK
	private void canvasLrcKaraoke(Canvas canvas, LrcBean lrcBean, int currentPosition) {
		canvas.save();
		// 第一条歌词偏移出去的高度 只有第一条是多行的情况才会有偏移
		float firstItemOffset = (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() + (lrcBean.lrcTranslateLineList == null ? 0 : lrcBean.lrcTranslateLineList.size()) - 1)
				* lrcTextH / 2;
		List<String> list = lrcBean.lrcBreakLineList;
		// 当前绘制的折行歌词条目
		int currentBreakLineIndex = 0;
		// 歌词宽度
		float lrcTextW = getTextWidth(lrcPaint, lrcBean.lrc);
		// 卡拉OK移动的距离
		float goWidth = calculateGoWidth(lrcBean, lrcTextW, currentPosition);
		// 单行歌词有换行的歌词判断
		if (list != null && list.size() > 1) {
			if (goWidth > 0) {
				if (goWidth >= (currentBreakLineIndex + 1) * lrcMaxLength) {
					// 计算折行歌词条目
					currentBreakLineIndex = (int) (goWidth / lrcMaxLength);
				}
				if (currentBreakLineIndex >= list.size()) return;
				if (currentBreakLineIndex > 0) {// 绘制已经播放的卡拉OK模式歌词
					for (int j = 0; j < currentBreakLineIndex; j++) {
						lrcPaint.setColor(playLrcColor);
						lrcPaint.setAlpha(currentAlpha);
						canvas.drawText(list.get(j), (float) getMeasuredWidth() / 2,
								(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) + j * lrcTextH - firstItemOffset, lrcPaint);
					}
				}
				if (goWidth - currentBreakLineIndex * lrcMaxLength <= 0) return;
				// Bitmap textBitmap = Bitmap.createBitmap((int) (goWidth -
				// currentBreakLineIndex * lrcMaxLength) > 0 ? (int) (goWidth -
				// currentBreakLineIndex * lrcMaxLength) : 1,
				// (int) (lrcTextH + lrcPadding / 2), Bitmap.Config.ARGB_8888);
				// Canvas textCanvas = new Canvas(textBitmap);
				// textCanvas.drawText(list.get(currentBreakLineIndex),
				// lrcPaint.measureText(list.get(currentBreakLineIndex)) / 2, lrcTextH,
				// lrcPaint);
				// canvas.drawBitmap(textBitmap, (getMeasuredWidth() -
				// lrcPaint.measureText(list.get(currentBreakLineIndex))) / 2,
				// (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent +
				// calculateOffset(currentPosition) - lrcTextH + currentBreakLineIndex *
				// lrcTextH - firstItemOffset,
				// null);
				// textBitmap.recycle();
				// 裁剪一个矩形用来绘制已经唱的歌词
				canvas.clipRect((getMeasuredWidth() - getTextWidth(lrcPaint, list.get(currentBreakLineIndex))) / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - lrcTextH + currentBreakLineIndex * lrcTextH - firstItemOffset,
						(getMeasuredWidth() - getTextWidth(lrcPaint, list.get(currentBreakLineIndex))) / 2 + (goWidth - currentBreakLineIndex * lrcMaxLength),
						(float) getMeasuredHeight() / 2 + lrcTextH / 2 + calculateOffset(currentPosition) + currentBreakLineIndex * lrcTextH - firstItemOffset);
				canvas.drawText(list.get(currentBreakLineIndex), (float) getMeasuredWidth() / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) + currentBreakLineIndex * lrcTextH - firstItemOffset, lrcPaint);
			}
		} else {
			if (goWidth > 0) {
				// Bitmap textBitmap = Bitmap.createBitmap(goWidth > 0 ? (int) goWidth : 1,
				// (int) (lrcTextH + lrcPadding / 2), Bitmap.Config.ARGB_8888);
				// Canvas textCanvas = new Canvas(textBitmap);
				// textCanvas.drawText(lrcBean.lrc, lrcTextW / 2, lrcTextH, lrcPaint);
				// canvas.drawBitmap(textBitmap, (getMeasuredWidth() - lrcTextW) / 2,
				// (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent +
				// calculateOffset(currentPosition) - lrcTextH - firstItemOffset, null);
				// textBitmap.recycle();
				// 裁剪一个矩形用来绘制已经唱的歌词
				canvas.clipRect((getMeasuredWidth() - lrcTextW) / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - lrcTextH - firstItemOffset,
						(getMeasuredWidth() - lrcTextW) / 2 + goWidth, (float) getMeasuredHeight() / 2 + lrcTextH / 2 + calculateOffset(currentPosition) - firstItemOffset);
				canvas.drawText(lrcBean.lrc, (float) getMeasuredWidth() / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - firstItemOffset, lrcPaint);
			}
		}
		canvas.restore();
	}

	// 画音译歌词卡拉OK
	private void canvasTranslateLrcLrcBitmap(Canvas canvas, LrcBean lrcBean, int currentPosition) {
		canvas.save();
		// 设置卡拉OK模式颜色
		lrcTranslatePaint.setColor(playLrcColor);
		// 同步透明度
		lrcTranslatePaint.setAlpha(currentAlpha);
		if (lrcBean == null || lrcBean.lrcTranslateLineList == null || lrcBean.translateLrc == null || "".equals(lrcBean.translateLrc)) return;
		// 第一条歌词偏移出去的高度 只有第一条是多行的情况才会有偏移
		float firstItemOffset = (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() + lrcBean.lrcTranslateLineList.size() - 1) * lrcTextH / 2;
		List<String> list = lrcBean.lrcTranslateLineList;
		// 当前绘制的折行歌词条目
		int currentBreakLineIndex = 0;
		// 歌词宽度
		float lrcTextW = getTextWidth(lrcTranslatePaint, lrcBean.translateLrc);
		// 卡拉OK移动的距离
		float goWidth = calculateGoWidth(lrcBean, lrcTextW, currentPosition);
		// 单行歌词有换行的歌词判断
		if (list.size() > 1) {
			if (goWidth > 0) {
				if (goWidth >= (currentBreakLineIndex + 1) * lrcMaxLength) {
					// 计算折行歌词条目
					currentBreakLineIndex = (int) (goWidth / lrcMaxLength);
				}
				if (currentBreakLineIndex >= list.size()) return;
				if (currentBreakLineIndex > 0) {// 绘制已经播放的卡拉OK模式歌词
					for (int j = 0; j < currentBreakLineIndex; j++) {
						lrcTranslatePaint.setColor(playLrcColor);
						lrcTranslatePaint.setAlpha(currentAlpha);
						canvas.drawText(list.get(j), (float) getMeasuredWidth() / 2, (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition)
								+ j * lrcTextH - firstItemOffset + lrcBean.lrcBreakLineList.size() * lrcTextH, lrcTranslatePaint);
					}
				}
				// 裁剪一个矩形用来绘制已经唱的歌词
				canvas.clipRect((getMeasuredWidth() - getTextWidth(lrcTranslatePaint, list.get(currentBreakLineIndex))) / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - lrcTextH + currentBreakLineIndex * lrcTextH - firstItemOffset
								+ (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH),
						(getMeasuredWidth() - getTextWidth(lrcTranslatePaint, list.get(currentBreakLineIndex))) / 2 + (goWidth - currentBreakLineIndex * lrcMaxLength),
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) + currentBreakLineIndex * lrcTextH - firstItemOffset
								+ (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH));
				canvas.drawText(list.get(currentBreakLineIndex), (float) getMeasuredWidth() / 2, (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent
						+ calculateOffset(currentPosition) + currentBreakLineIndex * lrcTextH - firstItemOffset + (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH),
						lrcTranslatePaint);
			}
		} else {
			if (goWidth > 0) {
				// 裁剪一个矩形用来绘制已经唱的歌词
				canvas.clipRect((getMeasuredWidth() - lrcTextW) / 2,
						(float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - lrcTextH - firstItemOffset
								+ (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH),
						(getMeasuredWidth() - lrcTextW) / 2 + goWidth, (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition) - firstItemOffset
								+ (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH));
				canvas.drawText(lrcBean.translateLrc, (float) getMeasuredWidth() / 2, (float) getMeasuredHeight() / 2 + fontMetrics.bottom + fontMetrics.descent + calculateOffset(currentPosition)
						- firstItemOffset + (lrcBean.lrcBreakLineList == null ? 0 : lrcBean.lrcBreakLineList.size() * lrcTextH), lrcTranslatePaint);
			}
		}
		canvas.restore();
	}

	// 绘制卡拉OK样式
	private void canvasKaraoke(Canvas canvas, int currentPosition) {
		// 设置卡拉OK模式颜色
		lrcPaint.setColor(playLrcColor);
		// 同步透明度
		lrcPaint.setAlpha(currentAlpha);
		LrcBean lrcBean = lrcBeans.get(currentPosition);
		if (lrcBean == null) return;
		canvasLrcKaraoke(canvas, lrcBean, currentPosition);
		if (isTranslateLrcDrawColor) canvasTranslateLrcLrcBitmap(canvas, lrcBean, currentPosition);
	}

	// 画歌词
	private void canvasModeLrc(Canvas canvas, int currentPosition, float width, float height) {
		float addPadding = 0;// 折行之后新增的padding
		float multiLinesHeight = 0;// 折行的歌词高度
		float firstItemOffset = 0;// 第一条歌词折行之后向上偏移的距离
		float multiLinesTranslateHeight = 0;// 折行的歌词高度
		// 画歌词
		for (int i = 0; i < lrcBeans.size(); i++) {
			LrcBean lrcBean = lrcBeans.get(i);
			if (lrcBean == null || lrcBean.lrc == null || "".equals(lrcBean.lrc)) break;
			// 设置默认字体
			lrcPaint.setColor(dfLrcColor);
			lrcTranslatePaint.setColor(translateLrcColor);
			// 设置正在播放位置颜色
			if (lrcMode == NORMAL && i == currentPosition) lrcPaint.setColor(playLrcColor);
			// 当前滑动到的位置
			int position = calculateDraggingPosition();
			// 上半部分内容充满情况
			if (position > heightLightItems / 2) {
				// 当前拖动位置上半部分透明度设置
				if (i < position) {
					lrcPaint.setAlpha((int) (255 - (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
					lrcTranslatePaint.setAlpha((int) (255 - (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
				} else {
					// 当前拖动位置下半部分透明度设置
					lrcPaint.setAlpha((int) (255 + (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
					lrcTranslatePaint.setAlpha((int) (255 + (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
				}
				// 设置中心线上半部分高亮
				for (int j = position - heightLightItems / 2; j < position; j++) {
					if (i == j) {
						lrcPaint.setAlpha(255);
						lrcTranslatePaint.setAlpha(255);
					}
				}
				// 设置中心线下半部分高亮
				for (int j = position; j < position + heightLightItems / 2 + 1; j++) {
					if (i == j) {
						lrcPaint.setAlpha(255);
						lrcTranslatePaint.setAlpha(255);
					}
				}
			} else {// 开始播放时上半部分没有内容情况
				// 设置当前位置下半部分透明度
				lrcPaint.setAlpha((int) (255 + (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
				lrcTranslatePaint.setAlpha((int) (255 + (mOffset - lrcBeans.get(i).offset) * 255 / (height / 2 + lrcBeans.get(1).offset / 2)));
				// 设置首位置高亮
				for (int j = 0; j < heightLightItems / 2; j++) {
					if (i == j) {
						lrcPaint.setAlpha(255);
						lrcTranslatePaint.setAlpha(255);
					}
				}
				// 设置滑动位置高亮
				for (int j = position; j < position + heightLightItems / 2 + 1; j++) {
					if (i == j) {
						lrcPaint.setAlpha(255);
						lrcTranslatePaint.setAlpha(255);
					}
				}
			}
			// 单行歌词超出屏幕处理换行显示
			if (lrcBean.lrcBreakLineList == null || lrcBean.lrcBreakLineList.size() == 0) continue;
			// 为单行歌词超出宽度进行折行添加新歌词之间的的padding
			if (i > 0 && getTextWidth(lrcPaint, lrcBeans.get(i - 1).lrc) > lrcMaxLength) {
				addPadding += multiLinesHeight - lrcTextH;
			}
			// 为单行翻译歌词超出宽度进行折行添加新歌词之间的的padding
			if (lrcBean.lrcTranslateLineList != null && i > 0 && lrcBeans.get(i - 1).translateLrc != null && !"".equals(lrcBeans.get(i - 1).translateLrc)) {
				addPadding += multiLinesTranslateHeight;
			}
			// 只有第一条是多行的情况才会有偏移
			firstItemOffset = i == 0 ? (lrcBean.lrcBreakLineList.size() + (lrcBean.lrcTranslateLineList == null ? 0 : lrcBean.lrcTranslateLineList.size()) - 1) * lrcTextH / 2 : firstItemOffset;
			// 单行不折行的文本绘制
			if (lrcBean.lrcBreakLineList.size() == 1) {
				canvas.drawText(lrcBean.lrc, width / 2, height / 2 + lrcTextRealH / 2 + calculateTop(i) + addPadding - firstItemOffset, lrcPaint);
			}
			// 文本折行的多行绘制
			if (lrcBean.lrcBreakLineList.size() > 1) {
				// 记录多行歌词高
				multiLinesHeight = lrcTextH * lrcBean.lrcBreakLineList.size();
				// 绘制折行歌词
				for (int j = 0; j < lrcBean.lrcBreakLineList.size(); j++) {
					canvas.drawText(lrcBean.lrcBreakLineList.get(j), width / 2, height / 2 + calculateTop(i) + lrcTextRealH / 2 + addPadding + j * lrcTextH - firstItemOffset, lrcPaint);
				}
			}
			if (lrcBean.lrcTranslateLineList != null) {
				multiLinesTranslateHeight = lrcBean.lrcTranslateLineList.size() * lrcTextH;
				// 同步歌词音译歌词绘制播放颜色
				if (isTranslateLrcDrawColor && i == currentPosition) {
					if (lrcMode == NORMAL) lrcTranslatePaint.setColor(playLrcColor);
				}
				// 翻译单行不折行的文本绘制
				if (lrcBean.lrcTranslateLineList.size() == 1) {
					if (lrcBean.translateLrc != null && !"".equals(lrcBean.translateLrc)) canvas.drawText(lrcBean.translateLrc, width / 2,
							height / 2 + lrcTextRealH / 2 + calculateTop(i) + addPadding + lrcTextH * lrcBean.lrcBreakLineList.size() - firstItemOffset, lrcTranslatePaint);
				}
				// 翻译文本折行的多行绘制
				if (lrcBean.lrcTranslateLineList.size() > 1) {
					// 绘制折行歌词
					for (int j = 0; j < lrcBean.lrcTranslateLineList.size(); j++) {
						if (lrcBean.translateLrc != null && !"".equals(lrcBean.translateLrc)) canvas.drawText(lrcBean.lrcTranslateLineList.get(j), width / 2,
								height / 2 + lrcTextRealH / 2 + calculateTop(i) + addPadding + j * lrcTextH + lrcTextH * lrcBean.lrcBreakLineList.size() - firstItemOffset, lrcTranslatePaint);
					}
				}
			}
			if (isFirstAddData) {
				// 添加歌词高度
				if (lrcBean.lrcTranslateLineList != null) lrcBean.lrcHeight = (lrcBean.lrcBreakLineList.size() + lrcBean.lrcTranslateLineList.size()) * lrcTextH;
				else lrcBean.lrcHeight = lrcBean.lrcBreakLineList.size() * lrcTextH;
				// 添加偏移量
				lrcBean.offset = i == 0 ? 0 : lrcBeans.get(i - 1).offset + lrcBeans.get(i - 1).lrcHeight / 2 + lrcPadding + lrcBean.lrcHeight / 2;
			}
			// 只初始化一次padding控制
			if (i == lrcBeans.size() - 1) {
				if (isFirstAddData) isFirstAddData = false;
			}
			// 获取当前播放的位置透明度 为了同步卡拉OK模式设置
			if (lrcMode == KARAOKE && i == currentPosition) currentAlpha = lrcPaint.getAlpha();
		}
	}

	private void canvasLrc(Canvas canvas) {
		float width = getMeasuredWidth();
		float height = getMeasuredHeight();

		// 画无歌词情况
		if (isEmptyLrc()) {
			canvas.drawText(emptyLrc, width / 2, height / 2 + fontMetrics.bottom, lrcPaint);
			return;
		}

		// 获取当前播放的歌词条目
		int currentPosition = getCurrentPosition();

		// 计算画布偏移量
		canvas.translate(0, -mOffset);

		// 绘制歌词
		canvasModeLrc(canvas, currentPosition, width, height);
		// 播放中歌词显示模式
		if (lrcMode == KARAOKE) {// 播放歌词卡拉OK模式
			// 绘制卡拉OK样式
			canvasKaraoke(canvas, currentPosition);
		}

		// 判断滚动时机 当前播放歌词位置跟上次不同时 并且 没有手动拖动 时候进行自动滚动
		if (lastPosition != currentPosition && !isDragging) {
			scrollToPosition(currentPosition);
		}
		lastPosition = currentPosition;
	}

	private float calculateTop(int position) {
		return position * (lrcPadding + lrcTextH);
	}

	/**
	 * 画时间轴
	 *
	 * @param canvas
	 *            canvas
	 */
	private void canvasTimeLine(Canvas canvas) {
		if (isEmptyLrc()) return;
		// 计算拖拽到歌词位置
		int draggingPosition = calculateDraggingPosition();
		float playWidth = 0;
		if (playBitmap != null) playWidth = playBitmap.getWidth();
		// 时间
		String time = getMinuteSecondStrForLong(lrcBeans.get(draggingPosition).startTime);
		// 时间文本宽度
		float timeWidth = getTextWidth(timePaint, time);
		switch (lrcTimeDirectionMode) {
			case LEFT:// 左侧时间右侧播放按钮
				// 画时间
				canvas.drawText(time, timeWidth / 2 + timeLinePadding, (float) getMeasuredHeight() / 2 + getTextHeight(timePaint, time) / 2 - 4 + mOffset, timePaint);
				// 左线颜色
				if (lrcLeftLineMode == CANVAS_LEFT_LINE_COLOR) {
					// 画左线
					canvas.drawLine(timeWidth + 2 * timeLinePadding, (float) getMeasuredHeight() / 2 + mOffset, timeWidth + 2 * timeLinePadding + timeLineLength,
							(float) getMeasuredHeight() / 2 + mOffset, timeLeftLinePaint);
				}
				// 左线资源图片
				if (lrcLeftLineMode == CANVAS_LEFT_LINE_DRAWABLE) {
					leftLineDrawable.setBounds((int) (timeWidth + 2 * timeLinePadding), (int) (getMeasuredHeight() / 2 + mOffset - timeLineHeight / 2),
							(int) (timeWidth + 2 * timeLinePadding + timeLineLength), (int) ((float) getMeasuredHeight() / 2 + mOffset + timeLineHeight / 2));
					leftLineDrawable.draw(canvas);
				}
				// 右线颜色
				if (lrcRightLineMode == CANVAS_RIGHT_LINE_COLOR) {
					// 画右线
					canvas.drawLine(getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding - timeLineLength, (float) getMeasuredHeight() / 2 + mOffset,
							getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding, (float) getMeasuredHeight() / 2 + mOffset, timeRightLinePaint);
				}
				// 右线资源图片
				if (lrcRightLineMode == CANVAS_RIGHT_LINE_DRAWABLE) {
					rightLineDrawable.setBounds((int) (getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding - timeLineLength),
							(int) (getMeasuredHeight() / 2 + mOffset - timeLineHeight / 2), (int) (getMeasuredWidth() - Math.max(playWidth, timeWidth) - 2 * timeLinePadding),
							(int) ((float) getMeasuredHeight() / 2 + mOffset + timeLineHeight / 2));
					rightLineDrawable.draw(canvas);
				}
				// 画播放按钮
				if (playBitmap != null) {
					if (playWidth >= timeWidth) {
						canvas.drawBitmap(playBitmap, getMeasuredWidth() - playWidth - timeLinePadding, (float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2) + mOffset, null);
					} else {
						canvas.drawBitmap(playBitmap, getMeasuredWidth() - timeWidth / 2 - playWidth / 2 - timeLinePadding, (float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2) + mOffset,
								null);
					}
				}
				break;
			case RIGHT:// 左侧播放按钮右侧时间
				// 画时间
				canvas.drawText(time, getMeasuredWidth() - timeWidth / 2 - timeLinePadding, (float) getMeasuredHeight() / 2 + getTextHeight(timePaint, time) / 2 + mOffset, timePaint);
				// 左线颜色
				if (lrcLeftLineMode == CANVAS_LEFT_LINE_COLOR) {
					// 画左线
					canvas.drawLine(Math.max(playWidth, timeWidth) + 2 * timeLinePadding, (float) getMeasuredHeight() / 2 + mOffset,
							Math.max(playWidth, timeWidth) + 2 * timeLinePadding + timeLineLength, (float) getMeasuredHeight() / 2 + mOffset, timeLeftLinePaint);
				}
				// 左线资源图片
				if (lrcLeftLineMode == CANVAS_LEFT_LINE_DRAWABLE) {
					leftLineDrawable.setBounds((int) (Math.max(playWidth, timeWidth) + 2 * timeLinePadding), (int) (getMeasuredHeight() / 2 + mOffset - timeLineHeight / 2),
							(int) (Math.max(playWidth, timeWidth) + 2 * timeLinePadding + timeLineLength), (int) ((float) getMeasuredHeight() / 2 + mOffset + timeLineHeight / 2));
					leftLineDrawable.draw(canvas);
				}
				// 右线颜色
				if (lrcRightLineMode == CANVAS_RIGHT_LINE_COLOR) {
					// 画右线
					canvas.drawLine(getMeasuredWidth() - timeWidth - 2 * timeLinePadding - timeLineLength, (float) getMeasuredHeight() / 2 + mOffset,
							getMeasuredWidth() - timeWidth - 2 * timeLinePadding, (float) getMeasuredHeight() / 2 + mOffset, timeRightLinePaint);
				}
				// 右线资源图片
				if (lrcRightLineMode == CANVAS_RIGHT_LINE_DRAWABLE) {
					rightLineDrawable.setBounds((int) (getMeasuredWidth() - timeWidth - 2 * timeLinePadding - timeLineLength), (int) (getMeasuredHeight() / 2 + mOffset - timeLineHeight / 2),
							(int) (getMeasuredWidth() - timeWidth - 2 * timeLinePadding), (int) ((float) getMeasuredHeight() / 2 + mOffset + timeLineHeight / 2));
					rightLineDrawable.draw(canvas);
				}
				// 画播放按钮
				if (playBitmap != null) {
					if (playWidth >= timeWidth) {
						canvas.drawBitmap(playBitmap, timeLinePadding, (float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2) + mOffset, null);
					} else {
						canvas.drawBitmap(playBitmap, timeWidth / 2 - playWidth / 2 + timeLinePadding, (float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2) + mOffset, null);
					}
				}
				break;
		}
	}

	// 如果字符串超过最大宽度则换行存储数据
	private List<String> getStrings(String text, float width) {
		if (text == null || "".equals(text)) return null;
		int line = (int) Math.ceil(getTextWidth(lrcPaint, text) / width);
		if (line == 0) return null;
		List<String> texts = new ArrayList<>();
		if (line == 1) {
			texts.add(text);
			return texts;
		}
		int index = 0;
		int lines = 1;
		for (int i = 0; i < lines; i++) {
			StringBuilder newText = new StringBuilder();
			for (int j = index; j < text.length(); j++) {
				newText.append(text.charAt(j));
				if (getTextWidth(lrcPaint, newText.toString()) > width) {
					index = j;
					newText.deleteCharAt(newText.length() - 1);
					break;
				}
				if (j == text.length() - 1) {
					index = j + 1;
				}
			}
			if (newText.length() > 0) {
				texts.add(newText.toString());
				if (index < text.length()) {
					lines++;
				}
			}
		}
		return texts;
	}

	// 获取文字宽
	private float getTextWidth(Paint paint, String text) {
		if (text == null || "".equals(text)) return 0;
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect.width();
	}

	// 获取文字高
	private float getTextHeight(Paint paint, String text) {
		if (text == null || "".equals(text)) return 0;
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect.height();
	}

	/**
	 * 根据long类型转分秒类型字符串
	 *
	 * @param mill
	 *            long类型时间戳
	 * @return 返回string类型时间分秒
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
		ValueAnimator animator = ValueAnimator.ofFloat(mOffset, calculateOffset(linePosition));
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
		return lrcBeans.get(position).offset;
	}

	// // 计算拖拽到歌词位置
	private int calculateDraggingPosition() {
		int draggingPosition = 0;
		for (int i = 0; i < lrcBeans.size(); i++) {
			if (i > 0 && i < lrcBeans.size() - 1) {
				if (mOffset >= lrcBeans.get(i - 1).offset + lrcBeans.get(i - 1).lrcHeight / 2 + lrcPadding / 2 && mOffset <= lrcBeans.get(i).offset + lrcBeans.get(i).lrcHeight / 2 + lrcPadding / 2) {
					draggingPosition = i;
					break;
				}
			} else if (i > 0 && i + 1 == lrcBeans.size() && mOffset >= lrcBeans.get(i - 1).offset + lrcBeans.get(i - 1).lrcHeight / 2 + lrcPadding / 2) {
				draggingPosition = lrcBeans.size() - 1;
				break;
			}
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

	@SuppressLint("ClickableViewAccessibility") @Override public boolean onTouchEvent(MotionEvent event) {
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
		if (lrcBeans == null || lrcBeans.size() == 0) return;
		// 时间文本宽度
		float timeWidth = getTextWidth(timePaint, getMinuteSecondStrForLong(lrcBeans.get(0).startTime));
		float playWidth = playBitmap.getWidth();
		float leftLimitX;
		if (playWidth >= timeWidth) {
			if (lrcTimeDirectionMode == LEFT) leftLimitX = getMeasuredWidth() - playWidth - timeLinePadding;
			else leftLimitX = timeLinePadding;
		} else {
			if (lrcTimeDirectionMode == LEFT) leftLimitX = getMeasuredWidth() - timeWidth / 2 - playWidth / 2 - timeLinePadding;
			else leftLimitX = timeWidth / 2 - playWidth / 2 + timeLinePadding;
		}
		if ((lrcTimeDirectionMode == LEFT && upX >= leftLimitX && upX <= leftLimitX + playWidth && upY >= ((float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2))
				&& upY <= ((float) (getMeasuredHeight() / 2 + playBitmap.getHeight() / 2)))
				|| (lrcTimeDirectionMode == RIGHT && upX >= leftLimitX && upX <= leftLimitX + playWidth && upY >= ((float) (getMeasuredHeight() / 2 - playBitmap.getHeight() / 2))
						&& upY <= ((float) (getMeasuredHeight() / 2 + playBitmap.getHeight() / 2)))) {
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
		if (playBitmap != null && !playBitmap.isRecycled()) {
			playBitmap.recycle();
			playBitmap = null;
		}
		lrcPaint = null;
		timeLeftLinePaint = null;
		timeRightLinePaint = null;
		timePaint = null;
		mScroller = null;
		fontMetrics = null;
		mVelocityTracker = null;
		linearGradientLeftLine = null;
		linearGradientRightLine = null;
		leftLineDrawable = null;
		rightLineDrawable = null;
	}

	public void setSeekListener(SeekListener seekListener) {
		this.seekListener = seekListener;
	}

	public interface SeekListener {

		void seekToDuration(LrcView lrcView, long duration);

	}
}
