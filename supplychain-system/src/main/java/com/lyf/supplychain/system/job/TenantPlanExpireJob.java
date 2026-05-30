package com.lyf.supplychain.system.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysMessage;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.service.MessageCenterService;
import com.lyf.supplychain.system.service.SysTenantService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户套餐到期扫描任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@Component
public class TenantPlanExpireJob {

    private final SysTenantService tenantService;

    private final MessageCenterService messageCenterService;

    public TenantPlanExpireJob(SysTenantService tenantService, MessageCenterService messageCenterService) {
        this.tenantService = tenantService;
        this.messageCenterService = messageCenterService;
    }

    /**
     * 扫描临期和已到期租户，发送站内信提醒并更新到期状态。
     */
    @XxlJob("tenantPlanExpireJob")
    public void scanTenantPlanExpire() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        List<SysTenant> expiringTenants = tenantService.list(new LambdaQueryWrapper<SysTenant>()
                .between(SysTenant::getPlanEndTime, now, sevenDaysLater)
                .eq(SysTenant::getIsDeleted, 0));
        expiringTenants.forEach(this::sendExpireWarning);

        boolean updated = tenantService.update(new LambdaUpdateWrapper<SysTenant>()
                .set(SysTenant::getStatus, SystemConstants.TENANT_STATUS_EXPIRED)
                .lt(SysTenant::getPlanEndTime, now)
                .eq(SysTenant::getStatus, SystemConstants.TENANT_STATUS_ENABLED)
                .eq(SysTenant::getIsDeleted, 0));
        log.info("租户套餐到期扫描完成，临期租户数={}，到期状态更新={}", expiringTenants.size(), updated);
    }

    private void sendExpireWarning(SysTenant tenant) {
        if (tenant.getAdminUserId() == null) {
            log.warn("租户套餐即将到期但没有管理员用户，tenantId={}", tenant.getId());
            return;
        }
        SysMessage message = new SysMessage();
        message.setTenantId(tenant.getId());
        message.setReceiverType("USER");
        message.setReceiverId(tenant.getAdminUserId());
        message.setTitle("套餐即将到期提醒");
        message.setContent("您的套餐将在 " + tenant.getPlanEndTime() + " 到期，请及时续费。");
        message.setBizType("TENANT_PLAN_EXPIRE");
        message.setBizId(String.valueOf(tenant.getId()));
        message.setPriority("HIGH");
        message.setReadStatus(0);
        message.setCreateTime(LocalDateTime.now());
        messageCenterService.save(message);
        log.info("发送租户套餐到期提醒，tenantId={}，adminUserId={}", tenant.getId(), tenant.getAdminUserId());
    }
}
