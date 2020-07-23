package cn.how2j.diytomcat;

import cn.how2j.Http.Request;
import cn.how2j.Http.Response;
import cn.how2j.util.Constant;
import cn.how2j.util.ThreadPoolUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Bootstrap {

    public static void main(String[] args) {

        try {
            logJVM();
            //定义端口号
            int port = 18080;
            //判断端口是否使用
//            if(!NetUtil.isUsableLocalPort(port)) {
//                System.out.println(port +" 端口已经被占用了，排查并关闭本端口的办法请用：\r\nhttps://how2j.cn/k/tomcat/tomcat-portfix/545.html");
//                return;
//            }
            //启动一个ServerSocker,打开服务端的端口
            ServerSocket ss = new ServerSocket(port);
            while(true) {
                //请求来了
                final Socket s =  ss.accept();
                ThreadPoolUtil.run(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Request request = new Request(s);
                            String uri = request.getUri();
                            if(null==uri) return;
                            System.out.println("浏览器的输入信息： \r\n" + request.getRequestString());
                            System.out.println("浏览器的uri： \r\n"+request.getUri());

                            //准备一个输出流，把字符串发送出去
                            //HTTP协议格式的
                            Response response = new Response();
                            if(uri.equals("/")){
                                String html = "Hello DIY Tomcat from how2j.cn";
                                response.getWriter().println(html);
                            }else{
                                String fileName = StrUtil.removePrefix(uri,"/");
                                File file = FileUtil.file(Constant.rootFolder,fileName);
                                if(file.exists()){
                                    String fileContent = FileUtil.readUtf8String(file);
                                    response.getWriter().println(fileContent);
                                    if(fileName.equals("timeConsume.html")){
                                        ThreadUtil.sleep(1000);
                                    }
                                }else{
                                    response.getWriter().println("File Not Found");
                                }
                            }
                            handle200(s, response);
                        }catch (Exception e){
                            LogFactory.get().error(e);
                            e.printStackTrace();
                        }
                    }
                });


            }
        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    public static void handle200(Socket s, Response response) throws IOException {
        String contentType = response.getContentType();
        String headText = Constant.response_head_200;
        headText = StrUtil.format(headText, contentType);
        byte[] head = headText.getBytes();

        byte[] body = response.getBody();

        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
        s.close();
    }

    private static void logJVM(){
        Map<String,String> infos = new LinkedHashMap<String, String>();
        infos.put("Server Name","diytomcat/1.0.1");
        infos.put("Server built","2020-7-23");
        infos.put("Server version","1.0.1");
        infos.put("OS NAME\t", SystemUtil.get("os.name"));
        infos.put("OS Version",SystemUtil.get("os.version"));
        infos.put("Architecture",SystemUtil.get("os.arch"));
        infos.put("JAVA Home",SystemUtil.get("java.home"));
        infos.put("JVM Version",SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor",SystemUtil.get("java.vm.specification.vendor"));
        Set<String> keys = infos.keySet();
        for(String key:keys){
            LogFactory.get().info(key+":\t\t"+infos.get(key));
        }
    }
}