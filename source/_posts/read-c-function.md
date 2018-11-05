---
title: '解析int(*((*ptr(int,int))))(int);'
tags:
  - C++
categories:
  - 学习笔记
date: 2017-09-10 18:10:40
---

“顺时针阅读法([The Clockwise/Spiral Rule](http://c-faq.com/decl/spiral.anderson.html))”是一种很简单方便的用来解析c中复杂类型的方法。

<!--more-->

用`int(*((*ptr(int,int))))(int);`作为例子：
先从`ptr 是……`开始：
从ptr开始，向右阅读，遇到(int,int)，说明ptr是一个函数，接受参数(int,int)。
`ptr 是 一个函数 接受(int,int) 返回……`

然后转到左边，遇到*，代表ptr的返回值是个指针。

`ptr 是 一个函数 接受(int,int) 返回 一个指针，指向……`

目前的情况大概是：

`int(*((ptr)))(int);`
去掉两对括号后：

`int(*ptr)(int);`
右边遇到)，再到左边，遇到了*，代表这又是个指针。现在我们有：

`ptr 是 一个函数 接受(int,int) 返回 一个指针，指向 一个指针 指向……`

再向右，遇到(int)，代表接下来是一个函数。现在有：

`ptr 是 一个函数 接受(int,int) 返回 一个指针，指向 一个指针 指向 一个函数 接受(int) 返回……`

再转一圈，遇到最后的int。完整的ptr的类型是：

`ptr 是 一个函数 接受(int,int) 返回 一个指针，指向 一个指针 指向 一个函数 接受(int) 返回int`

或者可以换一个语序：

`ptr 是 一个接受(int,int)返回(一个指向(一个指向(一个接受(int)返回int的函数)的指针)的指针)的函数`

构造一段代码来测试一下这个结果：
```cpp
int foo(int){return 0;} //一个接受(int)返回int的函数

decltype(&foo) address_of_foo=&foo; //一个指向(一个接受(int)返回int的函数)的指针

int(*((*ptr(int,int))))(int) { //一个接受(int,int) 返回(一个指向(一个指向(一个接受(int)返回int的函数)的指针)的指针)的函数
    return &address_of_foo; //一个指向(一个指向(一个接受(int)返回int的函数)的指针)的指针
}
```

那么怎么才能把这个转换成一般人能读懂的代码呢，其实加一个alias就可以大大增强可读性：
```cpp
using FuncType = int(int);
FuncType** ptr2(int,int);
```
简单易懂。

本文原写为[知乎某问题的答案](https://www.zhihu.com/question/65116993/answer/227939489)。