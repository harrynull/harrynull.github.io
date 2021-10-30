---
title: Hackergame 2021 Writeup
date: 2021-10-30 11:01:09
tags:
  - CTF
  - 解谜
categories:
  - writeup
---

Hackergame (中国科学技术大学第八届信息安全大赛) 传送门：[hackergame](https://hack.lug.ustc.edu.cn/)

这次摸了个 rank 31 (卷不动了)。这里挑了几道感觉比较有意思的题目写 write-up。

![rk.jpg](https://i.loli.net/2021/10/30/jG3nUAL1Bt59g7K.jpg)
<!--more-->

## 卖瓜

> 你：你瞧瞧现在哪有瓜啊？这都是大棚的瓜，只有 6 斤一个和 9 斤一个的，你嫌贵我还嫌贵呢。
>
> HQ：给我来 20 斤的瓜。
>
> 你：行！
> 
> HQ：行？这瓜能称出 20 斤吗？
>
> 你：我开水果摊的，还不会称重？
> 
> HQ：我问你这瓜能称出 20 斤吗？
> 
> 你：你是故意找茬，是不是？你要不要吧！
> 
> HQ：你这瓜要是刚好 20 斤吗我肯定要啊。那它要是没有怎么办啊？
> 
> 你：要是不是 20 斤，我自己吃了它，满意了吧？

![melon.jpg](https://i.loli.net/2021/10/30/fDsuhjzM9d2Bt8x.jpg)

一开始考虑用小数，但是发现就算是直接改 post payload 也不会接受小数输入（好像会被直接 truncate 掉）。顺便试了一下负数也不可以。

既然小数和负数都不行，那很大的数呢？试了一下 2147483647 也不会导致溢出。本以为是不行了，然后突然想起来时代变了，现在都是 64 位了。试了一下 `9223372036854775807/6 = 1537228672809130000 + 6`，果然溢出了。

```php
  intval(6+1537228672809130000*6)+(1537228672809128622)*6 == 20
```

## 透明的文件
这道题目给了一个包括很多 ANSI escape sequence 的文件，形如下
```
[0;0H[20;58H[8;34H[13;27H[4;2H[38;2;1;204;177m [39m[14;10H[20;51H[23;4H[12;2H[38;2;2;207;173m [39m[19;61H[9;12H[22;8H[20;2H[38;2;3;210;169m [39m[3;23H[8;68H[19;10H[4;3H[38;2;4;214;165m [39m[19;23H[17;34H[11;52H[22;70H[12;3H[38;2;5;217;161m [39m[24;22H[2;25H[19;76H[19;3H[38;2;6;220;157m [39m[23;14H[21;12H[10;37H[2;37H[22;66H[16;45H[21;3H[38;2;7;222;153m [39m[10;47H[18;34H[23;3H[38;2;8;225;149m
```

简单看了下发现并没有空格以外的字符，而且所有 \e 也都被弄没了，也就是说肯定没法通过 `echo -e $(cat transparent.txt)` 这种方法直接查看到 flag。

于是上网找到了一个 python 的 [ANSI 解析库](https://github.com/helgefmi/ansiterm)，魔改了一下代码让它支持不带 `\e` 的 escape sequence，然后输出所有修改过的位置：

```python
    term = Ansiterm(25, 80)
    term.feed(f.read())
    tiles = Counter()
    for y in range(25):
        for tile in term.get_tiles(y * 80, y * 80 + 80):
            tiles[repr(tile.color)] += 1
    default = "{'fg': 37, 'bg': 40, 'reverse': False, 'bold': False}"
    print('\n'.join(
        ''.join(['x' if repr(tile.color) != default else ' ' for tile in term.get_tiles(y * 80, y * 80 + 80)]) for y
        in range(25)))
```

输出如下
```

    xx  xx                       xx          x
   x     x     xxx              x            x
 xxxxx   x        x    xxx      x     xxx    x     x   x
   x     x     xxxx   x   xx  xx         x   xxxx   x x   xxxx  xxxx
   x     x    x   x   x   x     x     xxxx   x   x   x    x   x x   x
   x     x    x   x    xxxx     x    x   x   x   x  x x   x   x x   x
   x      xx   xxx x      x      xx   xxx x  xxxx  x   x  x   x x   x
                       xxx
   x          x      x             xx
              x      x              x
 xxx    xxx   x      x  x   xxx     x     xxx xx    xxxx  xxx  x  x  x
   x   x   x  xxxx   x x       x    x     x  x  x  x     x   x x  x  x
   x   x   x  x   x  xx     xxxx    x     x  x  x  x     x   x x  x  x
   x   x   x  x   x  x x   x   x    x     x  x  x  x     x   x  x x x
    x   xxx   x   x  x  x   xxx x    xx   x  x  x   xxxx  xxx    x x

                         x     x   xxx   xx                 xx
  xxxx   xxx   x   x    x         x   x   x           x       x
 x          x  x   x  xxxxx  xxx  x   x   x    xxx  xxxxx     x
  xxxx   xxxx   xxxx    x      x   xxxx   x   x   x   x        xx
      x x   x      x    x      x      x   x   xxxxx   x       x
  xxxx   xxx x    x     x      x  x   x   x   x       x  x    x
                 x      x       x  xxx     xx  xxx     xx   xx
```

## FLAG 助力大红包

![ip.jpg](https://i.loli.net/2021/10/30/tcrkp2EdU3iGeNq.jpg)
这题是个 pdd 套路，需要 256 个不同的 /8 ip 地址砍一刀才能获得 flag。

> 4. 每个用户只能够助力一次。为了建设世界一流大砍刀平台，活动要求位于同一 /8 网段的用户将会被视为同一个用户。（比如 IP 地址为 202.38.64.1 和 202.39.64.1 将被视为同一用户。）达到助力次数上线后，将无法再帮助好友助力。我们使用前后端方式检查用户的 IP 。

简单试了下，发现是经典的 `X-FORWARDED-FOR`，于是直接写脚本构造请求。唯一的坑就是有请求限速，所以我还加了个重试循环。

```python
succeed_ip=[]
while len(succeed_ip)!=256:
    for i in range(0,256):
        ip = f'{i}.1.1.1'
        if ip in succeed_ip: continue
        txt=requests.post('http://202.38.93.111:10888/invite/9900a95d-3a58-47c6-8a88-89a093ff9f16',
                            data={'ip':ip}, headers={'X-FORWARDED-FOR':ip}).text
        suc = '成功' in txt
        print(ip, suc)
        if suc:
            succeed_ip.append(ip)
```

## 加密的 U 盘

这道题给了两个 LUKS （Linux Unified Key Setup）加密的镜像，其中第一个镜像的密码是已知的。解密出来发现是一个`随机过程.txt`，里面是一些数学笔记，并没有什么卵用。加密方式看了眼是 `aes-xts-plain64`。

……然后我就跑偏了，以为是一个 known-plaintext attack。啃了半天资料然后看了一眼这道题 `general` 的 tag，又看了下通关人数，感觉不大对。然后我在一次错误的解密尝试中，想到了该不会 passphrase 和加密用的 key 是不同的吧（废话）。于是就了解到了 master key 这么个东西。

![meme1.jpg](https://i.loli.net/2021/10/30/mBenN6JK4z72FrM.jpg)

```sh
 sudo cryptsetup luksDump --dump-master-key /dev/loop1p1 # 获得 masterkey 的
 cat masterkey| xxd -r -p > masterkey.bin
 sudo cryptsetup --master-key-file masterkey.bin luksOpen /dev/loop1p1 day2
 mount /dev/mapper/day2 ./day2
 mkdir day2
 sudo mount /dev/mapper/day2 ./day2
 cat day2/flag.txt
```

## 图之上的信息

从 graphql 上拿数据的白给题。

![graphql.gif](https://i.loli.net/2021/10/30/kZHUCPoj7fSBFph.gif)

## 赛博厨房

这道题本质上是个挖矿题（雾）。前两题都是教程，就不讲了。第四小题我没做出来，也不讲了（x），重点说下第三题。

通过阅读代码得知，每一天的菜谱是由 `sha256( '\n'.join(sha256(prog) for prog in programs) )` 算出来的值作为随机数种子，然后 ARC4 随机生成出来的。由于 hash 和随机数算法都挺正经的，大概是没法直接碰撞了。

看了下这道题目有 32 种不同的菜，随机挑选 6 个（有顺序），那么有 P(32,6)=652458240 种可能性。感觉只要运气好，还是可以跑出来的。于是手动实现了一下 ARC4 随机算法（没找到现成的库），然后用巨快的 pypy 跑（其实 cpp 应该更快，只是懒了）。

代码写得丑就不贴了，思路就是先创建 32 个程序分别对应菜谱为 0,0,0,0,0,0; 1,1,1,1,1,1 ...即六个都一样的场景。然后开始写一个 dummy 程序，内容不重要，能过检查就好。

我跑出来的结果如下：
```
向右 1 步\n拿起 6 个物品\n向左 1 步\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品
向右 2 步\n拿起 6 个物品\n向左 2 步\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品
...
向右 32 步\n拿起 6 个物品\n向左 32 步\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品\n放下 1 个物品
向左 1912766784062 步
```

这样就可以跑出来一个我们已有的32程序可以解决的菜谱。其实用 128 个程序（上限）更快，但是我懒了（

## 马赛克

![qr_raw.jpg](https://i.loli.net/2021/10/30/Dy3B6QTzvco59XF.jpg)

我最喜欢的一道题目之一，要从打了马赛克的二维码中还原信息。乍一看感觉不大可能，但是其实每个马赛克块的灰度数值其实提供了很多信息，我们完全可以枚举其覆盖的二维码块来找到会产生一致的马赛克块的情况。当然如果同时覆盖了 2*2 的块那就没办法了，不过二维码本身也是有纠错能力的，所以也不需要完全还原。

脚本在这里：[https://gist.github.com/harrynull/10e03907e835c48b7fbae72109381769](qr_mosaic_recovery.py)。写的比较乱，而且要稍微改一下才可以跑。跑出来的结果如下：

![result.png](https://i.loli.net/2021/10/30/n9GRpPozYMuewyh.png)

## minecRaft

这一题其实就是反混淆源码，然后从常数发现是用的 XTEA 加密。给了密文和密钥求明文。唯一拿出来提一下的原因是我又以为这是道密码学题了，因为我一开始反混淆出来以为是给了明文和密文求密钥，看了下 web 标签和过关人数我才清醒过来。

![meme2.jpg](https://i.loli.net/2021/10/30/Ofh1yNugPGWAVeM.jpg)

## JUST BE FUN
很有意思的题目，但是也很繁琐。题目定义了一种船新的、三维的、只有栈的语言，要实现 + * shift or xor pow 运算。作者唯一的温柔是所有的数字都是一位数，所以不用写 readint 了。稍微写了一点之后感觉太麻烦了于是自己编了一个[语言](https://gist.github.com/harrynull/c011c8949f4cba1551e3d840805347ed)和一个[编译器](https://gist.github.com/harrynull/24a7ab92ae48b318c158539daac5e148)，写了400+行代码（其实感觉更麻烦了吧喂）。

自编语言部分代码：
```
# === handle | ===
$|
!s
nop*
!s
>
# pop the operator
pop
# res = 0
# i = 1
0
1

# [op1, op2, res, i]
@or_while_cond
#while a + b != 0:
>
fetch 3
fetch 5
+
@or_while
|*
# a+b >= 0: UP
wrap up
#    if a % 2 + b % 2 != 0:
fetch 3
2
%
fetch 5
2
%
+
@or_bit_not_zero
|*
wrap up
#        res += i
# [op1, op2, res, i]
dup
# [op1, op2, res, i, i]
1
3
\
# [op1, op2, i, i, res]
+
# [op1, op2, i, RES]
1
2
\
# end if }
>
```

编译结果如下：
![diagram.jpg](https://i.loli.net/2021/10/30/AQtoTH57BkEz6cY.jpg)

## p😭q
最喜欢的题目之一，从频谱动画来还原音频。一件我从来没想过可行，但是一想却觉得很合理的事情。

![flag.gif](https://i.loli.net/2021/10/30/2SeDHPmog7QE6uv.gif)


这道题目的正解当然是找一位 hifi 发烧友盯着 gif 看一下然后脑内还原音频（bushi）。但是我附近没有发烧友所以我只能写代码还原。

```python
gif = Image.open("./flag.gif")
frames = []
for frame in range(8, gif.n_frames):
    gif.seek(frame)
    frames.append(np.sum(np.array(gif), axis=0)[3::4] + min_db)

frames = np.asarray(frames).transpose()
y_inv = librosa.feature.inverse.mel_to_audio(
    librosa.db_to_power(frames),
    n_fft=fft_window_size,
    hop_length=frame_step_size,
    window=window_function_type
)
sf.write('output.wav', y_inv, 22050, 'PCM_24')  # 634bil 971 mil 243 tho 582
```
