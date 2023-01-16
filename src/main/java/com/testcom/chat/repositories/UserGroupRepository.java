package com.testcom.chat.repositories;

import java.util.List;

public interface UserGroupRepository {
    List<Integer> findGroupIdByUserId(Integer userId);
}
