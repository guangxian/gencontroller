# GenController 编译时根据service生成controller
## 主要用于spring项目，根据service生成controller，避免手工重复工作
### 只需要简单的4步

### 第1步
引入
```
<dependency>
    <groupId>io.github.guangxian</groupId>
    <artifactId>gencontroller</artifactId>
    <version>1.0.2</version>
</dependency>
```
### 第2步
创建一个配置类
```
@GenControllerConfig(packagePath = "com.example.demo.web.controller")
public class GenCtrlConfig {

}
```
packagePath必须，指定controller生成的位置
### 第3步
给service添加注解(可以加在接口上也可以加在实现类上)
```
@GenController(name = "user")
public interface UserService {

    UserLoginResponse userLogin(UserLoginRequest request);
	
	@GenControllerMethod(ignore = true)
    DeleteUserResponse deleteUser(DeleteUserRequest request);
	
}
```
### 第4步
启动程序，生成完毕。   
启动后controller会生成至target/generated-source/annotations内

___
## 其他说明
- 在实现类中只会生成public修饰符的方法
- 目前生成后只有POST方式，service内的方法入参和出参，必须为对象类型并只能有一个参数
- @GenController(name = "systemUser")，转换后的controller名称为SystemUserController，路由为"/system-user"
- 如果多个service的GenController.name相同，那么只生成一个controller并包含每个service的方法
- 支持controller返回时自定义包装类型，例如
```
@GenControllerConfig(
        packagePath = "com.example.demo.web.controller",
        responseType = "com.example.demo.response.ResponseVO",
        returnExpression = "ResponseVO.success($L)"
)
```
对应controller片段
```
import com.example.demo.response.ResponseVO;
@PostMapping("/user/user-login")
  public ResponseVO<UserLoginResponse> userLogin(@Validated @RequestBody UserLoginRequest request) {
    UserLoginResponse response = userService.userLogin(request);
    return ResponseVO.success(response);
  }
```
- 支持swagger（springdoc）
