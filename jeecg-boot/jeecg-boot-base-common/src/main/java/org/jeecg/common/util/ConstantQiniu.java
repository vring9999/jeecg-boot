package org.jeecg.common.util;

import com.google.gson.Gson;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ConstantQiniu {

    private static String accessKey;
    private static String secretKey;
    private static String bucket;
    private static String path;

    @Value("${qiniu.accessKey}")
    public void setAccessKey(String accessKey) {
        ConstantQiniu.accessKey = accessKey;
    }

    @Value("${qiniu.secretKey}")
    public void setSecretKey(String secretKey) {
        ConstantQiniu.secretKey = secretKey;
    }

    @Value("${qiniu.bucket}")
    public void setBucket(String bucket) {
        ConstantQiniu.bucket = bucket;
    }

    @Value("${qiniu.path}")
    public void setPath(String path) {
        ConstantQiniu.path = path;
    }

    /**
     * 将图片上传到七牛云   MultipartFile multipartFile, String fileName
     */
    public Result<?> uploadQNImg(MultipartFile multipartFile,Result<?> result) {
        byte[] bytes = getBytesWithMultipartFile(multipartFile);
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone2());
        // 其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //图片名
        String orgName = multipartFile.getOriginalFilename();// 获取文件名
        String imageName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
//      String imageName = DateUtil.getCurrentTime("yyyyMMddHHmmssSSS");
        // 生成上传凭证，然后准备上传
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket,imageName,3600,new StringMap().put("insertOnly", 1));
            try {
                //超时响应
                Response response = uploadManager.put(bytes, imageName, upToken);
                // 解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                String returnPath = path + "/" + putRet.key;
                result.setMessage(returnPath);
                result.setSuccess(true);
//                json.put("errorCode","10000");
//                json.put("src", returnPath);
                return result;
            } catch (QiniuException ex) {
                Response r = ex.response;
                try {
                    log.error("七牛云解析：{}",r.bodyString());
                } catch (QiniuException ex2) {
                    log.error("QiniuException:{}", ex2);
                }
                result.setSuccess(false);
                result.setMessage("解析失败，请检查网络");
//                json.put("errorCode", 500);
//                json.put("msg", "解析失败，请检查网络");
            }
        } catch (Exception e) {
            log.error("{}", e);
            result.setSuccess(false);
            result.setMessage("上传失败");
//            json.put("errorCode", 500);
//            json.put("msg", "上传失败");
        }
        return result;
    }

    public byte[] getBytesWithMultipartFile(MultipartFile multipartFile) {
        try {
            return multipartFile.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public  boolean testQr(FileInputStream file){
        boolean flag = false;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap, hints);// 对图像进行解码
//            JSONObject content = JSONObject.parseObject(result.getText());
            String mes = result.getText();
            log.info("mes:{}",mes);
//            log.info("author:{},zxing: {} ",content.getString("author"),content.getString("zxing"));
            log.info("encode:{} ",result.getBarcodeFormat());
            flag = true;
        } catch (Exception e) {
            log.error("二维码解析失败:{}",e);
            e.printStackTrace();
        }
        return flag;
    }



}