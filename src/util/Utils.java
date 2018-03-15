package util;

import encryptor.Encryptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Utils {
    public static String getHex(byte[] reply, int len){
        String ret="";
        for(int i=0;i<len;++i){
            byte b=reply[i];
            int l=b&0xF, h=(b&0xF0)>>4;
            ret+=("\\x"+Integer.toHexString(h)+Integer.toHexString(l));
        }
        return ret;
    }

    /**
     * @param raw
     * @return
     * Object[0]: String    ->  addr
     * Object[1]: Integer   ->  port
     * Object[2]: Integer   ->  Header length
     */
    public static Object[] parseAddress(byte[] raw){
        Object[] ret = new Object[3];
        String addr="";
        int port=0;
        int headerLength=0;
        if(raw[0]==0x00) {
            headerLength = 1 + 4 + 2;
            for(int i=1;i<4;++i)
                addr=addr+String.valueOf(((int)raw[i])&0xFF)+'.';
            addr=addr+String.valueOf(((int)raw[4])&0xFF);
            port=getDWORD(raw,5,false);
        }
        else {
            int len=getDWORD(raw,1,false);
            headerLength = 1 + 2 +len+2;
            addr = new String(ArrayUtils.subarray(raw,1+2,1+2+len),
                    Charset.forName("UTF-8"));
            port = getDWORD(raw,headerLength-2,false);
        }
        ret[0]=addr;
        ret[1]=port;
        ret[2]=headerLength;
        return ret;
    }

    public static byte[] wrapAddress(byte[] content, String address, int port, boolean isIP){
        //TODO
        int headerLength=0;
        byte[] addr = address.getBytes(Charset.forName("UTF-8"));
        int len=addr.length;
        if(isIP) {
            headerLength=1+4+2;
        }
        else{
            headerLength=1+2+len+2;
        }
        byte[] ret = new byte[headerLength+content.length];
        if(isIP)
            ret[0]=0x00;
        else
            ret[0]=0x01;
        putDWORD(ret,1,len,false);
        putArray(ret,3,addr);
        putDWORD(ret,len+3,port,false);
        putArray(ret,headerLength,content);
        return ret;
    }

    public static boolean putDWORD(byte[] dst, int idx, int DWORD, boolean BE){
        if(dst.length<idx+2)
            return false;
        if(!BE){
            dst[idx]=(byte)(DWORD&0xFF);
            dst[idx+1]=(byte)((DWORD>>8)&0xFF);
        }
        else{
            dst[idx+1]=(byte)(DWORD&0xFF);
            dst[idx]=(byte)((DWORD>>8)&0xFF);
        }
        return true;
    }

    public static boolean putArray(byte[] dst, int idx, byte[] src){
        if(dst.length<idx+src.length)   return false;
        for(int i=0;i<src.length;++i)
            dst[i+idx]=src[i];
        return true;
    }

    public static int getDWORD(byte[] src, int idx, boolean BE){
        assert src.length>idx+1;
        int ret=0;
        int d0,d1;
        try {
            d0 = ((int) src[idx]) & 0xFF;
            d1 = ((int) src[idx + 1]) & 0xFF;
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        if(BE){
            ret=(d0<<8)+d1;
        }
        else{
            ret=(d1<<8)+d0;
        }
        return ret;
    }

    public static ByteBuf createFrame(byte[] encrypted){
        ByteBuf resp = ByteBufAllocator.DEFAULT.buffer();
        resp.writeShort(encrypted.length);
        resp.writeBytes(encrypted);
        return resp;
    }

    public static byte[] encryptBuf(ByteBuf bb, Encryptor encryptor){
        byte[] raw = new byte[bb.readableBytes()];
        bb.readBytes(raw);
//        return encryptor.encrypt(raw);
        return raw;
    }

    public static byte[] decryptBuf(ByteBuf bb, Encryptor encryptor){
        byte[] encrypted = new byte[bb.readableBytes()];
        bb.readBytes(encrypted);
//        return encryptor.decrypt(encrypted);
        return encrypted;
    }

    public static void main(String[] argv) throws Exception {
        String msg="Bright";
        byte[] bmsg=msg.getBytes("UTF-8");
        Encryptor encryptor = new Encryptor("aes-256-cfb","wangbowenmax");
        ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
        bmsg=wrapAddress(bmsg,"www.baidu.com",443,false);
        Object[] addr=parseAddress(bmsg);
        bb.writeBytes(bmsg);
        ByteBuf frame=createFrame(encryptBuf(bb,encryptor));
        byte[] bframe = new byte[frame.readableBytes()];
        frame.readBytes(bframe);

        int a=0;
    }
}
