package com.termux.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ToggleButton;

import com.termux.R;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
public final class ExtraKeysView extends GridLayout {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private boolean bLongClick = false;

    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);

        reload();
    }

    static void sendKey(View view, String keyName) {
        int keyCode = 0;
        String chars = null;
        switch (keyName) {
            case "ESC":
                keyCode = KeyEvent.KEYCODE_ESCAPE;
                break;
            case "TAB":
                keyCode = KeyEvent.KEYCODE_TAB;
                break;
            case "▲":
                keyCode = KeyEvent.KEYCODE_DPAD_UP;
                break;
            case "◀":
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case "▶":
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case "▼":
                keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case "HOME":
                keyCode = KeyEvent.KEYCODE_MOVE_HOME;
                break;
            case "END":
                keyCode = KeyEvent.KEYCODE_MOVE_END;
                break;
            case "PGUP":
                keyCode = KeyEvent.KEYCODE_PAGE_UP;
                break;
            case "PGDN":
                keyCode = KeyEvent.KEYCODE_PAGE_DOWN;
                break;
            case "―":
                chars = "-";
                break;
            default:
                chars = keyName;
        }

        if (keyCode > 0) {
            view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        } else {
            TerminalView terminalView = view.findViewById(R.id.terminal_view);
            TerminalSession session = terminalView.getCurrentSession();
            if (session != null) session.write(chars);
        }
    }

    private ToggleButton controlButton;
    private ToggleButton altButton;
    private ToggleButton fnButton;

    public boolean readControlButton() {
        if (controlButton.isPressed()) return true;
        boolean result = controlButton.isChecked();
        if (result) {
            controlButton.setChecked(false);
            controlButton.setTextColor(TEXT_COLOR);
        }
        return result;
    }

    public boolean readAltButton() {
        if (altButton.isPressed()) return true;
        boolean result = altButton.isChecked();
        if (result) {
            altButton.setChecked(false);
            altButton.setTextColor(TEXT_COLOR);
        }
        return result;
    }

    public boolean readFnButton() {
        if (fnButton.isPressed()) return true;
        boolean result = fnButton.isChecked();
        if (result) {
            fnButton.setChecked(false);
            fnButton.setTextColor(TEXT_COLOR);
        }
        return result;
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Button aResponse = (Button)msg.obj;
            if ((null != aResponse)) {
                sendKey(getRootView(), aResponse.getText().toString());
                removeMessages(1000);
                final Message msgObj = obtainMessage(1000, aResponse);
                handler.sendMessageDelayed(msgObj, 100);
            }
        }
    };

    void reload() {
        altButton = controlButton = null;
        removeAllViews();

        String[][] buttons = {
            {"ESC", "/", "|", "-", "▲", "$", "HOME", "PGUP"},
            {"TAB", "CTRL", "ALT", "◀", "▼", "▶", "PGDN","END"}
        };

        final int rows = buttons.length;
        final int cols = buttons[0].length;

        setRowCount(rows);
        setColumnCount(cols);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final String buttonText = buttons[row][col];

                Button button;
                switch (buttonText) {
                    case "CTRL":
                        button = controlButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
                        button.setClickable(true);
                        break;
                    case "ALT":
                        button = altButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
                        button.setClickable(true);
                        break;
                    case "FN":
                        button = fnButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
                        button.setClickable(true);
                        break;
                    default:
                        button = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
                        break;
                }

                button.setText(buttonText);
                button.setTextColor(TEXT_COLOR);

                final Button finalButton = button;
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finalButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        View root = getRootView();
                        switch (buttonText) {
                            case "CTRL":
                            case "ALT":
                            case "FN":
                                ToggleButton self = (ToggleButton) finalButton;
                                self.setChecked(self.isChecked());
                                self.setTextColor(self.isChecked() ? 0xFF80DEEA : TEXT_COLOR);
                                break;
                            default:
                                sendKey(root, buttonText);
                                break;
                        }
                    }
                });

                button.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        bLongClick = true;
                        final Message msgObj = handler.obtainMessage(1000, finalButton);
                        handler.sendMessageDelayed(msgObj, 100);
                        return true;
                    }
                });

                button.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        View root = getRootView();
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (bLongClick) {
                                handler.removeMessages(1000);
                            }
                        }
                        return false;
                    }
                });

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = param.width = 0;
                param.rightMargin = param.topMargin = 0;
                param.setGravity(Gravity.LEFT);
                float weight = "▲▼◀▶".contains(buttonText) ? 0.7f : 1.0f;
                weight = "/|-$".contains(buttonText) ? 0.1f : weight;
                weight = "HOMEPGUPPGDN".contains(buttonText) ? 1.4f : weight;
                param.columnSpec = GridLayout.spec(col, GridLayout.FILL, weight);
                param.rowSpec = GridLayout.spec(row, GridLayout.FILL, weight);
                button.setLayoutParams(param);

                addView(button);
            }
        }
    }

}
