package cn.how2j.Http;

import cn.how2j.util.MiniBrowser;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    private String requestString;
    private String uri;
    private Socket client;

    public Request(Socket socket) throws IOException {
        //获取到连接的客户端Socket
        InputStream inputStream = socket.getInputStream();
        this.client = socket;
        parseRequestString();
        if(StrUtil.isBlank(this.requestString)){
            return;
        }
        parseUri();
    }

    public void parseRequestString() throws IOException {
        InputStream inputStream = this.client.getInputStream();
        byte[] bytes = MiniBrowser.getByteArrayByIs(inputStream);
        //读到的字节数组用utf8编码
        requestString = new String(bytes,"utf-8");
    }

    public void parseUri(){
        String temp = StrUtil.subBetween(this.requestString," "," ");
        if(StrUtil.contains(temp,'?')){
            this.uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp,'?',true);
        this.uri = temp;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getUri() {
        return uri;
    }
}
