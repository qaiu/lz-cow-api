云盘解析服务 (nfd云解析)
预览地址 https://lz.qaiu.top  
main分支依赖JDK17, 提供了JDK11分支[main-jdk11](https://github.com/qaiu/netdisk-fast-download/tree/main-jdk11)  
**注意: 请不要过度依赖lz.qaiu.top预览地址服务，建议本地搭建或者云服务器自行搭建。
解析次数过多IP会被部分网盘厂商限制，不推荐做公共解析。**

[![Java CI with Maven](https://github.com/qaiu/netdisk-fast-download/actions/workflows/maven.yml/badge.svg)](https://github.com/qaiu/netdisk-fast-download/actions/workflows/maven.yml)
[![jdk](https://img.shields.io/badge/jdk-%3E%3D17-blue)](https://www.oracle.com/cn/java/technologies/downloads/)
[![vert.x](https://img.shields.io/badge/vert.x-4.5.6-blue)](https://vertx-china.github.io/)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/qaiu/netdisk-fast-download)](https://github.com/qaiu/netdisk-fast-download/releases/latest)

## 项目介绍
网盘直链解析工具能把网盘分享下载链接转化为直链，已支持蓝奏云/蓝奏云优享/奶牛快传/移动云云空间/小飞机盘/亿方云/123云盘/Cloudreve等，支持加密分享。  
**20240929 1k Star留念，0.1.8版本json接口格式有调整，尤其依赖lz.qaiu.top做下载服务的朋友们记得修改， 参考json返回数据格式示例**


*重要声明：本项目仅供学习参考；请不要将此项目用于任何商业用途，否则可能带来严重的后果。转发/分享该项目请注明来源*

## 网盘支持情况:
> 20230905 奶牛云直链做了防盗链，需加入请求头：Referer: https://cowtransfer.com/  
> 20230824 123云盘解析大文件(>100MB)失效，需要登录  
> 20230722 UC网盘解析失效，需要登录  

`网盘名称(网盘标识):`

- [蓝奏云 (lz)](https://pc.woozooo.com/)
- [蓝奏云优享 (iz)](https://www.ilanzou.com/)
- [奶牛快传 (cow)](https://cowtransfer.com/)
- [移动云云空间 (ec)](https://www.ecpan.cn/web)
- [小飞机网盘 (fj)](https://www.feijipan.com/)
- [亿方云 (fc)](https://www.fangcloud.com/)
- [123云盘 (ye)](https://www.123pan.com/)
- [文叔叔 (ws)](https://www.wenshushu.cn/)
- [联想乐云 (le)](https://lecloud.lenovo.com/)
- [QQ邮箱文件中转站 (qq)](https://mail.qq.com/)
- [超星网盘-开发中 (cx)](https://passport2.chaoxing.com/login?newversion=true&refer=https%3A%2F%2Fpan-yz.chaoxing.com%2F)
- [城通网盘(ce)](https://www.ctfile.com)
- [Cloudreve自建网盘(ce)](https://github.com/cloudreve/Cloudreve)    

**TODO:**
  - docker
  - 登录接口, 文件上传/下载/分享后端接口
  - 短地址服务
  - 前端界面(建设中...)

### API接口说明
your_host指的是您的域名或者IP，实际使用时替换为实际域名或者IP，端口默认6400，可以使用nginx代理来做域名访问。    
解析方式分为两种类型直接跳转下载文件和获取下载链接,每一种都提供了两种接口形式parser和网盘标志/分享key拼接的短地址（标志短链），所有规则参考示例。
- 通用接口: `/parser?url=分享链接`，加密分享需要加上参数pwd=密码;
- 标志短链: `/网盘标识/分享key` 在分享Key后面加上@密码;
- 直链JSON: `通用接口`和`标志短链`前加上`/json` 加密分享的密码规则同上;
- 网盘标识参考上面网盘支持情况
- 当带有分享密码时需要加上密码参数(pwd)
- 移动云云空间,小飞机网盘的加密分享的密码可以忽略
- 移动云空间分享key取分享链接中的data参数,比如`&data=xxx`的参数就是xxx

API规则: 
```

1. 解析并自动302跳转 :
    http://your_host/parser?url=分享链接&pwd=xxx
    http://your_host/网盘标识/分享key@分享密码
2. 获取解析后的直链--JSON格式
    http://your_host/json/parser?url=分享链接&pwd=xxx
    http://your_host/json/网盘标识/分享key@分享密码

```
json返回数据格式示例:
```json
{
  "code": 200,
  "msg": "success",
  "success": true,
  "count": 0,
  "data": {
    "shareKey": "lz:xxx",
    "directLink": "下载直链", 
    "cacheHit": true,
    "expires": "2024-09-18 01:48:02",
    "expiration": 1726638482825
  },
  "timestamp": 1726637151902
}
```

IDEA HttpClient示例:

```
# 解析并重定向到直链
### 蓝奏云普通分享
# @no-redirect
GET http://127.0.0.1:6400/parser?url=https://lanzoux.com/ia2cntg
### 奶牛快传普通分享
# @no-redirect
GET http://127.0.0.1:6400/parser?url=https://cowtransfer.com/s/9a644fe3e3a748
### 360亿方云加密分享
# @no-redirect
GET http://127.0.0.1:6400/parser?url=https://v2.fangcloud.com/sharing/e5079007dc31226096628870c7&pwd=QAIU

# Rest请求自动302跳转(只提供共享文件Id):
### 蓝奏云普通分享
# @no-redirect
GET http://127.0.0.1:6400/lz/ia2cntg
### 奶牛快传普通分享
# @no-redirect
GET http://127.0.0.1:6400/cow/9a644fe3e3a748
### 360亿方云加密分享
GET http://127.0.0.1:6400/json/fc/e5079007dc31226096628870c7@QAIU


# 解析返回json直链
### 蓝奏云普通分享
GET http://127.0.0.1:6400/json/lz/ia2cntg
### 奶牛快传普通分享
GET http://127.0.0.1:6400/json/cow/9a644fe3e3a748
### 360亿方云加密分享
GET http://127.0.0.1:6400/json/fc/e5079007dc31226096628870c7@QAIU


```

# 网盘对比


| 网盘名称        | 免登陆下载分享 | 加密分享     | 初始网盘空间    | 单文件大小限制         |
|-------------|---------|----------|-----------|-----------------|
| 蓝奏云         | √       | √        | 不限空间      | 100M            | 
| 奶牛快传        | √       | X        | 10G       | 不限大小            | 
| 移动云云空间(个人版) | √       | √(密码可忽略) | 5G(个人)    | 不限大小            |
| 小飞机网盘       | √       | √(密码可忽略) | 10G       | 不限大小            | 
| 360亿方云      | √       | √(密码可忽略) | 100G(须实名) | 不限大小            | 
| 123云盘       | √       | √        | 2T        | 100G（>100M需要登录） | 
| 文叔叔         | √       | √        | 10G       | 5GB             | 
| 夸克网盘        | x       | √        | 10G       | 不限大小            | 
| UC网盘        | x       | √        | 10G       | 不限大小            | 

# 打包部署

## JDK下载（lz.qaiu.top提供直链云解析服务）
- [阿里jdk17(Dragonwell17-windows-x86)](https://lz.qaiu.top/ec/e957acef36ce89e1053979672a90d219n)
- [阿里jdk17(Dragonwell17-linux-x86)](https://lz.qaiu.top/ec/6ebc9f2e0bbd53b4c4d5b11013f40a80NHvcYU)
- [阿里jdk17(Dragonwell17-linux-aarch64)](https://lz.qaiu.top/ec/d14c2d06296f61b52a876b525265e0f8tzxTc5)
- [解析有效性测试-移动云云空间-阿里jdk17-linux-x86](https://lz.qaiu.top/json/ec/6ebc9f2e0bbd53b4c4d5b11013f40a80NHvcYU)

## 开发和打包

```shell
# 环境要求: Jdk17 + maven;
mvn clean
mvn package

```
打包好的文件位于 web-service/target/netdisk-fast-download-bin.zip
## Linux服务部署
### [宝塔安装参考](https://blog.qaiu.top/archives/netdisk-fast-download-bao-ta-an-zhuang-jiao-cheng)
> 注意: netdisk-fast-download.service中的ExecStart的路径改为实际路径
```shell
cd ~
wget -O netdisk-fast-download.zip  https://github.com/qaiu/netdisk-fast-download/releases/download/0.1.8-release-fixed2/netdisk-fast-download-bin-fixed2.zip
unzip netdisk-fast-download-bin.zip
cd netdisk-fast-download
bash service-install.sh
```
服务相关命令:  

查看服务状态  
`systemctl status netdisk-fast-download.service`
 
启动服务  
`systemctl start netdisk-fast-download.service`

重启服务  
`systemctl restart netdisk-fast-download.service`

停止服务  
`systemctl stop netdisk-fast-download.service`

开机启动服务  
`systemctl enable netdisk-fast-download.servic`

停止开机启动  
`systemctl disable netdisk-fast-download.servic`

## Windows服务部署
1. 下载并解压releases版本netdisk-fast-download-bin.zip
2. 进入netdisk-fast-download下的bin目录
3. 使用管理员权限运行nfd-service-install.bat
如果不想使用服务运行可以直接运行run.bat
> 注意: 如果jdk环境变量的java版本不是17请修改nfd-service-template.xml中的java命令的路径改为实际路径

## 0.1.9 开发计划
- 超星网盘解析 doing
- 带Referer头的js请求下载 doing
- 城通网盘解析 √
- 目录解析(专属版)
- 带cookie/token参数解析大文件(专属版)

**技术栈:**
Jdk17+Vert.x4.4.1
Core模块集成Vert.x实现类似spring的注解式路由API


## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=qaiu/netdisk-fast-download&type=Date)](https://star-history.com/#qaiu/netdisk-fast-download&Date)


## 支持该项目
开源不易，用爱发电，本项目长期维护如果觉得有帮助, 可以请作者喝杯咖啡, 感谢支持  
赞助88元以上, 可以优先体验专享版--大文件解析,目录解析
![image](https://github.com/qaiu/netdisk-fast-download/assets/29825328/54276aee-cc3f-4ebd-8973-2e15f6295819)

[手机端支付宝打赏跳转链接](https://qr.alipay.com/fkx01882dnoxxtjenhlxt53)




