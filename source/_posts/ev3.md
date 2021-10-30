---
title: EV3 上放 Bad Apple!! 和马里奥
tags:
  - EV3
categories:
  - 学习笔记
date: 2019-08-05 13:47:00
---

当我在玩 [MakeCode](https://makecode.mindstorms.com/#editor) 时候，无意间发现 EV3 的显示屏可以显示自定义图像并且支持发出声音……

<!--more-->

## Bad Apple!!

首先先下载 Bad Apple!! 视频并利用 ffmpeg 转换成 png 图片 (178*128 10fps)
```bash
ffmpeg -i bad_apple.mp4 -vf scale=178:128 -r 10 ba_%5d.png
```

第一步是想办法将图片放置到 EV3 里。由于 MakeCode 似乎不支持上传文件，并且对代码长度有最大限制，于是就必须想出一种压缩方式。
这里我选用的压缩方式是将每帧转换成黑白图片，并且转换成 u8 的数组 a，数组`a[2n]`代表接下来黑色像素点数量，`a[2n+1]`代表白色像素点数量。最后再用base64加密放入 MakeCode 代码中（其实应该也可以直接创建数字数组的，可能没必要用 base64）。

压缩转换代码如下：
```python
import base64
from PIL import Image 

def compress(filename):
    res=[]
    img = Image.open(filename) .convert('L').point(lambda x : 255 if x > 200 else 0, mode='1')
    pixels = list(img.getdata())
    
    cnt=0
    lst_color=0 # start from black
    for p in pixels:
        if lst_color==p:
            if cnt==255: # avoid overflow
                res.append(cnt)
                res.append(0)
                cnt=0
                
            cnt+=1
        else:
            lst_color=p
            res.append(cnt)
            cnt=1
            
    res.append(cnt)
    return base64.b64encode(bytearray(res)).decode()
```

之后就简单了，直接在 EV3 上编码图片并显示即可：

```javascript
data.forEach(function (f: string) {
    let myImage = image.create(178, 128);
    let decoded = new Base64().decode(f);
    let is_black = true; // start from black
    let x = 0; let y = 0;
    for (let i = 0; i < decoded.length; ++i) {
        let n = decoded.charCodeAt(i);
        for (let j = 0; j < n; ++j) {
            if (is_black) myImage.setPixel(x, y, 1);
            x++;
            if (x == 178) { x = 0; y++; }
        }
        is_black = !is_black;
    }
    brick.showImage(myImage);
});
```

吐槽一下，MakeCode 用的是阉割版的 TypeScript，很多 JavaScript 里的东西都不能用，导致 Base64 都要造轮子。
(此处应该有一个视频，但是由于太懒了就没弄)

## 马里奥

做完 Bad Apple!! 之后发现 EV3 竟然支持 `playTone(frequency, duration)`，于是写了个简单的脚本转换 MIDI 文件成 MakeCode 代码。

```python
import sys
# From https://gist.github.com/YuxiUx/ef84328d95b10d0fcbf537de77b936cd
def noteToFreq(note):
    a = 440 #frequency of A (coomon value is 440Hz)
    return (a / 32) * (2 ** ((note - 9) / 12))
# http://valentin.dasdeck.com/midi/mid2txt.php
SPEED=1.04166666666667 #60000 / (BPM * PPQ)
#music.playTone(659, 1013 - 512);
#loops.pause(1536 - 1013)    
with open("out.txt", "w") as out:
    with open(sys.argv[1]) as file:
        for line in file:
            #0 On ch=1 n=76 v=110
            vals=line.split(" ")
            if len(vals)!=5:
                print("[*] unrecognized line", vals, "skipping...")
                continue
            time, status, _, note, vol = vals
            time=int(time)
            if status!="On" or time ==0 or vol==0:continue
            out.write("music.playTone(%s, %d);\n"%(noteToFreq(int(note[2:])), time/SPEED));
print("[*] ok!")
```