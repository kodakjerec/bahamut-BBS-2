package com.kota.Telnet;

import static java.lang.Thread.sleep;

import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WSSocketChannel implements TelnetSocketChannel {

    WebSocket _socket;
    String _receive_string = "";
    boolean isConnect = false;
    public WSSocketChannel() throws IOException, InterruptedException {
//        curl 'https://term.gamer.com.tw/bbs' --http1.1 \
//        -H 'Origin: https://term.gamer.com.tw' \
//        -H 'Sec-WebSocket-Key: OYYH/aLfG4nMS1p4+EAS7A==' \
//        -H 'Upgrade: websocket' \
//        -H 'Connection: Upgrade' \
//        -H 'Sec-WebSocket-Version: 13' \
//        -H 'Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits' \
//        --compressed --output
        String address = "wss://term.gamer.com.tw/bbs";
        String origin = "https://term.gamer.com.tw";
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(60, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(address)
                .header("Origin",origin)
                .build();

        _socket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket","連線成功");
                isConnect = true;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                _receive_string = text;
                Log.d("WebSocket","收到訊息" + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                _receive_string = String.valueOf(bytes);
                Log.d("WebSocket","收到訊息" + bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket","連線關閉中");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket","連線關閉");
                isConnect = false;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("WebSocket","發生錯誤" + t.getMessage());
                isConnect = false;
            }
        });

        while(!isConnect) {
            sleep(1000);
        }
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        if (_receive_string.length()>0) {
            String strByteArray = _receive_string.substring(_receive_string.indexOf("hex=")+4, _receive_string.length()-1);
            for(int i = 0; i < strByteArray.length(); i+=2){
                byte b = (byte) ((Character.digit(strByteArray.charAt(i), 16) << 4) + Character.digit(strByteArray.charAt(i+1), 16));
                buffer.put(b);
            }
        }
        return buffer.array().length>0?1:0;
    }

    @Override
    public int write(ByteBuffer buffer) throws IOException {
        String message = new String(buffer.array(), "big5");
        boolean isSuccess = this._socket.send(message);
        return isSuccess?1:0;
    }

    @Override
    public boolean finishConnect() throws IOException {
        return this._socket.close(1000,null);
    }
}
