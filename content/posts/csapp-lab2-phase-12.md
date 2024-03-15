---
title: '[CSAPP] Lab2 Phase 1,2 解谜笔记'
tags:
  - CS
  - CSAPP
  - 汇编
categories:
  - 学习笔记
date: 2017-08-25 10:25:25
---

介绍：这是一个“拆弹”游戏，玩家要在反汇编中找到每一关的拆弹密码，一共有6个phases。
该Lab的下载地址：[http://csapp.cs.cmu.edu/3e/labs.html](http://csapp.cs.cmu.edu/3e/labs.html)。

**警告：本文含有剧透内容，想要玩Lab2的慎看！**

<!--more-->

先看phase_1
```asm
0000000000400ee0 <phase_1>:
  400ee0:	sub    $0x8,%rsp
  400ee4:   mov    $0x402400,%esi
  400ee9:	callq  401338 <strings_not_equal>
  400eee:	test   %eax,%eax
  400ef0:	je     400ef7 <phase_1+0x17>
  400ef2:	callq  40143a <explode_bomb>
  400ef7:	add    $0x8,%rsp
  400efb:	retq   
```
很短很简单，第2行和第8行是分配和释放栈空间，用来存放局部变量。
第3行将地址放到esi，并在第4行调用了函数strings_not_equal。根据寄存器表
![reg.jpg](https://i.loli.net/2017/08/23/599d6f7191aa0.jpg)
可以看出这里的调用是``strings_not_equal(input, *0x402400)``
然后第5,6行进行了比较，如果字符串相同才不炸。
剩下的就简单了，用gdb查一下那个地址存着什么就可以了：
```
(gdb) x/s 0x402400
0x402400:       "Border relations with Canada have never been better."
```
输入该字符串过关。

接下来是phase2
```asm
0000000000400efc <phase_2>:
  400efc:	push   %rbp
  400efd:	push   %rbx
  400efe:	sub    $0x28,%rsp
  400f02:   mov    %rsp,%rsi    ;rsi=rsp
  400f05    callq  40145c <read_six_numbers> ;read 6 numbers into rsp,+4,+8,+12,+16,+20
  400f0a:   cmpl   $0x1,(%rsp)
  400f0e:	je     400f30 <phase_2+0x34> ;N1==1
  400f10:	callq  40143a <explode_bomb>
  400f15:	jmp    400f30 <phase_2+0x34>
  400f17:	mov    -0x4(%rbx),%eax       ;eax=*(rbx-4)
  400f1a:	add    %eax,%eax             ;eax*=2
  400f1c:	cmp    %eax,(%rbx)
  400f1e:	je     400f25 <phase_2+0x29> ;if *rbx==eax
  400f20:	callq  40143a <explode_bomb> ;         |
  400f25:   add    $0x4,%rbx             ; rbx+=4
  400f29:   cmp    %rbp,%rbx
  400f2c:   jne    400f17 <phase_2+0x1b> ;if rbx!=rbp ->400f17
  400f2e:   jmp    400f3c <phase_2+0x40> ;else ->400f3c
  400f30:   lea    0x4(%rsp),%rbx        ;rbx=rsp+4
  400f35:   lea    0x18(%rsp),%rbp       ;rbp=rsp+24
  400f3a:   jmp    400f17 <phase_2+0x1b> ;->400f17
  400f3c:   add    $0x28,%rsp
  400f40:	pop    %rbx
  400f41:	pop    %rbp
  400f42:	retq   
```
汇编的含义已经注释在代码里了，一开始用函数read_six_numbers读入了6个数到栈上（那个函数我读了半天(╯‵□′)╯︵┻━┻），然后观察可以看出来中间的部分其实是一个for循环，17-19行是判断语句，20-22行是初始化语句，11-16行则是循环体。转换成伪c代码大约是这样的：
```c
for(rbx=rsp+4,rbp=rsp+24;rbx!=rbp;rbx+=4){
    if((*(rbx-4))*2!=*rbx) explode_bomb();
}
```
结合第8行N1需要等于1的要求，很明显这段代码是要求6个由1开头每次*2的等比数列，即1 2 4 8 16 32。