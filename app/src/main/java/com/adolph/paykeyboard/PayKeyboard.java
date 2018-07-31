package com.adolph.paykeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by Adolph on 2018/4/23.
 */

public class PayKeyboard extends View {

    Paint paint;

    float width;
    float height;

    float unit;
    float textUnit;

    String titleMsg;
    int titleColor;
    int titleSize;

    String helpMsg;
    int helpMsgColor;
    int helpMsgSize;

    String[] numberLabel = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del"};
    int numberSize;
    int numberColor;
    int numberBackColor;
    int numberSpecBackColor;
    int numberLineColor;
    int numberPressColor;
    int deleteButtonBg;

    private int currentLength = 0;

    private int[][] preses = {{-1, -1}, {-1, -1}, {-1, -1}}; //save preses index

    private InputListener inputListener;
    private Encryption encryption;
    private ClickListener helpMsgListener;
    private ClickListener titleListener;
    private Object[] pass = new Object[6]; //password is char[6]

    private RectF[] clickable = new RectF[14]; //available click range
    private int[] clickType = {-1, -1, -1}; //save preses index, express have several touch down, -1 express none touch down.

    public PayKeyboard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();

        initAttr(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float ay = 0;  //record y location change, represent each part height
        drawTitle(canvas, 0, width, height / 10, paint);

        ay += height / 10;
        drawPasswordAndEdit(canvas, ay, width, height / 10 * 2.0f, paint);

        ay += height / 10 * 2.0f;
        drawHelpMsg(canvas, ay, width, height / 10 * 2.0f, paint);

        ay += height / 10 * 2.0f;
        drawKeyboard(canvas, ay, width, height / 10 * 5f, paint);
    }

    /*
        draw keyboard
     */
    private void drawKeyboard(Canvas canvas, float ay, float w, float h, Paint p) {
        float rw = w / 3;  //3 columns
        float rh = h / 4; //4 rows
        float lw = unit;  //line width

        p.setColor(numberBackColor);
        p.setStyle(Paint.Style.FILL);

        canvas.drawRect(0, ay, w, ay + h, p); //the keyboard background

        //draw grid line
        p.setColor(numberLineColor);
        p.setStrokeWidth(lw);
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(i * rw, ay, i * rw, ay + h, p);
        }
        for (int i = 0; i < 4; i++) {
            canvas.drawLine(0, ay + rh * i, w, ay + rh * i, p);
        }

        //draw del button and xxx button background
        p.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(lw, ay + rh * 3 + lw, rw - lw, ay + rh * 4 - lw, p);
        canvas.drawRect(rw * 2 + lw, ay + rh * 3 + lw, rw * 3 - lw, ay + rh * 4 - lw, p);

        //draw down button background
        p.setColor(numberColor);
        p.setTextSize(numberSize);
        p.setFakeBoldText(true);
        Rect textRect = new Rect();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                String label = numberLabel[i * 3 + j];
                p.getTextBounds(label, 0, label.length(), textRect);

                float x = j * rw + rw / 2 - textRect.width() / 2;
                float y = ay + i * rh + rh / 2 + textRect.height() / 2;

                for (int[] prese : preses) {
                    if (prese[0] == i && prese[1] == j) {
                        p.setColor(numberPressColor);
                        canvas.drawRect(rw * j + lw, ay + rh * i + lw, rw * j + rw - lw, ay + rh * i + rh - lw, p);
                        p.setColor(numberColor);
                    }
                }

