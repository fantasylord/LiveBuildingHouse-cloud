package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.LiveWhitelist;
import com.livehouse.vo.WhitelistBatchImportResult;

import java.util.List;

/**
 * 直播白名单服务接口
 */
public interface LiveWhitelistService extends IService<LiveWhitelist> {

    /**
     * 分页查询白名单列表
     */
    Page<LiveWhitelist> listPage(int pageNum, int pageSize, Long sessionId, String phone);

    /**
     * 获取白名单详情
     */
    LiveWhitelist getDetail(Long id);

    /**
     * 新增白名单
     */
    boolean addWhitelist(LiveWhitelist whitelist);

    /**
     * 批量添加白名单
     */
    WhitelistBatchImportResult batchImport(Long sessionId, String content);

    WhitelistBatchImportResult batchImportCustomers(Long sessionId, List<Long> customerIds, String remark);

    /**
     * 删除白名单
     */
    boolean deleteWhitelist(Long id);

    /**
     * 检查手机号是否在白名单中
     */
    boolean isInWhitelist(Long sessionId, String phone);
}
