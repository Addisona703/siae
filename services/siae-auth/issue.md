1. 认证服务的login和register方法后面需要使用验证码验证才能颁发token
2. 定时任务：定时清除user_auth表中过期的token信息
3. 考虑后期使用 MapStruct 替代 BeanUtil.copyProperties()