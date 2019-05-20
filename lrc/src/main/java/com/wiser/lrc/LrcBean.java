package com.wiser.lrc;

/**
 * @author Wiser
 * 
 *         歌词数据
 */
public class LrcBean implements Comparable<LrcBean> {

	public String	lrc;		// 单句歌词内容

	public long		startTime;	// 单句歌词开始时间

	public long		endTime;	// 单句歌词结束时间

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
