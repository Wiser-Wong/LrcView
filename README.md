# LrcView
音乐播放歌词控件LrcView 拥有歌词折行功能以及音译歌词功能。

## 使用方法

    lrcView.setLrc(lrc);
    lrcView.setMaxDuration(maxDuration);
    lrcView.setCurrentDuration(currentDuration);
    lrcView.setSeekListener(this);
    
    @Override 
    public void seekToDuration(LrcView lrcView, long duration) {
    
    }

    <com.wiser.lrc.LrcView
        android:id="@+id/lrc_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:lrcColor="#ffffff"
        app:lrcEmptyLrcText="加载失败，歌词未显示"
        app:lrcIsDashLine="true"
        app:lrcLeftLineSrc="@drawable/line"
        app:lrcLimitLines="11"
        app:lrcLineCenterColor="#CAFF70"
        app:lrcLineEndColor="#00ff00"
        app:lrcLineStartColor="#ff0000"
        app:lrcMode="KARAOKE"
        app:lrcPlayColor="#ffff00"
        app:lrcPlaySrc="@mipmap/lrc_play"
        app:lrcTextSize="18sp"
        app:lrcTimeDirectionMode="RIGHT"
        app:lrcTimeLineColor="#CAFF70"
        app:lrcTimeLineHeight="1dp"
        app:lrcTimeLineLength="100dp"
        app:lrcTimeLinePadding="15dp"
        app:lrcTimeTextColor="#ffff00"
        app:lrcTimeTextSize="15sp"
        app:lrcTranslateColor="#CCCCCC"
        app:lrcTranslateIsDrawColor="true" />


## 截图
### 正常歌词
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc7.jpg)

### 音译歌词 可设置音译歌词颜色 以及 是否与歌词模式（卡拉OK 对应显示）
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc1.jpg)

### 按下显示播放线
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc2.jpg)

### 滚动歌词显示效果
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc3.jpg)

### 无歌词效果
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc4.jpg)

### 按下播放线 播放按钮与时间显示位置调换
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc5.jpg)

### 折行歌词显示效果
![images](https://github.com/Wiser-Wong/LrcView/blob/master/images/lrc6.jpg)
