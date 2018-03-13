package diskIO;

import java.io.*;
import java.util.Random;

/**
 * Created by @author linxin on 13/03/2018.  <br>
 */
public class ReadTest {

    public static void main(String[] args) throws IOException {
//        SeqRread("/Users/ericens/Movies/1.mkv","/Users/ericens/Movies/2.mkv");
//        RandomRread("/Users/ericens/Movies/1.mkv","/Users/ericens/Movies/2.mkv");

        seqWrite("/Users/ericens/Movies/1.mkv","/Users/ericens/Movies/2.mkv");

    }

//    每秒55w每秒的随机读

//    RandomRread:read count per second:556282
//    RandomRread:read count per second:556039
//    RandomRread:read count per second:555495
//    RandomRread:read count per second:555796
    public static void RandomRread(String sourcePath,String  destPath) throws IOException {

        File source = new File(sourcePath);


        RandomAccessFile raf=new RandomAccessFile(source,"r");
        byte[] buf = new byte[512];
        int len = 0;
        long time = 0;
        long count=0;
        long size=0;
        long s = System.currentTimeMillis();
        long seek=2;
        Random random=new Random();
        while(count<1024*1024*1024+1024){
            while ((len = raf.read(buf)) != -1) {
                raf.seek(seek);
                long e = System.currentTimeMillis();

                while(raf.getFilePointer()+seek>1024*1024*1024+1024){
                    seek=random.nextInt();
                }

                time += (e-s);
                size +=len;
                count++;
                if(count%10000==0){
                    System.out.println("RandomRread:read count per second:"+count*1000/time);
                }
                s = System.currentTimeMillis();
            }
        }
        System.out.print("RandomRread:read:size:"+size+"read:" + time + "ms,");

        raf.close();
    }

    public static void SeqRread(String sourcePath,String  destPath) throws IOException {

        File source = new File(sourcePath);

        FileInputStream fis = new FileInputStream(source);

        byte[] buf = new byte[512];
        int len = 0;
        long time = 0;
        long count=0;
        long size=0;
        long s = System.currentTimeMillis();
        while ((len = fis.read(buf)) != -1) {
            long e = System.currentTimeMillis();
            time += (e-s);
            size +=len;
            count++;
            if(count%10000==0){
                //每秒钟可以读75w次左右，顺序读
                System.out.println("SeqRread:read count perCount:"+count*1000/time);
            }
            s = System.currentTimeMillis();
        }

        System.out.print("SeqRread:read:size:"+size+"read:" + time + "ms,");

        fis.close();
    }


    public static void seqWrite(String sourcePath,String  destPath) throws IOException {

        File source = new File(sourcePath);
        File dest = new File(destPath);

        if (!dest.exists()) {
            dest.createNewFile();
        }else{
            dest.delete();
        }

        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);

        byte[] buf = new byte[512];
        int len = 0;
        long time = 0;
        long count=0;
        long size=0;
        long s = System.currentTimeMillis();
        while ((len = fis.read(buf)) != -1) {

            size +=len;
            count++;
            if(count%10000==0){
                System.out.println("SeqRread:read count perCount:"+count*1000/time);
            }

            s = System.currentTimeMillis();
            fos.write(buf, 0, len);
            long e = System.currentTimeMillis();
            time += (e-s);
        }

        System.out.print("SeqRread:read:size:"+size+"read:" + time + "ms,");

        fis.close();
        fos.close();
    }
}
