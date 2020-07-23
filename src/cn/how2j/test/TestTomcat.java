package cn.how2j.test;

import cn.how2j.util.MiniBrowser;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestTomcat {
    private static final int port = 18080;
    private static final String host = "127.0.0.1";

    /**
     * 每次测试前,会之情此方法
     */
    @BeforeClass
    public static void checkPort(){
        if(NetUtil.isUsableLocalPort(port)){
            System.err.println("未开启diytomcat,请检查端口："+port);
            System.exit(1);
        }else{
            System.out.println("diytomcat已经启动,可以开始测试");
        }
    }

    @Test
    public void testBootstrap(){
        String content = getContent("/");
        Assert.assertEquals(content,"Hello DIY Tomcat from how2j.cn");
    }

    @Test
    public void testHtml(){
        String content = getContent("/a.html");
        Assert.assertEquals(content,"Hello DIY Tomcat from a.html");
    }

    @Test
    public void testConsumeHtml() throws InterruptedException {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20,20,60, TimeUnit.SECONDS
        ,new LinkedBlockingQueue<Runnable>(10));
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    getContent("/timeConsume.html");
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1,TimeUnit.HOURS);
        long duration = timeInterval.intervalMs();
        System.out.println("delayTime:"+duration);
        Assert.assertTrue(duration<3000);
    }

    public String getContent(String path){
        String url  = StrUtil.format("http://{}:{}{}",host,port,path);
        return MiniBrowser.getContentString(url,false);
    }
}
