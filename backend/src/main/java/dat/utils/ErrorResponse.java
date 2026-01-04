package dat.utils;

import io.javalin.http.Context;
import org.slf4j.Logger;

/**
 * Centralized error response utility for consistent API error handling
 */
public class ErrorResponse {

    public static void badRequest(Context ctx, String message) {
        ctx.status(400).json(new ErrorMessage(message));
    }

    public static void unauthorized(Context ctx, String message) {
        ctx.status(401).json(new ErrorMessage(message));
    }

    public static void forbidden(Context ctx, String message) {
        ctx.status(403).json(new ErrorMessage(message));
    }

    public static void notFound(Context ctx, String message) {
        ctx.status(404).json(new ErrorMessage(message));
    }

    public static void conflict(Context ctx, String message) {
        ctx.status(409).json(new ErrorMessage(message));
    }

    public static void unprocessableEntity(Context ctx, String message) {
        ctx.status(422).json(new ErrorMessage(message));
    }

    public static void internalError(Context ctx, String message) {
        ctx.status(500).json(new ErrorMessage(message));
    }

    public static void internalError(Context ctx, String message, Logger logger, Exception e) {
        logger.error(message, e);
        ctx.status(500).json(new ErrorMessage(message + ": " + e.getMessage()));
    }

    public static void notImplemented(Context ctx, String message) {
        ctx.status(501).json(new ErrorMessage(message));
    }

    /**
     * Simple error message wrapper
     */
    private static class ErrorMessage {
        public final String msg;

        public ErrorMessage(String msg) {
            this.msg = msg;
        }
    }
}


