package com.testcom.chat.repositories.impls;

import com.testcom.chat.repositories.UserGroupRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class UserGroupRepositoryImpl implements UserGroupRepository {
    /**
     * 組裝假數據，真實環境應該從數據庫數據
     */
     private HashMap<Integer, List<Integer>> userGroup = new HashMap<>(4);

     @PostConstruct
     public void init() {
        List<Integer> list = Arrays.asList(1, 2);
        userGroup.put(1, list);
        userGroup.put(2, list);
        userGroup.put(3, list);
        userGroup.put(4, list);
     }

     @Override
     public List<Integer> findGroupIdByUserId(Integer userId) {
         return this.userGroup.get(userId);
     }
}
