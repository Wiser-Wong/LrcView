package com.wiser.lrc;

import java.util.List;

/**
 * @author Wiser
 * 
 *         歌词数据
 */
public class LrcBean implements Comparable<LrcBean> {

	public String		lrc;					// 单句歌词内容

	public long			startTime;				// 单句歌词开始时间

	public long			endTime;				// 单句歌词结束时间

	public float		offset;					// 歌词偏移量

	public List<String>	lrcBreakLineList;		// 单句歌词折行之后的集合

	public List<String>	lrcTranslateLineList;	// 单句翻译歌词折行之后的集合

	public float		lrcHeight;				// 歌词高度

	public String		translateLrc;			// 翻译歌词

	public LrcBean() {}

	public LrcBean(String text, long startTime, long endTime) {
		this.lrc = text;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override public int compareTo(LrcBean o) {
		if (o == null) return -1;
		return (int) (this.startTime - o.startTime);
	}
}
