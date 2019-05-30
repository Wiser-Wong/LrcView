# LrcView
音乐播放歌词控件LrcView 拥有歌词折行功能以及音译歌词功能。

## 使用方法

    //设置歌词字符串
    lrcView.setLrc(lrc);
    //或者设置歌词集合
    lrcView.setLrcBeans(lrcBeans);
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
        app:lrcTextSize="15sp"
        app:lrcTimeDirectionMode="LEFT"
        app:lrcTimeLineColor="#CAFF70"
        app:lrcTimeLineHeight="1dp"
        app:lrcTimeLineLength="40dp"
        app:lrcTimeLinePadding="10dp"
        app:lrcTimeTextColor="#ffff00"
        app:lrcTimeTextSize="12sp"
        app:lrcTranslateColor="#CCCCCC"
        app:lrcTranslateIsDrawColor="true"
        app:lrcHeightLightItems="1"/>

## 操作手册

* 设置lrcHeightLightItems属性为高亮条数，因为是对称关系，所以最好设置成奇数
* 设置lrcLimitLines歌词行数时，因为是对称关系，同样最好设置成数，同时因为加入了歌词折行以及音译歌词，所以会根据折行以及音译歌词计算歌词行数，只做参考
* lrcColor：歌词颜色
* lrcEmptyLrcText：歌词空数据显示内容
* lrcIsDashLine：播放轴线是否为虚线
* lrcLeftLineSrc：播放轴左侧时间线Drawable或者mipmap下图片
* lrcRightLineSrc：播放轴右侧时间线Drawable或者mipmap下图片
* lrcLineStartColor：播放轴时间线可设置渐变颜色，开始颜色
* lrcLineCenterColor：播放轴时间线可设置渐变颜色，中间颜色
* lrcLineEndColor：播放轴时间线可设置渐变颜色，结尾颜色
* 播放轴时间线颜色渐变最多设置三种颜色，同时也可设置两种颜色，一种颜色，设置lrcLineStartColor，lrcLineCenterColor，lrcLineEndColor，可不全设置
* lrcMode：歌词绘制模式有两种KARAOKE-->卡拉OK模式，NORMAL-->正常模式只设置正在播放那条一整条歌词颜色
* lrcPlayColor：正在播放那行歌词颜色
* lrcPlaySrc：播放时间轴播放按钮图片
* lrcTextSize：歌词文本大小
* lrcTimeDirectionMode：播放时间轴时间位置LEFT-->时间在左 RIGHT-->时间在右
* lrcTimeLineColor：播放时间轴时间线颜色
* lrcTimeLineHeight：播放轴时间线高度
* lrcTimeLineLength：播放轴时间线长度
* lrcTimeLinePadding：播放轴时间线与时间间距
* lrcTimeTextColor：播放轴时间显示颜色
* lrcTimeTextSize：播放轴时间文本大小
* lrcTranslateColor：音译文本颜色 默认与歌词颜色同步
* lrcTranslateIsDrawColor：音译歌词是否随正在播放歌词颜色同步
* lrcHeightLightItems：歌词高亮显示条目 查看第一条说明

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
