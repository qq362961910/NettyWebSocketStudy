package com.wxsk.pusher.helper;

import com.wxsk.cas.client.interceptor.AccessRequiredAdminInteceptor;
import com.wxsk.passport.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserHelper {

    @Autowired
    private AccessRequiredAdminInteceptor accessRequiredAdminInteceptor;

    public User getUserByTicket(String ticket) {
        return accessRequiredAdminInteceptor.getCurrentLoginUser(ticket);
    }

}
