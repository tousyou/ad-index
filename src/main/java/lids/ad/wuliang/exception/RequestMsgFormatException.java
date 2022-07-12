package lids.ad.wuliang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RequestMsgFormatException extends RuntimeException{
    public RequestMsgFormatException(String msg){
        super(msg);
    }
}
