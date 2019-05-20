package com.wiser.lrc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Wiser
 * 
 *         解析歌词工具
 */
public class LrcParseTool {

	/**
	 * 解析歌词
	 *
	 * @param lrc
	 * @return
	 */
	public static List<LrcBean> parseLrc(String lrc) {
		lrc = lrc.replaceAll("&#58;", ":").replaceAll("&#10;", "\n").replaceAll("&#46;", ".").replaceAll("&#32;", " ").replaceAll("&#45;", "-").replaceAll("&#13;", "\r").replaceAll("&#39;", "'")
				.replaceAll("null", "");
		List<LrcBean> lrcBeans = new ArrayList<>();
		if (lrc == null || "".equals(lrc)) return lrcBeans;
		String[] split = lrc.split("\n");
		for (int i = 0; i < split.length; i++) {
			// 带时间以及歌词的内容
			String lineText = split[i];
			// 为空的歌词直接跳过
			if (lineText == null || "".equals(lineText)) continue;
			// 下一时间段歌词 为了获取歌词结尾时间
			String nextLineText;
			if (i < split.length - 1) {
				for (int j = i + 1; j < split.length; j++) {
					nextLineText = split[j];
					if (nextLineText != null && !"".equals(nextLineText)) break;
				}
			}
			// 只是歌词内容
			String lrcLine = "";
			// 时间戳数组 一条歌词可能会出现多个时间戳 需要记录数组分配歌词
			List<String> times = new ArrayList<>();
			// 最后一个时间戳中括号右边 为了获取歌词内容
			int lastPos = lineText.lastIndexOf("]");
			if (lastPos != -1) {
				// 通过截取最后一个中括号右边到最后长度 为歌词内容
				lrcLine = lineText.substring(lastPos + 1);
				// 控制获取歌词为空的情况不做添加
				if ("".equals(lrcLine)) continue;
			}
			// 第一个时间戳中括号左边
			int pos1 = lineText.indexOf("[");
			// 第一个时间戳中括号右边
			int pos2 = lineText.indexOf("]");
			String line = lineText;
			// 获取一句歌词多个时间戳
			while (pos1 != -1 && pos2 != -1) {
				String time = line.substring(pos1 + 1, pos2);
				if ("".equals(time)) break;
				times.add(time);
				line = line.substring(pos2 + 1);
				pos1 = line.indexOf("[");
				pos2 = line.indexOf("]");
			}
			if (times.size() == 0) break;
			for (int j = 0; j < times.size(); j++) {
				LrcBean lrcBean = new LrcBean();
				lrcBean.lrc = lrcLine;
				lrcBean.startTime = timeStrToLong(times.get(j));
				lrcBeans.add(lrcBean);
			}
		}

		Collections.sort(lrcBeans);

		for (int i = 0; i < lrcBeans.size(); i++) {
			LrcBean lrcBean = lrcBeans.get(i);
			if (i < lrcBeans.size() - 1) lrcBean.endTime = lrcBeans.get(i + 1).startTime;
			else lrcBean.endTime = lrcBean.startTime + 100000;
		}
		return lrcBeans;
	}

	// 将字符串转换为long类型
	private static long timeStrToLong(String strTime) {
		long showTime = -1;
		try {
			strTime = strTime.substring(1, strTime.length() - 1);
			String[] s1 = strTime.split(":");
			String[] s2 = s1[1].split("\\.");
			long min = Long.parseLong(s1[0]);
			long second = Long.parseLong(s2[0]);
			long mil = Long.parseLong(s2[1]);
			showTime = min * 60 * 1000 + second * 1000 + mil * 10;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return showTime;
	}
}
