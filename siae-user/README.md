### Award 问题 
1. User获奖信息排序
获奖类型 > 获奖名次
2. userAwardService：listUserAwardsByPage
   > 这个方法中使用列名来比较是防止sql注入
   

### College 和 major
基本信息直接从数据库添加，不格外写服务
目前不设接口，暂时只通过class服务直接调用mapper，后续业务复杂时在考虑是否添加。

### department 和 position
手动添加，不格外写服务，等后续项目规格大了再写

### UserServiceImpl
密码问题，注册和登录还有设置密码等问题后续处理

### UserThirdPartyAuth
写认证服务在写