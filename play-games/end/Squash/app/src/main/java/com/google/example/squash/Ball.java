package com.google.example.squash;

public class Ball {

    public double x;
    public double y;

    public double velX;
    public double velY;

    public double BOUNCE_ACCEL = 1.05;

    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Ball(Ball ball) {
        this.x = ball.x;
        this.y = ball.y;
    }

    public boolean move(SquashView sv, long dt) {
        this.x += 1.0 * velX / dt;
        this.y += 1.0 * velY / dt;

        if (x > sv.aspectRatio - SquashView.WALL_THICKNESS) {
            velX *= -1;
            velX *= BOUNCE_ACCEL;
            x = sv.aspectRatio - SquashView.WALL_THICKNESS
                    - SquashView.BALL_RADIUS;
        }

        double paddleY = sv.lastKnownPaddleLocation;

        // XXX Not right---need to check dominant direction, too.
        if (x > SquashView.PADDLE_DISTANCE_0 - SquashView.WALL_THICKNESS
                && x < SquashView.PADDLE_DISTANCE_0 + SquashView.WALL_THICKNESS) {
            if (y > paddleY -SquashView.PADDLE_RADIUS
                    && y < paddleY + SquashView.PADDLE_RADIUS) {
                velX *= -1;
                x = SquashView.PADDLE_DISTANCE_0 + SquashView.WALL_THICKNESS;
                velY = (paddleY - y) / SquashView.PADDLE_RADIUS * 0.013;

                sv.incrementScore(this);
            }
        }

        if (x < 0) {
            return false;
        }

        if (y > 1 - SquashView.WALL_THICKNESS && x > SquashView.WALL_VSTART) {
            velY *= -1;
            y = 1 - SquashView.WALL_THICKNESS - SquashView.BALL_RADIUS;
        }

        if (y < SquashView.WALL_THICKNESS && x > SquashView.WALL_VSTART) {
            velY *= -1;
            y = SquashView.WALL_THICKNESS + SquashView.BALL_RADIUS;
        }

        return true;
    }
}
