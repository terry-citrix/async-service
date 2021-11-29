package com.terrydu.asyncservice.api.servlet.controller;

import java.io.IOException;
import java.util.Queue;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class TerryWriteListener implements WriteListener {

    private ServletOutputStream output = null;
    private Queue queue = null;
    private AsyncContext context = null;

    TerryWriteListener(ServletOutputStream sos, Queue q, AsyncContext c) {
        output = sos;
        queue = q;
        context = c;
    }

    @Override
    public void onWritePossible() throws IOException {
        while (queue.peek() != null && output.isReady()) {
            String data = (String) queue.poll();
            output.print(data);
        }
        if (queue.peek() == null) {
            context.complete();
        }
    }

    @Override
    public void onError(final Throwable t) {
        context.complete();
        t.printStackTrace();
    }
}
