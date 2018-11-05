---
title: test %eax %eax 是什么
tags:
  - 汇编
url: 28.html
id: 28
categories:
  - 学习笔记
date: 2017-08-25 10:31:59
---
上次做lab2的时候看到了
```asm
test   %eax,%eax
je     400ef7
```
第一次看到这个代码的时候我下意识以为这就是jmp了，毕竟eax和eax比较当然相等辣。后来想想，编译器既然会生成这种代码肯定是有原因的。

<!--more-->

我先在 Intel® 64 and IA-32 Architectures Software Developer’s Manual 中查到了这几个指令：
je:
> 74 cb JE: Jump short if equal (ZF=1).

jne:
> 75 cb JNE: Jump short if not equal (ZF=0).

test:
> Operation:
TEMP ← SRC1 AND SRC2;
SF ← MSB(TEMP);
IF TEMP = 0
THEN ZF ← 1;
ELSE ZF ← 0;
FI:
PF ← BitwiseXNOR(TEMP[0:7]);
CF ← 0;
OF ← 0;
(* AF is undefined *)

可以看出test %eax %eax之后，ZF=(SRC1&SRC2==0)

由于SRC1和SRC2都是同一个eax，那么当eax==0时，ZF=1，触发JE；当eax!=0的时候，ZF=0，触发JNE。

所以这一段其实是等价于`if(!eax) jmp 0x400ef7;`，而不是无条件跳转。