package com.wtmbuy.wtmbuymall.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wtmbuy.wtmbuymall.R;
import com.wtmbuy.wtmbuymall.util.UIUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据外部传入的数据集合，实现显示文字的水平滚动的按钮集合，
 * 点击每个按钮，实现加载不同的数据
 *
 * @author huanghui
 */
public class HorizontalScrollTabView extends RelativeLayout {
    private HorizontalScrollView mHorizontalScrollView;
    private LinearLayout mLayoutContainer;
    private LinearLayout.LayoutParams params;
    private LinearLayout.LayoutParams paramsLine;
    private int leftPadding;
    private float textSize;
    private int textColor;
    private int lineColor;
    private OnItemClickListener listener;
    private ArrayList<TextView> mAllTextViews = new ArrayList<>();

    public HorizontalScrollTabView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalScrollTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public interface OnItemClickListener {
        public void onItemClick(int index);
    }

    private void init(Context context) {
        mHorizontalScrollView = new HorizontalScrollView(context);
        mHorizontalScrollView.setLayoutParams(
            new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.d_50dp)));
        mHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        mHorizontalScrollView.setFillViewport(true);

        mLayoutContainer = new LinearLayout(context);
        mLayoutContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mLayoutContainer.setOrientation(LinearLayout.HORIZONTAL);
        mLayoutContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
        mHorizontalScrollView.addView(mLayoutContainer);
        addView(mHorizontalScrollView);

        //初始化默认值
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsLine = new LinearLayout.LayoutParams((int) UIUtil.getInstance().getDensity() * 1, LinearLayout.LayoutParams.MATCH_PARENT);
        leftPadding = (int) UIUtil.getInstance().getDensity() * 15;
        int lineMarginTop = (int) UIUtil.getInstance().getDensity() * 10;
        paramsLine.setMargins(0, lineMarginTop, 0, lineMarginTop);
        textSize = getResources().getDimension(R.dimen.d_18dp);
        textColor = getResources().getColor(R.color.c_common);
        lineColor = getResources().getColor(R.color.c_line_color);
    }

    /**
     * 初始化文字大小，文本颜色，分割线颜色
     *
     * @param resDimenId
     * @param textColor
     * @param lineColor
     */
    public void initData(int resDimenId, int textColor, int lineColor) {
        this.textSize = getResources().getDimension(resDimenId);
        this.textColor = getResources().getColor(textColor);
        this.lineColor = getResources().getColor(lineColor);
    }

    public void loadData(Context context, List<?> list, String propertyName, OnItemClickListener listener) {
        loadData(context, list, propertyName, 0, listener);
    }

    /**
     * 加载数据，生成一个个按钮
     *
     * @param context
     * @param list
     * @param propertyName   属性名，list集合中的对象需要显示到按钮上的文字属性，在该对象中需要提供getter方法,如果是字符串集合，则传空字符串
     * @param scrollPosition 需要滚动到的位置，默认为0，也就是第一个
     * @param listener       点击每个按钮的监听
     */
    public void loadData(Context context, List<?> list, String propertyName, int scrollPosition, OnItemClickListener listener) {
        this.listener = listener;
        mAllTextViews.clear();
        mLayoutContainer.removeAllViews();
        String methodName = null;
        if (!TextUtils.isEmpty(propertyName)) {
            //取出第一个字母,转化为大写
            String firstLetter = propertyName.substring(0, 1).toUpperCase();
            methodName = "get" + firstLetter + propertyName.substring(1);
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object object = list.get(i);
            if (TextUtils.isEmpty(methodName) || object instanceof String) {
                TextView textView = initTextView(context, (String) object, i, scrollPosition);
                View view = initLineView(context);
                mLayoutContainer.addView(textView);
                mLayoutContainer.addView(view);
            } else {
                Class classType = object.getClass();
                try {
                    Method method = classType.getMethod(methodName, new Class[]{});
                    String name = (String) method.invoke(object, new Object[]{});
                    TextView textView = initTextView(context, name, i, scrollPosition);
                    View view = initLineView(context);
                    mLayoutContainer.addView(textView);
                    mLayoutContainer.addView(view);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TextView initTextView(Context context, String name, final int index, int defaultScrollPosition) {
        final TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setPadding(leftPadding, 0, leftPadding, 0);
        textView.setText(name);
        textView.setTextColor(textColor);
        textView.setBackgroundResource(R.drawable.selector_brand_tab);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setGravity(Gravity.CENTER);
        textView.setTag(index);
        if (index == defaultScrollPosition) {
            textView.setSelected(true);
            textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    textView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    scrollToPosition(textView, index);
                }
            });
        }
        mAllTextViews.add(textView);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                //滚动scrollView，使当前选中的项滚动到中间
                int size = mAllTextViews.size();
                for (int i = 0; i < size; i++) {
                    TextView tv = mAllTextViews.get(i);
                    if (index == i) {
                        tv.setSelected(true);
                    } else {
                        tv.setSelected(false);
                    }
                }
                scrollToPosition(v, index);
                if (listener != null) {
                    listener.onItemClick(index);
                }
            }
        });
        return textView;
    }

    private void scrollToPosition(View view, int i) {
        int width = 0;
        for (int j = 0; j < i; j++) {
            width += mAllTextViews.get(j).getMeasuredWidth();
        }
        int measureWidth = view.getMeasuredWidth();
        int current = (width + measureWidth) - UIUtil.getInstance().getScreenWidth() / 2;
        mHorizontalScrollView.smoothScrollTo(current, 0);
    }

    private View initLineView(Context context) {
        View view = new View(context);
        view.setLayoutParams(paramsLine);
        view.setBackgroundColor(lineColor);
        return view;
    }

}
