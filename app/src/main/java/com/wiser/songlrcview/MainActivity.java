package com.wiser.songlrcview;

import java.lang.ref.WeakReference;
import java.util.List;

import com.wiser.lrc.LrcBean;
import com.wiser.lrc.LrcParseTool;
import com.wiser.lrc.LrcView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Wiser
 * 
 *         模拟歌词测试
 */
public class MainActivity extends AppCompatActivity implements LrcView.SeekListener {

	private long			maxDuration		= 357000;	// 最大时间

	private long			currentDuration	= 0;		// 当前时间

	private final int		UPDATE_DURATION	= 11;

	private LrcView			lrcView;

	private DurationHandler	handler;

	@Override public void seekToDuration(LrcView lrcView, long duration) {
		this.currentDuration = duration;
		handler.removeMessages(UPDATE_DURATION);
		handler.sendEmptyMessage(UPDATE_DURATION);
	}

	private static class DurationHandler extends Handler {

		WeakReference<MainActivity> reference;

		DurationHandler(MainActivity activity) {
			reference = new WeakReference<>(activity);
		}

		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (reference != null && reference.get() != null) {
				if (msg.what == reference.get().UPDATE_DURATION) {
					reference.get().currentDuration += 50;
					if (reference.get().currentDuration <= 0) {
						reference.get().currentDuration = 0;
					}
					if (reference.get().currentDuration >= reference.get().maxDuration) {
						reference.get().currentDuration = reference.get().maxDuration;
						removeMessages(reference.get().UPDATE_DURATION);
						return;
					}
					reference.get().lrcView.setCurrentDuration(reference.get().currentDuration);
					reference.get().handler.sendEmptyMessageDelayed(reference.get().UPDATE_DURATION, 50);
				}
			}
		}
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lrcView = findViewById(R.id.lrc_view);

		String lrc = "[00:00.00]海阔天空\n" + "[00:05.00]Beyond\n" + "[00:15.00]专辑：乐与怒\n" + "[00:18.00]\n" + "[01:43.00][00:19.00]今天我寒夜里看雪飘过 \n"
				+ "[01:49.00][00:25.00]怀著冷却了的心窝飘远方 \n" + "[01:55.00][00:31.00]风雨里追赶 \n" + "[01:58.00][00:34.00]雾里分不清影踪 \n" + "[02:01.00][00:37.00]天空海阔你与我 \n" + "[02:05.00][00:40.00]可会变(谁没在变)\n"
				+ "[00:43.00]多少次迎著冷眼与嘲笑 \n" + "[00:50.00]从没有放弃过心中的理想 \n" + "[00:56.00]一刹那恍惚 \n" + "[00:59.00]若有所失的感觉 \n" + "[01:02.00]不知不觉已变淡 \n" + "[01:05.00]心里爱(谁明白我) \n"
				+ "[03:57.70][03:20.00][02:08.00][01:09.00]原谅我这一生不羁放纵爱自由 \n" + "[04:04.50][03:27.00][02:15.00][01:16.00]也会怕有一天会跌倒 \n"
				+ "[04:10.85][03:46.00][03:33.00][02:21.00][01:22.00]被弃了理想谁人都可以 \n" + "[04:17.00][03:52.00][03:39.60][02:28.00][01:28.00]哪会怕有一天只你共我\n" + "[03:08.60]仍然自由自我 \n" + "[03:12.00]永远高唱我歌\n"
				+ "[03:14.50]走遍千里\n";
		// 解析歌词
		List<LrcBean> lrcBeans = LrcParseTool.parseLrc(lrc);
		// 添加音译歌词
		if (lrcBeans != null && lrcBeans.size() > 0) {
			for (int i = 0; i < lrcBeans.size(); i++) {
				lrcBeans.get(i).translateLrc = "I am translate lrc";
			}
		}
		// 设置歌词字符串
//		 lrcView.setLrc(lrc);
		// 设置歌词集合
		lrcView.setLrcBeans(lrcBeans);
		lrcView.setMaxDuration(maxDuration);
		lrcView.setSeekListener(this);

		handler = new DurationHandler(this);

		handler.sendEmptyMessageDelayed(UPDATE_DURATION, 50);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		if (handler != null) {
			if (handler.reference != null) {
				handler.reference.clear();
				handler.reference = null;
			}
			handler.removeMessages(UPDATE_DURATION);
			handler = null;
		}
		lrcView = null;
	}
}
