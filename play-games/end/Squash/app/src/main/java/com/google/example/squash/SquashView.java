package com.google.example.squash;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SquashView extends View implements OnTouchListener {

    private final Rect mRect = new Rect();

    public double lastKnownPaddleLocation = 0.0f;
    public static final double WALL_VSTART = 0.5;

    public static final int STATE_RUNNING = 2;
    public static final int STATE_GAME_OVER = 3;
    public static final double PADDLE_DISTANCE_0 = 0.1;

    public static final double WALL_THICKNESS = 0.05;
    public static final double RANDOM_Y_SPEED = 0.01 * 30;
    public static final double SLOW_SPEED = 0.011 * 30;
    public static final double BALL_RADIUS = 0.025;
    public static final double PADDLE_RADIUS = 0.12;
    public static final double LAUNCH_SPEED_BOOST = 0.005 * 30;

    public static final long GAME_OVER_WAIT_TIME = 2 * 1000;

    public int mState = STATE_GAME_OVER;
    public int mScore = 0;
    public int mLaunchScore = 0;

    public ArrayList<Ball> balls = new ArrayList<Ball>();
    public ArrayList<Ball> livingBalls = new ArrayList<Ball>();

    public double mLaunchSpeed = 0;

    // Index into the participant array.
    public int currentPlayer = 0;

    public static final String TAG = "SquashView";

    public SquashView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
    }

    public SquashView(Context context) {
        super(context);

        mState = STATE_GAME_OVER;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        invalidate();
    }

    Paint p = new Paint();

    Boolean keepAnimating = false;

    public void setAnimating(Boolean val) {
        if (val && !keepAnimating) {
            mLastFrameTime = System.currentTimeMillis();
        }
        keepAnimating = val;
        if (val) {
            invalidate();
        }
    }

    double aspectRatio;
    double heightInPixels;

    // Convert back from screenspace
    public int sp(double screenSpaceCoordinate) {
        return (int) Math.round(screenSpaceCoordinate * heightInPixels);
    }

    long mLastFrameTime;
    long mShowMenuTime = 0;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = System.currentTimeMillis();
        long dt = currentTime - mLastFrameTime;
        mLastFrameTime = currentTime;

        int w = this.getWidth();
        int h = this.getHeight();

        aspectRatio = 1.0 * w / h;
        heightInPixels = h;

        // Not sure why I keep getting a null cliprect
        canvas.clipRect(0, 0, w, h);

        // Draw the background
        mRect.top = 0;
        mRect.bottom = h;
        mRect.left = 0;
        mRect.right = w;

        p.setColor(0xFF000000);
        canvas.drawRect(mRect, p);

        p.setColor(0xffffffFF);

        // Draw the side
        mRect.top = 0;
        mRect.bottom = sp(1);
        mRect.left = sp(aspectRatio - WALL_THICKNESS);
        mRect.right = sp(aspectRatio);

        canvas.drawRect(mRect, p);

        // Draw top and bottom rails
        mRect.top = 0;
        mRect.bottom = sp(WALL_THICKNESS);
        mRect.left = sp(0.5);
        mRect.right = sp(aspectRatio);

        canvas.drawRect(mRect, p);

        mRect.top = sp(1 - WALL_THICKNESS);
        mRect.bottom = sp(1);
        mRect.left = sp(0.5);
        mRect.right = sp(aspectRatio);

        canvas.drawRect(mRect, p);

        // Draw paddle 0 on the left, paddle 1 on the right
        double paddleY = Math.max(0 + PADDLE_RADIUS + WALL_THICKNESS,
                lastKnownPaddleLocation);
        paddleY = Math.min(1.0 - PADDLE_RADIUS - WALL_THICKNESS,
                paddleY);

        mRect.top = sp(paddleY - PADDLE_RADIUS);
        mRect.bottom = sp(paddleY + PADDLE_RADIUS);
        {
            mRect.left = sp(PADDLE_DISTANCE_0 - BALL_RADIUS);
            mRect.right = sp(PADDLE_DISTANCE_0 + BALL_RADIUS);
        }
        canvas.drawRect(mRect, p);

        if (balls != null) {
            for (Ball ball : balls) {
                mRect.top = sp(ball.y - BALL_RADIUS);
                mRect.bottom = sp(ball.y + BALL_RADIUS);
                mRect.left = sp(ball.x - BALL_RADIUS);
                mRect.right = sp(ball.x + BALL_RADIUS);

                canvas.drawRect(mRect, p);

                if (ball.move(this, dt)) {
                    livingBalls.add(ball);
                }

                // XXX Render trail here?
            }
        }

        balls.clear();
        balls.addAll(livingBalls);
        livingBalls.clear();

        if (balls.size() == 0 && mState == STATE_RUNNING) {
            endGame();
        }

        if (mState == STATE_GAME_OVER) {
            p.setColor(0xAAAAAAAA);
            p.setTextSize(sp(0.07));
            canvas.drawText("Game Over!!", sp(0.25), sp(0.5), p);
        }

        if (mScore > 0) {
            p.setColor(0xAAAAAAFF);
            p.setTextSize(sp(0.08));
            canvas.drawText("Score: " + mScore, sp(0.25), sp(0.7), p);
        }

        if (mShowMenuTime != 0 && currentTime >= mShowMenuTime) {
            ((SquashActivity) getContext()).onGameStop(this);
            mShowMenuTime = 0;
        }

        if (keepAnimating) {
            invalidate();
        }
    }

    public void splitBall(Ball ball) {
        Ball p = new Ball(ball.x, ball.y);
        p.velX = mLaunchSpeed;
        p.velY = (Math.random() - 0.5) * RANDOM_Y_SPEED;

        mLaunchSpeed += LAUNCH_SPEED_BOOST;

        livingBalls.add(p);
    }

    public void start()
    {
    	mState = STATE_RUNNING;

        mScore = 0;
        mLaunchScore = 3;
        mLaunchSpeed = SLOW_SPEED;

        balls = new ArrayList<Ball>();
        serve();
        ((SquashActivity) getContext()).onGameStart(this);
    }

    public void serve() {
        Ball p = new Ball(PADDLE_DISTANCE_0 + 0.1, 0.5);
        p.velX = mLaunchSpeed;
        p.velY = (Math.random() - 0.5) * RANDOM_Y_SPEED;

        mLaunchSpeed += 0.01;

        currentPlayer = 0;

        balls.add(p);
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {

        int action = arg1.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                //if (participants == null)
                //  break;
                double lastKnown = arg1.getY() / this.getHeight();
                lastKnownPaddleLocation = lastKnown;// * 500.0;

                break;
        }

        return true;
    }

    // Important for scoring and achievements
    public void incrementScore(Ball ball) {
        mScore++;

        if (mScore == mLaunchScore) {
            mLaunchScore = mScore + 5;
            splitBall(ball);
        }
    }

    public void endGame() {
        mState = STATE_GAME_OVER;
        mShowMenuTime = System.currentTimeMillis() + GAME_OVER_WAIT_TIME;
    }

}
