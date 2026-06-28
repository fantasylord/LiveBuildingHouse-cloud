package com.livehouse.common.constants;

/**
 * 系统常量配置类
 */
public class SystemConstants {

    private SystemConstants() {
    }

    /**
     * 超级管理员用户ID
     */
    public static final Long SUPER_ADMIN_USER_ID = 1L;

    /**
     * 超级管理员角色ID
     */
    public static final Long SUPER_ADMIN_ROLE_ID = 1L;

    /**
     * 菜单状态 - 可见
     */
    public static final Integer MENU_VISIBLE = 1;

    /**
     * 菜单状态 - 隐藏
     */
    public static final Integer MENU_HIDDEN = 0;

    /**
     * 删除标记 - 未删除
     */
    public static final Integer DELETED_NO = 0;

    /**
     * 删除标记 - 已删除
     */
    public static final Integer DELETED_YES = 1;

    /**
     * 状态 - 启用
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 状态 - 禁用
     */
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 根菜单父ID
     */
    public static final Long ROOT_MENU_PARENT_ID = 0L;
}