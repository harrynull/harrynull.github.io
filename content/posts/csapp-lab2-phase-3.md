---
title: '[CSAPP] Lab2 Phase 3 解谜笔记'
tags:
  - CS
  - CSAPP
  - 汇编
categories:
  - 学习笔记
date: 2017-08-25 10:29:59
---
介绍：这是一个“拆弹”游戏，玩家要在反汇编中找到每一关的拆弹密码，一共有6个phases。
该Lab的下载地址：[http://csapp.cs.cmu.edu/3e/labs.html](http://csapp.cs.cmu.edu/3e/labs.html)。

**警告：本文含有剧透内容，想要玩Lab2的慎看！**
<!--more-->
首先先看一下phase_3的汇编

```asm
0000000000400f43 <phase_3>:
  400f43:	sub    $0x18,%rsp
  400f47:   lea    0xc(%rsp),%rcx
  400f4c:   lea    0x8(%rsp),%rdx
  400f51:   mov    $0x4025cf,%esi
  400f56:	mov    $0x0,%eax
  400f5b:   callq  400bf0 <__isoc99_sscanf@plt>
  400f60:   cmp    $0x1,%eax
  400f63:	jg     400f6a <phase_3+0x27>
  400f65:	callq  40143a <explode_bomb>
  400f6a:	cmpl   $0x7,0x8(%rsp)
  400f6f:   ja     400fad <phase_3+0x6a>
  400f71:   mov    0x8(%rsp),%eax
  400f75:   jmpq   *0x402470(,%rax,8)
  400f7c:   mov    $0xcf,%eax
  400f81:   jmp    400fbe <phase_3+0x7b>
  400f83:   mov    $0x2c3,%eax
  400f88:   jmp    400fbe <phase_3+0x7b>
  400f8a:   mov    $0x100,%eax
  400f8f:   jmp    400fbe <phase_3+0x7b>
  400f91:   mov    $0x185,%eax
  400f96:   jmp    400fbe <phase_3+0x7b>
  400f98:   mov    $0xce,%eax
  400f9d:   jmp    400fbe <phase_3+0x7b>
  400f9f:   mov    $0x2aa,%eax
  400fa4:   jmp    400fbe <phase_3+0x7b>
  400fa6:   mov    $0x147,%eax
  400fab:   jmp    400fbe <phase_3+0x7b>
  400fad:   callq  40143a <explode_bomb>
  400fb2:   mov    $0x0,%eax
  400fb7:   jmp    400fbe <phase_3+0x7b>
  400fb9:   mov    $0x137,%eax
  400fbe:	cmp    0xc(%rsp),%eax
  400fc2:	je     400fc9 <phase_3+0x86>
  400fc4:	callq  40143a <explode_bomb>
  400fc9:	add    $0x18,%rsp
  400fcd:	retq   
```

这段汇编首先读入了两个数字N1,N2，存到了栈上，下面是等价的伪c代码：
``eax=sscanf(rdi,"%d %d",rsp+0x8,rsp+0xc)``
然后11行这里进行判断，限制N1<=7。
第14行，有趣的事情发生了：``jmpq   *0x402470(,%rax,8)``当时猜了好久什么意思，查了一下发现是间接跳转。
用gdb查一下那个地址有什么值：
```
(gdb) x/8ag 0x402470
0x402470:       0x400f7c <phase_3+57>   0x400fb9 <phase_3+118>
0x402480:       0x400f83 <phase_3+64>   0x400f8a <phase_3+71>
0x402490:       0x400f91 <phase_3+78>   0x400f98 <phase_3+85>
0x4024a0:       0x400f9f <phase_3+92>   0x400fa6 <phase_3+99>
```
果然，这是个跳转表。结合上面的代码可以猜测出来这是个switch。
随便找了一个组合过关。