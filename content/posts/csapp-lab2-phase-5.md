---
title: '[CSAPP] Lab2 Phase 5 解谜笔记'
tags:
  - CS
  - CSAPP
  - 汇编
categories:
  - 学习笔记
date: 2017-08-25 10:31:22
---
介绍：这是一个“拆弹”游戏，玩家要在反汇编中找到每一关的拆弹密码，一共有6个phases。
该Lab的下载地址：[http://csapp.cs.cmu.edu/3e/labs.html](http://csapp.cs.cmu.edu/3e/labs.html)。

**警告：本文含有剧透内容，想要玩Lab2的慎看！**

<!--more-->

``
惯例先看代码：
```asm
0000000000401062 <phase_5>:
  401062:   push   %rbx
  401063:   sub    $0x20,%rsp
  401067:   mov    %rdi,%rbx
  40106a:   mov    %fs:0x28,%rax
  401073:   mov    %rax,0x18(%rsp)
  401078:   xor    %eax,%eax
  40107a:   callq  40131b <string_length>
  40107f:   cmp    $0x6,%eax
  401082:   je     4010d2 <phase_5+0x70>
  401084:   callq  40143a <explode_bomb>
  401089:	jmp    4010d2 <phase_5+0x70>
  40108b:   movzbl (%rbx,%rax,1),%ecx
  40108f:   mov    %cl,(%rsp)
  401092:   mov    (%rsp),%rdx
  401096:	and    $0xf,%edx
  401099:   movzbl 0x4024b0(%rdx),%edx
  4010a0:   mov    %dl,0x10(%rsp,%rax,1)
  4010a4:   add    $0x1,%rax
  4010a8:   cmp    $0x6,%rax
  4010ac:   jne    40108b <phase_5+0x29>
  4010ae:   movb   $0x0,0x16(%rsp)
  4010b3:   mov    $0x40245e,%esi
  4010b8:   lea    0x10(%rsp),%rdi
  4010bd:   callq  401338 <strings_not_equal>
  4010c2:   test   %eax,%eax
  4010c4:   je     4010d9 <phase_5+0x77>
  4010c6:   callq  40143a <explode_bomb>
  4010cb:   nopl   0x0(%rax,%rax,1)
  4010d0:   jmp    4010d9 <phase_5+0x77>
  4010d2:   mov    $0x0,%eax
  4010d7:   jmp    40108b <phase_5+0x29>
  4010d9:	mov    0x18(%rsp),%rax
  4010de:	xor    %fs:0x28,%rax
  4010e7:	je     4010ee <phase_5+0x8c>
  4010e9:	callq  400b30 <__stack_chk_fail@plt>
  4010ee:	add    $0x20,%rsp
  4010f2:	pop    %rbx
  4010f3:	retq
```
这段汇编出现了一堆之前没见过的操作，比如开头的`mov    %fs:0x28,%rax`，经过查询后是用来进行stack-guard check的，详见[这个回答](https://stackoverflow.com/a/10325915/4358404)。
无视掉那些奇怪的操作以后，转换成伪c语言大约是这样：
```c
rbx=rdi; //input
*(rsp+18)=rax;
eax=string_length(rdi);
if(eax==6)goto 4010d2;
explode_bomb();
goto 4010d2;
40108b:
ecx=*(rbx+rax);
*rsp=cl; //cx低8bit
rdx=*rsp;
edx=edx & 0xf
edx=*(0x4024b0+rdx);
*(rsp+rax+0x10)=dl; //dx低8bit
rax++;
if(rax!=0x6) goto 40108b;
*(rsp+0x16)=0;
if(!strings_not_equal(rsp+0x10,*0x40245e)) goto 4010d9;
explode_bomb();
goto 4010d9;
4010d2:
eax=0;
goto 40108b;
4010d9:
rax=*(rsp+18)
```
经过简单的分析和整理，可得：
```c
rbx=rdi; //input
rax=string_length(rdi);
assert(rax==6);
rax=0;

for(rax=0;rax!=0x6;rax++){
    *rsp=rbx[rax];
    *(rsp+rax+0x10)=(*("maduiersnfotvbyl"+((*rsp)%16)));
}

*(rsp+0x16)=0;
assert(!strings_not_equal(rsp+0x10,"flyers"));
```
所以这段程序其实就是读入一个6个字符的字符串，对每个字符%16，然后映射到"maduiersnfotvbyl"这16个字符中用来构成"flyers"。
所以只要把9 15 14 5 6 7加16直到变成一个可以输入的字符即可。
用Python写一个脚本来找：
```python
>>> ["".join([chr(c+i*16) for c in [9,15,14,5,6,7]]) for i in range(0,10)]
['\t\x0f\x0e\x05\x06\x07', '\x19\x1f\x1e\x15\x16\x17', ")/.%&'", '9?>567', 'IONEFG', 'Y_^UVW', 'ionefg', 'y\x7f~uvw', '\x89\x8f\x8e\x85\x86\x87', '\x99\x9f\x
9e\x95\x96\x97']
```
随便选一个"IONEFG"输进去过关。