                clickable[i * 3 + j] = new RectF(rw * j + lw, ay + rh * i + lw, rw * j + rw - lw, ay + rh * i + rh - lw);
                if ("del".equals(label)) { //delete button can have image.
                    RectF rect = new RectF(rw * j + rw * 0.25f + lw, ay + rh * i + rh * 0.1f + lw, rw * j + rw - rw * 0.25f - lw, ay + rh * i - rh * 0.1f + rh - lw);
                    canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), deleteButtonBg), null, rect, p);
                } else {
                    canvas.drawText(label, x, y, p);
                }
            }
        }

    }

    /*
        draw help message such as forget PIN..
     */
    private void drawHelpMsg(Canvas canvas, float ay, float w, float h, Paint p) {
        p.setUnderlineText(true);
        p.setColor(helpMsgColor);
        p.setTextSize(helpMsgSize);
        p.setFakeBoldText(false);

        Rect rect = new Rect();
        p.getTextBounds(helpMsg, 0, helpMsg.length(), rect);

        float padding = w * 0.05f;
        float sx = w - rect.width() - padding;
        float sy = ay + (h + rect.height()) / 2;

        canvas.drawText(helpMsg, sx, sy, p);
        clickable[12] = new RectF(sx, sy, sx + rect.width(), sy + rect.height()); //this represent click available range
        p.setUnderlineText(false);
    }

    /*
        show current password length and input box
     */
    private void drawPasswordAndEdit(Canvas canvas, float ay, float w, float h, Paint p) {
        float pt = h * 0.3f;

        float sx = w * 0.05f;
        float ex = sx + w * 0.9f;
        float sy = pt + ay;
        float ey = h + ay;
        float rr = unit * 5;

        p.setColor(Color.LTGRAY);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(unit);
        canvas.drawRoundRect(new RectF(sx, sy, ex, ey), rr, rr, p); //password range background

        float singleW = (ex - sx) / 6;
        p.setColor(Color.LTGRAY);
        p.setStyle(Paint.Style.FILL);
        for (int i = 1; i < 6; i++) {
            canvas.drawLine(sx + singleW * i, sy, sx + singleW * i, ey, p); //password is 6 length so range average 6 shares.
        }

        drawPassword(canvas, currentLength, sx, sy, new RectF(sx, sy, sx + singleW, ey), p);
    }

    /*
        show password depend current input length
     */
    private void drawPassword(Canvas canvas, int length, float startX, float startY, RectF rect, Paint p) {
        p.setColor(Color.BLACK);

        float rw = rect.width();
        float rh = rect.height();

        float pw = rw / 5;
        float ph = rw / 5;

        for (int i = 0; i < length; i++) {
            float sx = startX + rw / 2 + rw * i - pw / 2;
            float sy = startY + rh / 2 - ph / 2;
            float ex = sx + pw;
            float ey = sy + ph;
            canvas.drawRoundRect(new RectF(sx, sy, ex, ey), pw / 2, ph / 2, p); //draw point
        }
    }

    private void drawTitle(Canvas canvas, float ay, float w, float h, Paint p) {
        p.setColor(Color.BLACK);
        p.setTextSize(titleSize);
        p.setFakeBoldText(true);

        Rect rect = new Rect();
        p.getTextBounds(titleMsg, 0, titleMsg.length(), rect);
        float x = (w - rect.width()) / 2;
        float y = (h + rect.height()) / 2;
        canvas.drawText(titleMsg, x, y, p);

        p.setColor(Color.LTGRAY);
        p.setStrokeWidth(unit);
        canvas.drawLine(0, h, w, h, p);
        canvas.drawLine(0, ay, w, ay, p);

        //draw close button
        float sx = (h * 0.3f);
        float ex = (sx + (h * 0.5f));
        float sy = h * 0.25f;
        float ey = h * 0.75f;
        canvas.drawLine(sx, sy, ex, ey, p);
        canvas.drawLine(ex, sy, sx, ey, p);

        clickable[13] = new RectF(sx, sy, ex, ey);  //title can click
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        unit = getResources().getDisplayMetrics().density;
        textUnit = getResources().getDisplayMetrics().scaledDensity;
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PayKeyboard);

        titleMsg = typedArray.getString(R.styleable.PayKeyboard_title_text);
        titleColor = typedArray.getColor(R.styleable.PayKeyboard_title_text_color, Color.BLACK);
        titleSize = typedArray.getInt(R.styleable.PayKeyboard_title_text_size, 18 * TypedValue.COMPLEX_UNIT_SP);

        helpMsg = typedArray.getString(R.styleable.PayKeyboard_help_text);
        helpMsgColor = typedArray.getColor(R.styleable.PayKeyboard_help_text_color, Color.BLUE);
        helpMsgSize = typedArray.getInt(R.styleable.PayKeyboard_title_text_size, 15 * TypedValue.COMPLEX_UNIT_SP);

        numberBackColor = typedArray.getColor(R.styleable.PayKeyboard_number_background_color, Color.WHITE);
        numberColor = typedArray.getColor(R.styleable.PayKeyboard_number_text_color, Color.DKGRAY);
        numberSize = typedArray.getInt(R.styleable.PayKeyboard_title_text_size, 20 * TypedValue.COMPLEX_UNIT_SP);
        numberSpecBackColor = typedArray.getColor(R.styleable.PayKeyboard_number_spec_background_color, Color.LTGRAY);
        numberLineColor = typedArray.getColor(R.styleable.PayKeyboard_number_rect_line_color, Color.LTGRAY);
        numberPressColor = typedArray.getColor(R.styleable.PayKeyboard_number_pressed_color, Color.LTGRAY);

        deleteButtonBg = typedArray.getResourceId(R.styleable.PayKeyboard_delete_button_background, android.R.drawable.ic_input_delete);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = width * 0.9f;
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onPress(event);
                return true; //must return true otherwise move and up action not execute.
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //more than one touch
                onPress(event);
                break;
            case MotionEvent.ACTION_POINTER_UP: //more than one touch
                onUp(event);
                break;
        }

        return super.onTouchEvent(event);
    }

    private void onPressNumberKey(int i, int index) {
        int row = i / 3;
        int col = i % 3;

        preses[index][0] = row; //record last down index
        preses[index][1] = col;

        invalidate();
    }

    private void onMove(MotionEvent event) {
        int index = event.getPointerId(event.getActionIndex());

        if (clickType[index] != -1 && clickType[index] < clickable.length) {
            RectF rect = clickable[clickType[index]];
            if (!rect.contains(event.getX(), event.getY())) {
                clearTouchDown(index);
                invalidate();
            }
        }
    }

    private void onUp(MotionEvent event) {
        int index = event.getPointerId(event.getActionIndex());

        if (clickType[index] != -1) {
            switch (clickType[index]) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    clickNumber(clickType[index]);
                    break;
                case 11:
                    clickDelete();
                    break;
                case 12:
                    if (helpMsgListener != null) {
                        helpMsgListener.onClick(); //click help msg.
                    }
                    break;
                case 13:
                    if (titleListener != null){
                        titleListener.onClick();
                    }
                    break;
            }
            clearTouchDown(index); //finished this touch event.
            invalidate();
        }
    }
    /*
        touch down
     */
    private void onPress(MotionEvent event) {
        int index = event.getPointerId(event.getActionIndex()); //touch index

        for (int i = 0; i < clickable.length; i++) {
            RectF rect = clickable[i];
            if (rect.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        onPressNumberKey(i, index); //number button down
                        clickType[index] = i;
                        break;
                    case 11:
                        onPressNumberKey(i, index); //delete button down
                        clickType[index] = i;
                        break;
                    case 12:
                        clickType[index] = i; //help msg down
                        break;
                    case 13:
                        clickType[index] = i;
                        break;
                }
                break;
            }
        }
    }

    private void clickNumber(int i) {
        if (currentLength < 6) {
            if (encryption != null) {
                pass[currentLength] = encryption.encryptionPW(numberLabel[i]); //encryption PIN code
            } else {
                pass[currentLength] = numberLabel[i];
            }

            currentLength++;  //current input length add
        }

        if (currentLength == 6 && inputListener != null) {
            inputListener.onComplete(pass); //callback some listener
        }
    }

    private void clickDelete() {
        if (currentLength > 0 && currentLength <= 6) {
            currentLength--;
            pass[currentLength] = null;
        }
    }

    private void clearTouchDown(int i) {
        clickType[i] = -1;
        preses[i][0] = -1;
        preses[i][1] = -1;
    }

    public void clearPassword() {
        currentLength = 0;
        for (int i = 0; i < pass.length; i++) {
            pass[i] = null;
        }
        for (int i = 0; i < clickType.length; i++) {
            clickType[i] = -1;
            preses[i][0] = -1;
            preses[i][1] = -1;
        }
        invalidate();
    }

    public void setNumberLabel(String[] labels) {
        if (labels.length < 12) {
            throw new IllegalArgumentException("labels length should more than 12!");
        }

        System.arraycopy(labels, 0, numberLabel, 0, numberLabel.length);

        invalidate();
    }

    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public void setHelpMsgListener(ClickListener helpMsgListener) {
        this.helpMsgListener = helpMsgListener;
    }

    public void setTitleListener(ClickListener titleListener) {
        this.titleListener = titleListener;
    }

    public interface InputListener {
        void onComplete(Object[] pass);
    }

    public interface Encryption {
        Object encryptionPW(String pass);
    }

    public interface ClickListener {
        void onClick();
    }

}
