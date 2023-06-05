# 关于项目

[Unifycom](https://github.com/Unifycom/Unifycom) 项目简化了TCP、UDP、蓝牙、串口等远程通信编程复杂度，统一客户端和服务端编程界面，将通信报文或通讯状态转换成事件驱动，用户关注于报文解码、编码和事件处理业务即可。  
支持以下特性：  
1、根据连接串创建客户端或者服务器；  
2、客户端断线自动重连，可设置重连周期，默认5秒；  
3、支持客户端通道空闲心跳维持，默认5秒；  
4、支持将通道建立成功、断开、空闲等状态转换为事件；  
5、支持扫描注解将报文转换为事件，相反，支持注解将事件处理结果转换为报文；  
6、内置报文分组处理，支持按照接收到的报文自定义规则分组，确保同组报文顺序处理，不同组报文并行处理，提升吞吐率；  
7、内置tcp proxy protocol v2协议解码器，确保获得穿透代理的客户端真实IP； 
8、支持事件拦截器（Interceptor）以及全局事件拦截器。

## 架构图
![架构图](https://raw.githubusercontent.com/Unifycom/Unifycom/main/architecture.png)

1、L0 底层通讯层，已经实现TCP客户端、服务器端，和串口的客户端；  
2、L1 报文解码、编码层，将通道中接收到的字节流解码为报文对象，或将报文对象编码为字节流给管道发送出去，报文编码解码器需要用户实现，已内置部分通用报文编码解码器；  
3、L2 报文分发层，将报文对象通过L3层事件解码器转换为事件，并调用对应的处理器（`EventHandler`）处理完成，事件处理结果通过L3层的编码器转换为报文对象；  
4、L3 事件解码、编码层，将报文对象通过解码器转换为事件对象，或将事件处理结果通过编码器转换为报文对象，事件编码解码器需要用户实现；  
5、L4 应用层，应用层处理器（`EventHandler`）将事件处理完成后，返回事件处理结果（`EventResult`），事件处理器需用户实现。  

## 工程依赖图
```
                                       pom.xml[parent]
                                             |
                                          unifycom
                                             |
                           +-----------------+--------------+
                           |                                |
                           V                                V
                    unifycom-netty                 unifycom-websocket
                           |
           +---------------+---------------+
           |               |               |
           V               V               V
     unifycom-tcp   unifycom-rxtx  unifycom-bluetooth
```
各模块功能

|          模块          |            功能                              |
|-----------------------|----------------------------------------------|
|pom.xml[parent]        |项目POM，管理Maven项目模块、依赖、版本              |
|unifycom                 |项目框架，封装了Channel基础接口、事件、拦截器和分发器  |
|unifycom-netty           |基于Netty Channel的封装，让使用Netty Channel的子项目使用更简单，例如 断线重连|
|unifycom-tcp             |基于Netty的TCP Channel，已实现客户端和服务端|
|unifycom-rxtx            |基于Netty的串口通道|
|unifycom-bluetooth       |基于Netty的蓝牙通道，已实现客户端|
|unifycom-websocket       |基于Undertow的Websocket通道，已实现客户端和服务端|
## 如何使用
以TCP为例，创建客户端
```
TcpChannel channel = new TcpChannel("localhost:8080", decoder, encoder, dispatcher);
channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
channel.connect().blockUntilConnected();
```
创建服务器端
```
TcpServerChannel channel = new TcpServerChannel("8080", decoder, encoder, dispatcher);
channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
channel.startup();
```
主要的区别只有两处：  
1、客户端创建TcpChannel对象，服务器端创建TcpServerChannel；  
2、客户端端connect启动连接，服务器端startup启动监听；  

> 更多使用方法见“[example](Unifycom/-/tree/main/example)”

## 路线图
1、[已完成]把Tcp和串口做完善，增加串口服务器，把文档完善，增加example；  
4、增加蓝牙、UDP、Websocket等通道。  

## 常见问题
1、协议帧已经解码为Message对象，为什么不直接暴露Message给Handler，而是要转换为Event？
> 首先，这是项目分层问题。
> Message处在协议层，Message是对协议的完整翻译，确保100%还原报文到Message对象。因为协议设计的需要，协议报文中有很多非业务字段，例如 报文头中的ID、序号、版本号、校验符合等等。
> 而Handler在业务层，业务层并不关心非业务字段，例如 协议版本、报文ID等，所以需要把业务关系的字段从Message传递给Event对象，在Handler仅可见Event的业务信息即可。

