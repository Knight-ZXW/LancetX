## LanceX [![Maven Central](https://img.shields.io/maven-central/v/io.github.knight-zxw/lancet-plugin.svg?label=Maven%20Central)](https://repo1.maven.org/maven2/io/github/knight-zxw/lancet-plugin/)

LancetX 是一个为Android项目设计的字节码插桩框架，其使用方式类似AspectJ。

该项目核心实现原理参考了 ele开源的 [lancet](https://github.com/eleme/lancet) 字节码插桩框架，与原有的lancet的不同点在于
本项目的plugin使用字节跳动的ByteX进行 class 文件的并行化 以便加快编译速度。

另外项目还并修复了原有项目的一些BUG，增加了一部分特性，比如提供了 功能分组、单独配置开关的能力。

## 使用

### 安装
在项目根目录的 build.gradle中，引入 ByteX 及 Lancex 插件依赖
```cpp
buildscript {
    repositories {
        //... 其他maven地址
        maven { setUrl("https://artifact.bytedance.com/repository/byteX/") }
    }
    
    dependencies {
      //0.3.0 或其他更高版本
      classpath "com.bytedance.android.byteX:base-plugin:0.3.0"
      classpath "io.github.knight-zxw:lancet-plugin:${lancexVersion}"
    }
}
```

在app目录的 build.gradle 引入 sdk 并配置插件
```cpp
apply plugin: 'bytex'
ByteX {
    enable true
    enableInDebug true
}


apply plugin: 'LancetX'
LancetX{
    enable true
    enableInDebug true

}
dependencies {
    implementation 'io.github.knight-zxw:lancet-runtime:${lancexVersion}'
}


```

### 三分钟示例
LanceX要求所有的字节码织入定义在申明了 @Weaver的类中，类名可以随意定义
```cpp
@Weaver
public class InsertTest{


}
```
在类中，通过在函数上定义使用不同的注解，如 @Proxy @Insert 来定义不同的函数字节码修改行为。

#### Insert
`@Insert ` 类似AspectJ的 @Around ，可以实现在原函数前后插入代码。 比如我们希望监控Activity对象 onCreate函数的耗时，则可以用以下的定义实现
```java
@Insert(mayCreateSuper = true)
@TargetMethod(methodName = "onCreate")
@TargetClass(value = "android.app.Activity", scope = Scope.LEAF)
public void onCreate2(@Nullable Bundle savedInstanceState) {
    long begin = System.currentTimeMillis();
    Origin.callVoid();
    long end = System.currentTimeMillis();
    Activity activity = ((Activity) This.get());
    Log.e("insertTest", activity + " onCreate cost "+(end-begin)+" ms");
    }
```
通过 `@TargetClass` 和 `@TargetMethod` 表明及约束了对象哪些类的哪些函数进行类修改。`@Insert` 的mayCreateSuper 当在目标类未找到目标函数时，是否自动创建该函数被调用父类函数，默认值为false。
其中 `@TargetClass `的 scope参数，可以实现对目标类的进一步约束，`scope` 将在其他小节详细介绍，示例中实现效果 是目标类为 android.app.Activity 的所有最终子类。

示例中的Origin 及 This 是 钩子类，Origin的相关API可以实现对原函数的调用,  而This来说， 你可以把它当成 java中的 this关键字对待，其表示了被编织类运行时的对象，通过getFiled()可以获取当前对象的成员变量，通过 putField 可以修改成员变量的值。
#### Proxy
`@Insert`在底层的实现是查找 目标类中符合的目标函数实现的，但是对于系统的类，比如 android.util
.Log , 并未参与编译流程，这些类最终也不会打包对APK中，因此通过 @Insert 的方式无法进行修改。 虽然我们无法修改Log类及对应的函数实现，但我们可以修改自身代码（非JDK、androd SDK ）中对这些系统代码的调用。
比如 我们的函数中本来调用了 Log.i()函数，可以修改为我们定义的 LogProxy.i() 函数，在LogProxy.i()中对原来的函数调用进行切面操作。
```java
@Weaver
public class LogProxy {

    @Proxy()
    @TargetClass(value = "android.util.Log",scope = Scope.SELF)
    @TargetMethod(methodName = "i")
    public static int replaceLogI(String tag,String msg){
        msg = msg + "lancet";
        return (int) Origin.call();
    }
}
```
## API详解
### Insert注解
类似AspectJ的Around功能，可以实现对原函数实现切面编程，支持在原函数前后插入新的代码，控制原函数的调用(通过Origin钩子)。
### Proxy注解
使用新的函数 替换原有函数的调用， 对于 (JDK/Android SDK)的函数，只能通过proxy的方式修改。
### TargetClass注解
表示修改的目标类
#### Scope
以类的继承体系角度，配置或限定 Insert、Proxy 修改的范围.

- Scope.SELF 代表仅匹配 `value` 指定的目标类
- Scope.DIRECT 代表匹配 `value` 指定类的直接子类（直接继承于目标类的）
- Scope.All 代表匹配 `value` 指定类及其所有子类
- Scope.ALL_CHILDREN  代表匹配 `value` 指定类的所有子类
- Scope.LEAF 代表匹配 `value` 指定类的最终子类 （即没有任何其他类再继承这个类）
### TargetMethod注解
表示修改的目标函数名称
### ClassOf注解
`ClassOf` 用于函数参数中， 实现对无法import类(私有、包级的)的引用
`ClassOf `的 value 一定要按照 `**(package_name.)(outer_class_name$)inner_class_name([]...)**`的模板.
比如:

- java.lang.Object
- java.lang.Integer[][]
- A[]
- A$B
### Origin
`Origin` 用来调用原目标方法. 可以被多次调用.
`Origin.call()` 用来调用有返回值的方法.
`Origin.callVoid()` 用来调用没有返回值的方法.
另外，如果你有捕捉异常的需求.可以使用
`Origin.call/callThrowOne/callThrowTwo/callThrowThree()`
`Origin.callVoid/callVoidThrowOne/callVoidThrowTwo/callVoidThrowThree()`
比如 代理 InputStream的 read函数
```java
@TargetClass("java.io.InputStream")
@TargetMethod(methodName="read")
@Proxy()
public int read(byte[] bytes) throws IOException {
    try {
        return (int) Origin.<IOException>callThrowOne();
    } catch (IOException e) {
        e.printStackTrace();
        throw e;
    }
}
```
### This
仅用于`Insert` 方式的非静态方法的Hook中. 
`get()`
返回目标方法被调用的实例化对象.  相当于java 的this，只不过指向的对象是运行时被修改的那个类的实例对象
`**putField & getField**`
你可以直接存取目标类的所有属性，无论是 protected or private.
另外，如果这个属性不存在，我们还会自动创建这个属性. Exciting!
自动装箱拆箱肯定也支持了.
一些已知的缺陷:

- Proxy 不能使用 This
- 你不能存取你父类的属性. 当你尝试存取父类属性时，我们还是会创建新的属性.

例如:
```java
package com.knightboost.weaver;
public class Main {
    private int a = 1;

    public void nothing(){

    }

    public int getA(){
        return a;
    }
}

@TargetClass("com.knightboost.weaver.Main")
@TargetMethod(methodName="nothing")
@Insert()
public void testThis() {
    Log.e("debug", This.get().getClass().getName());
    This.putField(3, "a");
    Origin.callVoid();
}
```

### 功能分组能力
  你可能会有对不同的插桩功能进行独立开关控制，而不是全局控制，通过 @Group 注解，你可以为某个Weaver类的插桩功能进行分组命名，
  在分组之后你可以在gradle 配置中对这组插桩功能进行单独的开关控制。
 动态配置
 ```java
 @Weaver
 @Group("insertTest")
 public class InsertTest {
 }
 ```

 ``` gradle
apply plugin: 'LancetX'
LancetX{
    enable true //插件开关
    enableInDebug //debug包编译时的插件开关

    weaveGroup{
        //insertTest group所属的字节码修改功能开关
        insertTest {
            enable true
        }
    }
}
 ```

## 底层实现说明
todo

