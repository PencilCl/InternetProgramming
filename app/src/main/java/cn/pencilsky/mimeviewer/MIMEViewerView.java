package cn.pencilsky.mimeviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.*;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by chenlin on 03/06/2017.
 */
public class MIMEViewerView extends ViewGroup {

    private float baseLineY;

    private Paint textPaint;

    private int mLayoutWidth;
    private int mLayoutHeight;

    private String defaultStr = "没有要显示的资源";

    public MIMEViewerView(Context context) {
        this(context, null);
    }

    public MIMEViewerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MIMEViewerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        removeAllViews();

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true); // 锯齿不显示

        setWillNotDraw(false); // 设置为重写onDraw方法, 否则onDraw方法不被调用
    }

    public void display(String filename, String mime) {
        removeAllViews();

        if (mime == null) {
            defaultStr = "不支持显示该类型资源";
        } else if (mime.contains("text/html")) {
            // html
            WebView webView = new WebView(getContext());
            webView.loadUrl(Uri.fromFile(new File(filename)).toString());
            addView(webView);
        } else if (mime.contains("text/plain")) {
            // 纯文本
            TextView newView = new TextView(getContext());
            newView.setText(loadFile(filename));
            addView(newView);
        } else if (mime.contains("video")) {
            // 视频文件
            VideoView videoView = new VideoView(getContext());
            videoView.setMediaController(new MediaController(getContext()));
            videoView.setVideoURI(Uri.fromFile(new File(filename)));
            addView(videoView);
        } else if (mime.contains("image")) {
            // 图片文件
            ImageView imageView = new ImageView(getContext());
            imageView.setImageURI(Uri.fromFile(new File(filename)));
            addView(imageView);
        }

        invalidate();
    }

    /**
     * 读取文件内容
     * @param filename 文件名
     * @return 读取失败返回null
     */
    private String loadFile(String filename) {
        String res = null;

        try {
            // 判断文件编码格式
            FileInputStream fis = new FileInputStream(filename);
            BufferedInputStream bin = new BufferedInputStream(fis);
            int p = (bin.read() << 8) + bin.read();
            String code;
            switch (p) {
                case 0xefbb:
                    code = "UTF-8";
                    break;
                case 0xfffe:
                    code = "Unicode";
                    break;
                case 0xfeff:
                    code = "UTF-16BE";
                    break;
                default:
                    code = "GBK";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName(code)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            res = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            mLayoutHeight = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();

            if (getChildCount() == 1) {
                measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
                mLayoutHeight = Math.max(mLayoutHeight, getChildAt(0).getMeasuredHeight());
            }
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        baseLineY = (mLayoutHeight - fontMetrics.top - fontMetrics.bottom) / 2.0f;

        textPaint.setTextSize(mLayoutWidth * 1.0f / defaultStr.length());

        setMeasuredDimension(mLayoutWidth, mLayoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() != 0) {
            getChildAt(0).layout(0, 0, mLayoutWidth, mLayoutHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getChildCount() == 0) {
            canvas.drawText(defaultStr, 16, baseLineY, textPaint);
        } else {
            getChildAt(0).layout(0, 0, mLayoutWidth, mLayoutHeight);
        }
    }
}
