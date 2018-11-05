---
title: '[CSAPP] Lab2 Phase 4 解谜笔记'
tags:
  - CS
  - CSAPP
  - 汇编
categories:
  - 学习笔记
date: 2017-08-25 10:30:49
---

介绍：这是一个“拆弹”游戏，玩家要在反汇编中找到每一关的拆弹密码，一共有6个phases。
该Lab的下载地址：[http://csapp.cs.cmu.edu/3e/labs.html](http://csapp.cs.cmu.edu/3e/labs.html)。

**警告：本文含有剧透内容，想要玩Lab2的慎看！**
<!--more-->
首先先看一下phase_4的汇编
```asm
000000000040100c <phase_4>:
  40100c:	sub    $0x18,%rsp
  401010:   lea    0xc(%rsp),%rcx
  401015:   lea    0x8(%rsp),%rdx
  40101a:   mov    $0x4025cf,%esi
  40101f:	mov    $0x0,%eax
  401024:   callq  400bf0 <__isoc99_sscanf@plt>=
  401029:   cmp    $0x2,%eax
  40102c:	jne    401035 <phase_4+0x29>
  40102e:	cmpl   $0xe,0x8(%rsp)
  401033:   jbe    40103a <phase_4+0x2e>
  401035:   callq  40143a <explode_bomb>
  40103a:   mov    $0xe,%edx
  40103f:	mov    $0x0,%esi
  401044:   mov    0x8(%rsp),%edi
  401048:   callq  400fce <func4>
  40104d:   test   %eax,%eax
  40104f:   jne    401058 <phase_4+0x4c>
  401051:   cmpl   $0x0,0xc(%rsp)
  401056:	je     40105d <phase_4+0x51>
  401058:	callq  40143a <explode_bomb>
  40105d:	add    $0x18,%rsp
  401061:	retq   
```
通过汇编分析可知，第7行处调用了`sscanf(rdi, *0x4025cf, rsp+0x8, rsp+0xc)`，其中*0x4025cf即"%d %d"，将两个数读入到了栈上（后用N1,N2代指这两个数）。
接着在第11行处可以分析出N1<=0xe。
16行又是一个函数调用（参数个数未知，估计是3个），`func4(rdi,0,0xE)`.
通过后面的代码可以推测出func4的返回值为0且N2为0。所以现在的目标就是找到N1使func4(N1,0,0xE)==0。

然后来看看func4吧：
```asm
0000000000400fce <func4>: ;edi=N1, esi=0, edx=0xE
  400fce:	sub    $0x8,%rsp
  400fd2:   mov    %edx,%eax ;eax=arg3
  400fd4:   sub    %esi,%eax ;eax-=arg2
  400fd6:   mov    %eax,%ecx  ;ecx=eax=arg3-arg2
  400fd8:   shr    $0x1f,%ecx ;ecx>>=0x1f (logic)
  400fdb:	add    %ecx,%eax ;eax+=ecx
  400fdd:	sar    %eax     ;eax/=2
  400fdf:	lea    (%rax,%rsi,1),%ecx ;ecx=rax+rsi
  400fe2:	cmp    %edi,%ecx
  400fe4:	jle    400ff2 <func4+0x24> ;ecx<=edi -> 400ff2
  400fe6:	lea    -0x1(%rcx),%edx  ;edx=rcx-1
  400fe9:	callq  400fce <func4>
  400fee:	add    %eax,%eax
  400ff0:	jmp    401007 <func4+0x39>
  400ff2:	mov    $0x0,%eax
  400ff7:   cmp    %edi,%ecx
  400ff9:   jge    401007 <func4+0x39>
  400ffb:   lea    0x1(%rcx),%esi
  400ffe:   callq  400fce <func4>
  401003:   lea    0x1(%rax,%rax,1),%eax
  401007:   add    $0x8,%rsp
  40100b:	retq   
```
然后将其手动翻译成等价的伪C代码：
```c
func4(edi=N1, esi=0, edx=0xE){
    eax=edx;
    eax-=esi;
    ecx=eax=edx-esi;
    ecx>>=0x1f (logic);
    eax+=ecx;
    eax/=2;
    ecx=rax+rsi;
    if(ecx<=edi) goto 400ff2;
    edx=rcx-1;
    func4(edi,esi,edx);
    eax*=2;
    goto 401007;
400ff2:
    eax=0;
    if(ecx>=edi) goto 401007;
    esi=rcx+1;
    func4(edi,esi,edx);
    eax=rax+rax+1;
401007:
    return eax;
}
func4(N1,0,0xE);
```
然后再稍微整(魔)理(改)下：
```c
func4(edi=N1, esi=0, edx=0xE){
    ret=(edx-esi+sign(edx-esi))/2; //sign(ecx)是ecx的符号位
    ecx=ret+esi;
    if(ecx>edi){
        return func4(N1,esi,ecx-1)*2;
    }
    if(ecx<edi){
        return func4(N1,ecx+1,edx)*2+1;
    }
    return 0;
}
func4(N1,0,0xE);
```
这里发现中间的代码是两个if，然后前面的移位其实是在取符号位。
可以看出这一段代码其实是类似于二分查找的东西，那么想让返回值为0的话，需要ecx>=edi。edi直接取0试了一下，竟然过了。
所以N1=0,N2=0。
phase4的答案为"0 0"。