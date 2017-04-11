package com.wxsk.pusher.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Scope("prototype")
@Component
public class HttpActionController extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LogManager.getLogger(HttpActionController.class);

    private static final String URI_PATH = "/upload";
    private static final String UPLOAD_FILE_ROOT_PATH = "/home/amen/test/upload/";
    private static final byte[] FILE_CONTENT_LINE = "\r\n\r\n".getBytes();
    private static final byte[] NEW_LINE_BYTES = "\r\n".getBytes();
    private static final byte[] CONTENT_DISPOSITION_BYTES = HttpHeaderNames.CONTENT_DISPOSITION.toLowerCase().toByteArray();
    private static final byte[] SEMICOLON_BYTES = ";".getBytes();
    private static final byte[] EQUALS_BYTES = "=".getBytes();
    private static final byte[] DOUBLE_QUOTATION_BYTES = "\"".getBytes();
    private static final String BOUNDARY = "boundary";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (accept(req)) {
            byte[] content = new byte[req.content().readableBytes()];
            req.content().readBytes(content);
            ContentType contentType = analyserContentType(req);
            if (contentType != null) {
                Map<ContentDisposition, byte[]> result = analyseFiles(contentType.boundary, content);
                if (!result.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String partPth = sdf.format(new Date());
                    for (Map.Entry<ContentDisposition, byte[]> entry: result.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().length > 0) {
                            File file = new File(UPLOAD_FILE_ROOT_PATH + partPth + File.separator + entry.getKey().properties.get("filename"));
                            File directory = file.getParentFile();
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }
                            OutputStream os = null;
                            try {
                                os = new BufferedOutputStream(new FileOutputStream(file));
                                os.write(entry.getValue());
                            } catch (IOException e) {
                                logger.error(String.format("upload file error, filename %s", entry.getKey().properties.get("filename")), e);
                            }finally {
                                if (os != null) {
                                    os.close();
                                }
                            }
                        }
                    }
                }
            }
            ByteBuf fileBuf = Unpooled.copiedBuffer("OK".getBytes());
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, fileBuf);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpHeaderUtil.setContentLength(res, fileBuf.readableBytes());
            sendHttpResponse(ctx, req, res);
            ReferenceCountUtil.release(req);
        }
        else {
            ctx.fireChannelRead(req);
        }
    }

    public boolean accept(FullHttpRequest req) {
        return URI_PATH.equals(req.uri());
    }

    public Map<ContentDisposition, byte[]> analyseFiles(String boundary, byte[] content) {
        if (content == null) {
            return Collections.emptyMap();
        }
        Map<ContentDisposition, byte[]> result = new HashMap<>();
        //--与\r\n共4个长度
        int index = boundary.getBytes().length + 4;
        byte[] oneFileBoundaryBytes = ("--" + boundary).getBytes();
        while (index < content.length) {
            //查找文件结束符,去掉最后面的\r\n两个长度
            int endIndex = indexOf(content, oneFileBoundaryBytes, index) - 2;
            if (endIndex > -1) {
                byte[] file = Arrays.copyOfRange(content, index, endIndex);
                int lineIndex = indexOf(file, FILE_CONTENT_LINE, 0);
                byte[] metaData = Arrays.copyOfRange(file, 0, lineIndex);
                byte[] fileContent = Arrays.copyOfRange(file, lineIndex + FILE_CONTENT_LINE.length, file.length);
                ContentDisposition contentDisposition = analyseContentDisposition(metaData);
                //实际文件长度
                index+=file.length;
                //文件结尾换行
                index+=NEW_LINE_BYTES.length;
                //文件结尾结束符+换行符
                index+=oneFileBoundaryBytes.length + 2;
                result.put(contentDisposition, fileContent);
                //已到文件结尾
                if(content[index-1] == '-' && content[index - 2] == '-') {
                    index+=2;
                    break;
                }
            }
        }
        return result;
    }
    public ContentType analyserContentType(FullHttpRequest req) {
        if (req == null) {
            return null;
        }
        String contentTypeStr = (String)req.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeStr != null) {
            String[] elements = contentTypeStr.split(";");
            if (elements.length>1) {
                ContentType contentType = new ContentType();
                contentType.enctype = elements[0];
                contentType.boundary = elements[1].substring(elements[1].indexOf(BOUNDARY) + BOUNDARY.length() + 1);
                return contentType;
            }
        }
        return null;
    }

    public ContentDisposition analyseContentDisposition(byte[] metaData) {
        ContentDisposition contentDisposition = new ContentDisposition();
        String metaDataStr = new String(metaData).toLowerCase();
        byte[] metaDataToUse = metaDataStr.getBytes();
        int begin = indexOf(metaDataToUse, CONTENT_DISPOSITION_BYTES, 0);
        int end = indexOf(metaDataToUse, NEW_LINE_BYTES, begin + CONTENT_DISPOSITION_BYTES.length );
        byte[] contentDispositionBytes = Arrays.copyOfRange(metaData, begin + CONTENT_DISPOSITION_BYTES.length + 1, end);
        byte[] contentDispositionBytesToUse = Arrays.copyOfRange(metaDataToUse, begin + CONTENT_DISPOSITION_BYTES.length + 1, end);
        int nextElementEndIndex = indexOf(contentDispositionBytesToUse, SEMICOLON_BYTES, 0);
        nextElementEndIndex+=SEMICOLON_BYTES.length;
        contentDisposition.enctype =  new String(Arrays.copyOfRange(contentDispositionBytes, 0, nextElementEndIndex - 1));
        contentDisposition.properties = new HashMap<>();
        int max = contentDispositionBytesToUse.length;
        while (nextElementEndIndex < max) {
            int currentIndex = nextElementEndIndex;
            int nextElementEqualIndex = indexOf(contentDispositionBytesToUse, EQUALS_BYTES, currentIndex);
            if (nextElementEndIndex < 0) {
                break;
            }
            int propertyEndIndex = indexOf(contentDispositionBytesToUse, SEMICOLON_BYTES, currentIndex);
            if (propertyEndIndex < 0) {
                propertyEndIndex = max;
            }
            byte[] propertyBytes = Arrays.copyOfRange(contentDispositionBytes, currentIndex, propertyEndIndex);
            int propertyNameEndIndex = indexOf(propertyBytes, EQUALS_BYTES, 0);
            String propertyName = new String(Arrays.copyOfRange(propertyBytes,0, propertyNameEndIndex)).trim();
            int propertyValueBeginIndex = indexOf(propertyBytes, DOUBLE_QUOTATION_BYTES, propertyNameEndIndex + 1);
            String propertyValue = new String(Arrays.copyOfRange(propertyBytes,propertyValueBeginIndex + 1, propertyBytes.length - 1));
            contentDisposition.properties.put(propertyName, propertyValue);
            nextElementEndIndex+=propertyBytes.length + SEMICOLON_BYTES.length;
        }
        return contentDisposition;
    }

    public String readLine(byte[] content, int fromIndex) {
        int index = indexOf(content, NEW_LINE_BYTES, fromIndex);
        if (index > 0) {
            return new String(Arrays.copyOfRange(content, fromIndex, index -1 ));
        }
        return null;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static int indexOf(byte[] source,byte[] target,int fromIndex) {
        if (fromIndex >= source.length) {
            return (target.length == 0 ? source.length : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (target.length == 0) {
            return fromIndex;
        }
        byte first = target[0];
        int max = source.length - target.length;

        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }
            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length - 1;
                for (int k = 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }
        return -1;
    }

    public HttpActionController() {
        super(false);
    }

    private static class ContentType {
        private String enctype;
        private String boundary;
    }
    private static class ContentDisposition {
        private String enctype;
        private Map<String, String> properties;
    }


}
